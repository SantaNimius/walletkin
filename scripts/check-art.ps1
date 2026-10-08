$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
if(-not $root.StartsWith('D:\',[StringComparison]::OrdinalIgnoreCase)){throw 'Keep project on D:'}
Add-Type -AssemblyName System.Drawing
$base=Join-Path $root 'app\src\main\assets\canonical'
$j=Get-Content -LiteralPath (Join-Path $root 'app\src\main\assets\legends.json') -Raw|ConvertFrom-Json
if($j.entries.Count -ne 14){throw 'Expected 13 lineages and one Anomaly'}
$rows=foreach($entry in $j.entries){
 $id=if($entry.id -eq 'fox'){'astrafox'}else{$entry.id}
 $p=Join-Path $base ($id+'\portrait.png')
 $b=[Drawing.Bitmap]::new($p)
 try{
  if($b.Width -lt 768 -or $b.Height -lt 768){throw "Low-resolution portrait: $id"}
  if($b.GetPixel(0,0).A -gt 8){throw "Nontransparent corner: $id"}
  [pscustomobject]@{Species=$entry.name;Id=$entry.id;Width=$b.Width;Height=$b.Height;SHA256=(Get-FileHash -LiteralPath $p -Algorithm SHA256).Hash}
 }finally{$b.Dispose()}
}
if(@($rows.SHA256|Select-Object -Unique).Count -ne 14){throw 'Canonical appearances must be distinct'}
New-Item -ItemType Directory -Path (Join-Path $root 'art') -Force|Out-Null
$rows|Export-Csv -LiteralPath (Join-Path $root 'art\CANONICAL-MANIFEST.csv') -NoTypeInformation -Encoding UTF8
$rows|Format-Table Species,Width,Height
