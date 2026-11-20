package trigono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Reconocedor Descendente Recursivo y Evaluador Semántico Directo basado en una
 * Gramática LL(1) libre de recursividad por la izquierda.
 *
 * <pre>
 * Gramática LL(1) diseñada:
 *   1. E  -> T E'
 *   2. E' -> '+' T E'
 *   3. E' -> '-' T E'
 *   4. E' -> ε
 *   5. T  -> U T'
 *   6. T' -> '*' U T'
 *   7. T' -> '/' U T'
 *   8. T' -> ε
 *   9. U  -> '-' U
 *  10. U  -> '+' U
 *  11. U  -> P
 *  12. P  -> F P'
 *  13. P' -> '^' U
 *  14. P' -> ε
 *  15. F  -> '(' E ')'
 *  16. F  -> FUNC '(' E ')'
 *  17. F  -> NUMBER
 *  18. F  -> VARIABLE
 *  19. FUNC -> 'sin' | 'cos' | 'tan'
 * </pre>
 *
 * Jerarquía de precedencia y asociatividad:
 * <ol>
 *   <li>Paréntesis {@code (E)}, funciones {@code sin(E)}, {@code cos(E)}, {@code tan(E)}, constantes y variables</li>
 *   <li>Potencia {@code ^} (asociativa por la derecha, ej. {@code 2^3^2 = 2^(3^2) = 512})</li>
 *   <li>Signos unarios / negación {@code -x}, {@code +x} (mayor precedencia que {@code * / + -}, menor que {@code ^}: {@code -2^2 = -(2^2) = -4} y {@code 2^-2 = 0.25})</li>
 *   <li>Multiplicación {@code *} y división {@code /} (asociativas por la izquierda)</li>
 *   <li>Suma {@code +} y resta {@code -} (asociativas por la izquierda)</li>
 * </ol>
 */
public class RecursiveDescentParser {
    /** Umbral para detectar indeterminación en tan(x) cuando cos(x) tiende a 0 */
    private static final double COS_ZERO_EPSILON = 1e-15;

    private final List<Token> tokens;
    private final Map<String, Double> variableValues;
    private int currentIndex;

    public RecursiveDescentParser(List<Token> tokens, Map<String, Double> variableValues) {
        this.tokens = tokens;
        this.variableValues = variableValues != null ? variableValues : Collections.emptyMap();
        this.currentIndex = 0;
    }

    /**
     * Inicia el análisis sintáctico descendente recursivo desde el símbolo inicial E
     * y evalúa simultáneamente el resultado numérico con precisión {@code double}.
     *
     * @return Valor numérico resultante de evaluar la expresión.
     * @throws SyntaxException   Si la entrada no pertenece al lenguaje generado por la gramática LL(1).
     * @throws MathEvalException Si ocurre un error matemático durante la evaluación semántica.
     */
    public double parseAndEvaluate() throws SyntaxException, MathEvalException {
        this.currentIndex = 0;

        if (lookahead().getType() == TokenType.EOF) {
            throw new SyntaxException("La expresión está vacía; se esperaba una expresión válida.", 1);
        }

        double result = parseE();

        Token endToken = lookahead();
        if (endToken.getType() != TokenType.EOF) {
            throw new SyntaxException(
                    String.format("Token inesperado '%s' (%s) en la posición %d; se esperaba el fin de la expresión o un operador.",
                            endToken.getLexeme(), endToken.getType().getDescription(), endToken.getPosition()),
                    endToken.getPosition());
        }

        return normalizeNegativeZero(result);
    }

    /**
     * Regla 1: E -> T E'
     * FIRST(E) = { '-', '+', '(', 'sin', 'cos', 'tan', NUMBER, VARIABLE }
     */
    private double parseE() throws SyntaxException, MathEvalException {
        double leftValue = parseT();
        return parseEPrime(leftValue);
    }

