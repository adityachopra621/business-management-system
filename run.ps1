# 1. Check if PostgreSQL database on port 5433 is running. If not, start it automatically.
$portActive = $false
try {
    $client = New-Object System.Net.Sockets.TcpClient
    $iar = $client.BeginConnect('127.0.0.1', 5433, $null, $null)
    $portActive = $iar.AsyncWaitHandle.WaitOne(1000)
    $client.Close()
} catch {}

if (-not $portActive) {
    Write-Output "Starting local database service..."
    if (Test-Path 'C:\pgdata_local\postmaster.pid') {
        Remove-Item 'C:\pgdata_local\postmaster.pid' -Force -ErrorAction SilentlyContinue
    }
    Start-Process -FilePath 'C:\Program Files\PostgreSQL\18\bin\postgres.exe' -ArgumentList '-D C:\pgdata_local -p 5433' -WindowStyle Hidden
    Start-Sleep -Seconds 3
}

# 2. Setup JavaFX modules and Classpath
$m2 = 'C:\Users\adity\.m2\repository'
$moduleJars = @(
    "$m2\org\openjfx\javafx-base\22.0.1\javafx-base-22.0.1-win.jar",
    "$m2\org\openjfx\javafx-base\22.0.1\javafx-base-22.0.1.jar",
    "$m2\org\openjfx\javafx-controls\22.0.1\javafx-controls-22.0.1-win.jar",
    "$m2\org\openjfx\javafx-controls\22.0.1\javafx-controls-22.0.1.jar",
    "$m2\org\openjfx\javafx-fxml\22.0.1\javafx-fxml-22.0.1-win.jar",
    "$m2\org\openjfx\javafx-fxml\22.0.1\javafx-fxml-22.0.1.jar",
    "$m2\org\openjfx\javafx-graphics\22.0.1\javafx-graphics-22.0.1-win.jar",
    "$m2\org\openjfx\javafx-graphics\22.0.1\javafx-graphics-22.0.1.jar"
)
$modulePath = $moduleJars -join ';'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

$cp = [System.IO.File]::ReadAllText("$scriptDir\cp.txt").Trim()
$fullCp = "$scriptDir\target\classes;$cp"

Write-Output "Launching Business Management System JavaFX Application..."
& java --module-path $modulePath --add-modules javafx.controls,javafx.fxml -cp $fullCp com.businessmgmt.App
