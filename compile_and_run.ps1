# Compile and Run CodeAlpha Stock Trading Platform

# 1. Check for dependency JAR
if (!(Test-Path -Path "lib/flatlaf-3.7.1.jar")) {
    Write-Host "FlatLaf dependency not found. Fetching..." -ForegroundColor Yellow
    powershell -ExecutionPolicy Bypass -File .\download_dependencies.ps1
}

# 2. Create bin/ output directory
if (!(Test-Path -Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
    Write-Host "Created bin output directory." -ForegroundColor Green
}

Write-Host "Compiling Java sources..." -ForegroundColor Cyan
try {
    # Recursively fetch all Java files
    $javaFiles = Get-ChildItem -Path "src" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
    
    # Run compiler
    javac -cp "lib/*;src" -d bin $javaFiles
    Write-Host "Compilation successful!" -ForegroundColor Green
} catch {
    Write-Error "Compilation failed. Please inspect errors above."
    exit
}

Write-Host "Launching CodeAlpha Stock Trading Platform..." -ForegroundColor Green
java -cp "lib/*;bin" com.codealpha.stocktrading.Main
