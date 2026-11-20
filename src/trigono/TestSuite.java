package trigono;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Suite de pruebas automatizadas y validación matemática para el Reconocedor
 * Descendente Recursivo LL(1) y Evaluador de Expresiones Trigonométricas.
 *
 * <p>Ejecuta todos los casos de prueba exigidos en el Trabajo Práctico 3, además de
 * pruebas exhaustivas de precedencia, asociatividad, negación unaria, variables
 * simbólicas y clasificación de errores léxicos (exit code 1), sintácticos (exit code 2)
 * y matemáticos (exit code 3).</p>
 */
public class TestSuite {
    private static final double TOLERANCE = 1e-9;
    private static final PrintWriter OUT = new PrintWriter(System.out, true, StandardCharsets.UTF_8);

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        OUT.println("+====================================================================================+");
        OUT.println("| BATERIA DE PRUEBAS Y VALIDACION MATEMATICA - PARSER EVALUADOR LL(1)                |");
        OUT.println("+====================================================================================+");

        runPdfExampleTests();
        runArithmeticAndPrecedenceTests();
        runErrorClassificationTests();

        OUT.println("+====================================================================================+");
        OUT.printf(Locale.US, "| RESUMEN FINAL: Total = %d  |  Aprobados = %d  |  Fallidos = %d                     |%n",
                totalTests, passedTests, failedTests);
        OUT.println("+====================================================================================+");

