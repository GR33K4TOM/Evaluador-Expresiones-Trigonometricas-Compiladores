package trigono;

/**
 * Códigos de salida (exit codes) distintos para clasificar el resultado de la
 * ejecución y los errores léxicos, sintácticos y matemáticos.
 */
public final class ExitCodes {
    /** Ejecución exitosa sin errores */
    public static final int SUCCESS = 0;

    /** Error léxico: carácter no reconocido, número mal formado o función desconocida */
    public static final int LEXICAL_ERROR = 1;

    /** Error sintáctico: estructura que viola la gramática LL(1) */
    public static final int SYNTAX_ERROR = 2;

    /** Error matemático/semántico: división por cero, indeterminación, dominio inválido */
    public static final int MATH_ERROR = 3;

    /** Error de uso o entrada de usuario inválida */
    public static final int INPUT_ERROR = 4;

    private ExitCodes() {
        // Clase de constantes
    }
}
