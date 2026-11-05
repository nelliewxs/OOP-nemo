$ErrorActionPreference = 'Stop'
& (Join-Path $PSScriptRoot 'build.ps1')
$projectRoot = Split-Path -Parent $PSScriptRoot
Push-Location $projectRoot
try {
    New-Item -ItemType Directory -Path 'build/test-classes' -Force | Out-Null
    $sources = @(Get-ChildItem -LiteralPath 'src/test/java' -Filter '*.java' -Recurse | ForEach-Object FullName)
    & javac --release 17 -encoding UTF-8 -Xlint:all -Werror -cp build/classes -d build/test-classes $sources
    if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed.' }
    & java -cp "build/classes$([IO.Path]::PathSeparator)build/test-classes" oopnemo.CampusServiceTest
    if ($LASTEXITCODE -ne 0) { throw 'Tests failed.' }
} finally { Pop-Location }
