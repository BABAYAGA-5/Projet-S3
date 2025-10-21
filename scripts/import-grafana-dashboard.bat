@echo off
REM Script to automatically import Grafana dashboard

setlocal enabledelayedexpansion

echo ========================================
echo Grafana Dashboard Auto-Import
echo ========================================

REM Get Minikube IP
for /f %%i in ('"C:\ProgramData\chocolatey\bin\minikube.exe" ip') do set MINIKUBE_IP=%%i
echo Minikube IP: %MINIKUBE_IP%

REM Set Grafana URL
set GRAFANA_URL=http://%MINIKUBE_IP%:30300
set GRAFANA_USER=admin
set GRAFANA_PASS=admin

echo Waiting for Grafana to be ready...
timeout /t 30 /nobreak >nul

echo.
echo Importing dashboard...

REM Create dashboard JSON payload
set DASHBOARD_FILE=%~dp0..\docs\projet-s3-dashboard.json

REM Import dashboard using curl
curl -X POST "%GRAFANA_URL%/api/dashboards/db" ^
  -H "Content-Type: application/json" ^
  -u %GRAFANA_USER%:%GRAFANA_PASS% ^
  --data-binary "@%DASHBOARD_FILE%"

echo.
echo ========================================
echo Dashboard imported successfully!
echo Access it at: %GRAFANA_URL%
echo ========================================

endlocal
