# SonarQube Analysis Script for Projet S3
# This script runs a clean SonarQube analysis

Write-Host "=== Starting SonarQube Analysis ===" -ForegroundColor Green
Write-Host ""

# Configuration
$PROJECT_KEY = "JAVA_Projet_S3"
$PROJECT_NAME = "Projet S3"
$SONAR_URL = "http://127.0.0.1:53156"
$SONAR_TOKEN = "squ_98395e7e95f60fe9f8575b787498765b661fc660"

# Clean previous build artifacts
Write-Host "Cleaning previous build artifacts..." -ForegroundColor Yellow
if (Test-Path "target") {
    Remove-Item -Recurse -Force "target"
}

# Run Maven compile and SonarQube analysis
Write-Host "Running Maven compile and SonarQube analysis..." -ForegroundColor Yellow
Write-Host ""

mvn clean compile sonar:sonar `
    "-Dsonar.projectKey=$PROJECT_KEY" `
    "-Dsonar.projectName=$PROJECT_NAME" `
    "-Dsonar.host.url=$SONAR_URL" `
    "-Dsonar.login=$SONAR_TOKEN"

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "=== Analysis Successful! ===" -ForegroundColor Green
    Write-Host "View your results at: $SONAR_URL" -ForegroundColor Cyan
    Write-Host "Project: $PROJECT_NAME" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "=== Analysis Failed ===" -ForegroundColor Red
    Write-Host "Check the error messages above" -ForegroundColor Red
}
