package trigono;

/**
 * Tipos de tokens reconocidos por el analizador léxico para expresiones
 * aritméticas y trigonométricas.
 */
public enum TokenType {
    // Constantes numéricas reales
    NUMBER("NÚMERO"),

    // Variables simbólicas (x, y, z o identificadores simbólicos)
    VARIABLE("VARIABLE"),

    // Funciones trigonométricas
    SIN("FUNCIÓN(sin/sen)"),
    COS("FUNCIÓN(cos)"),
    TAN("FUNCIÓN(tan)"),

    // Operadores aritméticos
    PLUS("OPERADOR(+)"),
    MINUS("OPERADOR(-)"),
    MUL("OPERADOR(*)"),
    DIV("OPERADOR(/)"),
    POW("OPERADOR(^)"),

    // Agrupación mediante paréntesis
    LPAREN("PARÉNTESIS_IZQ(()"),
    RPAREN("PARÉNTESIS_DER())"),

    // Fin de la entrada
    EOF("FIN_DE_ENTRADA");

    private final String description;

    TokenType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
