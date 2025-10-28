@echo off
echo ============================================
echo   DESCARGA DE DEPENDENCIAS
echo   Sistema Trans Velasco
echo ============================================
echo.

:: Crear carpeta lib si no existe
if not exist "lib" mkdir lib

echo Descargando dependencias necesarias...
echo.

echo [1/3] Descargando PostgreSQL JDBC Driver...
powershell -Command "Invoke-WebRequest -Uri 'https://jdbc.postgresql.org/download/postgresql-42.7.1.jar' -OutFile 'lib\postgresql-42.7.1.jar'"

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] No se pudo descargar PostgreSQL Driver
    echo Por favor descarga manualmente desde: https://jdbc.postgresql.org/download.html
    echo.
)

echo.
echo [2/3] Descargando JavaMail API...
powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/sun/mail/javax.mail/1.6.2/javax.mail-1.6.2.jar' -OutFile 'lib\javax.mail-1.6.2.jar'"

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] No se pudo descargar JavaMail
    echo Por favor descarga manualmente desde: https://javaee.github.io/javamail/
    echo.
)

echo.
echo [3/3] Descargando Activation Framework...
powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/javax/activation/activation/1.1.1/activation-1.1.1.jar' -OutFile 'lib\activation-1.1.1.jar'"

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] No se pudo descargar Activation Framework
    echo.
)

echo.
echo ============================================
echo Dependencias descargadas en carpeta lib\
echo ============================================
echo.
echo Archivos en lib\:
dir /b lib
echo.
echo Ahora puedes ejecutar: compilar.bat
echo.
pause
