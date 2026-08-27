# Smart Park - PowerShell Build Script
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "   Compiling Smart Park Management System (Java + JDBC)" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

$javaFiles = Get-ChildItem -Path "src" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 -cp "lib/*" -d "bin" $javaFiles

if ($LASTEXITCODE -eq 0) {
    Write-Host "[SUCCESS] Compilation completed with 0 errors!" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
}
