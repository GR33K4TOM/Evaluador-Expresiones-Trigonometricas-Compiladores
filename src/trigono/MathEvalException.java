package trigono;

/**
 * Excepción lanzada durante la evaluación semántica cuando ocurre un error
 * matemático (ej. división por cero, tangente indefinida en múltiplos impares
 * de pi/2, base negativa elevada a exponente no entero, o desbordamiento).
 * Código de salida asociado: {@link ExitCodes#MATH_ERROR} (3).
 */
public class MathEvalException extends EvaluatorException {
    public MathEvalException(String message, int position) {
        super(message, ExitCodes.MATH_ERROR, position);
    }

    @Override
    public String getErrorCategory() {
        return "ERROR MATEMÁTICO";
    }
}
