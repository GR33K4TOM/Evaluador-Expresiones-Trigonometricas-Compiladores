# Reconocedor Descendente LL(1) y Evaluador para Expresiones Trigonométricas

Implementación en **Java** (100% biblioteca estándar, sin dependencias de sistema operativo) de un analizador léxico propio (*Lexer*), un reconocedor sintáctico descendente recursivo basado en una **Gramática LL(1)** y un evaluador semántico directo para expresiones aritméticas y trigonométricas con variables simbólicas.

---

## Características Principales

- **Operadores soportados:** Suma (`+`), resta (`-`), multiplicación (`*`), división (`/`), potencia (`^`), negación unaria (`-x`, `+x`) y agrupación con paréntesis `( )`.
- **Funciones trigonométricas:** `sin` (o su alias en español `sen`), `cos` y `tan` evaluadas en radianes con precisión `double` (`Math.sin`, `Math.cos`, `Math.tan`).
- **Variables simbólicas dinámicas:** Detecta automáticamente todas las variables simbólicas presentes en la expresión (`x`, `y`, `z`, etc.), solicita su valor numérico al usuario y las sustituye durante la evaluación.
- **Gramática LL(1) formal:** Libre de ambigüedad y sin recursividad por la izquierda, preservando la asociatividad por la izquierda en `+`, `-`, `*`, `/` y la asociatividad por la derecha en `^`.
- **Códigos de salida diferenciados (`ExitCodes`):**
  - `0` (`SUCCESS`): Evaluación exitosa.
  - `1` (`LEXICAL_ERROR`): Error léxico (carácter ilegal, número real mal formado, función no reconocida).
  - `2` (`SYNTAX_ERROR`): Error sintáctico (estructura que viola la gramática LL(1), paréntesis desbalanceados, operadores consecutivos, etc.).
  - `3` (`MATH_ERROR`): Error matemático/semántico (división por cero, tangente indefinida en múltiplos impares de $\pi/2$, indeterminación $0^0$, raíz par de número negativo en $\mathbb{R}$).
  - `4` (`INPUT_ERROR`): Error en argumentos de entrada de línea de comandos.
- **Portabilidad multiplataforma:** Interfaz CLI agnóstica al sistema operativo compatible con **Windows** (`run.bat`) y **Linux / macOS** (`run.sh`).

---

## Estructura del Proyecto

```text
Trigono/
├── src/
│   └── trigono/
│       ├── TokenType.java              # Tipos de tokens reconocidos por el lexer
│       ├── Token.java                  # Representación de token (tipo, lexema, valor, posición)
│       ├── ExitCodes.java              # Códigos de salida (0, 1, 2, 3, 4)
│       ├── EvaluatorException.java     # Excepción base con categoría, posición y exit code
│       ├── LexicalException.java       # Error léxico (Código de salida 1)
│       ├── SyntaxException.java        # Error sintáctico LL(1) (Código de salida 2)
│       ├── MathEvalException.java      # Error matemático/semántico (Código de salida 3)
│       ├── Lexer.java                  # Analizador léxico propio y extractor de variables
│       ├── RecursiveDescentParser.java # Reconocedor descendente recursivo LL(1) y evaluador
│       ├── CliPrinter.java             # Utilidad de impresión CLI agnóstica al SO
│       ├── Main.java                   # Punto de entrada CLI (interactivo y por argumentos)
│       └── TestSuite.java              # Suite de 31 pruebas automatizadas y validación matemática
├── docs/
│   ├── Compiladores-Trabajo Práctico 3.pdf             # Enunciado original del Trabajo Práctico
│   ├── GRAMATICA.md                                    # Especificación formal de la Gramática LL(1)
│   ├── informes/
│   │   └── INFORME_TECNICO.docx                        # Entregable 1: Informe técnico (formato Word DOCX)
│   └── pruebas/
│       └── ENTRADAS_Y_RESULTADOS_PRUEBA.md             # Entregable 3: Casos de prueba y validación matemática
├── run.sh                                              # Script de compilación y ejecución para Linux / macOS
├── run.bat                                             # Script de compilación y ejecución para Windows
└── README.md                                           # Guía de uso y documentación general del proyecto
```

