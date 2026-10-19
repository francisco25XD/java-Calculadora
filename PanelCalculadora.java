import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Pestaña principal: pantalla, teclado numérico y funciones científicas. */
public class PanelCalculadora extends JPanel {

    private static final String[] APERTURAS = {"sin(", "cos(", "tan(", "ln(", "log(", "root(", "sqrt(", "√("};

    private final Historial historial;
    private final Runnable alCambiarHistorial;

    private final JTextField campo = new JTextField();
    private final JLabel vista = new JLabel(" ");
    private final JLabel modoEtiqueta = new JLabel("GRADOS");
    private final Boton botonModo;
    private final Boton botonTema;
    private final List<Boton> botones = new ArrayList<>();

    private JPanel panelPantalla;
    private JLabel ayudaEtiqueta;

    private boolean grados = true;
    private boolean mostrandoResultado = false; // la pantalla muestra un resultado recién calculado
    private boolean ajustando = false;          // evita reaccionar a cambios hechos por el propio programa
    private boolean vistaTieneError = false;
    private String ultimoResultado = "";

    public PanelCalculadora(Historial historial, Runnable alCambiarHistorial) {
        this.historial = historial;
        this.alCambiarHistorial = alCambiarHistorial;

        setLayout(new BorderLayout(0, 8));
        setBackground(Tema.FONDO);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        botonTema = Tema.crearBotonModo();
        botonModo = new Boton("Grados", Boton.Tipo.FUNCION);
        botones.add(botonModo);

        add(crearPantalla(), BorderLayout.NORTH);
        add(crearTeclado(), BorderLayout.CENTER);
        add(crearAyuda(), BorderLayout.SOUTH);

        Tema.agregarObservador(this::actualizarTema);
    }

    // ------------------------------------------------------------------
    // Construcción de la interfaz
    // ------------------------------------------------------------------

