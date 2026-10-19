import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Panel lateral con la lista de cálculos; permite reutilizarlos, borrarlos, guardarlos y cargarlos. */
public class PanelHistorial extends JPanel {

    private final Historial historial;
    private final DefaultListModel<Historial.Entrada> modelo = new DefaultListModel<>();
    private final JList<Historial.Entrada> lista = new JList<>(modelo);
    private final Consumer<Historial.Entrada> alElegir;

    private final JLabel titulo = new JLabel("Historial");
    private final JLabel ayuda = new JLabel("Doble clic en un cálculo para reutilizarlo");
    private final JScrollPane scroll;
    private final Boton guardar;
    private final Boton cargar;
    private final Boton borrar;

    public PanelHistorial(Historial historial, Consumer<Historial.Entrada> alElegir) {
        this.historial = historial;
        this.alElegir = alElegir;

        setLayout(new BorderLayout(0, 8));
        setBackground(Tema.FONDO);
        setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
        setPreferredSize(new Dimension(290, 400));

        titulo.setForeground(Tema.TEXTO);
        titulo.setFont(Tema.fuente(java.awt.Font.BOLD, 18));
        add(titulo, BorderLayout.NORTH);

        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setBackground(Tema.PANTALLA);
        lista.setCellRenderer(new Celda());
        lista.setToolTipText("Doble clic para reutilizar el cálculo");
        lista.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && lista.getSelectedValue() != null) {
                    alElegir.accept(lista.getSelectedValue());
                }
            }
        });
        scroll = new JScrollPane(lista);
        scroll.setBorder(BorderFactory.createLineBorder(Tema.BORDE));
        scroll.getViewport().setBackground(Tema.PANTALLA);
        add(scroll, BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout(0, 6));
        abajo.setOpaque(false);
        ayuda.setForeground(Tema.TEXTO_SUAVE);
        ayuda.setFont(Tema.fuente(java.awt.Font.PLAIN, 11));
        ayuda.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel botones = new JPanel(new GridLayout(1, 3, 6, 0));
        botones.setOpaque(false);
        guardar = new Boton("Guardar", Boton.Tipo.ACENTO);
        cargar = new Boton("Cargar", Boton.Tipo.ACENTO);
        borrar = new Boton("Borrar", Boton.Tipo.BORRAR);
        for (Boton b : new Boton[]{guardar, cargar, borrar}) b.setFont(Tema.fuente(java.awt.Font.BOLD, 13));
        guardar.setToolTipText("Guardar el historial en un archivo de texto");
        cargar.setToolTipText("Recuperar un historial guardado");
        borrar.setToolTipText("Vaciar el historial");
        guardar.addActionListener(e -> guardarArchivo());
        cargar.addActionListener(e -> cargarArchivo());
        borrar.addActionListener(e -> borrarTodo());
        botones.add(guardar);
        botones.add(cargar);
        botones.add(borrar);

        abajo.add(botones, BorderLayout.CENTER);
        abajo.add(ayuda, BorderLayout.SOUTH);
        add(abajo, BorderLayout.SOUTH);

        refrescar();
        Tema.agregarObservador(this::actualizarTema);
    }

    public void actualizarTema() {
        setBackground(Tema.FONDO);
        titulo.setForeground(Tema.TEXTO);
        lista.setBackground(Tema.PANTALLA);
        scroll.setBorder(BorderFactory.createLineBorder(Tema.BORDE));
        scroll.getViewport().setBackground(Tema.PANTALLA);
        ayuda.setForeground(Tema.TEXTO_SUAVE);
        guardar.actualizarTema();
        cargar.actualizarTema();
        borrar.actualizarTema();
        lista.repaint();
        repaint();
    }

    /** Vuelve a mostrar el contenido del historial y baja hasta el último cálculo. */
    public void refrescar() {
        modelo.clear();
        for (Historial.Entrada e : historial.getEntradas()) modelo.addElement(e);
        if (!modelo.isEmpty()) lista.ensureIndexIsVisible(modelo.size() - 1);
    }

    // ------------------------------------------------------------------
    // Acciones
    // ------------------------------------------------------------------

    private void guardarArchivo() {
        if (historial.getEntradas().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El historial está vacío, no hay nada para guardar.",
                    "Guardar historial", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar historial");
        fc.setFileFilter(new FileNameExtensionFilter("Archivo de texto (*.txt)", "txt"));
        fc.setSelectedFile(new File("historial.txt"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".txt")) f = new File(f.getPath() + ".txt");
        if (f.exists() && JOptionPane.showConfirmDialog(this, "El archivo ya existe. ¿Reemplazarlo?",
                "Guardar historial", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            historial.guardar(f.toPath());
            JOptionPane.showMessageDialog(this, "Historial guardado en:\n" + f.getAbsolutePath(),
                    "Guardar historial", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar el archivo:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarArchivo() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Cargar historial");
        fc.setFileFilter(new FileNameExtensionFilter("Archivo de texto (*.txt)", "txt"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        Path origen = fc.getSelectedFile().toPath();
        boolean reemplazar = false;
        if (!historial.getEntradas().isEmpty()) {
            Object[] opciones = {"Reemplazar", "Agregar al actual", "Cancelar"};
            int r = JOptionPane.showOptionDialog(this, "¿Qué hacemos con el historial actual?",
                    "Cargar historial", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, opciones, opciones[1]);
            if (r == 2 || r == JOptionPane.CLOSED_OPTION) return;
            reemplazar = (r == 0);
        }
        try {
            int n = historial.cargar(origen, reemplazar);
            refrescar();
            JOptionPane.showMessageDialog(this, "Se cargaron " + n + " cálculos.",
                    "Cargar historial", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo leer el archivo:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void borrarTodo() {
        if (historial.getEntradas().isEmpty()) return;
        if (JOptionPane.showConfirmDialog(this, "¿Borrar todo el historial?", "Borrar historial",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            historial.limpiar();
            refrescar();
        }
    }

    // ------------------------------------------------------------------
    // Presentación de cada cálculo: operación arriba, resultado abajo
    // ------------------------------------------------------------------

    private static class Celda implements ListCellRenderer<Historial.Entrada> {
        private final JPanel panel = new JPanel(new BorderLayout());
        private final JLabel operacion = new JLabel();
        private final JLabel resultado = new JLabel();

        Celda() {
            operacion.setFont(Tema.fuente(java.awt.Font.PLAIN, 12));
            operacion.setHorizontalAlignment(SwingConstants.RIGHT);
            resultado.setFont(Tema.fuente(java.awt.Font.BOLD, 17));
            resultado.setHorizontalAlignment(SwingConstants.RIGHT);
            panel.add(operacion, BorderLayout.NORTH);
            panel.add(resultado, BorderLayout.SOUTH);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Historial.Entrada> l, Historial.Entrada e,
                int indice, boolean seleccionado, boolean foco) {
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDE),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)));
            operacion.setText(e.operacion + (e.fecha.isEmpty() ? "" : "   · " + e.fecha));
            operacion.setForeground(Tema.TEXTO_SUAVE);
            resultado.setText("= " + e.resultado);
            resultado.setForeground(Tema.TEXTO);
            panel.setBackground(seleccionado ? (Tema.esOscuro ? Tema.FUNCION : Tema.NUMERO) : Tema.PANTALLA);
            return panel;
        }
    }

    /** Extrae el número inicial de un resultado como "212 °F" o "+25%" (para reutilizarlo en la calculadora). */
    public static String numeroDe(String resultado) {
        Matcher m = Pattern.compile("^[−+-]?[0-9]+(?:[.,][0-9]+)?(?:E-?[0-9]+)?").matcher(resultado.trim());
        return m.find() ? m.group().replace('−', '-').replace("+", "") : resultado;
    }
}
