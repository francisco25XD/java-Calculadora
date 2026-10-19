import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Lista de cálculos realizados, con guardado y carga desde archivo de texto. */
public class Historial {

    public static class Entrada {
        public final String operacion;
        public final String resultado;
        public final String fecha;

        public Entrada(String operacion, String resultado, String fecha) {
            this.operacion = operacion;
            this.resultado = resultado;
            this.fecha = fecha;
        }

        @Override
        public String toString() {
            return operacion + "  =  " + resultado;
        }
    }

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int MAXIMO = 500;

    /** Archivo donde se guarda automáticamente el historial. */
    public static final Path ARCHIVO_AUTO =
            Paths.get(System.getProperty("user.home"), ".calculadora_historial.txt");

    private final List<Entrada> entradas = new ArrayList<>();

    public List<Entrada> getEntradas() {
        return entradas;
    }

    public Entrada agregar(String operacion, String resultado) {
        Entrada e = new Entrada(operacion, resultado, LocalDateTime.now().format(FORMATO));
        entradas.add(e);
        while (entradas.size() > MAXIMO) entradas.remove(0);
        return e;
    }

    public void limpiar() {
        entradas.clear();
    }

    /** Guarda el historial: una línea por entrada, campos separados por tabulación. */
    public void guardar(Path destino) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            w.write("# Historial de la calculadora (operacion<TAB>resultado<TAB>fecha)");
            w.newLine();
            for (Entrada e : entradas) {
                w.write(limpiarCampo(e.operacion) + "\t" + limpiarCampo(e.resultado) + "\t" + limpiarCampo(e.fecha));
                w.newLine();
            }
        }
    }

    /** Carga un historial desde archivo. Devuelve cuántas entradas se leyeron. */
    public int cargar(Path origen, boolean reemplazar) throws IOException {
        List<Entrada> leidas = new ArrayList<>();
        try (BufferedReader r = Files.newBufferedReader(origen, StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = r.readLine()) != null) {
                if (linea.isBlank() || linea.startsWith("#")) continue;
                String[] p = linea.split("\t", -1);
                if (p.length < 2) continue;
                leidas.add(new Entrada(p[0], p[1], p.length > 2 ? p[2] : ""));
            }
        }
        if (reemplazar) entradas.clear();
        entradas.addAll(leidas);
        while (entradas.size() > MAXIMO) entradas.remove(0);
        return leidas.size();
    }

    private static String limpiarCampo(String s) {
        return s.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
    }
}
