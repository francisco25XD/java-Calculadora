import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

/** Pestaña de porcentajes: aumento y descuento, variación porcentual y "X% de Y". */
public class PanelPorcentajes extends JPanel {

    private final Historial historial;
    private final Runnable alCambiarHistorial;

    private final JTextField valor = campo("100");
    private final JTextField porcentaje = campo("20");
    private final JLabel res1 = etiquetaResultado();

    private final JTextField inicial = campo("80");
    private final JTextField finalV = campo("100");
    private final JLabel res2 = etiquetaResultado();

    private final JTextField parte = campo("15");
    private final JTextField total = campo("60");
    private final JLabel res3 = etiquetaResultado();

    private final JLabel titulo = new JLabel("Cálculo de porcentajes");
    private final Boton botonTema;
    private final List<Boton> botones = new ArrayList<>();
    private final List<JLabel> etiquetas = new ArrayList<>();
    private final List<JTextField> campos = new ArrayList<>();
    private final List<JLabel> resultados = new ArrayList<>();

    private JPanel p1, p2, p3;
    private TitledBorder b1, b2, b3;

    public PanelPorcentajes(Historial historial, Runnable alCambiarHistorial) {
        this.historial = historial;
        this.alCambiarHistorial = alCambiarHistorial;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Tema.FONDO);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        botonTema = Tema.crearBotonModo();

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        cabecera.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        titulo.setForeground(Tema.TEXTO);
        titulo.setFont(Tema.fuente(Font.BOLD, 18));
        cabecera.add(titulo, BorderLayout.WEST);
        cabecera.add(botonTema, BorderLayout.EAST);

        add(cabecera);
        add(javax.swing.Box.createVerticalStrut(10));
        add(seccion1());
        add(javax.swing.Box.createVerticalStrut(10));
        add(seccion2());
        add(javax.swing.Box.createVerticalStrut(10));
        add(seccion3());

        campos.add(valor);
        campos.add(porcentaje);
        campos.add(inicial);
        campos.add(finalV);
        campos.add(parte);
        campos.add(total);

        resultados.add(res1);
        resultados.add(res2);
        resultados.add(res3);

