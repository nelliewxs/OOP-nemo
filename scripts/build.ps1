$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Push-Location $projectRoot
try {
    New-Item -ItemType Directory -Path 'build/classes' -Force | Out-Null
    $sources = @(Get-ChildItem -LiteralPath 'src/main/java' -Filter '*.java' -Recurse | ForEach-Object FullName)
    & javac --release 17 -encoding UTF-8 -Xlint:all -Werror -d build/classes $sources
    if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed.' }
    Write-Output 'Build passed (Java 17 target).'
} finally { Pop-Location }
