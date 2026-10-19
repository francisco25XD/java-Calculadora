# Calculadora científica en Java

Calculadora con interfaz gráfica (Java Swing). No necesita librerías externas.
Requiere **Java 11 o superior** (JDK para compilar, JRE alcanza para ejecutar el `.jar`).

## Cómo ejecutarla

**Opción 1 – con el `.jar` ya compilado** (doble clic, o desde la terminal):

    java -jar Calculadora.jar

**Opción 2 – compilando el código fuente:**

    javac -encoding UTF-8 *.java
    java Calculadora

## Cómo se usa

- Se puede escribir con el teclado o con los botones. **Enter** calcula y **Esc** limpia.
- Mientras escribís, debajo de la pantalla aparece una vista previa del resultado.
- Decimales con coma o punto. Los resultados se muestran con coma.
- **⌫** borra el último dígito (o una función completa como `sin(`). **C** limpia todo.
- **±** cambia el signo del último número.
- **Grados / Radianes**: el botón de arriba a la izquierda alterna el modo para sin, cos y tan.
- **Modo Oscuro / Modo Claro**: botón en la parte superior derecha de cada una de las tres pestañas (Calculadora, Conversor y Porcentajes) para alternar el tema visual en toda la aplicación.
- **Porcentaje**: `200+10%` da 220, `200-10%` da 180, `50%` da 0,5.
- **Raíz enésima**: `root(n;x)`, por ejemplo `root(3;27)` da 3. Los argumentos se separan con `;`.
- **Potencias**: `2^10`, botón `x²` y botón `10ˣ`. **Raíz cuadrada**: `√(9)` o `sqrt(9)`.
- **Logaritmos**: `ln` (natural) y `log` (base 10). **Constantes**: `π` y `e`.
- **Ans** inserta el último resultado.
- Errores como dividir por cero, raíz de un negativo o paréntesis sin cerrar se muestran
  como mensaje en pantalla sin cerrar el programa.

## Pestañas

- **Conversor**: longitud, masa, temperatura, tiempo, volumen, área, velocidad y datos.
- **Porcentajes**: aumento y descuento, variación porcentual entre dos valores,
  "¿qué % es A de B?" y "A% de B".

## Historial

- Cada cálculo (de las tres pestañas) queda registrado a la derecha.
- **Doble clic** en un cálculo lo vuelve a cargar en la calculadora.
- **Guardar** / **Cargar** exportan e importan el historial como archivo de texto (`.txt`).
- Además se guarda solo al cerrar y se recupera al abrir, en
  `~/.calculadora_historial.txt` (carpeta personal del usuario).

## Archivos

| Archivo | Contenido |
|---|---|
| `Evaluador.java` | Motor de cálculo: prioridad de operaciones, paréntesis, funciones, errores |
| `Conversor.java` | Datos y fórmulas de conversión de unidades |
| `Historial.java` | Lista de cálculos y guardado/carga en archivo |
| `Calculadora.java` | Ventana principal (`main`) |
| `PanelCalculadora.java` | Pantalla y teclado científico |
| `PanelConversor.java`, `PanelPorcentajes.java`, `PanelHistorial.java` | Pestañas y panel lateral |
| `Boton.java`, `Tema.java` | Botones redondeados, colores y fuentes |
