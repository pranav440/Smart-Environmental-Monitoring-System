$MAVEN_EXEC = Join-Path $PSScriptRoot "smart-environment-monitor\.maven\apache-maven-3.9.6\bin\mvn.cmd"
$PROJECT_DIR = Join-Path $PSScriptRoot "smart-environment-monitor"

Write-Host "--- Smart Environmental Monitor Startup ---" -ForegroundColor Cyan

# 1. Check for Java
if (!(Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Host "Error: Java is not installed or not in your PATH." -ForegroundColor Red
    Write-Host "Please install Java 17+ and try again."
    Pause
    exit
}

# 2. Check for Maven
if (Test-Path $MAVEN_EXEC) {
    Write-Host "Using Local Maven: $MAVEN_EXEC"
    Set-Location $PROJECT_DIR
    
    Write-Host "Starting Spring Boot Application..." -ForegroundColor Yellow
    & $MAVEN_EXEC spring-boot:run
    
    Write-Host "`nProcess finished. Press any key to close."
    Pause
} else {
    Write-Host "Error: Could not find Maven at $MAVEN_EXEC" -ForegroundColor Red
    Write-Host "Please ensure you are running this from the correct root directory."
    Pause
}
