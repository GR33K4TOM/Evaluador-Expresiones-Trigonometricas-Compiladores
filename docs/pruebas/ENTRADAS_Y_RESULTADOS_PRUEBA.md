# Entradas de Prueba, Resultados Obtenidos y Validación Matemática

Este documento constituye el **Entregable 3** del Trabajo Práctico 3 (*Diseño de Gramática, Reconocedor Descendente y Evaluador para Expresiones Trigonométricas*). Contiene la totalidad de las entradas de prueba ejecutadas sobre el parser descendente recursivo LL(1) en Java, los resultados numéricos con precisión `double` obtenidos y su respectiva demostración y validación matemática paso a paso.

---

## 1. Casos de Prueba Oficiales del Enunciado (Expresiones Trigonométricas y Aritméticas)

> **Nota sobre unidades angulares:** Tal como especifica la sección *Evaluación semántica* del documento (`Math.sin`, `Math.cos`, `Math.tan`), todas las funciones trigonométricas operan en **radianes** con precisión `double` IEEE 754 (64 bits).

| ID | Expresión de Entrada | Variables Simbólicas Sustituidas | Resultado Obtenido (`double`) | Resultado Matemático Esperado | Validación Matemática Paso a Paso | Estado |
|:---|:---|:---|:---|:---|:---|:---:|
| **PDF-01** | `sen(3.14 / 2) + cos(0) * 2^2` | *(Ninguna)* | `4.9999996829318345` | `4.9999996829318345` | 1. Paréntesis: $3.14 / 2 = 1.57$<br>2. Funciones: $\sin(1.57) \approx 0.9999996829318346$, $\cos(0) = 1.0$<br>3. Potencia: $2^2 = 4.0$<br>4. Multiplicación: $1.0 \times 4.0 = 4.0$<br>5. Suma: $0.9999996829318346 + 4.0 = 4.9999996829318345$ | **OK (0)** |
| **PDF-02** | `sin(x / 2) + cos(0) * 2^2` | $x = \pi \approx 3.141592653589793$ | `5.0` | `5.0` | 1. Sustitución: $x / 2 = \pi / 2$<br>2. $\sin(\pi / 2) = 1.0$, $\cos(0) = 1.0$<br>3. Potencia y producto: $1.0 \times 2^2 = 4.0$<br>4. Suma: $1.0 + 4.0 = 5.0$ (Ejemplo exacto de la pág. 2 del PDF) | **OK (0)** |
| **PDF-03** | `sen(x) + cos(y)` | $x = \frac{\pi}{2} \approx 1.5707963268$<br>$y = 0.0$ | `2.0` | `2.0` | 1. Sustitución: $\sin(\pi/2) = 1.0$ y $\cos(0) = 1.0$<br>2. Suma: $1.0 + 1.0 = 2.0$ | **OK (0)** |
| **PDF-04** | `sen(cos(x))+ tan(y)/cos(x)` | $x = 0.0$<br>$y = \frac{\pi}{4} \approx 0.7853981634$ | `1.8414709848078963` | `1.8414709848078963` | 1. Función anidada: $\cos(0) = 1.0 \Rightarrow \sin(1.0) \approx 0.8414709848078965$<br>2. Segundo término: $\tan(\pi/4) / \cos(0) = 1.0 / 1.0 = 1.0$<br>3. Suma: $0.8414709848078965 + 1.0 = 1.8414709848078963$ | **OK (0)** |
| **PDF-05** | `cos(x)^2 + sen(x)^2` | $x = 1.2345678$ | `1.0` | `1.0` | Identidad pitagórica trigonométrica fundamental:<br>$\cos^2(1.2345678) + \sin^2(1.2345678) = 0.1088572191 + 0.8911427809 = 1.0$ | **OK (0)** |
| **PDF-06** | `sin(x)` | $x = \frac{\pi}{6} \approx 0.5235987756$ | `0.5` | `0.5` | $\sin(\pi / 6) = \sin(30^\circ) = 0.5$ | **OK (0)** |
| **PDF-07** | `cos(x + y)` | $x = \frac{\pi}{3} \approx 1.0471975512$<br>$y = \frac{\pi}{6} \approx 0.5235987756$ | `0.0` | `0.0` | 1. Suma interior: $\frac{\pi}{3} + \frac{\pi}{6} = \frac{\pi}{2}$<br>2. Evaluación: $\cos(\pi / 2) = 0.0$ | **OK (0)** |
| **PDF-08** | `tan(x^2 + sin(x))` | $x = 0.5$ | `0.8938835261687129` | `0.8938835261687129` | 1. Potencia: $0.5^2 = 0.25$<br>2. Seno: $\sin(0.5) \approx 0.479425538604203$<br>3. Suma: $0.25 + 0.479425538604203 = 0.729425538604203$<br>4. Tangente: $\tan(0.729425538604203) \approx 0.8938835261687129$ | **OK (0)** |