        if (failedTests > 0) {
            System.exit(1);
        }
        System.exit(ExitCodes.SUCCESS);
    }

    /**
     * 1. Casos de prueba oficiales extraídos directamente del PDF del Trabajo Práctico 3.
     */
    private static void runPdfExampleTests() {
        OUT.println("\n[SECCION 1] Expresiones oficiales del enunciado (Trabajo Practico 3):");
        OUT.println("--------------------------------------------------------------------------------------");

        // Caso 1.1: sen(3.14 / 2) + cos(0) * 2^2
        assertValidExpression(
                "PDF-01",
                "sen(3.14 / 2) + cos(0) * 2^2",
                Collections.emptyMap(),
                Math.sin(3.14 / 2.0) + Math.cos(0.0) * Math.pow(2.0, 2.0),
                "sin(1.57) + 1 * 4 = 0.9999996829 + 4 = 4.9999996829"
        );

        // Caso 1.2 (Página 2 del PDF): sin(pi / 2) + cos(0) * 2^2 con x = pi/2
        assertValidExpression(
                "PDF-02",
                "sin(x / 2) + cos(0) * 2^2",
                mapOf("x", Math.PI),
                Math.sin(Math.PI / 2.0) + Math.cos(0.0) * Math.pow(2.0, 2.0),
                "sin(pi/2) + cos(0)*4 = 1 + 1*4 = 5.0"
        );

        // Caso 1.3: sen(x) + cos(y)
        assertValidExpression(
                "PDF-03",
                "sen(x) + cos(y)",
                mapOf("x", Math.PI / 2.0, "y", 0.0),
                Math.sin(Math.PI / 2.0) + Math.cos(0.0),
                "con x=pi/2, y=0 -> sin(pi/2) + cos(0) = 1.0 + 1.0 = 2.0"
        );

        // Caso 1.4: sen(cos(x))+ tan(y)/cos(x) // con comentario incluido tal como en el PDF
        assertValidExpression(
                "PDF-04",
                "sen(cos(x))+ tan(y)/cos(x) // luego se deberia ingresar o pedir los valores de x, y",
                mapOf("x", 0.0, "y", Math.PI / 4.0),
                Math.sin(Math.cos(0.0)) + Math.tan(Math.PI / 4.0) / Math.cos(0.0),
                "con x=0, y=pi/4 -> sin(1) + 1/1 = 0.8414709848 + 1 = 1.8414709848"
        );

        // Caso 1.5: cos(x)^2 + sen(x)^2 (Identidad pitagórica fundamental = 1.0 para cualquier x)
        assertValidExpression(
                "PDF-05",
                "cos(x)^2 + sen(x)^2",
                mapOf("x", 1.2345678),
                1.0,
                "Identidad pitagorica cos^2(x) + sin^2(x) = 1.0 (para x=1.2345678)"
        );

        // Caso 1.6: sin(x)
        assertValidExpression(
                "PDF-06",
                "sin(x)",
                mapOf("x", Math.PI / 6.0),
                0.5,
                "con x=pi/6 -> sin(pi/6) = 0.5"
        );

        // Caso 1.7: cos(x + y)
        assertValidExpression(
                "PDF-07",
                "cos(x + y)",
                mapOf("x", Math.PI / 3.0, "y", Math.PI / 6.0),
                Math.cos(Math.PI / 2.0),
                "con x=pi/3, y=pi/6 -> cos(pi/2) = 0.0"
        );

        // Caso 1.8: tan(x^2 + sin(x))
        assertValidExpression(
                "PDF-08",
                "tan(x^2 + sin(x))",
                mapOf("x", 0.5),
                Math.tan(Math.pow(0.5, 2.0) + Math.sin(0.5)),
                "con x=0.5 -> tan(0.25 + sin(0.5)) = tan(0.7294255386) = 0.8938945921"
        );
    }

    /**
     * 2. Casos de prueba de operadores (+, -, *, /, ^), negación unaria, paréntesis,
     * precedencia, asociatividad y variables múltiples (x, y, z).
     */
    private static void runArithmeticAndPrecedenceTests() {
        OUT.println("\n[SECCION 2] Precedencia, asociatividad, negacion unaria y parentesis:");
        OUT.println("--------------------------------------------------------------------------------------");

        // Precedencia estándar y asociatividad por la izquierda de - y /
        assertValidExpression(
                "ARIT-01",
                "10 - 4 - 2",
                Collections.emptyMap(),
                4.0,
                "Asociatividad izquierda de resta: (10 - 4) - 2 = 4.0 (no 8.0)"
        );

        assertValidExpression(
                "ARIT-02",
                "24 / 6 / 2",
                Collections.emptyMap(),
                2.0,
                "Asociatividad izquierda de division: (24 / 6) / 2 = 2.0 (no 8.0)"
        );

        // Asociatividad por la derecha de la potencia ^
        assertValidExpression(
                "ARIT-03",
                "2 ^ 3 ^ 2",
                Collections.emptyMap(),
                512.0,
                "Asociatividad derecha de potencia: 2^(3^2) = 2^9 = 512.0 (no 64.0)"
        );

        // Negación unaria vs potencia
        assertValidExpression(
                "ARIT-04",
                "-2^2",
                Collections.emptyMap(),
                -4.0,
                "Precedencia de potencia sobre negacion unaria: -(2^2) = -4.0"
        );

        assertValidExpression(
                "ARIT-05",
                "(-2)^2",
                Collections.emptyMap(),
                4.0,
                "Base negativa agrupada con parentesis: (-2)^2 = 4.0"
        );

        // Exponente negativo con negación unaria
        assertValidExpression(
                "ARIT-06",
                "2^-3",
                Collections.emptyMap(),
                0.125,
                "Potencia con exponente negativo unario: 2^(-3) = 1/8 = 0.125"
        );

        // Negación unaria múltiple y combinada con funciones y paréntesis
        assertValidExpression(
                "ARIT-07",
                "-sin(-x) + -(cos(y) * -z)",
                mapOf("x", Math.PI / 2.0, "y", 0.0, "z", 3.0),
                -Math.sin(-Math.PI / 2.0) + -(Math.cos(0.0) * -3.0),
                "con x=pi/2, y=0, z=3 -> -(-1) + -(1 * -3) = 1 + 3 = 4.0"
        );

        // Tres variables simbólicas x, y, z con paréntesis anidados
        assertValidExpression(
                "ARIT-08",
                "((x + y) * z - 2) / (1 + tan(0))",
                mapOf("x", 2.5, "y", 1.5, "z", 4.0),
                ((2.5 + 1.5) * 4.0 - 2.0) / (1.0 + Math.tan(0.0)),
                "con x=2.5, y=1.5, z=4 -> ((4)*4 - 2) / 1 = 14.0"
        );
    }

    /**
     * 3. Casos de prueba de errores léxicos (Exit Code 1), sintácticos (Exit Code 2)
     * y matemáticos (Exit Code 3).
     */
    private static void runErrorClassificationTests() {
        OUT.println("\n[SECCION 3] Deteccion de errores lexicos (1), sintacticos (2) y matematicos (3):");
        OUT.println("--------------------------------------------------------------------------------------");

        // --- ERRORES LÉXICOS (Código de salida = 1) ---
        assertErrorExpression(
                "ERR-LEX-01",
                "sin(x) + 3.14.15",
                mapOf("x", 1.0),
                ExitCodes.LEXICAL_ERROR,
                "Numero real mal formado con multiples puntos decimales"
        );

        assertErrorExpression(
                "ERR-LEX-02",
                "cos(x) @ 2",
                mapOf("x", 0.0),
                ExitCodes.LEXICAL_ERROR,
                "Caracter ilegal '@' no perteneciente al alfabeto"
        );

        assertErrorExpression(
                "ERR-LEX-03",
                "cot(x) + 1",
                mapOf("x", 0.5),
                ExitCodes.LEXICAL_ERROR,
                "Funcion desconocida 'cot' (solo sin/sen, cos, tan son validas)"
        );

        assertErrorExpression(
                "ERR-LEX-04",
                "2 ** 3",
                Collections.emptyMap(),
                ExitCodes.LEXICAL_ERROR,
                "Operador '**' no valido en la gramatica (se debe usar '^')"
        );

        // --- ERRORES SINTÁCTICOS (Código de salida = 2) ---
        assertErrorExpression(
                "ERR-SIN-01",
                "sin(x + 2",
                mapOf("x", 0.0),
                ExitCodes.SYNTAX_ERROR,
                "Parentesis de cierre ')' faltante"
        );

        assertErrorExpression(
                "ERR-SIN-02",
                "cos(x)) + 1",
                mapOf("x", 0.0),
                ExitCodes.SYNTAX_ERROR,
                "Parentesis de cierre ')' sobrante sin apertura correspondiente"
        );

        assertErrorExpression(
                "ERR-SIN-03",
                "5 * / 2",
                Collections.emptyMap(),
                ExitCodes.SYNTAX_ERROR,
                "Dos operadores binarios consecutivos ('* /') sin operando intermedio"
        );

        assertErrorExpression(
                "ERR-SIN-04",
                "sin() + 1",
                Collections.emptyMap(),
                ExitCodes.SYNTAX_ERROR,
                "Funcion trigonometrica 'sin()' invocada sin argumento"
        );

        assertErrorExpression(
                "ERR-SIN-05",
                "sin x + 1",
                mapOf("x", 0.0),
                ExitCodes.SYNTAX_ERROR,
                "Funcion trigonometrica sin parentesis obligatorios '(' E ')'"
        );

        assertErrorExpression(
                "ERR-SIN-06",
                "3 + 4 *",
                Collections.emptyMap(),
                ExitCodes.SYNTAX_ERROR,
                "Expresion incompleta que termina en un operador binario '*'"
        );

        // --- ERRORES MATEMÁTICOS / SEMÁNTICOS (Código de salida = 3) ---
        assertErrorExpression(
                "ERR-MAT-01",
                "sen(x) / (cos(0) - 1)",
                mapOf("x", 1.0),
                ExitCodes.MATH_ERROR,
                "Division por cero explicita o resultante de subexpresion (cos(0) - 1 = 0)"
        );

        assertErrorExpression(
                "ERR-MAT-02",
                "tan(x)",
                mapOf("x", Math.PI / 2.0),
                ExitCodes.MATH_ERROR,
                "Tangente indefinida en x = pi/2 (cos(pi/2) = 0, asintota vertical)"
        );

        assertErrorExpression(
                "ERR-MAT-03",
                "(-4) ^ 0.5",
                Collections.emptyMap(),
                ExitCodes.MATH_ERROR,
                "Raiz cuadrada de numero negativo en el dominio de los numeros reales"
        );

        assertErrorExpression(
                "ERR-MAT-04",
                "0 ^ 0",
                Collections.emptyMap(),
                ExitCodes.MATH_ERROR,
                "Indeterminacion matematica 0^0"
        );

        assertErrorExpression(
                "ERR-MAT-05",
                "0 ^ -2",
                Collections.emptyMap(),
                ExitCodes.MATH_ERROR,
                "Cero elevado a exponente negativo (1 / 0^2 -> division por cero)"
        );
    }

    private static void assertValidExpression(
            String id,
            String expression,
            Map<String, Double> vars,
            double expectedValue,
            String mathValidationNote) {
        totalTests++;
        try {
            Lexer lexer = new Lexer(expression);
            List<Token> tokens = lexer.tokenize();
            RecursiveDescentParser parser = new RecursiveDescentParser(tokens, vars);
            double actualValue = parser.parseAndEvaluate();

            double diff = Math.abs(actualValue - expectedValue);
            if (diff <= TOLERANCE) {
                passedTests++;
                OUT.printf(Locale.US, "  [OK]   %-10s | Expr: %-34s | Obtenido: %-14s | Esperado: %-14s%n",
                        id, truncate(expression, 34),
                        CliPrinter.formatDouble(actualValue),
                        CliPrinter.formatDouble(expectedValue));
                OUT.printf(Locale.US, "                    | Validacion: %s%n", mathValidationNote);
            } else {
                failedTests++;
                OUT.printf(Locale.US, "  [FAIL] %-10s | Expr: %s | Obtenido: %.12f != Esperado: %.12f%n",
                        id, expression, actualValue, expectedValue);
            }
        } catch (EvaluatorException ex) {
            failedTests++;
            OUT.printf(Locale.US, "  [FAIL] %-10s | Expr: %s | Lanza excepcion inesperada (%s - code %d): %s%n",
                    id, expression, ex.getErrorCategory(), ex.getExitCode(), ex.getMessage());
        }
    }

    private static void assertErrorExpression(
            String id,
            String expression,
            Map<String, Double> vars,
            int expectedExitCode,
            String description) {
        totalTests++;
        try {
            Lexer lexer = new Lexer(expression);
            List<Token> tokens = lexer.tokenize();
            RecursiveDescentParser parser = new RecursiveDescentParser(tokens, vars);
            double val = parser.parseAndEvaluate();

            failedTests++;
            OUT.printf(Locale.US, "  [FAIL] %-10s | Expr: %-28s | Se esperaba error (code %d), pero evaluo a: %s%n",
                    id, expression, expectedExitCode, CliPrinter.formatDouble(val));
        } catch (EvaluatorException ex) {
            if (ex.getExitCode() == expectedExitCode) {
                passedTests++;
                OUT.printf(Locale.US, "  [OK]   %-10s | Expr: %-28s | Detectado %-16s (Exit Code = %d)%n",
                        id, truncate(expression, 28), ex.getErrorCategory(), ex.getExitCode());
                OUT.printf(Locale.US, "                    | Motivo: %s -> \"%s\"%n", description, ex.getMessage());
            } else {
                failedTests++;
                OUT.printf(Locale.US, "  [FAIL] %-10s | Expr: %-28s | Exit Code obtenido %d != esperado %d (%s)%n",
                        id, expression, ex.getExitCode(), expectedExitCode, ex.getMessage());
            }
        }
    }

    private static Map<String, Double> mapOf(Object... keyValues) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], ((Number) keyValues[i + 1]).doubleValue());
        }
        return map;
    }

    private static String truncate(String s, int maxLen) {
        int commentIdx = s.indexOf("//");
        String clean = (commentIdx > 0) ? s.substring(0, commentIdx).trim() : s;
        if (clean.length() <= maxLen) {
            return clean;
        }
        return clean.substring(0, maxLen - 3) + "...";
    }
}
