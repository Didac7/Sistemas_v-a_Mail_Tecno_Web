@echo off
echo ============================================
echo   SISTEMA TRANS VELASCO - EJECUCION
echo   Grupo09sc - 2025
echo ============================================
echo.

:: Verificar si está compilado
if not exist "bin\Main.class" (
    echo [ERROR] El proyecto no esta compilado!
    echo Ejecuta primero: compilar.bat
    echo.
    pause
    exit /b 1
)

echo Iniciando el sistema...
echo.
echo NOTA: El sistema comenzara a monitorear emails.
echo Presiona Ctrl+C para detener el sistema.
echo.
echo ============================================
echo.

:: Ejecutar el programa
java -cp "bin;lib\*" Main

pause
