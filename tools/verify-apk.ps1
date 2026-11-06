param([Parameter(Mandatory)][string]$Apk,[Parameter(Mandatory)][ValidateSet('notifications','sms')][string]$Edition,[Parameter(Mandatory)][string]$BuildTools)
$ErrorActionPreference='Stop'
$tool=Join-Path $BuildTools 'aapt2.exe'
$permissions=@(& $tool dump permissions $Apk)
if($LASTEXITCODE -ne 0){throw 'Could not inspect APK permissions.'}
$xml=(& $tool dump xmltree $Apk --file AndroidManifest.xml) -join "`n"
if($LASTEXITCODE -ne 0){throw 'Could not inspect APK components.'}
$actual=@([regex]::Matches(($permissions -join "`n"),"uses-permission: name='([^']+)'") | ForEach-Object {$_.Groups[1].Value})
$expected=if($Edition -eq 'sms'){@('android.permission.RECEIVE_SMS')}else{@()}
if((($actual | Sort-Object) -join ',') -cne (($expected | Sort-Object) -join ',')){throw "Unexpected $Edition APK permissions: $($actual -join ', ')"}
if($Edition -eq 'sms'){
 if($xml -match 'AlertListener|BIND_NOTIFICATION_LISTENER_SERVICE|SMS_DELIVER|E: queries'){throw 'SMS APK contains notification capture or app queries.'}
 if($xml -notmatch 'SmsReceiver' -or $xml -notmatch 'android.permission.BROADCAST_SMS' -or $xml -notmatch 'android.provider.Telephony.SMS_RECEIVED'){throw 'SMS receiver must be protected and registered.'}
}else{
 if($xml -match 'SmsReceiver|android.provider.Telephony.SMS_RECEIVED'){throw 'Notifications APK contains an SMS receiver.'}
 if($xml -notmatch 'AlertListener' -or $xml -notmatch 'BIND_NOTIFICATION_LISTENER_SERVICE'){throw 'Notification listener is missing.'}
 if($xml -match 'android.intent.category.LAUNCHER' -and ($xml -split 'E: application')[0] -match 'android.intent.category.LAUNCHER'){throw 'Notification app discovery must not enumerate all launcher apps.'}
}
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive=[IO.Compression.ZipFile]::OpenRead([IO.Path]::GetFullPath($Apk))
try{
 $entry=$archive.GetEntry('classes.dex');$stream=$entry.Open();$memory=[IO.MemoryStream]::new()
 try{$stream.CopyTo($memory);$dex=[Text.Encoding]::ASCII.GetString($memory.ToArray())}finally{$stream.Dispose();$memory.Dispose()}
 $absent=if($Edition -eq 'sms'){'Lin/local/liveplus/AlertListener;'}else{'Lin/local/liveplus/SmsReceiver;'}
 if($dex.Contains($absent)){throw "Wrong edition component in DEX: $absent"}
 if($dex.Contains('Landroid/provider/Telephony$Sms$Inbox;') -or $dex.Contains('content://sms')){throw 'Inbox access found in APK.'}
}finally{$archive.Dispose()}
Write-Output "PASS: $Edition APK permissions, component isolation and no inbox access"