    /**
     * Reglas 2-4:
     * E'(inh) -> '+' T { val = inh + T.val } E'(val)
     *          | '-' T { val = inh - T.val } E'(val)
     *          | ε     { syn = inh }
     *
     * FIRST(E') = { '+', '-', ε }
     * FOLLOW(E') = { ')', EOF }
     */
    private double parseEPrime(double inheritedValue) throws SyntaxException, MathEvalException {
        Token token = lookahead();
        TokenType type = token.getType();

        if (type == TokenType.PLUS) {
            consume(TokenType.PLUS);
            double termValue = parseT();
            double sum = checkFinite(inheritedValue + termValue, "Desbordamiento numérico en suma", token.getPosition());
            return parseEPrime(sum);
        } else if (type == TokenType.MINUS) {
            consume(TokenType.MINUS);
            double termValue = parseT();
            double diff = checkFinite(inheritedValue - termValue, "Desbordamiento numérico en resta", token.getPosition());
            return parseEPrime(diff);
        } else if (type == TokenType.RPAREN || type == TokenType.EOF) {
            // Producción ε (transición válida según FOLLOW(E') = { ')', EOF })
            return inheritedValue;
        } else {
            throw new SyntaxException(
                    String.format("Error sintáctico en posición %d: se encontró '%s' (%s), pero se esperaba un operador (+, -, *, /, ^) o ')'.",
                            token.getPosition(), token.getLexeme(), type.getDescription()),
                    token.getPosition());
        }
    }

    /**
     * Regla 5: T -> U T'
     * FIRST(T) = { '-', '+', '(', 'sin', 'cos', 'tan', NUMBER, VARIABLE }
     */
    private double parseT() throws SyntaxException, MathEvalException {
        double leftValue = parseU();
        return parseTPrime(leftValue);
    }

    /**
     * Reglas 6-8:
     * T'(inh) -> '*' U { val = inh * U.val } T'(val)
     *          | '/' U { val = inh / U.val } T'(val)
     *          | ε     { syn = inh }
     *
     * FIRST(T') = { '*', '/', ε }
     * FOLLOW(T') = { '+', '-', ')', EOF }
     */
    private double parseTPrime(double inheritedValue) throws SyntaxException, MathEvalException {
        Token token = lookahead();
        TokenType type = token.getType();

        if (type == TokenType.MUL) {
            consume(TokenType.MUL);
            double unaryValue = parseU();
            double product = checkFinite(inheritedValue * unaryValue, "Desbordamiento numérico en multiplicación", token.getPosition());
            return parseTPrime(product);
        } else if (type == TokenType.DIV) {
            consume(TokenType.DIV);
            int divPos = token.getPosition();
            double divisor = parseU();
            if (divisor == 0.0) {
                throw new MathEvalException(
                        String.format("División por cero detectada en la posición %d (dividendo = %s, divisor = 0).",
                                divPos, formatNumber(inheritedValue)),
                        divPos);
            }
            double quotient = checkFinite(inheritedValue / divisor, "Desbordamiento numérico en división", divPos);
            return parseTPrime(quotient);
        } else if (type == TokenType.PLUS || type == TokenType.MINUS
                || type == TokenType.RPAREN || type == TokenType.EOF) {
            // Producción ε (transición válida según FOLLOW(T') = { '+', '-', ')', EOF })
            return inheritedValue;
        } else {
            throw new SyntaxException(
                    String.format("Error sintáctico en posición %d: token inesperado '%s' (%s); falta un operador entre operandos.",
                            token.getPosition(), token.getLexeme(), type.getDescription()),
                    token.getPosition());
        }
    }

    /**
     * Reglas 9-11:
     * U -> '-' U { U.val = -U1.val }
     *    | '+' U { U.val = +U1.val }
     *    | P     { U.val = P.val }
     *
     * FIRST(U) = { '-', '+', '(', 'sin', 'cos', 'tan', NUMBER, VARIABLE }
     */
    private double parseU() throws SyntaxException, MathEvalException {
        Token token = lookahead();
        TokenType type = token.getType();

        if (type == TokenType.MINUS) {
            consume(TokenType.MINUS);
            double operand = parseU();
            return -operand;
        } else if (type == TokenType.PLUS) {
            consume(TokenType.PLUS);
            return parseU();
        } else {
            return parseP();
        }
    }

