@echo off
echo ============================================
echo   SISTEMA TRANS VELASCO - COMPILACION
echo   Grupo09sc - 2025
echo ============================================
echo.

echo [1/3] Limpiando compilaciones anteriores...
if exist "bin" rmdir /s /q bin
mkdir bin

echo [2/3] Compilando proyecto Java...
echo.

:: Compilar todas las clases Java
javac -d bin -cp "lib\*" -sourcepath src src\Main.java src\database\*.java src\email\*.java src\commands\*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] La compilacion fallo!
    echo Verifica que tengas Java JDK instalado.
    pause
    exit /b 1
)

echo.
echo [3/3] Compilacion exitosa!
echo.
echo Archivos compilados en: bin\
echo.
echo Para ejecutar el sistema, usa: ejecutar.bat
echo.
pause
