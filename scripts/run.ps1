param([switch]$Demo)
$ErrorActionPreference = 'Stop'
& (Join-Path $PSScriptRoot 'build.ps1')
$projectRoot = Split-Path -Parent $PSScriptRoot
Push-Location $projectRoot
try {
    if ($Demo) { & java -cp build/classes oopnemo.app.Main --demo }
    else { & java -cp build/classes oopnemo.app.Main }
    if ($LASTEXITCODE -ne 0) { throw 'Application failed.' }
} finally { Pop-Location }