---

## 2. Casos de Prueba de Precedencia, Asociatividad, Negación Unaria y Paréntesis

| ID | Expresión de Entrada | Variables Simbólicas | Resultado Obtenido (`double`) | Resultado Matemático Esperado | Validación Matemática y Regla Gramatical Verificada | Estado |
|:---|:---|:---|:---|:---|:---|:---:|
| **ARIT-01** | `10 - 4 - 2` | *(Ninguna)* | `4.0` | `4.0` | **Asociatividad izquierda de `-`**: $(10 - 4) - 2 = 6 - 2 = 4.0$ (Si fuera asociativa a derecha daría $10 - (4 - 2) = 8.0$, lo cual sería incorrecto). | **OK (0)** |
| **ARIT-02** | `24 / 6 / 2` | *(Ninguna)* | `2.0` | `2.0` | **Asociatividad izquierda de `/`**: $(24 / 6) / 2 = 4 / 2 = 2.0$ (Si fuera asociativa a derecha daría $24 / (6 / 2) = 8.0$). | **OK (0)** |
| **ARIT-03** | `2 ^ 3 ^ 2` | *(Ninguna)* | `512.0` | `512.0` | **Asociatividad derecha de `^`**: $2^{(3^2)} = 2^9 = 512.0$ (Regla `P' -> '^' U`). | **OK (0)** |
| **ARIT-04** | `-2^2` | *(Ninguna)* | `-4.0` | `-4.0` | **Precedencia de `^` sobre negación unaria `-`**: $-(2^2) = -4.0$ (Regla `U -> '-' U` sobre `P -> F P'`). | **OK (0)** |
| **ARIT-05** | `(-2)^2` | *(Ninguna)* | `4.0` | `4.0` | **Agrupación con paréntesis en base negativa**: $(-2)^2 = 4.0$ (Regla `F -> '(' E ')'`). | **OK (0)** |
| **ARIT-06** | `2^-3` | *(Ninguna)* | `0.125` | `0.125` | **Exponente con negación unaria**: $2^{-3} = \frac{1}{2^3} = \frac{1}{8} = 0.125$. | **OK (0)** |
| **ARIT-07** | `-sin(-x) + -(cos(y) * -z)` | $x = \frac{\pi}{2}, y = 0, z = 3$ | `4.0` | `4.0` | $-\sin(-\pi/2) + -(\cos(0) \times (-3)) = -(-1) + -(1 \times -3) = 1 + 3 = 4.0$. | **OK (0)** |
| **ARIT-08** | `((x + y) * z - 2) / (1 + tan(0))` | $x = 2.5, y = 1.5, z = 4$ | `14.0` | `14.0` | $((2.5 + 1.5) \times 4 - 2) / (1 + 0) = (4 \times 4 - 2) / 1 = 14.0$. | **OK (0)** |

---

## 3. Casos de Prueba de Detección de Errores (Códigos de Salida Distintos)

El sistema distingue estrictamente entre **Errores Léxicos (Código 1)**, **Errores Sintácticos (Código 2)** y **Errores Matemáticos/Semánticos (Código 3)**:

