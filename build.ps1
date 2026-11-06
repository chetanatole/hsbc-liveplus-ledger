param(
 [string]$JavaHome = $env:JAVA_HOME,
 [string]$AndroidJar,
 [string]$BuildTools,
 [ValidateSet("notifications","sms")][string]$Edition = "notifications",
 [string]$Output
)
$ErrorActionPreference = 'Stop'
function Run-Checked([string]$Program,[string[]]$Arguments) {
 & $Program @Arguments
 if ($LASTEXITCODE -ne 0) { throw "$Program failed with exit code $LASTEXITCODE" }
}
$sdkRoot = if ($env:ANDROID_SDK_ROOT) { $env:ANDROID_SDK_ROOT } else { $env:ANDROID_HOME }
if (!$AndroidJar -and $sdkRoot) { $AndroidJar = Join-Path $sdkRoot 'platforms/android-35/android.jar' }
if (!$BuildTools -and $sdkRoot) { $BuildTools = Join-Path $sdkRoot 'build-tools/35.0.0' }
if (!$JavaHome -or !$AndroidJar -or !$BuildTools) { throw 'Pass build tool paths or set JAVA_HOME and ANDROID_SDK_ROOT / ANDROID_HOME.' }
if (!$Output) { $Output = Join-Path $PSScriptRoot ("build/LivePlus-Ledger-" + $(if ($Edition -eq "sms") { "SMS" } else { "Notifications" }) + ".apk") }
$Output = [IO.Path]::GetFullPath($Output)
foreach ($required in @("$JavaHome/bin/javac.exe", "$JavaHome/bin/java.exe", "$JavaHome/bin/jar.exe", "$JavaHome/bin/keytool.exe", $AndroidJar, "$BuildTools/aapt2.exe", "$BuildTools/zipalign.exe", "$BuildTools/lib/d8.jar", "$BuildTools/lib/apksigner.jar", "$BuildTools/core-lambda-stubs.jar")) {
 if (!(Test-Path -LiteralPath $required -PathType Leaf)) { throw "Required tool missing: $required" }
}
$build = Join-Path "$PSScriptRoot/build" ("run-" + [guid]::NewGuid().ToString('N'))
$main = Join-Path $PSScriptRoot 'app/src/main'
$editionSources = Join-Path $PSScriptRoot "app/src/$Edition/java"
New-Item -ItemType Directory -Force -Path "$build/classes","$build/dex","$build/res","$build/generated","$build/test",(Split-Path -Parent $Output) | Out-Null
Run-Checked "$JavaHome/bin/javac.exe" @('-encoding','UTF-8','-source','8','-target','8','-Xlint:-options','-d',"$build/test","$PSScriptRoot/tools/Compile.java")
Run-Checked "$JavaHome/bin/javac.exe" @('-encoding','UTF-8','-source','8','-target','8','-Xlint:-options','-d',"$build/test","$main/java/in/local/liveplus/Engine.java","$main/java/in/local/liveplus/NotificationCapture.java","$main/java/in/local/liveplus/SmsCapture.java","$PSScriptRoot/tests/EngineTest.java","$PSScriptRoot/tests/NotificationCaptureTest.java","$PSScriptRoot/tests/SmsCaptureTest.java","$main/java/in/local/liveplus/SettingsValues.java","$PSScriptRoot/tests/SettingsValuesTest.java")
Run-Checked "$JavaHome/bin/java.exe" @('-cp',"$build/test",'EngineTest')
# Derive the SMS manifest from the common app/version metadata. No listener or app queries survive.
[xml]$manifest = Get-Content -LiteralPath "$main/AndroidManifest.xml" -Raw
$androidNs = 'http://schemas.android.com/apk/res/android'
if ($Edition -eq 'sms') {
 $manifest.manifest.RemoveChild($manifest.manifest.queries) | Out-Null
 $manifest.manifest.application.RemoveChild($manifest.manifest.application.service) | Out-Null
 $permission = $manifest.CreateElement('uses-permission')
 $permission.SetAttribute('name',$androidNs,'android.permission.RECEIVE_SMS') | Out-Null
 $manifest.manifest.AppendChild($permission) | Out-Null
 $receiver = $manifest.CreateElement('receiver')
 $receiver.SetAttribute('name',$androidNs,'.SmsReceiver') | Out-Null
 $receiver.SetAttribute('permission',$androidNs,'android.permission.BROADCAST_SMS') | Out-Null
 $receiver.SetAttribute('exported',$androidNs,'true') | Out-Null
 $filter = $manifest.CreateElement('intent-filter');$action = $manifest.CreateElement('action')
 $action.SetAttribute('name',$androidNs,'android.provider.Telephony.SMS_RECEIVED') | Out-Null
 $filter.AppendChild($action) | Out-Null;$receiver.AppendChild($filter) | Out-Null
 $manifest.manifest.application.AppendChild($receiver) | Out-Null
}
$generatedManifest = Join-Path $build 'AndroidManifest.xml'
$manifest.Save($generatedManifest)
Run-Checked "$BuildTools/aapt2.exe" @('compile','--dir',"$main/res",'-o',"$build/res.zip")
Run-Checked "$BuildTools/aapt2.exe" @('link','-I',$AndroidJar,'--manifest',$generatedManifest,'-o',"$build/resources.apk",'--java',"$build/generated",'--auto-add-overlay',"$build/res.zip")
$sources = @(Get-ChildItem -LiteralPath "$main/java",$editionSources,"$build/generated" -Filter '*.java' -Recurse | ForEach-Object { $_.FullName })
Run-Checked "$JavaHome/bin/java.exe" (@('-Xmx384m','-cp',"$build/test",'Compile','-encoding','UTF-8','-source','8','-target','8','-bootclasspath',"$AndroidJar;$BuildTools/core-lambda-stubs.jar",'-d',"$build/classes") + $sources)
Run-Checked "$JavaHome/bin/jar.exe" @('cf',"$build/classes.jar",'-C',"$build/classes",'.')
Run-Checked "$JavaHome/bin/java.exe" @('-Xmx512m','-cp',"$BuildTools/lib/d8.jar",'com.android.tools.r8.D8','--lib',$AndroidJar,'--min-api','26','--output',"$build/dex","$build/classes.jar")
Copy-Item -LiteralPath "$build/resources.apk" -Destination "$build/unsigned.apk" -Force
Run-Checked "$JavaHome/bin/jar.exe" @('uf',"$build/unsigned.apk",'-C',"$build/dex",'classes.dex')
Run-Checked "$BuildTools/zipalign.exe" @('-f','4',"$build/unsigned.apk","$build/aligned.apk")
$signing=Join-Path $PSScriptRoot '.signing'
New-Item -ItemType Directory -Force -Path $signing | Out-Null
$key=Join-Path $signing 'personal-release.p12'
$passfile=Join-Path $signing 'key-password.txt'
if (!(Test-Path -LiteralPath $key)) {
 $secret=[guid]::NewGuid().ToString('N')
 [IO.File]::WriteAllText($passfile,$secret)
 Run-Checked "$JavaHome/bin/keytool.exe" @('-genkeypair','-keystore',$key,'-storepass:file',$passfile,'-keypass:file',$passfile,'-alias','liveplus-personal','-keyalg','RSA','-keysize','3072','-validity','10000','-dname','CN=LivePlus Ledger Personal Build','-storetype','PKCS12')
}
if (!(Test-Path -LiteralPath $passfile)) { throw 'Existing signing key has no password file. Restore it privately; do not regenerate the installed app key.' }
Run-Checked "$JavaHome/bin/java.exe" @('-Xmx256m','-jar',"$BuildTools/lib/apksigner.jar",'sign','--ks',$key,'--ks-pass',"file:$passfile",'--ks-key-alias','liveplus-personal','--out',$Output,"$build/aligned.apk")
Run-Checked "$JavaHome/bin/java.exe" @('-jar',"$BuildTools/lib/apksigner.jar",'verify','--verbose','--print-certs',$Output)
Run-Checked "$BuildTools/zipalign.exe" @('-c','4',$Output)
& "$PSScriptRoot/tools/verify-apk.ps1" -Apk $Output -Edition $Edition -BuildTools $BuildTools
Write-Output "APK ready ($Edition): $Output"
