package trigono;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Utilidad de impresión CLI agnóstica al Sistema Operativo.
 * Utiliza exclusivamente la biblioteca estándar de Java ({@link PrintWriter},
 * {@link System#lineSeparator()} y formato ASCII universal) sin dependencias
 * nativas ni secuencias dependientes del SO, garantizando visualización idéntica
 * en Windows (CMD/PowerShell) y Linux/macOS.
 */
public final class CliPrinter {
    private static final String LINE_SEP = "+----------------------------------------------------------------------------+";
    private static final String SUB_SEP  = "|----------------------------------------------------------------------------|";
    private final PrintWriter out;
    private final PrintWriter err;

    public CliPrinter() {
        this.out = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
        this.err = new PrintWriter(System.err, true, StandardCharsets.UTF_8);
    }

    public void printBanner() {
        out.println(LINE_SEP);
        out.println("|   RECONOCEDOR DESCENDENTE LL(1) Y EVALUADOR DE EXPRESIONES TRIGONOMETRICAS |");
        out.println("|   Operadores: +, -, *, /, ^, negacion unaria (-), parentesis ( )           |");
        out.println("|   Funciones : sin (o sen), cos, tan  |  Variables simbolicas: x, y, z...   |");
        out.println(LINE_SEP);
    }

    public void printHelp() {
        printBanner();
        out.println("Modos de uso (portable en Windows y Linux):");
        out.println("  1. Modo interactivo (consola):");
        out.println("       java -cp bin trigono.Main");
        out.println("  2. Evaluar una expresion directamente:");
        out.println("       java -cp bin trigono.Main --eval \"sen(3.14 / 2) + cos(0) * 2^2\"");
        out.println("  3. Evaluar una expresion con variables simbolicas predefinidas:");
        out.println("       java -cp bin trigono.Main --eval \"sen(x) + cos(y)\" --var x=1.57079632679 --var y=0");
        out.println("  4. Mostrar la Gramatica LL(1) y conjuntos FIRST / FOLLOW:");
        out.println("       java -cp bin trigono.Main --grammar");
        out.println("  5. Ejecutar la bateria completa de casos de prueba y validacion:");
        out.println("       java -cp bin trigono.TestSuite");
        out.println();
        out.println("Codigos de salida (Exit Codes):");
        out.println("  0 -> Ejecucion exitosa");
        out.println("  1 -> Error Lexico (caracter ilegal, numero mal formado o funcion desconocida)");
        out.println("  2 -> Error Sintactico (violacion de la gramatica LL(1))");
        out.println("  3 -> Error Matematico / Semantico (division por cero, tangente indefinida, etc.)");
        out.println("  4 -> Error de entrada de usuario");
        out.println(LINE_SEP);
    }

    public void printGrammarInfo() {
        out.println(LINE_SEP);
        out.println("| GRAMATICA LL(1) DISENADA (SIN RECURSIVIDAD POR LA IZQUIERDA)               |");
        out.println(SUB_SEP);
        out.println("|  1. E    -> T E'                                                           |");
        out.println("|  2. E'   -> '+' T E' | '-' T E' | epsilon                                  |");
        out.println("|  3. T    -> U T'                                                           |");
        out.println("|  4. T'   -> '*' U T' | '/' U T' | epsilon                                  |");
        out.println("|  5. U    -> '-' U | '+' U | P                                              |");
        out.println("|  6. P    -> F P'                                                           |");
        out.println("|  7. P'   -> '^' U | epsilon                                                |");
        out.println("|  8. F    -> '(' E ')' | FUNC '(' E ')' | NUMBER | VARIABLE                 |");
        out.println("|  9. FUNC -> 'sin' | 'cos' | 'tan'  (acepta alias 'sen' para 'sin')         |");
        out.println(LINE_SEP);
    }

    public void printTokens(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t.getType() == TokenType.EOF) {
                sb.append("EOF");
            } else {
                sb.append(t.getType().name()).append("('").append(t.getLexeme()).append("')");
            }
            if (i < tokens.size() - 1) {
                sb.append(" -> ");
            }
        }
        out.println("  [Tokens]    : " + sb);
    }

    public void printDetectedVariables(Set<String> variables) {
        if (!variables.isEmpty()) {
            out.println("  [Variables] : " + String.join(", ", variables));
        }
    }

    public void printSubstitutedVariables(Map<String, Double> values) {
        if (!values.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (Map.Entry<String, Double> entry : values.entrySet()) {
                if (i++ > 0) {
                    sb.append(", ");
                }
                sb.append(entry.getKey()).append(" = ").append(formatDouble(entry.getValue()));
            }
            out.println("  [Valores]   : " + sb);
        }
    }

    public void printEvaluationSuccess(String expression, double result) {
        out.println("  [Expresion] : " + expression);
        out.println("  [Resultado] : " + formatDouble(result) + " (double: " + result + ")");
        out.println("  [Estado]    : OK (Codigo de salida: " + ExitCodes.SUCCESS + ")");
    }

    public void printError(String expression, EvaluatorException ex) {
        err.println(LINE_SEP);
        err.println("  [" + ex.getErrorCategory() + "] (Codigo de salida: " + ex.getExitCode() + ")");
        if (expression != null && !expression.isEmpty()) {
            err.println("  Expresion : " + expression);
            if (ex.getPosition() > 0 && ex.getPosition() <= expression.length() + 1) {
                String pointer = " ".repeat(Math.max(0, ex.getPosition() - 1)) + "^";
                err.println("              " + pointer);
            }
        }
        err.println("  Detalle   : " + ex.getMessage());
        err.println(LINE_SEP);
    }

    public void printPrompt(String promptText) {
        out.print(promptText);
        out.flush();
    }

    public void println(String text) {
        out.println(text);
    }

    public void printlnErr(String text) {
        err.println(text);
    }

    public static String formatDouble(double value) {
        return String.format(Locale.US, "%.10f", value)
                .replaceAll("0+$", "")
                .replaceAll("\\.$", ".0");
    }
}