    /**
     * Regla 12: P -> F P'
     * FIRST(P) = { '(', 'sin', 'cos', 'tan', NUMBER, VARIABLE }
     */
    private double parseP() throws SyntaxException, MathEvalException {
        double baseValue = parseF();
        return parsePPrime(baseValue);
    }

    /**
     * Reglas 13-14:
     * P'(base) -> '^' U { syn = base ^ U.val }
     *           | ε     { syn = base }
     *
     * FIRST(P') = { '^', ε }
     * FOLLOW(P') = { '*', '/', '+', '-', ')', EOF }
     */
    private double parsePPrime(double baseValue) throws SyntaxException, MathEvalException {
        Token token = lookahead();
        TokenType type = token.getType();

        if (type == TokenType.POW) {
            consume(TokenType.POW);
            int powPos = token.getPosition();
            double exponent = parseU();

            if (baseValue == 0.0 && exponent == 0.0) {
                throw new MathEvalException(
                        String.format("Indeterminación matemática 0^0 en la posición %d.", powPos),
                        powPos);
            }
            if (baseValue == 0.0 && exponent < 0.0) {
                throw new MathEvalException(
                        String.format("División por cero implícita en potencia (0 elevado a exponente negativo %s) en la posición %d.",
                                formatNumber(exponent), powPos),
                        powPos);
            }
            if (baseValue < 0.0 && exponent != Math.floor(exponent)) {
                throw new MathEvalException(
                        String.format("Dominio real inválido en potencia: base negativa (%s) elevada a exponente no entero (%s) en la posición %d.",
                                formatNumber(baseValue), formatNumber(exponent), powPos),
                        powPos);
            }

            double result = Math.pow(baseValue, exponent);
            return checkFinite(result, "Resultado no finito o fuera de rango en operación de potencia (^)", powPos);
        } else if (type == TokenType.MUL || type == TokenType.DIV
                || type == TokenType.PLUS || type == TokenType.MINUS
                || type == TokenType.RPAREN || type == TokenType.EOF) {
            // Producción ε según FOLLOW(P')
            return baseValue;
        } else {
            throw new SyntaxException(
                    String.format("Error sintáctico en posición %d: se encontró '%s' (%s) después de un factor, pero se esperaba un operador o ')'.",
                            token.getPosition(), token.getLexeme(), type.getDescription()),
                    token.getPosition());
        }
    }

    /**
     * Reglas 15-19:
     * F -> '(' E ')'
     *    | FUNC '(' E ')'
     *    | NUMBER
     *    | VARIABLE
     * FUNC -> 'sin' | 'cos' | 'tan'
     *
     * FIRST(F) = { '(', 'sin', 'cos', 'tan', NUMBER, VARIABLE }
     */
    private double parseF() throws SyntaxException, MathEvalException {
        Token token = lookahead();
        TokenType type = token.getType();

        switch (type) {
            case LPAREN: {
                int openPos = token.getPosition();
                consume(TokenType.LPAREN);
                if (lookahead().getType() == TokenType.RPAREN) {
                    throw new SyntaxException(
                            String.format("Paréntesis vacíos '()' en la posición %d; se esperaba una expresión dentro de los paréntesis.",
                                    openPos),
                            openPos);
                }
                double innerValue = parseE();
                expect(TokenType.RPAREN, "Se esperaba ')' para cerrar el paréntesis abierto en la posición " + openPos);
                return innerValue;
            }

            case SIN:
            case COS:
            case TAN: {
                Token funcToken = consume(type);
                expect(TokenType.LPAREN,
                        String.format("Se esperaba '(' inmediatamente después de la función '%s' en la posición %d",
                                funcToken.getLexeme(), funcToken.getPosition()));
                if (lookahead().getType() == TokenType.RPAREN) {
                    throw new SyntaxException(
                            String.format("La función '%s' en la posición %d requiere un argumento dentro de los paréntesis.",
                                    funcToken.getLexeme(), funcToken.getPosition()),
                            funcToken.getPosition());
                }
                double argValue = parseE();
                expect(TokenType.RPAREN,
                        String.format("Se esperaba ')' para cerrar el argumento de la función '%s' iniciada en la posición %d",
                                funcToken.getLexeme(), funcToken.getPosition()));
                return evaluateTrigFunction(funcToken, argValue);
            }

            case NUMBER: {
                Token numToken = consume(TokenType.NUMBER);
                return numToken.getNumericValue();
            }

            case VARIABLE: {
                Token varToken = consume(TokenType.VARIABLE);
                String varName = varToken.getLexeme();
                if (!variableValues.containsKey(varName)) {
                    throw new MathEvalException(
                            String.format("No se proporcionó un valor numérico para la variable simbólica '%s' (posición %d).",
                                    varName, varToken.getPosition()),
                            varToken.getPosition());
                }
                Double val = variableValues.get(varName);
                if (val == null || Double.isNaN(val) || Double.isInfinite(val)) {
                    throw new MathEvalException(
                            String.format("El valor asignado a la variable '%s' no es un número real finito válido.", varName),
                            varToken.getPosition());
                }
                return val;
            }

            default:
                throw new SyntaxException(
                        String.format("Error sintáctico en posición %d: se encontró '%s' (%s), pero se esperaba un número, una variable, una función (sin, cos, tan) o '('.",
                                token.getPosition(), token.getLexeme(), type.getDescription()),
                        token.getPosition());
        }
    }

