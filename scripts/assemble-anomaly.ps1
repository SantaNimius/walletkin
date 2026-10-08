$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.Drawing
function ReadFrame($id,$name,$destination,$pivot,$motion=0,$phase=0) {
 $p="D:\Chainling\art\canonical\anomaly\$id\$name.png"
 $b=[Drawing.Bitmap]::new($p)
 try {
  $x0=$b.Width;$y0=$b.Height;$x1=0;$y1=0
  for($y=0;$y -lt $b.Height;$y+=8){for($x=0;$x -lt $b.Width;$x+=8){if($b.GetPixel($x,$y).A -gt 150){$x0=[Math]::Min($x0,$x);$y0=[Math]::Min($y0,$y);$x1=[Math]::Max($x1,$x);$y1=[Math]::Max($y1,$y)}}}
  return [ordered]@{name=$name;size=@($b.Width,$b.Height);source=@([Math]::Max(0,$x0-8),[Math]::Max(0,$y0-8),[Math]::Min($b.Width,$x1+16),[Math]::Min($b.Height,$y1+16));destination=$destination;pivot=$pivot;motion=$motion;phase=$phase}
 }finally{$b.Dispose()}
}
function BodyFrame($id,$destination,$pivot) {
 $f=ReadFrame $id 'body' $destination $pivot
 $f.source=@(0,0,$f.size[0],$f.size[1])
 return $f
}
function SaveRig($id,$rig) {
 $dir="D:\Chainling\art\canonical\anomaly\$id"
 [IO.File]::WriteAllText("$dir\rig.json",($rig|ConvertTo-Json -Depth 15))
 $canvas=[Drawing.Bitmap]::new([int]$rig.width,[int]$rig.height)
 $g=[Drawing.Graphics]::FromImage($canvas)
 $g.Clear([Drawing.Color]::FromArgb(255,14,18,30))
 $g.InterpolationMode=[Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
 foreach($f in $rig.layers) {
  $b=[Drawing.Bitmap]::new("$dir\$($f.name).png")
  $d=$f.destination;$s=$f.source
  $state=$g.Save()
  if($f.clip){$c=$f.clip;$g.SetClip([Drawing.RectangleF]::new($c[0],$c[1],$c[2]-$c[0],$c[3]-$c[1]))}
  if($f.restAngle){$g.TranslateTransform($f.pivot[0],$f.pivot[1]);$g.RotateTransform($f.restAngle);$g.TranslateTransform(-$f.pivot[0],-$f.pivot[1])}
  $g.DrawImage($b,[Drawing.RectangleF]::new($d[0],$d[1],$d[2]-$d[0],$d[3]-$d[1]),[Drawing.RectangleF]::new($s[0],$s[1],$s[2]-$s[0],$s[3]-$s[1]),[Drawing.GraphicsUnit]::Pixel)
  $g.Restore($state);$b.Dispose()
 }
 $g.Dispose()
 $canvas.Save("$dir\assembled-preview.png",[Drawing.Imaging.ImageFormat]::Png);$canvas.Dispose()
}
$rig=Get-Content 'D:\Chainling\art\canonical\anomaly\dachshund\rig.json' -Raw | ConvertFrom-Json
SaveRig 'dachshund' $rig
$rig=[ordered]@{name='Celestial Manta';width=1536;height=1024;eye=@(290,465,610,670);mode='hover';blinkDestination=@(250,210,1050,745);layers=@(
 (ReadFrame 'manta' 'tail' @(860,535,1500,990) @(890,555) 2.2 2),
 (BodyFrame 'manta' @(250,210,1050,745) @(600,640)),
 (ReadFrame 'manta' 'wing_left' @(10,0,560,430) @(510,385) 1.4 0),
 (ReadFrame 'manta' 'wing_right' @(745,15,1515,655) @(790,540) 1.4 0.4)
)}
SaveRig 'manta' $rig
$rig=[ordered]@{name='Prismatic Fractal';width=1218;height=1292;eye=@(590,180,735,340);mode='hover';layers=@(
 (ReadFrame 'fractal' 'fragments_left' @(45,260,370,640) @(210,440) 2 0),
 (ReadFrame 'fractal' 'fragments_right' @(875,340,1180,720) @(1020,525) 2 1.2),
 (ReadFrame 'fractal' 'fragments_top' @(150,15,355,255) @(255,150) 2 2.1),
 (ReadFrame 'fractal' 'fragments_bottom' @(875,835,1105,1250) @(985,1050) 2 3.0),
 (BodyFrame 'fractal' @(0,0,1218,1292) @(610,650))
)}
SaveRig 'fractal' $rig
$l=ReadFrame 'snail' 'stalk_left' @(1200,190,1315,475) @(1260,465) 1.2 0
$l.blink=ReadFrame 'snail' 'stalk_left_blink' $l.destination $l.pivot
$r=ReadFrame 'snail' 'stalk_right' @(1285,130,1465,480) @(1370,470) 1.3 1.7
$r.blink=ReadFrame 'snail' 'stalk_right_blink' $r.destination $r.pivot
$rig=[ordered]@{name='Galactic Snail';width=1536;height=1024;eye=@(0,0,0,0);mode='glimmer';blinkBody=$false;layers=@(
 (BodyFrame 'snail' @(0,0,1536,1024) @(768,960)),$l,$r)}
SaveRig 'snail' $rig

$destinations=@(@(160,20,650,660),@(15,325,645,745),@(30,630,650,970),@(315,600,690,1180),@(740,85,1260,700),@(750,485,1300,850),@(720,680,1150,1180),@(655,610,1020,1180))
$pivots=@(@(610,610),@(610,635),@(610,665),@(650,665),@(780,620),@(790,650),@(770,695),@(750,695))
$layers=@()
for($i=0;$i -lt 8;$i++){$layers += ReadFrame 'octopus' ('tentacle_'+($i+1).ToString('00')) $destinations[$i] $pivots[$i] 2.2 ($i*0.65)}
$layers += BodyFrame 'octopus' @(290,-10,1070,705) @(650,650)
$rig=[ordered]@{name='Psychedelic Octopus';width=1312;height=1199;eye=@(470,365,840,510);mode='squish';blinkDestination=@(290,-10,1070,705);layers=$layers}
SaveRig 'octopus' $rig