---

## Documentación del Proyecto

- **[docs/informes/INFORME_TECNICO.docx](./docs/informes/INFORME_TECNICO.docx):** Informe técnico completo en formato Word (Introducción, objetivos, gramática LL(1), tabla predictiva, descripción del algoritmo descendente recursivo y resultados).
- **[docs/GRAMATICA.md](./docs/GRAMATICA.md):** Documento dedicado exclusivamente a la definición formal de la Gramática LL(1), niveles de precedencia, conjuntos `FIRST` / `FOLLOW` y tabla LL(1).
- **[docs/pruebas/ENTRADAS_Y_RESULTADOS_PRUEBA.md](./docs/pruebas/ENTRADAS_Y_RESULTADOS_PRUEBA.md):** Entradas de prueba, resultados obtenidos con precisión `double` y validación matemática paso a paso.

---

## Gramática LL(1)

```text
1. E    -> T E'
2. E'   -> '+' T E' | '-' T E' | ε
3. T    -> U T'
4. T'   -> '*' U T' | '/' U T' | ε
5. U    -> '-' U | '+' U | P
6. P    -> F P'
7. P'   -> '^' U | ε
8. F    -> '(' E ')' | FUNC '(' E ')' | NUMBER | VARIABLE
9. FUNC -> 'sin' | 'cos' | 'tan'   (acepta 'sen' como alias de 'sin')
```

---

## Requisitos Previos

- **Java Development Kit (JDK)** versión 11 o superior (`javac` y `java` disponibles en el `PATH`).

---

## Instrucciones de Compilación y Ejecución

### 1. Modo Interactivo (Consola)

En este modo el programa solicita la expresión, muestra los tokens generados, detecta las variables simbólicas (`x`, `y`, `z`, etc.), pide al usuario el valor numérico de cada una y devuelve el resultado con precisión `double`:

- **En Linux / macOS:**
  ```bash
  ./run.sh
  ```
- **En Windows (CMD o PowerShell):**
  ```cmd
  run.bat
  ```

*(Comandos internos disponibles en la consola interactiva: `gramatica`, `ayuda`, `salir`)*

### 2. Evaluación Directa por Línea de Comandos (CLI)

- **Expresión sin variables:**
  ```bash
  # Linux / macOS
  ./run.sh --eval "sen(3.14 / 2) + cos(0) * 2^2"

  # Windows
  run.bat --eval "sen(3.14 / 2) + cos(0) * 2^2"
  ```

- **Expresión con variables simbólicas predefinidas (`--var nombre=valor`):**
  ```bash
  # Linux / macOS
  ./run.sh --eval "sen(cos(x))+ tan(y)/cos(x)" --var x=0 --var y=pi/4

  # Windows
  run.bat --eval "sen(cos(x))+ tan(y)/cos(x)" --var x=0 --var y=pi/4
  ```
  *(Nota: si una expresión contiene variables y no se pasan con `--var`, el programa las solicitará interactivamente por teclado).*

- **Mostrar la Gramática LL(1) en pantalla:**
  ```bash
  ./run.sh --grammar
  ```

### 3. Ejecución de la Suite de Pruebas y Validación Matemática

Ejecuta los **31 casos de prueba automatizados** (incluyendo todas las expresiones del enunciado, pruebas de precedencia/asociatividad/negación unaria y verificación de códigos de salida `1`, `2` y `3` para errores léxicos, sintácticos y matemáticos):

- **En Linux / macOS:**
  ```bash
  ./run.sh test
  ```
- **En Windows:**
  ```cmd
  run.bat test
  ```

### 4. Compilación y Ejecución Manual (Sin Scripts)

Si se desea compilar y ejecutar directamente con `javac` y `java` en cualquier sistema operativo:

```bash
mkdir bin
javac -encoding UTF-8 -d bin src/trigono/*.java

# Ejecutar modo interactivo
java -cp bin trigono.Main

# Ejecutar suite de pruebas
java -cp bin trigono.TestSuite
```
