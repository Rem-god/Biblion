@echo off
title Biblion - Librería Atenea (ObjectDB MVC)
echo ========================================================
echo   BIBLION - Libreria y Distribuidora Cultural Atenea
echo   Base de Datos Orientada a Objetos (ObjectDB / JPA)
echo   Tecnologico Superior de Jalisco - Roman Emmanuel Castaneda Barboza
echo ========================================================
echo.
cd /d "%~dp0"

if exist "target\biblion-1.0.0.jar" (
    echo Iniciando aplicacion desde JAR ejecutable...
    java -jar target\biblion-1.0.0.jar
) else (
    echo Compilando y ejecutando mediante Maven...
    call "C:\Users\Román Emmanuel\tools\apache-maven-3.9.6\bin\mvn.cmd" compile exec:java
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Ocurrio un error al ejecutar la aplicacion.
    pause
)

