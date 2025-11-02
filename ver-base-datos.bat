@echo off
chcp 65001 > nul
echo ============================================
echo   VER CONTENIDO DE BASE DE DATOS
echo   Grupo09sc - 2025
echo ============================================
echo.

echo Compilando...
javac -encoding UTF-8 -d bin -cp "lib\*" src\VerBaseDatos.java

if %errorlevel% neq 0 (
    echo.
    echo ❌ Error al compilar
    pause
    exit /b 1
)

echo.
echo Conectando a la base de datos...
echo.
echo ============================================
echo.

java -cp "bin;lib\*" VerBaseDatos

echo.
echo ============================================
pause
