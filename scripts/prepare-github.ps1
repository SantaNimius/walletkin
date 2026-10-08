$ErrorActionPreference='Stop'
$root='D:\Chainling'
$dest='D:\Chainling\work\github-ready\walletkin'
New-Item -ItemType Directory -Path $dest -Force | Out-Null
foreach($relative in @('app\src','docs','scripts')){
 $target=Join-Path $dest $relative
 New-Item -ItemType Directory -Path (Split-Path $target -Parent) -Force | Out-Null
 New-Item -ItemType Directory -Path $target -Force | Out-Null
 Get-ChildItem -LiteralPath (Join-Path $root $relative) -Recurse -File | ForEach-Object {
  $itemTarget=Join-Path $dest $_.FullName.Substring($root.Length+1)
  New-Item -ItemType Directory -Path (Split-Path $itemTarget -Parent) -Force | Out-Null
  Copy-Item -LiteralPath $_.FullName -Destination $itemTarget -Force
 }
}
New-Item -ItemType Directory -Path "$dest\backend" -Force | Out-Null
Get-ChildItem -LiteralPath "$root\backend" -File | Where-Object {$_.Extension -in @('.mjs','.json','.md') -and $_.Name -ne 'ledger.json'} | ForEach-Object {Copy-Item -LiteralPath $_.FullName -Destination "$dest\backend" -Force}
foreach($relative in @('README.md','AGENTS.md','.gitignore','build.gradle','settings.gradle','gradle.properties','app\build.gradle','art\motion-audit\custom-rigs.json','art\trait-audit\anchors.json')){
 $target=Join-Path $dest $relative
 New-Item -ItemType Directory -Path (Split-Path $target -Parent) -Force | Out-Null
 Copy-Item -LiteralPath (Join-Path $root $relative) -Destination $target -Force
}
Get-ChildItem -LiteralPath "$root\art\canonical" -Recurse -File -Filter '*PROMPT.md' | ForEach-Object {
 $target=Join-Path $dest $_.FullName.Substring($root.Length+1)
 New-Item -ItemType Directory -Path (Split-Path $target -Parent) -Force | Out-Null
 Copy-Item -LiteralPath $_.FullName -Destination $target -Force
}
New-Item -ItemType Directory -Path "$dest\docs\hackathon" -Force | Out-Null
Copy-Item -LiteralPath "$root\artifacts\hackathon\SUBMISSION-EN.md" -Destination "$dest\docs\hackathon\SUBMISSION-EN.md" -Force
git -C $dest init -b main
if($LASTEXITCODE -ne 0){throw 'git init failed'}
git -C $dest add .
if($LASTEXITCODE -ne 0){throw 'git stage failed'}
$tracked=@(git -C $dest ls-files)
if($tracked -match '(^|/)(cache|toolchain|tmp|build|node_modules|data)/|\.keystore$|\.jks$|local.properties$|\.mp4$|\.apk$|(^|/)\.env'){throw 'Forbidden path in prepared repository'}
$files=$tracked | ForEach-Object {Get-Item -LiteralPath (Join-Path $dest $_)}
$large=@($files | Where-Object Length -gt 100MB)
if($large.Count){throw 'GitHub file size limit exceeded'}
Write-Output ('Prepared files: '+$files.Count)
Write-Output ('Total MiB: '+[Math]::Round(($files | Measure-Object Length -Sum).Sum/1MB,2))
Add-Type -AssemblyName System.IO.Compression
$zipPath="$root\artifacts\hackathon\walletkin-source-0.4.1.zip"
$stream=[IO.File]::Open($zipPath,[IO.FileMode]::Create)
$zip=[IO.Compression.ZipArchive]::new($stream,[IO.Compression.ZipArchiveMode]::Create)
try {
 foreach($f in $files){
  $rel=$f.FullName.Substring($dest.Length+1).Replace('\','/')
  $entry=$zip.CreateEntry('walletkin/'+$rel,[IO.Compression.CompressionLevel]::Optimal)
  $inputStream=[IO.File]::OpenRead($f.FullName); $outputStream=$entry.Open()
  try{$inputStream.CopyTo($outputStream)}finally{$inputStream.Dispose();$outputStream.Dispose()}
 }
} finally {$zip.Dispose();$stream.Dispose()}
$verify=[IO.Compression.ZipFile]::OpenRead($zipPath)
try{if($verify.Entries.Count -ne $files.Count){throw 'Source archive verification failed'};Write-Output ('ZIP verified: '+$verify.Entries.Count+' files')}finally{$verify.Dispose()}
