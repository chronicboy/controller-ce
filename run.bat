@echo off
cd /d "%~dp0"
title Controller CE - Client Launcher

echo [Controller CE] Clearing any zombie background processes...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$procs = Get-Process java, javaw -ErrorAction SilentlyContinue | Where-Object { $_.Path -notlike '*antigravity*' -and $_.MainWindowTitle -eq '' }; foreach ($p in $procs) { try { $cmd = (Get-CimInstance Win32_Process -Filter ('ProcessId = ' + $p.Id)).CommandLine; if ($cmd -match 'Minecraft|runClient|fabric|loom') { Stop-Process -Id $p.Id -Force; Write-Host ('Freed locked process ' + $p.Id) } } catch {} }"

echo [Controller CE] Stopping lingering daemons to ensure clean GUI launch...
call gradlew.bat --stop >nul 2>&1

echo [Controller CE] Starting Minecraft Client in interactive foreground mode...
call gradlew.bat runClient --no-daemon

if errorlevel 1 (
    echo.
    echo [Controller CE] Run ended with exit code %errorlevel%.
)
