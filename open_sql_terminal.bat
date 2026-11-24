@echo off
setlocal
title PostgreSQL Query Shell (business_mgmt)
cd /d "%~dp0"

echo =======================================================
echo   PostgreSQL Terminal - business_mgmt Database
echo =======================================================
echo.
echo Checking database service status...

powershell -ExecutionPolicy Bypass -Command "$client = New-Object System.Net.Sockets.TcpClient; try { $iar = $client.BeginConnect('127.0.0.1', 5433, $null, $null); if (-not $iar.AsyncWaitHandle.WaitOne(1000)) { if (Test-Path 'C:\pgdata_local\postmaster.pid') { Remove-Item 'C:\pgdata_local\postmaster.pid' -Force }; Start-Process -FilePath 'C:\Program Files\PostgreSQL\18\bin\postgres.exe' -ArgumentList '-D C:\pgdata_local -p 5433' -WindowStyle Hidden; Start-Sleep -Seconds 2 } } catch {} finally { $client.Close() }"

echo Connecting to business_mgmt...
echo.
echo Commands you can run:
echo   - List tables:        \dt
echo   - Exit terminal:      \q
echo   - Run any SQL query (remember the ';' at the end!)
echo =======================================================
echo.

"C:\Program Files\PostgreSQL\18\bin\psql.exe" -h localhost -p 5433 -U postgres -d business_mgmt

pause
