import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Motor de cálculo: analiza y evalúa expresiones matemáticas respetando
 * la prioridad de operaciones, paréntesis, funciones y constantes.
 *
 * Gramática (de menor a mayor prioridad):
 *   expr     := termino (('+' | '-') termino)*
 *   termino  := unario (('*' | '/') unario)*      (también multiplicación implícita: 2π, 3(4+1))
 *   unario   := ('-' | '+') unario | potencia
 *   potencia := postfijo ('^' unario)?            (asociativa a la derecha)
 *   postfijo := primario ('%')*
 *   primario := número | constante | función '(' args ')' | '(' expr ')'
 *
 * Decimales: se acepta coma o punto. Separador de argumentos: ';'
 */
public class Evaluador {

    /** Error de cálculo con mensaje legible para el usuario. */
    public static class CalcException extends RuntimeException {
        public CalcException(String mensaje) {
            super(mensaje);
        }
    }

    private final String texto;
    private final boolean grados;
    private int pos = 0;

    // Para el comportamiento de "100 + 10%" = 110
    private boolean ultimoTerminoEsPorcentaje = false;

    private Evaluador(String texto, boolean grados) {
        this.texto = texto;
        this.grados = grados;
    }

    /** Evalúa una expresión y devuelve el resultado. */
    public static double evaluar(String expresion, boolean modoGrados) {
        if (expresion == null || expresion.trim().isEmpty()) {
            throw new CalcException("Expresión vacía");
        }
        Evaluador ev = new Evaluador(expresion, modoGrados);
        double r = ev.parseExpresion();
        ev.saltarEspacios();
        if (ev.pos < ev.texto.length()) {
            char c = ev.texto.charAt(ev.pos);
            if (c == ')') throw new CalcException("Paréntesis sin abrir");
            throw new CalcException("Carácter inesperado: '" + c + "'");
        }
        return verificar(r);
    }

    // ------------------------------------------------------------------
    // Análisis sintáctico
    // ------------------------------------------------------------------

    private double parseExpresion() {
        double izq = parseTermino();
        while (true) {
            saltarEspacios();
            char c = actual();
            if (c == '+' || c == '-' || c == '−') {
                pos++;
                double der = parseTermino();
                // Porcentaje relativo: 200 + 10% => 200 + (200 * 0.10)
                if (ultimoTerminoEsPorcentaje) der = izq * der;
                izq = (c == '+') ? izq + der : izq - der;
                izq = verificar(izq);
            } else {
                return izq;
            }
        }
    }

    private double parseTermino() {
        int factores = 1;
        ultimoTerminoEsPorcentaje = false;
        double izq = parseUnario();
        boolean porcentaje = ultimoTerminoEsPorcentaje;
        while (true) {
            saltarEspacios();
            char c = actual();
            if (c == '*' || c == '×' || c == 'x' && !empiezaNombre(pos + 1)) {
                pos++;
                izq = verificar(izq * parseUnario());
                factores++;
            } else if (c == '/' || c == '÷') {
                pos++;
                double der = parseUnario();
                if (der == 0) throw new CalcException("No se puede dividir por cero");
                izq = verificar(izq / der);
                factores++;
            } else if (c == '(' || c == 'π' || c == '√' || Character.isLetter(c)) {
                // Multiplicación implícita: 2π, 3(4+1), 2sin(30)
                izq = verificar(izq * parseUnario());
                factores++;
            } else {
                break;
            }
            porcentaje = false;
        }
        ultimoTerminoEsPorcentaje = (factores == 1) && porcentaje;
        return izq;
    }

    private double parseUnario() {
        saltarEspacios();
        char c = actual();
        if (c == '-' || c == '−') {
            pos++;
            double v = -parseUnario();
            ultimoTerminoEsPorcentaje = false;
            return v;
        }
        if (c == '+') {
            pos++;
            return parseUnario();
        }
        return parsePotencia();
    }

    private double parsePotencia() {
        double base = parsePostfijo();
        boolean eraPorcentaje = ultimoTerminoEsPorcentaje;
        saltarEspacios();
        if (actual() == '^') {
            pos++;
            double exp = parseUnario();
            ultimoTerminoEsPorcentaje = false;
            return potencia(base, exp);
        }
        ultimoTerminoEsPorcentaje = eraPorcentaje;
        return base;
    }

