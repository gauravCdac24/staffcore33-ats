@echo off
REM Staffcore33 ATS — one-click local start (Windows)
REM Auto-installs missing JDK / Maven / Node.js / npm when needed
REM Data: PostgreSQL (staffcore33_ats) | Uploads: project\device\
REM Frees ports 3000 / 8080 if occupied, then starts API + Web.

setlocal
cd /d "%~dp0"

echo.
echo Staffcore33 ATS — starting local stack...
echo.

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-local.ps1" %*
if errorlevel 1 (
  echo.
  echo Startup failed. See messages above.
  pause
  exit /b 1
)

endlocal
