/**
 * Conversión de unidades. Cada categoría define sus unidades con un factor
 * respecto a una unidad base. La temperatura se trata aparte (no es lineal).
 */
public class Conversor {

    public static class Categoria {
        public final String nombre;
        public final String[] unidades;
        public final double[] factores; // valor_en_base = valor * factor

        Categoria(String nombre, String[] unidades, double[] factores) {
            this.nombre = nombre;
            this.unidades = unidades;
            this.factores = factores;
        }

        public boolean esTemperatura() {
            return nombre.equals("Temperatura");
        }

        @Override
        public String toString() {
            return nombre;
        }
    }

    public static final Categoria[] CATEGORIAS = {
        new Categoria("Longitud",
            new String[]{"Milímetro (mm)", "Centímetro (cm)", "Metro (m)", "Kilómetro (km)",
                         "Pulgada (in)", "Pie (ft)", "Yarda (yd)", "Milla (mi)"},
            new double[]{0.001, 0.01, 1, 1000, 0.0254, 0.3048, 0.9144, 1609.344}),

        new Categoria("Masa",
            new String[]{"Miligramo (mg)", "Gramo (g)", "Kilogramo (kg)", "Tonelada (t)",
                         "Onza (oz)", "Libra (lb)"},
            new double[]{0.000001, 0.001, 1, 1000, 0.028349523125, 0.45359237}),

        new Categoria("Temperatura",
            new String[]{"Celsius (°C)", "Fahrenheit (°F)", "Kelvin (K)"},
            new double[]{1, 1, 1}),

        new Categoria("Tiempo",
            new String[]{"Segundo (s)", "Minuto (min)", "Hora (h)", "Día", "Semana", "Año (365 días)"},
            new double[]{1, 60, 3600, 86400, 604800, 31536000}),

        new Categoria("Volumen",
            new String[]{"Mililitro (ml)", "Litro (l)", "Metro cúbico (m³)",
                         "Galón US (gal)", "Taza US (cup)", "Onza líquida US (fl oz)"},
            new double[]{0.001, 1, 1000, 3.785411784, 0.2365882365, 0.0295735295625}),

        new Categoria("Área",
            new String[]{"Centímetro² (cm²)", "Metro² (m²)", "Hectárea (ha)", "Kilómetro² (km²)",
                         "Pie² (ft²)", "Acre"},
            new double[]{0.0001, 1, 10000, 1000000, 0.09290304, 4046.8564224}),

        new Categoria("Velocidad",
            new String[]{"Metro/segundo (m/s)", "Kilómetro/hora (km/h)", "Milla/hora (mph)", "Nudo (kn)"},
            new double[]{1, 1000.0 / 3600.0, 0.44704, 1852.0 / 3600.0}),

        new Categoria("Datos",
            new String[]{"Byte (B)", "Kilobyte (KB)", "Megabyte (MB)", "Gigabyte (GB)", "Terabyte (TB)"},
            new double[]{1, 1024, 1024.0 * 1024, 1024.0 * 1024 * 1024, 1024.0 * 1024 * 1024 * 1024}),
    };

    /** Convierte un valor entre dos unidades (por índice) de una categoría. */
    public static double convertir(Categoria cat, double valor, int desde, int hasta) {
        if (cat.esTemperatura()) {
            return convertirTemperatura(valor, desde, hasta);
        }
        return valor * cat.factores[desde] / cat.factores[hasta];
    }

    // Índices: 0 = Celsius, 1 = Fahrenheit, 2 = Kelvin
    private static double convertirTemperatura(double v, int desde, int hasta) {
        double celsius;
        switch (desde) {
            case 1:  celsius = (v - 32) * 5.0 / 9.0; break;
            case 2:  celsius = v - 273.15; break;
            default: celsius = v;
        }
        if (celsius < -273.15 - 1e-9) {
            throw new Evaluador.CalcException("La temperatura está por debajo del cero absoluto");
        }
        switch (hasta) {
            case 1:  return celsius * 9.0 / 5.0 + 32;
            case 2:  return celsius + 273.15;
            default: return celsius;
        }
    }
}
