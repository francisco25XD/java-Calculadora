import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Pestaña de conversión de unidades (longitud, masa, temperatura, tiempo, volumen, área, velocidad, datos). */
public class PanelConversor extends JPanel {

    private final Historial historial;
    private final Runnable alCambiarHistorial;

    private final JComboBox<Conversor.Categoria> categoria = new JComboBox<>(Conversor.CATEGORIAS);
    private final JComboBox<String> desde = new JComboBox<>();
    private final JComboBox<String> hasta = new JComboBox<>();
    private final JTextField entrada = new JTextField("1");
    private final JLabel resultado = new JLabel(" ");
    private final JLabel aviso = new JLabel(" ");
    private final JLabel titulo = new JLabel("Conversión de unidades");
    private final Boton intercambiar;
    private final Boton guardar;
    private final Boton botonTema;
    private final List<JLabel> etiquetas = new ArrayList<>();

    public PanelConversor(Historial historial, Runnable alCambiarHistorial) {
        this.historial = historial;
        this.alCambiarHistorial = alCambiarHistorial;

        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        botonTema = Tema.crearBotonModo();

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 4, 6, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        titulo.setForeground(Tema.TEXTO);
        titulo.setFont(Tema.fuente(java.awt.Font.BOLD, 18));
        cabecera.add(titulo, BorderLayout.WEST);
        cabecera.add(botonTema, BorderLayout.EAST);

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        form.add(cabecera, c);
        c.gridwidth = 1;

        agregarFila(form, c, 1, "Categoría", categoria);
        agregarFila(form, c, 2, "Valor", entrada);
        agregarFila(form, c, 3, "De", desde);
        agregarFila(form, c, 4, "A", hasta);

        intercambiar = new Boton("⇅  Intercambiar unidades", Boton.Tipo.FUNCION);
        intercambiar.addActionListener(e -> {
            int d = desde.getSelectedIndex();
            desde.setSelectedIndex(hasta.getSelectedIndex());
            hasta.setSelectedIndex(d);
        });
        c.gridx = 0; c.gridy = 5; c.gridwidth = 2;
        form.add(intercambiar, c);

        guardar = new Boton("Guardar en el historial", Boton.Tipo.IGUAL);
        guardar.addActionListener(e -> guardarEnHistorial());
        c.gridy = 6;
        form.add(guardar, c);

        resultado.setForeground(Tema.TEXTO);
        resultado.setFont(Tema.fuente(java.awt.Font.BOLD, 22));
        resultado.setHorizontalAlignment(SwingConstants.CENTER);
        resultado.setOpaque(true);
        resultado.setBackground(Tema.PANTALLA);
        resultado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDE),
                BorderFactory.createEmptyBorder(14, 8, 14, 8)));
        c.gridy = 7;
        form.add(resultado, c);

        aviso.setForeground(Tema.ERROR);
        aviso.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = 8;
        form.add(aviso, c);

        add(form, BorderLayout.NORTH);

        estilizar(categoria);
        estilizar(desde);
        estilizar(hasta);
        entrada.setFont(Tema.fuente(java.awt.Font.PLAIN, 16));
        entrada.setBackground(Tema.PANTALLA);
        entrada.setForeground(Tema.TEXTO);
        entrada.setCaretColor(Tema.TEXTO);
        entrada.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDE),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));

        categoria.addActionListener(e -> cargarUnidades());
        desde.addActionListener(e -> convertir());
        hasta.addActionListener(e -> convertir());
        entrada.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { convertir(); }
            @Override public void removeUpdate(DocumentEvent e) { convertir(); }
            @Override public void changedUpdate(DocumentEvent e) { convertir(); }
        });

        cargarUnidades();
        Tema.agregarObservador(this::actualizarTema);
    }

    private void agregarFila(JPanel form, GridBagConstraints c, int fila, String texto, java.awt.Component campo) {
        JLabel l = new JLabel(texto);
        l.setForeground(Tema.TEXTO_SUAVE);
        etiquetas.add(l);
        c.gridx = 0; c.gridy = fila; c.weightx = 0;
        form.add(l, c);
        c.gridx = 1; c.weightx = 1;
        form.add(campo, c);
    }

    private void estilizar(JComboBox<?> cb) {
        cb.setFont(Tema.fuente(java.awt.Font.PLAIN, 14));
        cb.setBackground(Tema.PANTALLA);
        cb.setForeground(Tema.TEXTO);
    }

    public void actualizarTema() {
        setBackground(Tema.FONDO);
        titulo.setForeground(Tema.TEXTO);
        for (JLabel l : etiquetas) {
            l.setForeground(Tema.TEXTO_SUAVE);
        }
        entrada.setBackground(Tema.PANTALLA);
        entrada.setForeground(Tema.TEXTO);
        entrada.setCaretColor(Tema.TEXTO);
        entrada.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDE),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));

        categoria.setBackground(Tema.PANTALLA);
        categoria.setForeground(Tema.TEXTO);
        desde.setBackground(Tema.PANTALLA);
        desde.setForeground(Tema.TEXTO);
        hasta.setBackground(Tema.PANTALLA);
        hasta.setForeground(Tema.TEXTO);

        resultado.setBackground(Tema.PANTALLA);
        resultado.setForeground(Tema.TEXTO);
        resultado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDE),
                BorderFactory.createEmptyBorder(14, 8, 14, 8)));

        aviso.setForeground(Tema.ERROR);
        intercambiar.actualizarTema();
        guardar.actualizarTema();
        botonTema.actualizarTema();
        repaint();
    }

    private void cargarUnidades() {
        Conversor.Categoria cat = (Conversor.Categoria) categoria.getSelectedItem();
        desde.setModel(new DefaultComboBoxModel<>(cat.unidades));
        hasta.setModel(new DefaultComboBoxModel<>(cat.unidades));
        hasta.setSelectedIndex(Math.min(1, cat.unidades.length - 1));
        convertir();
    }

    /** Devuelve el texto del resultado o null si no se puede convertir. */
    private String calcular() {
        aviso.setText(" ");
        String t = entrada.getText().trim();
        if (t.isEmpty() || desde.getSelectedIndex() < 0 || hasta.getSelectedIndex() < 0) return null;
        try {
            // Permite escribir expresiones simples, por ejemplo 2*5 o 1/4
            double valor = Evaluador.evaluar(t, true);
            Conversor.Categoria cat = (Conversor.Categoria) categoria.getSelectedItem();
            double r = Conversor.convertir(cat, valor, desde.getSelectedIndex(), hasta.getSelectedIndex());
            return Evaluador.formatear(valor) + " " + abreviatura((String) desde.getSelectedItem())
                    + "  =  " + Evaluador.formatear(r) + " " + abreviatura((String) hasta.getSelectedItem());
        } catch (Evaluador.CalcException ex) {
            aviso.setText(ex.getMessage());
            return null;
        }
    }

    private void convertir() {
        String r = calcular();
        resultado.setText(r == null ? " " : r);
    }

    private void guardarEnHistorial() {
        String r = calcular();
        if (r == null) return;
        String[] partes = r.split("  =  ");
        historial.agregar("Conversión: " + partes[0], partes[1]);
        alCambiarHistorial.run();
    }

    /** Extrae la abreviatura entre paréntesis: "Kilómetro (km)" -> "km". */
    private static String abreviatura(String unidad) {
        int a = unidad.lastIndexOf('(');
        int b = unidad.lastIndexOf(')');
        if (a >= 0 && b > a) return unidad.substring(a + 1, b);
        return unidad;
    }
}