| ID | Expresión de Entrada | Categoría Esperada | Código de Salida Obtenido | Mensaje de Diagnóstico Generado por el Evaluador | Estado |
|:---|:---|:---|:---:|:---|:---:|
| **ERR-LEX-01** | `sin(x) + 3.14.15` | **ERROR LÉXICO** | `1` | `Número real mal formado con múltiples puntos decimales cerca de la posición 14` | **OK (1)** |
| **ERR-LEX-02** | `cos(x) @ 2` | **ERROR LÉXICO** | `1` | `Carácter ilegal '@' en la posición 8` | **OK (1)** |
| **ERR-LEX-03** | `cot(x) + 1` | **ERROR LÉXICO** | `1` | `Identificador o función no reconocida 'cot' en la posición 1 (funciones válidas: sin, sen, cos, tan; variables válidas: una letra como x, y, z)` | **OK (1)** |
| **ERR-LEX-04** | `2 ** 3` | **ERROR LÉXICO** | `1` | `Operador '**' no válido en la gramática; utilice '^' para potencia en posición 3` | **OK (1)** |
| **ERR-SIN-01** | `sin(x + 2` | **ERROR SINTÁCTICO** | `2` | `Se esperaba ')' para cerrar el argumento de la función 'sin' iniciada en la posición 1 (se encontró 'EOF' [FIN_DE_ENTRADA] en la posición 10).` | **OK (2)** |
| **ERR-SIN-02** | `cos(x)) + 1` | **ERROR SINTÁCTICO** | `2` | `Token inesperado ')' (PARÉNTESIS_DER())) en la posición 7; se esperaba el fin de la expresión o un operador.` | **OK (2)** |
| **ERR-SIN-03** | `5 * / 2` | **ERROR SINTÁCTICO** | `2` | `Error sintáctico en posición 5: se encontró '/' (OPERADOR(/)), pero se esperaba un número, una variable, una función (sin, cos, tan) o '('.` | **OK (2)** |
| **ERR-SIN-04** | `sin() + 1` | **ERROR SINTÁCTICO** | `2` | `La función 'sin' en la posición 1 requiere un argumento dentro de los paréntesis.` | **OK (2)** |
| **ERR-SIN-05** | `sin x + 1` | **ERROR SINTÁCTICO** | `2` | `Se esperaba '(' inmediatamente después de la función 'sin' en la posición 1 (se encontró 'x' [VARIABLE] en la posición 5).` | **OK (2)** |
| **ERR-SIN-06** | `3 + 4 *` | **ERROR SINTÁCTICO** | `2` | `Error sintáctico en posición 8: se encontró 'EOF' (FIN_DE_ENTRADA), pero se esperaba un número, una variable, una función (sin, cos, tan) o '('.` | **OK (2)** |
| **ERR-MAT-01** | `sen(x) / (cos(0) - 1)` | **ERROR MATEMÁTICO** | `3` | `División por cero detectada en la posición 8 (dividendo = 0.8414709848078965, divisor = 0).` | **OK (3)** |
| **ERR-MAT-02** | `tan(x)` *(con $x = \pi/2$)* | **ERROR MATEMÁTICO** | `3` | `Tangente indefinida en 'tan(1.5707963267948966)' (posición 1): cos(1.5707963267948966) = 0 (asíntota vertical en múltiplo impar de pi/2).` | **OK (3)** |
| **ERR-MAT-03** | `(-4) ^ 0.5` | **ERROR MATEMÁTICO** | `3` | `Dominio real inválido en potencia: base negativa (-4) elevada a exponente no entero (0.5) en la posición 6.` | **OK (3)** |
| **ERR-MAT-04** | `0 ^ 0` | **ERROR MATEMÁTICO** | `3` | `Indeterminación matemática 0^0 en la posición 3.` | **OK (3)** |
| **ERR-MAT-05** | `0 ^ -2` | **ERROR MATEMÁTICO** | `3` | `División por cero implícita en potencia (0 elevado a exponente negativo -2) en la posición 3.` | **OK (3)** |

---

## 4. Reproducción Automatizada de las Pruebas (Windows y Linux)

Para ejecutar y verificar automáticamente los **31 casos de prueba**:

- **En Linux / macOS:**
  ```bash
  ./run.sh test
  ```
- **En Windows (CMD o PowerShell):**
  ```cmd
  run.bat test
  ```
