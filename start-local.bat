@echo off
REM Start Staffcore33 ATS locally (Windows)
REM Prerequisites: PostgreSQL running, JDK 17+, Maven, Node.js

echo Starting backend on :8080 ...
start "Staffcore33 API" cmd /k "cd /d %~dp0backend && mvn -DskipTests spring-boot:run"

timeout /t 8 /nobreak >nul

echo Starting frontend on :3000 ...
start "Staffcore33 Web" cmd /k "cd /d %~dp0frontend && npm run dev"

echo.
echo Open http://localhost:3000
echo Login: admin@staffcore33.com / Admin@123
