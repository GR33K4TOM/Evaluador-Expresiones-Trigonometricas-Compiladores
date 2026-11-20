package trigono;

/**
 * Representa un token individual extraído por el analizador léxico.
 * Incluye el tipo de token, el lexema original, el valor numérico (si aplica)
 * y la posición (columna 1-based) en la expresión original.
 */
public class Token {
    private final TokenType type;
    private final String lexeme;
    private final double numericValue;
    private final int position;

    public Token(TokenType type, String lexeme, int position) {
        this(type, lexeme, 0.0, position);
    }

    public Token(TokenType type, String lexeme, double numericValue, int position) {
        this.type = type;
        this.lexeme = lexeme;
        this.numericValue = numericValue;
        this.position = position;
    }

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public double getNumericValue() {
        return numericValue;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        if (type == TokenType.NUMBER) {
            return String.format("[%s: '%s' (%.4f) @pos=%d]", type.name(), lexeme, numericValue, position);
        }
        return String.format("[%s: '%s' @pos=%d]", type.name(), lexeme, position);
    }
}
