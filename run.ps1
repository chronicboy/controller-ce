# Controller CE - Interactive GUI Launcher with Auto Lock-Clearing
Write-Host "[Controller CE] Checking for stale locks or lingering Minecraft processes..." -ForegroundColor Cyan

$procs = Get-Process java, javaw -ErrorAction SilentlyContinue | Where-Object { $_.Path -notlike "*antigravity*" -and $_.MainWindowTitle -eq "" }
foreach ($p in $procs) {
    try {
        $cmd = (Get-CimInstance Win32_Process -Filter "ProcessId = $($p.Id)").CommandLine
        if ($cmd -match "Minecraft|runClient|fabric|loom") {
            Stop-Process -Id $p.Id -Force
            Write-Host "  -> Freed orphaned process $($p.Id)" -ForegroundColor Yellow
        }
    } catch {}
}

Write-Host "[Controller CE] Stopping lingering daemons to ensure clean GUI launch..." -ForegroundColor Cyan
& .\gradlew.bat --stop | Out-Null

Write-Host "[Controller CE] Launching Minecraft in interactive foreground mode..." -ForegroundColor Green
& .\gradlew.bat runClient --no-daemon
