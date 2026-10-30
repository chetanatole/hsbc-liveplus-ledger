param(
  [string]$JavaHome = $env:JAVA_HOME,
  [string]$OutputDirectory
)
$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
if (!$OutputDirectory) { $OutputDirectory = Join-Path $repoRoot 'build/test' }
$OutputDirectory = [IO.Path]::GetFullPath($OutputDirectory)
New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null
$suffix = if ([IO.Path]::DirectorySeparatorChar -eq '\') { '.exe' } else { '' }
if ($JavaHome) {
  $javac = Join-Path $JavaHome "bin/javac$suffix"
  $java = Join-Path $JavaHome "bin/java$suffix"
} else {
  $javac = (Get-Command "javac$suffix" -ErrorAction Stop).Source
  $java = (Get-Command "java$suffix" -ErrorAction Stop).Source
}
$sources = @(
  (Join-Path $repoRoot 'app/src/main/java/in/local/liveplus/Engine.java'),
  (Join-Path $repoRoot 'tests/EngineTest.java')
)
& $javac -encoding UTF-8 -source 8 -target 8 -Xlint:-options -d $OutputDirectory @sources
if ($LASTEXITCODE -ne 0) { throw "Test compilation failed: $LASTEXITCODE" }
& $java -cp $OutputDirectory EngineTest
if ($LASTEXITCODE -ne 0) { throw "Tests failed: $LASTEXITCODE" }
