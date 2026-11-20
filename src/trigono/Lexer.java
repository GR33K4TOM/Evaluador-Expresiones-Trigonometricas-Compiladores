package trigono;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Analizador Léxico (Lexer) propio construido desde cero.
 * Convierte una cadena de entrada en una secuencia de {@link Token}:
 * - Funciones trigonométricas: sin (o sen), cos, tan
 * - Operadores aritméticos: +, -, *, /, ^
 * - Paréntesis: (, )
 * - Constantes numéricas reales: enteros y decimales (ej. 3.14, 2, 0.5)
 * - Variables simbólicas: letras individuales (ej. x, y, z)
 */
public class Lexer {
    private final String input;
    private final int length;
    private int pos;

    public Lexer(String input) {
        this.input = input != null ? input : "";
        this.length = this.input.length();
        this.pos = 0;
    }

    /**
     * Ejecuta el análisis léxico completo sobre la cadena de entrada.
     *
     * @return Lista de tokens terminada siempre con un token {@link TokenType#EOF}.
     * @throws LexicalException Si se encuentra algún carácter o lexema inválido.
     */
    public List<Token> tokenize() throws LexicalException {
        List<Token> tokens = new ArrayList<>();
        pos = 0;

        while (pos < length) {
            char current = input.charAt(pos);

            // Ignorar espacios en blanco, tabulaciones y saltos de línea
            if (Character.isWhitespace(current)) {
                pos++;
                continue;
            }

            // Soportar comentarios de línea opcionales (como en el PDF: // comentario)
            if (current == '/' && (pos + 1 < length) && input.charAt(pos + 1) == '/') {
                break;
            }

            int tokenPos = pos + 1; // Columna 1-based para reportes claros

            switch (current) {
                case '+':
                    tokens.add(new Token(TokenType.PLUS, "+", tokenPos));
                    pos++;
                    break;
                case '-':
                    tokens.add(new Token(TokenType.MINUS, "-", tokenPos));
                    pos++;
                    break;
                case '*':
                    if (pos + 1 < length && input.charAt(pos + 1) == '*') {
                        throw new LexicalException(
                                "Operador '**' no válido en la gramática; utilice '^' para potencia en posición " + tokenPos,
                                tokenPos);
                    }
                    tokens.add(new Token(TokenType.MUL, "*", tokenPos));
                    pos++;
                    break;
                case '/':
                    tokens.add(new Token(TokenType.DIV, "/", tokenPos));
                    pos++;
                    break;
                case '^':
                    tokens.add(new Token(TokenType.POW, "^", tokenPos));
                    pos++;
                    break;
                case '(':
                    tokens.add(new Token(TokenType.LPAREN, "(", tokenPos));
                    pos++;
                    break;
                case ')':
                    tokens.add(new Token(TokenType.RPAREN, ")", tokenPos));
                    pos++;
                    break;
                default:
                    if (Character.isDigit(current) || current == '.') {
                        tokens.add(readNumber(tokenPos));
                    } else if (isAsciiLetter(current)) {
                        tokens.add(readIdentifierOrFunction(tokenPos));
                    } else {
                        throw new LexicalException(
                                String.format("Carácter ilegal '%c' en la posición %d", current, tokenPos),
                                tokenPos);
                    }
                    break;
            }
        }

        tokens.add(new Token(TokenType.EOF, "EOF", length + 1));
        return tokens;
    }

    /**
     * Extrae en orden de aparición todas las variables simbólicas únicas detectadas
     * en una lista de tokens.
     */
    public static Set<String> extractVariables(List<Token> tokens) {
        Set<String> variables = new LinkedHashSet<>();
        for (Token token : tokens) {
            if (token.getType() == TokenType.VARIABLE) {
                variables.add(token.getLexeme());
            }
        }
        return variables;
    }

    /**
     * Reconoce una constante numérica real (ej. 3.14, 0, 25, 0.75).
     */
    private Token readNumber(int startPos) throws LexicalException {
        int start = pos;
        boolean hasDot = false;
        int digitsBeforeDot = 0;
        int digitsAfterDot = 0;

        while (pos < length) {
            char c = input.charAt(pos);
            if (Character.isDigit(c)) {
                if (!hasDot) {
                    digitsBeforeDot++;
                } else {
                    digitsAfterDot++;
                }
                pos++;
            } else if (c == '.') {
                if (hasDot) {
                    throw new LexicalException(
                            String.format("Número real mal formado con múltiples puntos decimales cerca de la posición %d",
                                    pos + 1),
                            startPos);
                }
                hasDot = true;
                pos++;
            } else {
                break;
            }
        }

        String lexeme = input.substring(start, pos);

        // Validar que no sea solo un punto "." ni termine en punto "3." ni empiece en ".5" sin dígito entero
        if (digitsBeforeDot == 0 || (hasDot && digitsAfterDot == 0)) {
            throw new LexicalException(
                    String.format("Constante numérica real mal formada '%s' en la posición %d", lexeme, startPos),
                    startPos);
        }

        try {
            double value = Double.parseDouble(lexeme);
            if (Double.isInfinite(value) || Double.isNaN(value)) {
                throw new LexicalException(
                        String.format("Constante numérica fuera del rango double '%s' en la posición %d", lexeme, startPos),
                        startPos);
            }
            return new Token(TokenType.NUMBER, lexeme, value, startPos);
        } catch (NumberFormatException e) {
            throw new LexicalException(
                    String.format("Formato numérico inválido '%s' en la posición %d", lexeme, startPos),
                    startPos);
        }
    }

    /**
     * Reconoce funciones trigonométricas (sin, sen, cos, tan), la constante simbólica
     * especial o variables simbólicas (x, y, z, etc.).
     */
    private Token readIdentifierOrFunction(int startPos) throws LexicalException {
        int start = pos;
        while (pos < length && (isAsciiLetter(input.charAt(pos)) || Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '_')) {
            pos++;
        }

        String lexeme = input.substring(start, pos);
        String lower = lexeme.toLowerCase();

        switch (lower) {
            case "sin":
            case "sen":
                return new Token(TokenType.SIN, lexeme, startPos);
            case "cos":
                return new Token(TokenType.COS, lexeme, startPos);
            case "tan":
                return new Token(TokenType.TAN, lexeme, startPos);
            default:
                // Variables simbólicas: se aceptan identificadores de una sola letra (ej. x, y, z, a, b...)
                if (lexeme.length() == 1 && isAsciiLetter(lexeme.charAt(0))) {
                    return new Token(TokenType.VARIABLE, lexeme, startPos);
                }
                throw new LexicalException(
                        String.format(
                                "Identificador o función no reconocida '%s' en la posición %d (funciones válidas: sin, sen, cos, tan; variables válidas: una letra como x, y, z)",
                                lexeme, startPos),
                        startPos);
        }
    }

    private boolean isAsciiLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }
}