    private double parsePostfijo() {
        double v = parsePrimario();
        boolean porcentaje = false;
        saltarEspacios();
        while (actual() == '%') {
            pos++;
            v = v / 100.0;
            porcentaje = true;
            saltarEspacios();
        }
        ultimoTerminoEsPorcentaje = porcentaje;
        return v;
    }

    private double parsePrimario() {
        saltarEspacios();
        char c = actual();

        if (c == '(') {
            pos++;
            double v = parseExpresion();
            saltarEspacios();
            if (actual() != ')') throw new CalcException("Falta cerrar un paréntesis");
            pos++;
            ultimoTerminoEsPorcentaje = false;
            return v;
        }
        if (Character.isDigit(c) || c == '.' || c == ',') {
            return parseNumero();
        }
        if (c == 'π') {
            pos++;
            return Math.PI;
        }
        if (c == '√') {
            pos++;
            saltarEspacios();
            return raizCuadrada(parsePrimarioConPostfijo());
        }
        if (Character.isLetter(c)) {
            return parseNombre();
        }
        if (c == '\0') throw new CalcException("Expresión incompleta");
        throw new CalcException("Carácter inesperado: '" + c + "'");
    }

    private double parsePrimarioConPostfijo() {
        double v = parsePrimario();
        ultimoTerminoEsPorcentaje = false;
        return v;
    }

    private double parseNumero() {
        int inicio = pos;
        boolean puntoVisto = false;
        while (pos < texto.length()) {
            char c = texto.charAt(pos);
            if (Character.isDigit(c)) {
                pos++;
            } else if ((c == '.' || c == ',') && !puntoVisto) {
                puntoVisto = true;
                pos++;
            } else {
                break;
            }
        }
        String s = texto.substring(inicio, pos).replace(',', '.');
        if (s.equals(".")) throw new CalcException("Número mal escrito");
        // Notación científica con E mayúscula (así se muestran los resultados muy grandes o pequeños): 1,5E10
        if (pos < texto.length() && texto.charAt(pos) == 'E') {
            int p = pos + 1;
            if (p < texto.length() && (texto.charAt(p) == '-' || texto.charAt(p) == '+')) p++;
            int digitos = p;
            while (p < texto.length() && Character.isDigit(texto.charAt(p))) p++;
            if (p > digitos) {
                s = s + texto.substring(pos, p);
                pos = p;
            }
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            throw new CalcException("Número mal escrito");
        }
    }

    private double parseNombre() {
        int inicio = pos;
        while (pos < texto.length() && Character.isLetter(texto.charAt(pos))) pos++;
        String nombre = texto.substring(inicio, pos).toLowerCase();

        // Constantes
        if (nombre.equals("e")) return Math.E;
        if (nombre.equals("pi")) return Math.PI;

        // Funciones: requieren paréntesis
        saltarEspacios();
        if (actual() != '(') {
            throw new CalcException("Función o nombre desconocido: " + nombre);
        }
        pos++;
        java.util.List<Double> args = new java.util.ArrayList<>();
        args.add(parseExpresion());
        saltarEspacios();
        while (actual() == ';') {
            pos++;
            args.add(parseExpresion());
            saltarEspacios();
        }
        if (actual() != ')') throw new CalcException("Falta cerrar un paréntesis");
        pos++;
        ultimoTerminoEsPorcentaje = false;
        return aplicarFuncion(nombre, args);
    }

    // ------------------------------------------------------------------
    // Funciones matemáticas
    // ------------------------------------------------------------------

