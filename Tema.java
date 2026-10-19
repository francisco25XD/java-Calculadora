import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/** Colores, fuentes y gestión de temas (modo oscuro y modo claro) compartidos por toda la interfaz. */
public final class Tema {
    private Tema() {}

    public static boolean esOscuro = true;

    // Colores dinámicos según el modo activo
    public static Color FONDO;
    public static Color PANTALLA;
    public static Color NUMERO;
    public static Color FUNCION;
    public static Color OPERADOR;
    public static Color IGUAL;
    public static Color BORRAR;
    public static Color TEXTO;
    public static Color TEXTO_SUAVE;
    public static Color ERROR;
    public static Color ACENTO;
    public static Color BORDE;

    private static final List<Runnable> observadores = new ArrayList<>();

    static {
        aplicarColores();
    }

    public static void agregarObservador(Runnable observador) {
        observadores.add(observador);
    }

    public static void alternarTema() {
        setOscuro(!esOscuro);
    }

    public static void setOscuro(boolean oscuro) {
        esOscuro = oscuro;
        aplicarColores();
        for (Runnable r : observadores) {
            r.run();
        }
    }

    private static void aplicarColores() {
        if (esOscuro) {
            FONDO       = new Color(0x1B1F2B);
            PANTALLA    = new Color(0x12151D);
            NUMERO      = new Color(0x2C3347);
            FUNCION     = new Color(0x3B4563);
            OPERADOR    = new Color(0xE08A1E);
            IGUAL       = new Color(0x2E9D6B);
            BORRAR      = new Color(0xC04A4A);
            TEXTO       = new Color(0xF2F4F8);
            TEXTO_SUAVE = new Color(0x9AA3B8);
            ERROR       = new Color(0xFF6B6B);
            ACENTO      = new Color(0x5B8DEF);
            BORDE       = new Color(0x3B4563);
        } else {
            FONDO       = new Color(0xF2F4F8);
            PANTALLA    = new Color(0xFFFFFF);
            NUMERO      = new Color(0xE2E7F0);
            FUNCION     = new Color(0xD0D7E5);
            OPERADOR    = new Color(0xE08A1E);
            IGUAL       = new Color(0x2E9D6B);
            BORRAR      = new Color(0xD64545);
            TEXTO       = new Color(0x1A202C);
            TEXTO_SUAVE = new Color(0x718096);
            ERROR       = new Color(0xE53E3E);
            ACENTO      = new Color(0x3182CE);
            BORDE       = new Color(0xCBD5E0);
        }
    }

    public static Color colorBase(Boton.Tipo tipo) {
        if (tipo == null) return FUNCION;
        switch (tipo) {
            case NUMERO:   return NUMERO;
            case FUNCION:  return FUNCION;
            case OPERADOR: return OPERADOR;
            case IGUAL:    return IGUAL;
            case BORRAR:   return BORRAR;
            case ACENTO:   return ACENTO;
            case TEMA:     return FUNCION;
            default:       return FUNCION;
        }
    }

    public static Color colorTexto(Boton.Tipo tipo) {
        if (tipo == null) return TEXTO;
        switch (tipo) {
            case OPERADOR:
            case IGUAL:
            case BORRAR:
            case ACENTO:
                return Color.WHITE;
            case NUMERO:
            case FUNCION:
            case TEMA:
            default:
                return TEXTO;
        }
    }

    public static String textoBotonTema() {
        return esOscuro ? "☀️ Modo Claro" : "🌙 Modo Oscuro";
    }

    public static String tooltipBotonTema() {
        return esOscuro ? "Cambiar a modo claro" : "Cambiar a modo oscuro";
    }

    public static Boton crearBotonModo() {
        Boton b = new Boton(textoBotonTema(), Boton.Tipo.TEMA);
        b.setFont(fuente(Font.BOLD, 12));
        b.setPreferredSize(new Dimension(125, 30));
        b.setToolTipText(tooltipBotonTema());
        b.addActionListener(e -> alternarTema());
        return b;
    }

    public static Font fuente(int estilo, int tam) {
        return new Font(Font.SANS_SERIF, estilo, tam);
    }
}
