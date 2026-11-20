# Gramática LL(1) para Expresiones Aritméticas y Trigonométricas

## 1. Definición Formal de la Gramática $G = (V_N, V_T, P, E)$

- **Símbolos No Terminales ($V_N$):**
  $$V_N = \{ E, E', T, T', U, P, P', F, FUNC \}$$
- **Símbolos Terminales ($V_T$):**
  $$V_T = \{ \text{'+'}, \text{'-'}, \text{'*'}, \text{'/'}, \text{'\textasciicircum'}, \text{'('}, \text{')'}, \text{'sin'}, \text{'cos'}, \text{'tan'}, \text{NUMBER}, \text{VARIABLE}, \$ \}$$
  *(Nota: el terminal `'sin'` acepta tanto `sin` como su alias en español `sen`; $\$$ representa el fin de entrada `EOF`).*
- **Símbolo Inicial:** $E$

---

## 2. Reglas de Producción ($P$)

```text
 1. E    -> T E'
 2. E'   -> '+' T E'
 3. E'   -> '-' T E'
 4. E'   -> ε
 5. T    -> U T'
 6. T'   -> '*' U T'
 7. T'   -> '/' U T'
 8. T'   -> ε
 9. U    -> '-' U
10. U    -> '+' U
11. U    -> P
12. P    -> F P'
13. P'   -> '^' U
14. P'   -> ε
15. F    -> '(' E ')'
16. F    -> FUNC '(' E ')'
17. F    -> NUMBER
18. F    -> VARIABLE
19. FUNC -> 'sin' | 'cos' | 'tan'
```

---

## 3. Jerarquía de Precedencia y Asociatividad

| Nivel | Operadores / Construcciones | No Terminales | Asociatividad |
|:---:|:---|:---:|:---|
| **1 (Menor)** | Suma (`+`) y Resta (`-`) | `E`, `E'` | Izquierda |
| **2** | Multiplicación (`*`) y División (`/`) | `T`, `T'` | Izquierda |
| **3** | Negación unaria (`-x`) y Signo positivo unario (`+x`) | `U` | Derecha (Prefijo) |
| **4** | Potencia (`^`) | `P`, `P'` | Derecha |
| **5 (Mayor)** | Paréntesis `( E )`, Funciones `sin(E)`, `cos(E)`, `tan(E)`, `NUMBER`, `VARIABLE` | `F`, `FUNC` | — |

---

## 4. Conjuntos `FIRST` y `FOLLOW`

| No Terminal | Conjunto `FIRST` | Conjunto `FOLLOW` | ¿Anulable ($\Rightarrow^* \varepsilon$)? |
|:---:|:---|:---|:---:|
| **`E`** | `{ '-', '+', '(', 'sin', 'cos', 'tan', NUMBER, VARIABLE }` | `{ ')', $ }` | No |
| **`E'`** | `{ '+', '-', ε }` | `{ ')', $ }` | Sí |
| **`T`** | `{ '-', '+', '(', 'sin', 'cos', 'tan', NUMBER, VARIABLE }` | `{ '+', '-', ')', $ }` | No |
| **`T'`** | `{ '*', '/', ε }` | `{ '+', '-', ')', $ }` | Sí |
| **`U`** | `{ '-', '+', '(', 'sin', 'cos', 'tan', NUMBER, VARIABLE }` | `{ '*', '/', '+', '-', ')', $ }` | No |
| **`P`** | `{ '(', 'sin', 'cos', 'tan', NUMBER, VARIABLE }` | `{ '*', '/', '+', '-', ')', $ }` | No |
| **`P'`** | `{ '^', ε }` | `{ '*', '/', '+', '-', ')', $ }` | Sí |
| **`F`** | `{ '(', 'sin', 'cos', 'tan', NUMBER, VARIABLE }` | `{ '^', '*', '/', '+', '-', ')', $ }` | No |
| **`FUNC`** | `{ 'sin', 'cos', 'tan' }` | `{ '(' }` | No |

---

## 5. Tabla Sintáctica Predictiva LL(1)

| No Terminal | `+` | `-` | `*` | `/` | `^` | `(` | `)` | `sin`/`cos`/`tan` | `NUMBER` | `VARIABLE` | `$` (`EOF`) |
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| **`E`** | `E -> T E'` | `E -> T E'` | | | | `E -> T E'` | | `E -> T E'` | `E -> T E'` | `E -> T E'` | |
| **`E'`** | `E' -> + T E'` | `E' -> - T E'` | | | | | `E' -> ε` | | | | `E' -> ε` |
| **`T`** | `T -> U T'` | `T -> U T'` | | | | `T -> U T'` | | `T -> U T'` | `T -> U T'` | `T -> U T'` | |
| **`T'`** | `T' -> ε` | `T' -> ε` | `T' -> * U T'` | `T' -> / U T'` | | | `T' -> ε` | | | | `T' -> ε` |
| **`U`** | `U -> + U` | `U -> - U` | | | | `U -> P` | | `U -> P` | `U -> P` | `U -> P` | |
| **`P`** | | | | | | `P -> F P'` | | `P -> F P'` | `P -> F P'` | `P -> F P'` | |
| **`P'`** | `P' -> ε` | `P' -> ε` | `P' -> ε` | `P' -> ε` | `P' -> ^ U` | | `P' -> ε` | | | | `P' -> ε` |
| **`F`** | | | | | | `F -> ( E )` | | `F -> FUNC ( E )` | `F -> NUMBER` | `F -> VARIABLE` | |
