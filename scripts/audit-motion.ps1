$ErrorActionPreference='Stop'
$env:TEMP='D:\Chainling\tmp';$env:TMP=$env:TEMP
Add-Type -AssemblyName System.Drawing
$root='D:\Chainling\app\src\main\assets\canonical'
$out='D:\Chainling\art\motion-audit'
$custom=Get-Content "$out\custom-rigs.json" -Raw|ConvertFrom-Json
$ids=@('astrafox','dragon','golem','wisp','shark','plant','mech','bird','slime','cosmic','degen_ape','cat','lynx','anomaly/dachshund','anomaly/manta','anomaly/octopus','anomaly/fractal','anomaly/snail')
function Weight($x,$y,$f) {
 $dist=[Math]::Sqrt([Math]::Pow($x-$f.root[0],2)+[Math]::Pow($y-$f.root[1],2))
 $t=[Math]::Clamp(($dist-$f.hold)/($f.reach-$f.hold),0,1)
 return $t*$t*(3-2*$t)
}
function Rect($a) { return [Drawing.RectangleF]::new($a[0],$a[1],$a[2]-$a[0],$a[3]-$a[1]) }
function DrawFrame($g,$b,$f,$strength) {
 $d=$f.destination;$s=$f.source
 $src=@(($s[0]*$b.Width/$f.size[0]),($s[1]*$b.Height/$f.size[1]),($s[2]*$b.Width/$f.size[0]),($s[3]*$b.Height/$f.size[1]))
 $saved=$g.Save()
 if($f.clip){$g.SetClip((Rect $f.clip))}
if($f.cutouts){foreach($cut in $f.cutouts){
  $path=[Drawing.Drawing2D.GraphicsPath]::new();$path.AddEllipse((Rect $cut.rect))
  $m=[Drawing.Drawing2D.Matrix]::new();$center=[Drawing.PointF]::new(($cut.rect[0]+$cut.rect[2])/2,($cut.rect[1]+$cut.rect[3])/2)
  $m.RotateAt($cut.angle,$center);$path.Transform($m);$g.SetClip($path,[Drawing.Drawing2D.CombineMode]::Exclude);$m.Dispose();$path.Dispose()
 }}

 if($f.flex) {
  $bend=$f.flex;$flexStrength=if([Math]::Abs($strength) -gt 1){[Math]::Sign($strength)*2}else{$strength};$wave=$bend.amplitude*$flexStrength
  for($row=0;$row -lt 12;$row++){for($col=0;$col -lt 16;$col++){
   $x0=$d[0]+($d[2]-$d[0])*$col/16;$x1=$d[0]+($d[2]-$d[0])*($col+1)/16
   $y0=$d[1]+($d[3]-$d[1])*$row/12;$y1=$d[1]+($d[3]-$d[1])*($row+1)/12
$y00=$y0+$wave*(Weight $x0 $y0 $bend);$y10=$y0+$wave*(Weight $x1 $y0 $bend)
   $y01=$y1+$wave*(Weight $x0 $y1 $bend);$y11=$y1+$wave*(Weight $x1 $y1 $bend)
   $a=[Drawing.PointF]::new($x0,$y00);$bpt=[Drawing.PointF]::new($x1,$y10);$cpt=[Drawing.PointF]::new($x0,$y01);$dpt=[Drawing.PointF]::new($x1,$y11)
   $sx=$src[0]+($src[2]-$src[0])*$col/16;$sy=$src[1]+($src[3]-$src[1])*$row/12
   $sr=[Drawing.RectangleF]::new($sx,$sy,($src[2]-$src[0])/16,($src[3]-$src[1])/12)
   $ts=$g.Save();$path=[Drawing.Drawing2D.GraphicsPath]::new();$path.AddPolygon([Drawing.PointF[]]@($a,$bpt,$cpt));$g.SetClip($path,[Drawing.Drawing2D.CombineMode]::Intersect)
   $g.DrawImage($b,[Drawing.PointF[]]@($a,$bpt,$cpt),$sr,[Drawing.GraphicsUnit]::Pixel);$g.Restore($ts);$path.Dispose()
   $ts=$g.Save();$path=[Drawing.Drawing2D.GraphicsPath]::new();$path.AddPolygon([Drawing.PointF[]]@($bpt,$dpt,$cpt));$g.SetClip($path,[Drawing.Drawing2D.CombineMode]::Intersect)
   $ghost=[Drawing.PointF]::new(($bpt.X+$cpt.X-$dpt.X),($bpt.Y+$cpt.Y-$dpt.Y))
   $g.DrawImage($b,[Drawing.PointF[]]@($ghost,$bpt,$cpt),$sr,[Drawing.GraphicsUnit]::Pixel);$g.Restore($ts);$path.Dispose()
  }}
 }else{
  $angle=$f.restAngle+$f.motion*$strength
  if($angle){$g.TranslateTransform($f.pivot[0],$f.pivot[1]);$g.RotateTransform($angle);$g.TranslateTransform(-$f.pivot[0],-$f.pivot[1])}
  $g.DrawImage($b,(Rect $d),(Rect $src),[Drawing.GraphicsUnit]::Pixel)
 }
 $g.Restore($saved)
}
$states=@('rest','idle-minus','idle-plus','tap-minus','tap-plus','blink')
$forces=@(0,-1,1,-3,3,0)
$font=[Drawing.Font]::new('Arial',12)
$manifest=@()
foreach($id in $ids){
 if($id -eq 'degen_ape'){continue}
 $rigPath="$root\$id\rig.json"
 $rig=if($custom.$id){$custom.$id}else{Get-Content $rigPath -Raw|ConvertFrom-Json}
 $sheet=[Drawing.Bitmap]::new(1800,330);$sg=[Drawing.Graphics]::FromImage($sheet);$sg.Clear([Drawing.Color]::FromArgb(255,14,18,30))
 $images=@{}
 foreach($f in $rig.layers){if(-not $images.ContainsKey($f.name)){$images[$f.name]=[Drawing.Bitmap]::new("$root\$id\$($f.name).png")}}
 if(Test-Path -LiteralPath "$root\$id\blink.png"){$images['blink']=[Drawing.Bitmap]::new("$root\$id\blink.png")}
 foreach($f in $rig.layers){if($f.blink){$images[$f.blink.name]=[Drawing.Bitmap]::new("$root\$id\$($f.blink.name).png")}}
 $safeId=$id.Replace('/','-')
 for($i=0;$i -lt $states.Count;$i++){
  $canvas=[Drawing.Bitmap]::new([int]$rig.width,[int]$rig.height);$g=[Drawing.Graphics]::FromImage($canvas)
  $g.Clear([Drawing.Color]::FromArgb(255,14,18,30));$g.InterpolationMode=[Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
  $strength=$forces[$i]
  if($rig.tapStrength -and [Math]::Abs($strength) -gt 1){$strength=[Math]::Sign($strength)*$rig.tapStrength}
  if($rig.mode -eq 'squish'){
   $wave=.012*[Math]::Sign($forces[$i]);if($i -in 3,4){$wave+=.055*[Math]::Sign($forces[$i])}
   $g.TranslateTransform($rig.width/2,$rig.height*.86);$g.ScaleTransform((1+$wave),(1-$wave));$g.TranslateTransform(-$rig.width/2,-$rig.height*.86)
  }
  foreach($f in $rig.layers){
   $frame=if($i -eq 5 -and $f.blink){$f.blink}else{$f}
   DrawFrame $g $images[$frame.name] $frame $strength
   if($i -eq 5 -and $f.name -eq 'body' -and $rig.blinkBody -ne $false){
    if($rig.blinkPatches){
     foreach($patch in $rig.blinkPatches){DrawFrame $g $images.blink $patch 0}
    }else{
     $saved=$g.Save();$g.SetClip((Rect $rig.eye));$dest=if($rig.blinkDestination){$rig.blinkDestination}else{$f.destination}
     $g.DrawImage($images.blink,(Rect $dest));$g.Restore($saved)
    }
   }
  }
  $g.Dispose()
  $canvas.Save("$out\$safeId-$($states[$i]).png",[Drawing.Imaging.ImageFormat]::Png)
  $factor=[Math]::Min(284/$rig.width,280/$rig.height)
  $sg.DrawImage($canvas,[Drawing.RectangleF]::new(($i*300+(300-$rig.width*$factor)/2),30,$rig.width*$factor,$rig.height*$factor))
  $sg.DrawString("$($rig.name) / $($states[$i])",$font,[Drawing.Brushes]::White,[single]($i*300+5),[single]8)
  $canvas.Dispose()
 }
 $sg.Dispose();$sheet.Save("$out\$safeId-sheet.png",[Drawing.Imaging.ImageFormat]::Png);$sheet.Dispose()
 foreach($b in $images.Values){$b.Dispose()}
 $manifest += [ordered]@{id=$id;name=$rig.name;states=$states;layers=$rig.layers.Count;review='pending visual inspection';source='runtime PNGs and runtime placement coordinates'}
}
$font.Dispose()
& 'D:\Chainling\scripts\audit-kong.ps1'
$manifest += [ordered]@{id='degen_ape';name='Degen Kong';states=$states;layers=1;review='whole portrait mesh: desktop preview, Seeker pending';source='DegenKongView 32x28 mesh, 3/8 pixel limits'}
$manifest|ConvertTo-Json -Depth 10|Set-Content "$out\manifest.json"
Write-Output ('Rendered '+$ids.Count+' creatures in six diagnostic states.')






