package trigono;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Punto de entrada principal del Reconocedor Descendente LL(1) y Evaluador
 * para Expresiones Trigonométricas.
 *
 * <p>Permite el uso tanto interactivo como por argumentos de línea de comandos,
 * detectando automáticamente cada variable simbólica presente en la expresión,
 * solicitando al usuario su valor numérico (si no fue provisto por CLI) y
 * sustituyéndolo durante la evaluación semántica directa.</p>
 */
public class Main {

    public static void main(String[] args) {
        CliPrinter cli = new CliPrinter();

        if (args.length > 0) {
            int exitCode = handleCommandLineArgs(args, cli);
            System.exit(exitCode);
        } else {
            int exitCode = runInteractiveLoop(cli);
            System.exit(exitCode);
        }
    }

    /**
     * Procesa la invocación por argumentos de línea de comandos y retorna el
     * código de salida correspondiente ({@link ExitCodes}).
     */
    public static int handleCommandLineArgs(String[] args, CliPrinter cli) {
        String expression = null;
        Map<String, Double> presetVariables = new LinkedHashMap<>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--help":
                case "-h":
                    cli.printHelp();
                    return ExitCodes.SUCCESS;

                case "--grammar":
                case "-g":
                    cli.printGrammarInfo();
                    return ExitCodes.SUCCESS;

                case "--eval":
                case "-e":
                    if (i + 1 >= args.length) {
                        cli.printlnErr("Error de uso: La opcion '--eval' requiere una expresion como argumento.");
                        return ExitCodes.INPUT_ERROR;
                    }
                    expression = args[++i];
                    break;

                case "--var":
                case "-v":
                    if (i + 1 >= args.length) {
                        cli.printlnErr("Error de uso: La opcion '--var' requiere un par nombre=valor (ej. x=1.57).");
                        return ExitCodes.INPUT_ERROR;
                    }
                    String pair = args[++i];
                    int eqIdx = pair.indexOf('=');
                    if (eqIdx <= 0 || eqIdx == pair.length() - 1) {
                        cli.printlnErr("Error de uso: Formato invalido en '--var " + pair + "'. Use nombre=valor.");
                        return ExitCodes.INPUT_ERROR;
                    }
                    String varName = pair.substring(0, eqIdx).trim();
                    String valStr = pair.substring(eqIdx + 1).trim();
                    try {
                        double val = parseUserNumericValue(valStr);
                        presetVariables.put(varName, val);
                    } catch (NumberFormatException ex) {
                        cli.printlnErr("Error de entrada: Valor numerico invalido para la variable '" + varName + "': " + valStr);
                        return ExitCodes.INPUT_ERROR;
                    }
                    break;

                default:
                    if (expression == null && !arg.startsWith("-")) {
                        expression = arg;
                    } else {
                        cli.printlnErr("Opcion desconocida: " + arg + ". Use --help para ver las opciones.");
                        return ExitCodes.INPUT_ERROR;
                    }
                    break;
            }
        }

        if (expression == null) {
            cli.printHelp();
            return ExitCodes.INPUT_ERROR;
        }

        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            return evaluateSingleExpression(expression, presetVariables, scanner, cli);
        }
    }

    /**
     * Ejecuta el bucle interactivo en consola (CLI portable Windows/Linux).
     */
    private static int runInteractiveLoop(CliPrinter cli) {
        cli.printBanner();
        cli.println("Ingrese una expresion trigonometrica/aritmetica para evaluar.");
        cli.println("Comandos especiales: 'gramatica' (ver LL(1)), 'ayuda', 'salir'.");
        cli.println("");

        int lastExitCode = ExitCodes.SUCCESS;

        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            while (true) {
                cli.printPrompt("trigono> ");
                if (!scanner.hasNextLine()) {
                    cli.println("");
                    break;
                }

                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }

                if (line.equalsIgnoreCase("salir") || line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                    cli.println("Saliendo del evaluador. ¡Hasta pronto!");
                    break;
                }

                if (line.equalsIgnoreCase("gramatica") || line.equalsIgnoreCase("grammar")) {
                    cli.printGrammarInfo();
                    continue;
                }

                if (line.equalsIgnoreCase("ayuda") || line.equalsIgnoreCase("help")) {
                    cli.printHelp();
                    continue;
                }

                lastExitCode = evaluateSingleExpression(line, new LinkedHashMap<>(), scanner, cli);
                cli.println("");
            }
        }

        return lastExitCode;
    }

    /**
     * Tokeniza, solicita las variables simbólicas faltantes, analiza sintácticamente
     * con el parser descendente recursivo LL(1) y evalúa semánticamente la expresión.
     */
    public static int evaluateSingleExpression(
            String expression,
            Map<String, Double> presetVariables,
            Scanner scanner,
            CliPrinter cli) {
        try {
            // 1. Análisis Léxico
            Lexer lexer = new Lexer(expression);
            List<Token> tokens = lexer.tokenize();
            cli.printTokens(tokens);

            // 2. Detección de variables simbólicas en la expresión
            Set<String> detectedVars = Lexer.extractVariables(tokens);
            cli.printDetectedVariables(detectedVars);

            // 3. Primero validamos sintácticamente con valores ficticios seguros o pedimos las variables:
            //    El requerimiento pide: "Pedir al usuario el valor numérico de cada variable simbólica detectada"
            //    y "Sustituir dichos valores durante la evaluación".
            Map<String, Double> variableValues = new LinkedHashMap<>(presetVariables);
            for (String varName : detectedVars) {
                if (!variableValues.containsKey(varName)) {
                    if (scanner == null) {
                        cli.printlnErr("Error: No se proporciono valor para la variable simbolica '" + varName + "'.");
                        return ExitCodes.INPUT_ERROR;
                    }
                    Double userVal = promptVariableValue(varName, scanner, cli);
                    if (userVal == null) {
                        cli.printlnErr("Error de entrada: No se pudo leer el valor numerico para la variable '" + varName + "'.");
                        return ExitCodes.INPUT_ERROR;
                    }
                    variableValues.put(varName, userVal);
                }
            }

            if (!detectedVars.isEmpty()) {
                cli.printSubstitutedVariables(variableValues);
            }

            // 4. Análisis Sintáctico Descendente Recursivo LL(1) y Evaluación Semántica Directa
            RecursiveDescentParser parser = new RecursiveDescentParser(tokens, variableValues);
            double result = parser.parseAndEvaluate();

            cli.printEvaluationSuccess(expression, result);
            return ExitCodes.SUCCESS;

        } catch (EvaluatorException ex) {
            cli.printError(expression, ex);
            return ex.getExitCode();
        }
    }

    /**
     * Solicita interactivamente al usuario el valor numérico de una variable simbólica detectada.
     * Acepta números reales (ej. 1.57079632679, -0.5, 0) y también expresiones de apoyo
     * comunes como "pi" o "pi/2".
     */
    private static Double promptVariableValue(String varName, Scanner scanner, CliPrinter cli) {
        while (true) {
            cli.printPrompt("  -> Ingrese el valor numerico para la variable '" + varName + "' (en radianes si aplica): ");
            if (!scanner.hasNextLine()) {
                return null;
            }
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                cli.printlnErr("     Por favor ingrese un numero real valido.");
                continue;
            }
            try {
                return parseUserNumericValue(input);
            } catch (NumberFormatException ex) {
                cli.printlnErr("     Valor invalido '" + input + "'. Ingrese un numero real (ej. 1.57, 0, -2.5, pi, pi/2).");
            }
        }
    }

    /**
     * Convierte una cadena ingresada por el usuario para una variable en un {@code double}.
     * Soporta números reales estándar (con punto o coma decimal) y constantes convenientes
     * como {@code pi}, {@code -pi}, {@code pi/2}, {@code pi/4}, etc.
     */
    public static double parseUserNumericValue(String raw) {
        String cleaned = raw.trim().replace(',', '.').toLowerCase();
        if (cleaned.equals("pi")) {
            return Math.PI;
        }
        if (cleaned.equals("-pi")) {
            return -Math.PI;
        }
        if (cleaned.startsWith("pi/")) {
            double denom = Double.parseDouble(cleaned.substring(3).trim());
            if (denom == 0.0) {
                throw new NumberFormatException("Division por cero en constante pi");
            }
            return Math.PI / denom;
        }
        if (cleaned.startsWith("-pi/")) {
            double denom = Double.parseDouble(cleaned.substring(4).trim());
            if (denom == 0.0) {
                throw new NumberFormatException("Division por cero en constante -pi");
            }
            return -Math.PI / denom;
        }
        double value = Double.parseDouble(cleaned);
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new NumberFormatException("Valor no finito");
        }
        return value;
    }
}
