$ErrorActionPreference = 'Stop'
$chainlingRoot = Split-Path $PSScriptRoot -Parent
if (-not $chainlingRoot.StartsWith('D:\',[StringComparison]::OrdinalIgnoreCase)) { throw 'Walletkin must stay on D:' }
foreach ($relative in @('cache', 'cache\home\.android', 'cache\gradle', 'cache\project', 'tmp', 'artifacts')) {
    New-Item -ItemType Directory -Path (Join-Path $chainlingRoot $relative) -Force | Out-Null
}
$env:JAVA_HOME = (Get-ChildItem "$chainlingRoot\toolchain\java" -Directory | Select-Object -First 1).FullName
$env:ANDROID_HOME = "$chainlingRoot\toolchain\android-sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
$env:ANDROID_SDK_HOME = $null
$env:ANDROID_USER_HOME = "$chainlingRoot\cache\home\.android"
$env:ANDROID_EMULATOR_HOME = "$chainlingRoot\cache\android\emulator"
$env:GRADLE_USER_HOME = "$chainlingRoot\cache\gradle"
$env:TEMP = "$chainlingRoot\tmp"
$env:TMP = $env:TEMP
$env:NPM_CONFIG_CACHE = "$chainlingRoot\cache\npm"
$env:JAVA_TOOL_OPTIONS = "-Duser.home=$chainlingRoot/cache/home -Djava.io.tmpdir=$chainlingRoot/tmp"
Set-Location $chainlingRoot
& "$chainlingRoot\scripts\check-art.ps1"
$debugKeystore = "$chainlingRoot\cache\chainling-debug.keystore"
if (-not (Test-Path -LiteralPath $debugKeystore)) {
    & "$env:JAVA_HOME\bin\keytool.exe" -genkeypair -keystore $debugKeystore -storetype JKS -storepass android -keypass android -alias AndroidDebugKey -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Walletkin Local Debug"
    if ($LASTEXITCODE -ne 0) { throw "Could not create D: debug keystore" }
}
foreach ($buildStage in @(':app:testDebugUnitTest', ':app:lintDebug', ':app:assembleDebug')) {
    $stageHeap = switch ($buildStage) { ':app:assembleDebug' { '384m' }; ':app:lintDebug' { '512m' }; default { '640m' } }
    $stageJvmArgs = "-Xms32m -Xmx$stageHeap -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:ReservedCodeCacheSize=64m -XX:ActiveProcessorCount=1 -Dfile.encoding=UTF-8 -Djava.io.tmpdir=$chainlingRoot/tmp"
    & "$chainlingRoot\toolchain\gradle-8.9\bin\gradle.bat" --no-daemon --project-cache-dir "$chainlingRoot\cache\project" "-Dorg.gradle.jvmargs=$stageJvmArgs" $buildStage
    if ($LASTEXITCODE -ne 0) { throw "Android stage failed: $buildStage" }
}
Copy-Item "$chainlingRoot\app\build\outputs\apk\debug\app-debug.apk" "$chainlingRoot\artifacts\walletkin-mvp-debug.apk" -Force
Get-FileHash "$chainlingRoot\artifacts\walletkin-mvp-debug.apk" -Algorithm SHA256





