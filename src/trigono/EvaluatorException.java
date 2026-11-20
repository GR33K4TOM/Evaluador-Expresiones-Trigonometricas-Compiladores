package trigono;

/**
 * Excepción base para todos los errores detectados durante el análisis
 * léxico, sintáctico o la evaluación matemática.
 */
public abstract class EvaluatorException extends Exception {
    private final int exitCode;
    private final int position;

    protected EvaluatorException(String message, int exitCode, int position) {
        super(message);
        this.exitCode = exitCode;
        this.position = position;
    }

    public int getExitCode() {
        return exitCode;
    }

    public int getPosition() {
        return position;
    }

    public abstract String getErrorCategory();
}
