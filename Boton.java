import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

/** Botón redondeado con efecto al pasar el mouse y al presionar. Se ve igual en cualquier sistema. */
public class Boton extends JButton {

    public enum Tipo {
        NUMERO, FUNCION, OPERADOR, IGUAL, BORRAR, ACENTO, TEMA
    }

    private Tipo tipo;
    private Color base;
    private Color texto;
    private boolean encima = false;
    private boolean presionado = false;

    public Boton(String etiqueta, Tipo tipo) {
        this(etiqueta, Tema.colorBase(tipo), Tema.colorTexto(tipo));
        this.tipo = tipo;
    }

    public Boton(String etiqueta, Color fondo, Color colorTexto) {
        super(etiqueta);
        this.base = fondo;
        this.texto = colorTexto;
        setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFocusable(false); // así el cursor no sale del campo de la expresión
        setOpaque(false);
        setPreferredSize(new Dimension(64, 48));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { encima = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { encima = false; presionado = false; repaint(); }
            @Override public void mousePressed(MouseEvent e) { presionado = true; repaint(); }
            @Override public void mouseReleased(MouseEvent e) { presionado = false; repaint(); }
        });
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
        actualizarTema();
    }

    public void actualizarTema() {
        if (tipo != null) {
            this.base = Tema.colorBase(tipo);
            this.texto = Tema.colorTexto(tipo);
            if (tipo == Tipo.TEMA) {
                setText(Tema.textoBotonTema());
                setToolTipText(Tema.tooltipBotonTema());
            }
            repaint();
        }
    }

    /** Cambia el color de fondo (se usa para marcar la pestaña activa). */
    public void setColorBase(Color nuevo) {
        this.base = nuevo;
        repaint();
    }

    public void setColorTexto(Color nuevo) {
        this.texto = nuevo;
        repaint();
    }

    public void setColores(Color nuevoFondo, Color nuevoTexto) {
        this.base = nuevoFondo;
        this.texto = nuevoTexto;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color fondo = base;
        if (presionado) {
            fondo = base.darker();
        } else if (encima) {
            fondo = mezclar(base, Tema.esOscuro ? Color.WHITE : Color.BLACK, Tema.esOscuro ? 0.15f : 0.08f);
        }

        int w = getWidth() - 4;
        int h = getHeight() - 4;
        g2.setColor(fondo);
        g2.fillRoundRect(2, 2, w, h, 14, 14);

        g2.setColor(texto);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), x, y);
        g2.dispose();
    }

    private static Color mezclar(Color a, Color b, float t) {
        return new Color(
            Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
            Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
            Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }
}