    private JPanel crearPantalla() {
        panelPantalla = new JPanel(new BorderLayout());
        panelPantalla.setBackground(Tema.PANTALLA);
        panelPantalla.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        modoEtiqueta.setForeground(Tema.ACENTO);
        modoEtiqueta.setFont(Tema.fuente(java.awt.Font.BOLD, 12));

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        cabecera.add(modoEtiqueta, BorderLayout.WEST);
        cabecera.add(botonTema, BorderLayout.EAST);

        campo.setFont(Tema.fuente(java.awt.Font.PLAIN, 30));
        campo.setHorizontalAlignment(SwingConstants.RIGHT);
        campo.setBackground(Tema.PANTALLA);
        campo.setForeground(Tema.TEXTO);
        campo.setCaretColor(Tema.TEXTO);
        campo.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        campo.setToolTipText("Escribí con el teclado o usá los botones. Enter calcula, Esc limpia.");

        vista.setFont(Tema.fuente(java.awt.Font.PLAIN, 16));
        vista.setForeground(Tema.TEXTO_SUAVE);
        vista.setHorizontalAlignment(SwingConstants.RIGHT);

        panelPantalla.add(cabecera, BorderLayout.NORTH);
        panelPantalla.add(campo, BorderLayout.CENTER);
        panelPantalla.add(vista, BorderLayout.SOUTH);

        campo.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) calcular();
                else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) limpiar();
            }
        });
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { alEditar(); }
            @Override public void removeUpdate(DocumentEvent e) { alEditar(); }
            @Override public void changedUpdate(DocumentEvent e) { alEditar(); }
        });
        return panelPantalla;
    }

    private JPanel crearTeclado() {
        JPanel teclado = new JPanel(new BorderLayout(0, 8));
        teclado.setOpaque(false);

        // Bloque científico: 4 filas x 5 columnas
        JPanel ciencia = new JPanel(new GridLayout(4, 5, 6, 6));
        ciencia.setOpaque(false);

        botonModo.addActionListener(e -> alternarModo());
        botonModo.setToolTipText("Cambiar entre grados y radianes (afecta sin, cos y tan)");
        ciencia.add(botonModo);
        ciencia.add(boton("π", Boton.Tipo.FUNCION, () -> insertar("π", true), null));
        ciencia.add(boton("e", Boton.Tipo.FUNCION, () -> insertar("e", true), null));
        ciencia.add(boton("C", Boton.Tipo.BORRAR, this::limpiar, "Limpiar toda la operación (Esc)"));
        ciencia.add(boton("⌫", Boton.Tipo.BORRAR, this::borrarUltimo, "Borrar el último dígito"));

        ciencia.add(boton("sin", Boton.Tipo.FUNCION, () -> insertar("sin(", true), null));
        ciencia.add(boton("cos", Boton.Tipo.FUNCION, () -> insertar("cos(", true), null));
        ciencia.add(boton("tan", Boton.Tipo.FUNCION, () -> insertar("tan(", true), null));
        ciencia.add(boton("ln", Boton.Tipo.FUNCION, () -> insertar("ln(", true), "Logaritmo natural"));
        ciencia.add(boton("log", Boton.Tipo.FUNCION, () -> insertar("log(", true), "Logaritmo decimal (base 10)"));

        ciencia.add(boton("x²", Boton.Tipo.FUNCION, () -> insertar("^2", false), "Elevar al cuadrado"));
        ciencia.add(boton("xʸ", Boton.Tipo.FUNCION, () -> insertar("^", false), "Exponenciación: base ^ exponente"));
        ciencia.add(boton("√", Boton.Tipo.FUNCION, () -> insertar("√(", true), "Raíz cuadrada"));
        ciencia.add(boton("ⁿ√", Boton.Tipo.FUNCION, () -> insertar("root(", true),
                "Raíz enésima: root(n;x). Ejemplo: root(3;27) = 3"));
        ciencia.add(boton("%", Boton.Tipo.FUNCION, () -> insertar("%", false), "Porcentaje (200+10% = 220)"));

        ciencia.add(boton("(", Boton.Tipo.FUNCION, () -> insertar("(", true), null));
        ciencia.add(boton(")", Boton.Tipo.FUNCION, () -> insertar(")", false), null));
        ciencia.add(boton("±", Boton.Tipo.FUNCION, this::cambiarSigno, "Cambiar signo positivo/negativo"));
        ciencia.add(boton("Ans", Boton.Tipo.FUNCION, this::insertarAns, "Último resultado"));
        ciencia.add(boton("10ˣ", Boton.Tipo.FUNCION, () -> insertar("10^", true), null));

        // Bloque numérico: 4 filas x 4 columnas
        JPanel numeros = new JPanel(new GridLayout(4, 4, 6, 6));
        numeros.setOpaque(false);
        String[][] filas = {
            {"7", "8", "9", "÷"},
            {"4", "5", "6", "×"},
            {"1", "2", "3", "-"},
            {"0", ",", "=", "+"},
        };
        for (String[] fila : filas) {
            for (String t : fila) {
                switch (t) {
                    case "=":
                        numeros.add(boton("=", Boton.Tipo.IGUAL, this::calcular, null));
                        break;
                    case "÷": case "×": case "-": case "+":
                        numeros.add(boton(t.equals("-") ? "−" : t, Boton.Tipo.OPERADOR, () -> insertar(t, false), null));
                        break;
                    default:
                        numeros.add(boton(t, Boton.Tipo.NUMERO, () -> insertar(t, true), null));
                }
            }
        }
        numeros.setPreferredSize(new Dimension(300, 220));
        ciencia.setPreferredSize(new Dimension(300, 220));

        teclado.add(ciencia, BorderLayout.NORTH);
        teclado.add(numeros, BorderLayout.CENTER);
        return teclado;
    }

    private JLabel crearAyuda() {
        ayudaEtiqueta = new JLabel("Enter calcula   ·   Esc limpia");
        ayudaEtiqueta.setForeground(Tema.TEXTO_SUAVE);
        ayudaEtiqueta.setFont(Tema.fuente(java.awt.Font.PLAIN, 11));
        ayudaEtiqueta.setHorizontalAlignment(SwingConstants.CENTER);
        return ayudaEtiqueta;
    }

    private Boton boton(String texto, Boton.Tipo tipo, Runnable accion, String ayuda) {
        Boton b = new Boton(texto, tipo);
        b.addActionListener((ActionEvent e) -> accion.run());
        if (ayuda != null) b.setToolTipText(ayuda);
        botones.add(b);
        return b;
    }

    public void actualizarTema() {
        setBackground(Tema.FONDO);
        if (panelPantalla != null) panelPantalla.setBackground(Tema.PANTALLA);
        modoEtiqueta.setForeground(Tema.ACENTO);
        campo.setBackground(Tema.PANTALLA);
        campo.setForeground(Tema.TEXTO);
        campo.setCaretColor(Tema.TEXTO);
        vista.setForeground(vistaTieneError ? Tema.ERROR : Tema.TEXTO_SUAVE);
        if (ayudaEtiqueta != null) ayudaEtiqueta.setForeground(Tema.TEXTO_SUAVE);
        botonTema.actualizarTema();
        for (Boton b : botones) {
            b.actualizarTema();
        }
        repaint();
    }

    // ------------------------------------------------------------------
    // Edición de la expresión
    // ------------------------------------------------------------------

    private void insertar(String s, boolean reemplazaResultado) {
        if (mostrandoResultado && reemplazaResultado) {
            ajustando = true;
            campo.setText("");
            ajustando = false;
        }
        mostrandoResultado = false;
        campo.replaceSelection(s);
        campo.requestFocusInWindow();
    }

    private void insertarAns() {
        if (ultimoResultado.isEmpty()) return;
        insertar(ultimoResultado, true);
    }

    public void limpiar() {
        establecerTexto("");
        vistaTieneError = false;
        vista.setText(" ");
        vista.setForeground(Tema.TEXTO_SUAVE);
        mostrandoResultado = false;
        campo.requestFocusInWindow();
    }

    public void borrarUltimo() {
        mostrandoResultado = false;
        if (campo.getSelectionStart() != campo.getSelectionEnd()) {
            campo.replaceSelection("");
            return;
        }
        int pos = campo.getCaretPosition();
        if (pos == 0) return;
        String antes = campo.getText().substring(0, pos);
        int borrar = 1;
        for (String ap : APERTURAS) {
            if (antes.endsWith(ap)) {
                borrar = ap.length();
                break;
            }
        }
        campo.select(pos - borrar, pos);
        campo.replaceSelection("");
        campo.requestFocusInWindow();
    }

    public void cambiarSigno() {
        String t = campo.getText();
        if (t.isEmpty()) {
            establecerTexto("-");
            return;
        }
        Matcher envuelto = Pattern.compile("\\(-([0-9]+(?:[.,][0-9]+)?)\\)$").matcher(t);
        if (envuelto.find()) {
            t = t.substring(0, envuelto.start()) + envuelto.group(1);
        } else if (t.matches("-[0-9]+(?:[.,][0-9]+)?")) {
            t = t.substring(1);
        } else {
            Matcher numero = Pattern.compile("([0-9]+(?:[.,][0-9]*)?|[.,][0-9]+)$").matcher(t);
            if (numero.find()) {
                t = t.substring(0, numero.start()) + "(-" + numero.group(1) + ")";
            } else if (t.startsWith("-(") && t.endsWith(")")) {
                t = t.substring(2, t.length() - 1);
            } else {
                t = "-(" + t + ")";
            }
        }
        establecerTexto(t);
        campo.requestFocusInWindow();
    }

    private void alternarModo() {
        grados = !grados;
        botonModo.setText(grados ? "Grados" : "Radianes");
        modoEtiqueta.setText(grados ? "GRADOS" : "RADIANES");
        if (!mostrandoResultado) actualizarVista();
        campo.requestFocusInWindow();
    }

    public void cargarExpresion(String expresion) {
        establecerTexto(expresion);
        mostrandoResultado = false;
        campo.requestFocusInWindow();
    }

    public void insertarDesdeHistorial(String texto) {
        insertar(texto, false);
    }

    private void establecerTexto(String t) {
        ajustando = true;
        campo.setText(t);
        ajustando = false;
        mostrandoResultado = false;
        actualizarVista();
    }

    private void alEditar() {
        if (ajustando) return;
        mostrandoResultado = false;
        actualizarVista();
    }

    // ------------------------------------------------------------------
    // Cálculo
    // ------------------------------------------------------------------

    private void actualizarVista() {
        String expr = campo.getText().trim();
        vistaTieneError = false;
        vista.setForeground(Tema.TEXTO_SUAVE);
        if (expr.isEmpty() || expr.matches("[0-9.,]+")) {
            vista.setText(" ");
            return;
        }
        try {
            vista.setText("= " + Evaluador.formatear(Evaluador.evaluar(expr, grados)));
        } catch (Evaluador.CalcException ex) {
            vista.setText(" ");
        }
    }

    public void calcular() {
        String expr = campo.getText().trim();
        if (expr.isEmpty()) return;
        try {
            double r = Evaluador.evaluar(expr, grados);
            String resultado = Evaluador.formatear(r);
            historial.agregar(expr, resultado);
            ultimoResultado = resultado;
            establecerTexto(resultado);
            mostrandoResultado = true;
            vistaTieneError = false;
            vista.setText(" ");
            alCambiarHistorial.run();
        } catch (Evaluador.CalcException ex) {
            vistaTieneError = true;
            vista.setForeground(Tema.ERROR);
            vista.setText(ex.getMessage());
        }
        campo.requestFocusInWindow();
    }

    public boolean isGrados() {
        return grados;
    }
}
