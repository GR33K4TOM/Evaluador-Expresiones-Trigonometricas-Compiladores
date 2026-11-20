package trigono;

/**
 * Excepción lanzada cuando el analizador léxico encuentra un carácter inválido,
 * un número real mal formado o un identificador no permitido.
 * Código de salida asociado: {@link ExitCodes#LEXICAL_ERROR} (1).
 */
public class LexicalException extends EvaluatorException {
    public LexicalException(String message, int position) {
        super(message, ExitCodes.LEXICAL_ERROR, position);
    }

    @Override
    public String getErrorCategory() {
        return "ERROR LÉXICO";
    }
}
