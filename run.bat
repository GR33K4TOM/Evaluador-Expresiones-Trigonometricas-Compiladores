@echo off
REM ==============================================================================
REM Script portable de compilacion y ejecucion para Windows (CMD / PowerShell)
REM Uso:
REM   run.bat                 -> Inicia la CLI interactiva
REM   run.bat test            -> Ejecuta la suite completa de pruebas y validacion
REM   run.bat --eval "..."    -> Evalua una expresion por CLI
REM ==============================================================================
setlocal
cd /d "%~dp0"

if not exist "bin" mkdir "bin"
javac -encoding UTF-8 -d bin src\trigono\*.java
if errorlevel 1 exit /b %errorlevel%

if /I "%~1"=="test" (
    java -cp bin trigono.TestSuite
    exit /b %errorlevel%
) else (
    java -cp bin trigono.Main %*
    exit /b %errorlevel%
)
