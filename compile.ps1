$cp = [System.IO.File]::ReadAllText('cp.txt').Trim()
if (Test-Path 'target\classes') {
    Remove-Item 'target\classes' -Recurse -Force
}
New-Item -ItemType Directory -Force -Path 'target\classes' | Out-Null

# Copy resources
Copy-Item -Path 'src\main\resources\*' -Destination 'target\classes' -Recurse -Force

# Get all Java files
$javaFiles = Get-ChildItem -Path 'src\main\java' -Recurse -Filter '*.java' | Select-Object -ExpandProperty FullName
[System.IO.File]::WriteAllLines('sources.txt', $javaFiles)

Write-Output "Found $($javaFiles.Count) Java source files. Compiling..."

& javac -encoding UTF-8 -cp $cp -d 'target\classes' '@sources.txt'
if ($LASTEXITCODE -eq 0) {
    Write-Output "Compilation successful!"
} else {
    Write-Output "Compilation failed with exit code $LASTEXITCODE"
}