        Tema.agregarObservador(this::actualizarTema);
    }

    // ------------------------------------------------------------------
    // Secciones
    // ------------------------------------------------------------------

    private JPanel seccion1() {
        b1 = crearBordeSeccion("Aumento y descuento");
        p1 = panelConBorde(b1);
        fila(p1, 0, "Valor original", valor);
        fila(p1, 1, "Porcentaje (%)", porcentaje);

        Boton aumentar = new Boton("Aplicar aumento", Boton.Tipo.OPERADOR);
        Boton descontar = new Boton("Aplicar descuento", Boton.Tipo.BORRAR);
        botones.add(aumentar);
        botones.add(descontar);
        aumentar.addActionListener(e -> aplicar(true));
        descontar.addActionListener(e -> aplicar(false));
        botonesEnFila(p1, 2, aumentar, descontar);
        resultadoEnFila(p1, 3, res1);
        return p1;
    }

    private JPanel seccion2() {
        b2 = crearBordeSeccion("Variación porcentual entre dos valores");
        p2 = panelConBorde(b2);
        fila(p2, 0, "Valor inicial", inicial);
        fila(p2, 1, "Valor final", finalV);

        Boton calcular = new Boton("Calcular variación", Boton.Tipo.ACENTO);
        botones.add(calcular);
        calcular.addActionListener(e -> variacion());
        botonesEnFila(p2, 2, calcular, null);
        resultadoEnFila(p2, 3, res2);
        return p2;
    }

    private JPanel seccion3() {
        b3 = crearBordeSeccion("Parte y total");
        p3 = panelConBorde(b3);
        fila(p3, 0, "Parte (A)", parte);
        fila(p3, 1, "Total (B)", total);

        Boton queP = new Boton("¿Qué % es A de B?", Boton.Tipo.ACENTO);
        Boton pDe = new Boton("Calcular A% de B", Boton.Tipo.FUNCION);
        botones.add(queP);
        botones.add(pDe);
        queP.addActionListener(e -> quePorcentaje());
        pDe.addActionListener(e -> porcentajeDe());
        botonesEnFila(p3, 2, queP, pDe);
        resultadoEnFila(p3, 3, res3);
        return p3;
    }

    // ------------------------------------------------------------------
    // Cálculos
    // ------------------------------------------------------------------

    private void aplicar(boolean aumento) {
        try {
            double v = leer(valor, "el valor original");
            double pct = leer(porcentaje, "el porcentaje");
            double monto = v * pct / 100.0;
            double fin = aumento ? v + monto : v - monto;
            String f = Evaluador.formatear(fin);
            String texto = (aumento ? "Aumento" : "Descuento") + " de " + Evaluador.formatear(pct) + "%: "
                    + (aumento ? "+" : "−") + Evaluador.formatear(Math.abs(monto)) + "  →  " + f;
            res1.setForeground(Tema.TEXTO);
            res1.setText(texto);
            historial.agregar(Evaluador.formatear(v) + (aumento ? " + " : " − ") + Evaluador.formatear(pct) + "%", f);
            alCambiarHistorial.run();
        } catch (Evaluador.CalcException ex) {
            error(res1, ex);
        }
    }

    private void variacion() {
        try {
            double a = leer(inicial, "el valor inicial");
            double b = leer(finalV, "el valor final");
            if (a == 0) throw new Evaluador.CalcException("El valor inicial no puede ser cero");
            double pct = (b - a) / Math.abs(a) * 100.0;
            String p = Evaluador.formatear(Math.abs(pct)) + "%";
            String texto = pct > 0 ? "Aumento del " + p : pct < 0 ? "Descuento del " + p : "Sin variación (0%)";
            res2.setForeground(Tema.TEXTO);
            res2.setText(texto);
            historial.agregar("Variación de " + Evaluador.formatear(a) + " a " + Evaluador.formatear(b),
                    (pct > 0 ? "+" : pct < 0 ? "−" : "") + p);
            alCambiarHistorial.run();
        } catch (Evaluador.CalcException ex) {
            error(res2, ex);
        }
    }

    private void quePorcentaje() {
        try {
            double a = leer(parte, "la parte (A)");
            double b = leer(total, "el total (B)");
            if (b == 0) throw new Evaluador.CalcException("El total no puede ser cero");
            String r = Evaluador.formatear(a / b * 100.0) + "%";
            res3.setForeground(Tema.TEXTO);
            res3.setText(Evaluador.formatear(a) + " es el " + r + " de " + Evaluador.formatear(b));
            historial.agregar(Evaluador.formatear(a) + " es qué % de " + Evaluador.formatear(b), r);
            alCambiarHistorial.run();
        } catch (Evaluador.CalcException ex) {
            error(res3, ex);
        }
    }

    private void porcentajeDe() {
        try {
            double a = leer(parte, "el porcentaje (A)");
            double b = leer(total, "el total (B)");
            String r = Evaluador.formatear(a / 100.0 * b);
            res3.setForeground(Tema.TEXTO);
            res3.setText(Evaluador.formatear(a) + "% de " + Evaluador.formatear(b) + " = " + r);
            historial.agregar(Evaluador.formatear(a) + "% de " + Evaluador.formatear(b), r);
            alCambiarHistorial.run();
        } catch (Evaluador.CalcException ex) {
            error(res3, ex);
        }
    }

    private double leer(JTextField f, String nombre) {
        String t = f.getText().trim();
        if (t.isEmpty()) throw new Evaluador.CalcException("Ingresá " + nombre);
        try {
            return Evaluador.evaluar(t, true);
        } catch (Evaluador.CalcException ex) {
            throw new Evaluador.CalcException("Revisá " + nombre + ": " + ex.getMessage());
        }
    }

    private void error(JLabel destino, Evaluador.CalcException ex) {
        destino.setForeground(Tema.ERROR);
        destino.setText(ex.getMessage());
    }

    // ------------------------------------------------------------------
    // Piezas de interfaz
    // ------------------------------------------------------------------

    private static JTextField campo(String inicial) {
        JTextField f = new JTextField(inicial);
        f.setFont(Tema.fuente(Font.PLAIN, 15));
        f.setHorizontalAlignment(SwingConstants.RIGHT);
        f.setBackground(Tema.PANTALLA);
        f.setForeground(Tema.TEXTO);
        f.setCaretColor(Tema.TEXTO);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDE),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        return f;
    }

    private static JLabel etiquetaResultado() {
        JLabel l = new JLabel(" ");
        l.setFont(Tema.fuente(Font.BOLD, 15));
        l.setForeground(Tema.TEXTO);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        l.setOpaque(true);
        l.setBackground(Tema.PANTALLA);
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDE),
                BorderFactory.createEmptyBorder(8, 6, 8, 6)));
        return l;
    }

    private static TitledBorder crearBordeSeccion(String titulo) {
        TitledBorder b = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Tema.BORDE), " " + titulo + " ");
        b.setTitleColor(Tema.ACENTO);
        b.setTitleFont(Tema.fuente(Font.BOLD, 13));
        return b;
    }

    private static JPanel panelConBorde(TitledBorder b) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Tema.FONDO);
        p.setBorder(BorderFactory.createCompoundBorder(b, BorderFactory.createEmptyBorder(4, 8, 8, 8)));
        return p;
    }

    private void fila(JPanel p, int y, String texto, JTextField f) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        JLabel l = new JLabel(texto);
        l.setForeground(Tema.TEXTO_SUAVE);
        etiquetas.add(l);
        c.gridx = 0; c.gridy = y; c.weightx = 0;
        p.add(l, c);
        c.gridx = 1; c.weightx = 1;
        p.add(f, c);
    }

    private static void botonesEnFila(JPanel p, int y, Boton a, Boton b) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 4, 3, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = y; c.gridwidth = 2; c.weightx = 1;
        JPanel fila = new JPanel(new java.awt.GridLayout(1, b == null ? 1 : 2, 8, 0));
        fila.setOpaque(false);
        fila.add(a);
        if (b != null) fila.add(b);
        p.add(fila, c);
    }

    private static void resultadoEnFila(JPanel p, int y, JLabel l) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 4, 3, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = y; c.gridwidth = 2; c.weightx = 1;
        p.add(l, c);
    }

    public void actualizarTema() {
        setBackground(Tema.FONDO);
        titulo.setForeground(Tema.TEXTO);

        actualizarBordeSeccion(p1, b1, "Aumento y descuento");
        actualizarBordeSeccion(p2, b2, "Variación porcentual entre dos valores");
        actualizarBordeSeccion(p3, b3, "Parte y total");

        for (JLabel l : etiquetas) {
            l.setForeground(Tema.TEXTO_SUAVE);
        }
        for (JTextField f : campos) {
            f.setBackground(Tema.PANTALLA);
            f.setForeground(Tema.TEXTO);
            f.setCaretColor(Tema.TEXTO);
            f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Tema.BORDE),
                    BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        }
        for (JLabel r : resultados) {
            r.setBackground(Tema.PANTALLA);
            String texto = r.getText();
            if (texto.startsWith("Revisá") || texto.startsWith("Ingresá") || texto.contains("cero")) {
                r.setForeground(Tema.ERROR);
            } else {
                r.setForeground(Tema.TEXTO);
            }
            r.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Tema.BORDE),
                    BorderFactory.createEmptyBorder(8, 6, 8, 6)));
        }
        for (Boton b : botones) {
            b.actualizarTema();
        }
        botonTema.actualizarTema();
        repaint();
    }

    private void actualizarBordeSeccion(JPanel p, TitledBorder b, String titulo) {
        if (p == null || b == null) return;
        p.setBackground(Tema.FONDO);
        b.setBorder(BorderFactory.createLineBorder(Tema.BORDE));
        b.setTitleColor(Tema.ACENTO);
        p.setBorder(BorderFactory.createCompoundBorder(b, BorderFactory.createEmptyBorder(4, 8, 8, 8)));
    }
}
