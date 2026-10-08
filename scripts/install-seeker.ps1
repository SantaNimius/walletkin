$ErrorActionPreference = 'Stop'
$chainlingRoot = Split-Path $PSScriptRoot -Parent
if (-not $chainlingRoot.StartsWith('D:\',[StringComparison]::OrdinalIgnoreCase)) { throw 'Project must stay on D:' }
$env:ANDROID_SDK_HOME = "$chainlingRoot\cache\home"
$env:ANDROID_USER_HOME = "$chainlingRoot\cache\home\.android"
$env:ANDROID_PREFS_ROOT = "$chainlingRoot\cache\home\.android"
$env:USERPROFILE = "$chainlingRoot\cache\home"
$env:TEMP = "$chainlingRoot\tmp"
$env:TMP = $env:TEMP
$adb = "$chainlingRoot\toolchain\android-sdk\platform-tools\adb.exe"
$apk = "$chainlingRoot\artifacts\walletkin-mvp-debug.apk"
if (-not (Test-Path -LiteralPath $apk)) { throw 'Build the APK first with scripts/build.ps1' }
$devices = & $adb devices
$ready = @($devices | Where-Object { $_ -match '^\S+\s+device$' })
if ($ready.Count -ne 1) { throw 'Connect exactly one unlocked device and approve USB debugging' }
$model = (& $adb shell getprop ro.product.model).Trim()
Write-Output "Installing Walletkin on $model"
& $adb install -r $apk
if ($LASTEXITCODE -ne 0) { throw 'APK install failed' }
& $adb shell am start -n app.chainling/.MainActivity