    private double aplicarFuncion(String nombre, java.util.List<Double> a) {
        switch (nombre) {
            case "sqrt":
            case "raiz":
                necesita(nombre, a, 1);
                return raizCuadrada(a.get(0));
            case "root":
                necesita(nombre, a, 2);
                return raizEnesima(a.get(0), a.get(1));
            case "ln":
                necesita(nombre, a, 1);
                if (a.get(0) <= 0) throw new CalcException("ln solo está definido para números positivos");
                return Math.log(a.get(0));
            case "log":
                necesita(nombre, a, 1);
                if (a.get(0) <= 0) throw new CalcException("log solo está definido para números positivos");
                return Math.log10(a.get(0));
            case "sin":
            case "sen":
                necesita(nombre, a, 1);
                return limpiarResiduo(Math.sin(aRadianes(a.get(0))));
            case "cos":
                necesita(nombre, a, 1);
                return limpiarResiduo(Math.cos(aRadianes(a.get(0))));
            case "tan":
                necesita(nombre, a, 1);
                return tangente(a.get(0));
            case "abs":
                necesita(nombre, a, 1);
                return Math.abs(a.get(0));
            default:
                throw new CalcException("Función desconocida: " + nombre);
        }
    }

    private void necesita(String nombre, java.util.List<Double> a, int n) {
        if (a.size() != n) {
            throw new CalcException("La función " + nombre + " necesita " + n
                    + (n == 1 ? " argumento" : " argumentos (separados por ;)"));
        }
    }

    private double aRadianes(double x) {
        return grados ? Math.toRadians(x) : x;
    }

    private double tangente(double x) {
        // Detectar los puntos donde la tangente no existe (90°, 270°, ...)
        if (grados) {
            double m = ((x % 180) + 180) % 180;
            if (Math.abs(m - 90) < 1e-9) throw new CalcException("La tangente no está definida en " + formatear(x) + "°");
        } else {
            double m = Math.abs(Math.cos(x));
            if (m < 1e-12) throw new CalcException("La tangente no está definida en ese ángulo");
        }
        return Math.tan(aRadianes(x));
    }

    /** Elimina residuos de punto flotante: sin(180°) da 1,2E-16 en lugar de 0. */
    private static double limpiarResiduo(double v) {
        return Math.abs(v) < 1e-12 ? 0.0 : v;
    }

    private static double raizCuadrada(double x) {
        if (x < 0) throw new CalcException("No existe la raíz cuadrada de un número negativo");
        return Math.sqrt(x);
    }

    private static double raizEnesima(double n, double x) {
        if (n == 0) throw new CalcException("El índice de la raíz no puede ser cero");
        boolean nEntero = n == Math.rint(n);
        if (x < 0) {
            if (nEntero && Math.abs(n) % 2 == 1) {
                return -Math.pow(-x, 1.0 / n);
            }
            throw new CalcException("No existe esa raíz de un número negativo");
        }
        if (x == 0 && n < 0) throw new CalcException("No se puede dividir por cero");
        return Math.pow(x, 1.0 / n);
    }

    private static double potencia(double base, double exp) {
        if (base == 0 && exp < 0) throw new CalcException("No se puede dividir por cero");
        double r = Math.pow(base, exp);
        if (Double.isNaN(r)) throw new CalcException("Resultado no real (base negativa con exponente fraccionario)");
        return verificar(r);
    }

    private static double verificar(double v) {
        if (Double.isNaN(v)) throw new CalcException("Operación no válida");
        if (Double.isInfinite(v)) throw new CalcException("El resultado es demasiado grande");
        return v;
    }

    // ------------------------------------------------------------------
    // Utilidades del analizador
    // ------------------------------------------------------------------

    private char actual() {
        return pos < texto.length() ? texto.charAt(pos) : '\0';
    }

    private boolean empiezaNombre(int i) {
        return i < texto.length() && Character.isLetter(texto.charAt(i));
    }

    private void saltarEspacios() {
        while (pos < texto.length() && Character.isWhitespace(texto.charAt(pos))) pos++;
    }

    // ------------------------------------------------------------------
    // Formato de salida
    // ------------------------------------------------------------------

    /** Da formato legible al resultado: 12 cifras significativas, coma decimal. */
    public static String formatear(double v) {
        if (v == 0) return "0";
        BigDecimal bd = new BigDecimal(v).round(new MathContext(12)).stripTrailingZeros();
        double abs = Math.abs(v);
        String s;
        if (abs >= 1e15 || abs < 1e-9) {
            s = bd.toString().replace("E+", "E");
        } else {
            s = bd.toPlainString();
        }
        return s.replace('.', ',');
    }
}
