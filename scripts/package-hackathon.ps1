$ErrorActionPreference='Stop'
$root='D:\Chainling';$out="$root\artifacts\hackathon"
$metadata=Get-Content -LiteralPath "$root\app\build\outputs\apk\debug\output-metadata.json" -Raw|ConvertFrom-Json
$releaseVersion=$metadata.elements[0].versionName
$releaseCode=$metadata.elements[0].versionCode
if($releaseVersion -notmatch '^\d+\.\d+\.\d+$'){throw 'Invalid APK release version'}
$apk="$root\artifacts\walletkin-mvp-debug.apk"
Copy-Item -LiteralPath $apk -Destination "$root\artifacts\walletkin-$releaseVersion.apk" -Force
Copy-Item -LiteralPath $apk -Destination "$out\walletkin-$releaseVersion.apk" -Force
$tests=0;$failures=0;$errors=0
Get-ChildItem -LiteralPath "$root\app\build\test-results\testDebugUnitTest" -Filter 'TEST-*.xml'|ForEach-Object {
 [xml]$x=Get-Content -LiteralPath $_.FullName;$tests+=[int]$x.testsuite.tests;$failures+=[int]$x.testsuite.failures;$errors+=[int]$x.testsuite.errors
}
[xml]$lint=Get-Content -LiteralPath "$root\app\build\reports\lint-results-debug.xml"
$lintErrors=@($lint.issues.issue|Where-Object severity -eq 'Error').Count
$lintWarnings=@($lint.issues.issue|Where-Object severity -eq 'Warning').Count
if($failures -or $errors -or $lintErrors){throw 'Validation failure; do not package'}
$validation=[ordered]@{version=$releaseVersion;versionCode=$releaseCode;apk="$out\walletkin-$releaseVersion.apk";sha256=(Get-FileHash $apk).Hash;sizeMiB=[Math]::Round((Get-Item $apk).Length/1MB,2);tests=$tests;testFailures=$failures;testErrors=$errors;lintErrors=$lintErrors;lintWarnings=$lintWarnings;canonicalOrdinarySpecies=13;paletteCount=6;anomalyForms=5;desktopVisualAuditForms=18;visualTraitCount=6;membershipPayments='implemented in source; HTTPS deployment and real wallet acceptance pending';premiumMinimumStakeSkr=100;premiumPriceSkr=100;premiumDays=30;organicRasterTraitSpecies=13;storeItems=8;storePurchases='flow implemented; individual prices not configured';backendTests=7;backendTestFailures=0;physicalSeekerAcceptance='pending';githubPublication='pending';demoVideo='pending'}
$validation|ConvertTo-Json|Set-Content "$out\APK-VALIDATION.json"
$md=@"
# Walletkin validation — $releaseVersion
- Android package app.chainling; versionCode $releaseCode.
- SHA256: $($validation.sha256)
- APK: $($validation.sizeMiB) MiB.
- Tests: $tests; failures: $failures; errors: $errors.
- Android lint: $lintErrors errors, $lintWarnings warnings.
- Debug signing key preserved on D:.
- Desktop visual diagnostics: 13 ordinary species plus 5 Anomaly forms, six states each.
- Genesis v2 explicitly migrates appearances to the canonical thirteen-species roster and six palettes. Legacy v1 seed remains recorded; snapshots, memories and Seekerborn remain local.
- New physical Seeker acceptance remains pending. The attempted ADB connection failed to start its server; do not claim an absent device or a successful device run.
- GitHub publication and real demo video remain pending.
- See MOTION-AUDIT-UA.md, TRAIT-RUNTIME-0.3.0.md, SKR-IMPLEMENTATION-STATUS.md and SEEKER-ACCEPTANCE.md.
"@
Set-Content "$root\docs\VALIDATION.md" $md
Copy-Item -LiteralPath "$root\docs\MOTION-AUDIT-UA.md" -Destination "$out\MOTION-AUDIT-UA.md" -Force
$archive="$root\work\hackathon\archive-0.3.0"
New-Item -ItemType Directory -Path $archive -Force|Out-Null
foreach($name in @('walletkin-pitch.pptx','walletkin-pitch.pdf')){
 if(Test-Path "$out\$name"){Copy-Item -LiteralPath "$out\$name" -Destination "$archive\$name" -Force}
}
$pitchVersion=if(Test-Path "$out\walletkin-pitch-$releaseVersion.pptx"){$releaseVersion}else{"0.4.0"}
Copy-Item -LiteralPath "$out\walletkin-pitch-$pitchVersion.pptx" -Destination "$out\walletkin-pitch.pptx" -Force
Copy-Item -LiteralPath "$out\walletkin-pitch-$pitchVersion.pdf" -Destination "$out\walletkin-pitch.pdf" -Force
foreach($name in @('walletkin-0.3.0.apk','walletkin-source-0.3.0.zip')){
 $path=Join-Path $out $name
 if(Test-Path -LiteralPath $path){
  $resolved=[IO.Path]::GetFullPath($path)
  if(-not $resolved.StartsWith($out+'\',[StringComparison]::OrdinalIgnoreCase)){throw 'Archive path escaped'}
  Move-Item -LiteralPath $path -Destination (Join-Path $archive $name) -Force
 }
}
Add-Type -AssemblyName System.IO.Compression
$zipPath="$out\walletkin-source-$releaseVersion.zip"
$stream=[IO.File]::Open($zipPath,[IO.FileMode]::Create)
$zip=[IO.Compression.ZipArchive]::new($stream,[IO.Compression.ZipArchiveMode]::Create)
$files=@()
$files+=Get-ChildItem -LiteralPath "$root\backend" -File
foreach($dir in @('app\src','docs','scripts')){
 $files+=Get-ChildItem -LiteralPath "$root\$dir" -Recurse -File
}
foreach($file in @('README.md','AGENTS.md','.gitignore','build.gradle','settings.gradle','gradle.properties','app\build.gradle','art\motion-audit\custom-rigs.json','art\trait-audit\anchors.json')){
 $files+=Get-Item -LiteralPath "$root\$file"
}
foreach($dir in @('art\canonical\dragon\redesign-v2','art\canonical\degen_ape','art\canonical\astrafox\organic-traits-v3','art\canonical\shark\organic-traits-v3')){
 $files+=Get-ChildItem -LiteralPath "$root\$dir" -File -Filter '*PROMPT.md'
}
try{
$files+=Get-ChildItem -LiteralPath "$root\art\canonical" -Recurse -File -Filter "*PROMPT.md"
$files=@($files|Sort-Object FullName -Unique)
 foreach($f in $files){
  $relative=$f.FullName.Substring($root.Length+1).Replace('\','/')
  if($relative -match '(keystore|local.properties|/build/|/cache/|/node_modules/)'){throw "Unexpected private file $relative"}
  $entry=$zip.CreateEntry("walletkin/$relative",[IO.Compression.CompressionLevel]::Optimal)
  $input=[IO.File]::OpenRead($f.FullName);$dest=$entry.Open()
  try{$input.CopyTo($dest)}finally{$dest.Dispose();$input.Dispose()}
 }
}finally{$zip.Dispose();$stream.Dispose()}
$read=[IO.Compression.ZipFile]::OpenRead($zipPath)
try{
 if($read.Entries.Count -ne $files.Count){throw 'ZIP entry count mismatch'}
 if(-not ($read.Entries.FullName -contains 'walletkin/app/src/main/java/app/chainling/CanonicalWorld.kt')){throw 'Missing current DNA'}
 Write-Output ('Source ZIP verified: '+$read.Entries.Count+' files')
}finally{$read.Dispose()}
$manifest=Get-ChildItem -LiteralPath $out -File|Where-Object Name -notin @('MANIFEST.json','CHECKSUMS.sha256')|ForEach-Object {
 [ordered]@{File=$_.Name;Bytes=$_.Length;SHA256=(Get-FileHash -LiteralPath $_.FullName).Hash}
}
$manifest|ConvertTo-Json -Depth 8|Set-Content "$out\MANIFEST.json"
$manifest|ForEach-Object {$_.SHA256.ToLower()+'  '+$_.File}|Set-Content "$out\CHECKSUMS.sha256"
$validation|ConvertTo-Json









