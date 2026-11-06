param([string]$JavaHome=$env:JAVA_HOME,[string]$AndroidJar,[string]$BuildTools)
$ErrorActionPreference='Stop'
$repoRoot=Split-Path -Parent $PSScriptRoot
foreach($edition in @('notifications','sms')) { & "$repoRoot/build.ps1" @PSBoundParameters -Edition $edition }
$checksums=foreach($asset in @('LivePlus-Ledger-Notifications.apk','LivePlus-Ledger-SMS.apk')){
 $hash=(Get-FileHash -LiteralPath (Join-Path $repoRoot "build/$asset") -Algorithm SHA256).Hash.ToLowerInvariant()
 "$hash  $asset"
}
[IO.File]::WriteAllText((Join-Path $repoRoot 'build/SHA256SUMS.txt'),($checksums -join [Environment]::NewLine)+[Environment]::NewLine,[Text.UTF8Encoding]::new($false))