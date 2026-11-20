package trigono;

/**
 * Excepción lanzada cuando la secuencia de tokens no cumple con las reglas
 * de producción de la gramática LL(1).
 * Código de salida asociado: {@link ExitCodes#SYNTAX_ERROR} (2).
 */
public class SyntaxException extends EvaluatorException {
    public SyntaxException(String message, int position) {
        super(message, ExitCodes.SYNTAX_ERROR, position);
    }

    @Override
    public String getErrorCategory() {
        return "ERROR SINTÁCTICO";
    }
}
