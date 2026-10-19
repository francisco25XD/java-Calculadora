import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Calculadora científica con interfaz gráfica (Java Swing).
 * Ejecutar: java Calculadora
 */
public class Calculadora extends JFrame {

    private static final String[] NOMBRES = {"Calculadora", "Conversor", "Porcentajes"};

    private final Historial historial = new Historial();
    private PanelHistorial panelHistorial;
    private final PanelCalculadora panelCalc;
    private final PanelConversor panelConv;
    private final PanelPorcentajes panelPorc;
    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);
    private final Boton[] pestanas = new Boton[NOMBRES.length];
    private JPanel barraPestanas;
    private int pestanaActiva = 0;

    public Calculadora() {
        super("Calculadora científica");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        getContentPane().setBackground(Tema.FONDO);
        setLayout(new BorderLayout());

        cargarHistorialAutomatico();

        Runnable alCambiarHistorial = () -> {
            if (this.panelHistorial != null) this.panelHistorial.refrescar();
        };

        panelHistorial = new PanelHistorial(historial, this::reutilizar);
        panelCalc = new PanelCalculadora(historial, alCambiarHistorial);
        panelConv = new PanelConversor(historial, alCambiarHistorial);
        panelPorc = new PanelPorcentajes(historial, alCambiarHistorial);

        contenido.setBackground(Tema.FONDO);
        contenido.add(panelCalc, NOMBRES[0]);
        contenido.add(panelConv, NOMBRES[1]);
        contenido.add(panelPorc, NOMBRES[2]);

        barraPestanas = crearBarraPestanas();
        add(barraPestanas, BorderLayout.NORTH);
        add(contenido, BorderLayout.CENTER);
        add(panelHistorial, BorderLayout.EAST);

        mostrar(0);

        Tema.agregarObservador(this::actualizarTema);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                guardarHistorialAutomatico();
                dispose();
                System.exit(0);
            }
        });

        setMinimumSize(new Dimension(780, 610));
        setSize(840, 640);
        setLocationRelativeTo(null);
    }

    private JPanel crearBarraPestanas() {
        JPanel barra = new JPanel(new GridLayout(1, NOMBRES.length, 6, 0));
        barra.setBackground(Tema.FONDO);
        barra.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        for (int i = 0; i < NOMBRES.length; i++) {
            final int indice = i;
            pestanas[i] = new Boton(NOMBRES[i], Boton.Tipo.FUNCION);
            pestanas[i].setPreferredSize(new Dimension(120, 38));
            pestanas[i].addActionListener(e -> mostrar(indice));
            barra.add(pestanas[i]);
        }
        return barra;
    }

    private void mostrar(int indice) {
        pestanaActiva = indice;
        cartas.show(contenido, NOMBRES[indice]);
        for (int i = 0; i < pestanas.length; i++) {
            boolean activa = (i == indice);
            pestanas[i].setColorBase(activa ? Tema.ACENTO : Tema.FUNCION);
            pestanas[i].setColorTexto(activa ? Color.WHITE : Tema.TEXTO);
        }
    }

    private void actualizarTema() {
        getContentPane().setBackground(Tema.FONDO);
        contenido.setBackground(Tema.FONDO);
        if (barraPestanas != null) {
            barraPestanas.setBackground(Tema.FONDO);
        }
        mostrar(pestanaActiva);
        repaint();
    }

    /** Doble clic en el historial: vuelve a la calculadora con esa operación lista para editar. */
    private void reutilizar(Historial.Entrada entrada) {
        mostrar(0);
        try {
            // Si la operación es una expresión válida, se recupera tal cual
            Evaluador.evaluar(entrada.operacion, panelCalc.isGrados());
            panelCalc.cargarExpresion(entrada.operacion);
        } catch (Evaluador.CalcException ex) {
            // Conversiones y porcentajes: se recupera el número del resultado
            panelCalc.cargarExpresion(PanelHistorial.numeroDe(entrada.resultado));
        }
    }

    // ------------------------------------------------------------------
    // Guardado automático del historial entre sesiones
    // ------------------------------------------------------------------

    private void cargarHistorialAutomatico() {
        if (!java.nio.file.Files.exists(Historial.ARCHIVO_AUTO)) return;
        try {
            historial.cargar(Historial.ARCHIVO_AUTO, true);
        } catch (IOException ex) {
            System.err.println("No se pudo leer el historial anterior: " + ex.getMessage());
        }
    }

    private void guardarHistorialAutomatico() {
        try {
            historial.guardar(Historial.ARCHIVO_AUTO);
        } catch (IOException ex) {
            System.err.println("No se pudo guardar el historial: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignorada) {
            // si falla, se usa el aspecto por defecto
        }
        SwingUtilities.invokeLater(() -> new Calculadora().setVisible(true));
    }
}
