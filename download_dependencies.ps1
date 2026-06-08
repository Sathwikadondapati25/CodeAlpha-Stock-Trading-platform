# Create lib directory if it doesn't exist
if (!(Test-Path -Path "lib")) {
    New-Item -ItemType Directory -Path "lib" | Out-Null
    Write-Host "Created lib directory." -ForegroundColor Green
}

$url = "https://repo1.maven.org/maven2/com/formdev/flatlaf/3.7.1/flatlaf-3.7.1.jar"
$output = "lib/flatlaf-3.7.1.jar"

Write-Host "Downloading FlatLaf dependency from Maven Central..." -ForegroundColor Cyan
try {
    Invoke-WebRequest -Uri $url -OutFile $output -ErrorAction Stop
    Write-Host "FlatLaf downloaded successfully to $output" -ForegroundColor Green
} catch {
    Write-Error "Failed to download FlatLaf. Details: $_"
}
