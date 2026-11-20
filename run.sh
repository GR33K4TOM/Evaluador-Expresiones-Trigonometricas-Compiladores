#!/usr/bin/env sh
# ==============================================================================
# Script portable de compilacion y ejecucion para Linux / macOS
# Uso:
#   ./run.sh                 -> Inicia la CLI interactiva
#   ./run.sh test            -> Ejecuta la suite completa de pruebas y validacion
#   ./run.sh --eval "..."    -> Evalua una expresion por CLI
# ==============================================================================
set -e

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
cd "$SCRIPT_DIR"

mkdir -p bin
javac -encoding UTF-8 -d bin src/trigono/*.java

if [ "$1" = "test" ]; then
    shift
    exec java -cp bin trigono.TestSuite "$@"
else
    exec java -cp bin trigono.Main "$@"
fi
