# Smart Park - PowerShell Run Script
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "   Starting Smart Park Server on http://localhost:8080" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

java -cp "bin;lib/*" com.smartpark.Main
