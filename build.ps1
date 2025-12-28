# build.ps1 - compile sources and create server/client jars
Set-StrictMode -Version Latest
$PSScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$src = Join-Path $PSScriptRoot "src"
$out = Join-Path $PSScriptRoot "out"
$dist = Join-Path $PSScriptRoot "dist"

if(Test-Path $out){ Remove-Item $out -Recurse -Force }
New-Item -ItemType Directory -Path $out | Out-Null

Write-Host "Collecting Java sources..."
$files = Get-ChildItem -Path $src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
if(-not $files){ Write-Error "No .java files found under $src"; exit 1 }

Write-Host "Compiling $($files.Count) source files..."
& javac -d $out $files
if($LASTEXITCODE -ne 0){ Write-Error "javac failed with exit code $LASTEXITCODE"; exit $LASTEXITCODE }

if(Test-Path $dist){ Remove-Item $dist -Recurse -Force }
New-Item -ItemType Directory -Path $dist | Out-Null

Write-Host "Creating server.jar (Main-Class: SRC.server.main.ServerApp)"
& jar cfe (Join-Path $dist "server.jar") "SRC.server.main.ServerApp" -C $out .
if($LASTEXITCODE -ne 0){ Write-Error "jar failed for server.jar"; exit $LASTEXITCODE }

Write-Host "Creating client.jar (Main-Class: SRC.client.main.ClientApp)"
& jar cfe (Join-Path $dist "client.jar") "SRC.client.main.ClientApp" -C $out .
if($LASTEXITCODE -ne 0){ Write-Error "jar failed for client.jar"; exit $LASTEXITCODE }

Write-Host "Build finished. JAR files located in: $dist"
Write-Host "Run with: java -jar $dist\server.jar  or java -jar $dist\client.jar"