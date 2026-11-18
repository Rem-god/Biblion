# Script de ejecución para Biblion
Set-Location $PSScriptRoot

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  BIBLION - Librería y Distribuidora Cultural Atenea" -ForegroundColor Yellow
Write-Host "  Base de Datos Orientada a Objetos (ObjectDB / JPA)" -ForegroundColor White
Write-Host "  Tecnológico Superior de Jalisco" -ForegroundColor Green
Write-Host "  Román Emmanuel Castañeda Barboza (230110841)" -ForegroundColor Gray
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

$jarPath = Join-Path $PSScriptRoot "target\biblion-1.0.0.jar"
if (Test-Path $jarPath) {
    Write-Host "Ejecutando JAR empaquetado..." -ForegroundColor Green
    java -jar $jarPath
} else {
    Write-Host "Compilando y ejecutando con Maven..." -ForegroundColor Yellow
    & "C:\Users\Román Emmanuel\tools\apache-maven-3.9.6\bin\mvn.cmd" compile exec:java
}

