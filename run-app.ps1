# PowerShell script to run Spring Boot application
$env:JAVA_HOME = "$env:USERPROFILE\Downloads\jdk-25_windows-x64_bin\jdk-25"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "JAVA_HOME: $env:JAVA_HOME"
Write-Host "Java Version:"
java -version

Write-Host ""
Write-Host "Starting Spring Boot Application..."

# Try different approaches
Write-Host "Attempting Method 1: Maven Wrapper"
try {
    & ".\mvnw.cmd" spring-boot:run
} catch {
    Write-Host "Method 1 failed, trying Method 2: Direct Maven"
    try {
        & mvn spring-boot:run
    } catch {
        Write-Host "Method 2 failed, trying Method 3: Manual compilation"
        Write-Host "You may need to:"
        Write-Host "1. Install Maven directly"
        Write-Host "2. Or use your IDE to run the application"
        Write-Host "3. Or compile manually with javac"
    }
}