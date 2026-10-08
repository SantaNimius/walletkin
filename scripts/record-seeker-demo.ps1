param([ValidateRange(15,180)][int]$Seconds=120)
$ErrorActionPreference='Stop'
$chainlingRoot=Split-Path $PSScriptRoot -Parent
if(-not $chainlingRoot.StartsWith('D:\',[StringComparison]::OrdinalIgnoreCase)){throw 'Keep recordings on D:'}
$env:ANDROID_SDK_HOME="$chainlingRoot\cache\home"
$env:ANDROID_USER_HOME="$chainlingRoot\cache\home\.android"
$env:ANDROID_PREFS_ROOT=$env:ANDROID_USER_HOME
$env:USERPROFILE="$chainlingRoot\cache\home"
$env:TEMP="$chainlingRoot\tmp";$env:TMP=$env:TEMP
$adb="$chainlingRoot\toolchain\android-sdk\platform-tools\adb.exe"
$ready=@((& $adb devices)|Where-Object {$_ -match '^\S+\s+device$'})
if($ready.Count -ne 1){throw 'Connect exactly one unlocked Seeker and approve USB debugging'}
$dir="$chainlingRoot\artifacts\hackathon\recordings"
New-Item -ItemType Directory -Path $dir -Force|Out-Null
$stamp=Get-Date -Format 'yyyyMMdd-HHmmss'
$remote="/sdcard/Download/walletkin-demo-$stamp.mp4"
$local="$dir\walletkin-demo-$stamp.mp4"
& $adb shell am start -n app.chainling/.MainActivity
Write-Output "Recording real device for $Seconds seconds. Follow DEMO-SCRIPT.md. Narration must be added separately."
& $adb shell screenrecord --bit-rate 8000000 --time-limit $Seconds $remote
if($LASTEXITCODE -ne 0){throw 'Device recording failed'}
& $adb pull $remote $local
if($LASTEXITCODE -ne 0 -or -not(Test-Path -LiteralPath $local)){throw 'Could not copy recording to D:'}
Write-Output "Saved: $local"