    /**
     * Evalúa semánticamente las funciones trigonométricas {@code sin}, {@code cos}, {@code tan}
     * utilizando {@link Math#sin(double)}, {@link Math#cos(double)} y {@link Math#tan(double)}
     * (con argumento en radianes, tal como especifica el documento de trabajo práctico).
     */
    private double evaluateTrigFunction(Token funcToken, double radians) throws MathEvalException {
        TokenType type = funcToken.getType();
        int pos = funcToken.getPosition();

        switch (type) {
            case SIN:
                return checkFinite(Math.sin(radians), "Error al evaluar sin(" + formatNumber(radians) + ")", pos);

            case COS:
                return checkFinite(Math.cos(radians), "Error al evaluar cos(" + formatNumber(radians) + ")", pos);

            case TAN: {
                double cosVal = Math.cos(radians);
                if (Math.abs(cosVal) < COS_ZERO_EPSILON) {
                    throw new MathEvalException(
                            String.format("Tangente indefinida en '%s(%s)' (posición %d): cos(%s) = 0 (asíntota vertical en múltiplo impar de pi/2).",
                                    funcToken.getLexeme(), formatNumber(radians), pos, formatNumber(radians)),
                            pos);
                }
                return checkFinite(Math.tan(radians), "Error al evaluar tan(" + formatNumber(radians) + ")", pos);
            }

            default:
                throw new IllegalStateException("Función trigonométrica no soportada: " + type);
        }
    }

    private Token lookahead() {
        return tokens.get(currentIndex);
    }

    private Token consume(TokenType expected) throws SyntaxException {
        Token token = lookahead();
        if (token.getType() != expected) {
            throw new SyntaxException(
                    String.format("Se esperaba %s pero se encontró '%s' en la posición %d.",
                            expected.getDescription(), token.getLexeme(), token.getPosition()),
                    token.getPosition());
        }
        currentIndex++;
        return token;
    }

    private Token expect(TokenType expected, String customErrorMessage) throws SyntaxException {
        Token token = lookahead();
        if (token.getType() != expected) {
            throw new SyntaxException(
                    String.format("%s (se encontró '%s' [%s] en la posición %d).",
                            customErrorMessage, token.getLexeme(), token.getType().getDescription(), token.getPosition()),
                    token.getPosition());
        }
        currentIndex++;
        return token;
    }

    private double checkFinite(double value, String contextMessage, int position) throws MathEvalException {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new MathEvalException(
                    String.format("%s en la posición %d (resultado: %s).", contextMessage, position, value),
                    position);
        }
        return value;
    }

    private double normalizeNegativeZero(double value) {
        return (value == 0.0) ? 0.0 : value;
    }

    private String formatNumber(double value) {
        if (value == (long) value) {
            return String.format("%d", (long) value);
        }
        return String.valueOf(value);
    }
}
