$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.Drawing
$root='D:\Chainling';$out="$root\art\trait-audit"
New-Item -ItemType Directory -Path $out -Force|Out-Null
$source=Get-Content -LiteralPath "$root\app\src\main\java\app\chainling\CreatureTraits.kt" -Raw
$custom=Get-Content "$root\art\motion-audit\custom-rigs.json" -Raw|ConvertFrom-Json
$sheet=[Drawing.Bitmap]::new(1200,1400);$sg=[Drawing.Graphics]::FromImage($sheet);$sg.Clear([Drawing.Color]::FromArgb(255,14,18,30))
$font=[Drawing.Font]::new('Arial',12)
$genesis=[Drawing.Bitmap]::new("$root\app\src\main\assets\canonical\protocol\genesis.png")
$jupiter=[Drawing.Bitmap]::new("$root\app\src\main\assets\canonical\protocol\jupiter.png")
$records=@();$index=0
foreach($match in [regex]::Matches($source,'"([a-z_]+)" to rig\(([^)]+)\)')){
 $id=$match.Groups[1].Value;$args=$match.Groups[2].Value.Split(',')|ForEach-Object {[float]$_.Trim().TrimEnd('f')}
 $asset=if($id -eq 'fox'){'astrafox'}else{$id}
 $rig=if($custom.$asset){$custom.$asset}else{Get-Content "$root\app\src\main\assets\canonical\$asset\rig.json" -Raw|ConvertFrom-Json}
 $body=$rig.layers|Where-Object name -eq 'body'|Select-Object -First 1
 $b=[Drawing.Bitmap]::new("$root\app\src\main\assets\canonical\$asset\body.png")
 $alphas=@()
 for($i=3;$i -lt 11;$i+=2){
  $sx=($body.source[0]+($args[$i]-$body.destination[0])/($body.destination[2]-$body.destination[0])*($body.source[2]-$body.source[0]))*$b.Width/$body.size[0]
  $sy=($body.source[1]+($args[$i+1]-$body.destination[1])/($body.destination[3]-$body.destination[1])*($body.source[3]-$body.source[1]))*$b.Height/$body.size[1]
  $alpha=$b.GetPixel([int]$sx,[int]$sy).A
  if($alpha -lt 150){throw "Trait anchor outside opaque body: $id coordinate $i alpha=$alpha"}
  $alphas+=$alpha
 }
 $b.Dispose()
 $canvas=[Drawing.Bitmap]::new("$root\art\motion-audit\$asset-rest.png")
 $g=[Drawing.Graphics]::FromImage($canvas);$g.SmoothingMode=[Drawing.Drawing2D.SmoothingMode]::AntiAlias
 $r=$args[0]*$(if($id -in @('wisp','bird')){.019}else{.024});$x=$args[3];$y=$args[4]
 $pen=[Drawing.Pen]::new([Drawing.Color]::FromArgb(180,179,249,142),[single]($r*.13))
 for($i=-1;$i -le 1;$i++){
  $p=[Drawing.Drawing2D.GraphicsPath]::new();$p.AddBezier([single]($x-$r*2.4),[single]($y+$i*$r*.7),[single]($x-$r),[single]($y-$r*1.7+$i*$r*.4),[single]($x+$r),[single]($y+$r*1.5+$i*$r*.4),[single]($x+$r*2.4),[single]($y+$i*$r*.7));$g.DrawPath($pen,$p);$p.Dispose()
 }
 $brush=[Drawing.SolidBrush]::new([Drawing.Color]::FromArgb(206,214,255,145));$g.FillEllipse($brush,[single]($x-$r*.65),[single]($y-$r),[single]($r*1.3),[single]($r*2));$brush.Dispose()
 $x=$args[5];$y=$args[6];$rr=$r*.75;$saved=$g.Save();$p=[Drawing.Drawing2D.GraphicsPath]::new();$p.AddEllipse([single]($x-$rr),[single]($y-$rr),[single]($rr*2),[single]($rr*2));$g.SetClip($p);$g.DrawImage($genesis,[Drawing.RectangleF]::new($x-$rr,$y-$rr,$rr*2,$rr*2));$g.Restore($saved);$p.Dispose()
 $x=$args[7];$y=$args[8];$pen.Color=[Drawing.Color]::FromArgb(185,155,230,188)
 for($i=-1;$i -le 1;$i++){
  $p=[Drawing.Drawing2D.GraphicsPath]::new();$p.AddBezier([single]($x-$r*2.4),[single]($y+$i*$r*.7),[single]($x-$r),[single]($y-$r*1.2+$i*$r*.5),[single]($x+$r),[single]($y+$r*1.2+$i*$r*.5),[single]($x+$r*2.4),[single]($y+$i*$r*.7));$g.DrawPath($pen,$p);$p.Dispose()
 }
 $attributes=[Drawing.Imaging.ImageAttributes]::new()
$matrix=[Drawing.Imaging.ColorMatrix]::new()
$matrix.Matrix03=.333;$matrix.Matrix13=.333;$matrix.Matrix23=.333;$matrix.Matrix33=0
$attributes.SetColorMatrix($matrix)
$rect=[Drawing.Rectangle]::new([int]($x-$r*.8),[int]($y-$r*.8),[int]($r*1.6),[int]($r*1.6))
$g.DrawImage($jupiter,$rect,0,0,$jupiter.Width,$jupiter.Height,[Drawing.GraphicsUnit]::Pixel,$attributes);$attributes.Dispose()
$pen.Dispose();$x=$args[9];$y=$args[10]
 foreach($gem in @(@(-.63,.18,.42),@(.65,.27,.38),@(0,0,.87))){
  $gx=$x+$r*$gem[0];$gy=$y+$r*$gem[1];$gr=$r*$gem[2]
  $poly=[Drawing.PointF[]]@([Drawing.PointF]::new($gx,$gy-$gr),[Drawing.PointF]::new($gx+$gr*.64,$gy-$gr*.2),[Drawing.PointF]::new($gx+$gr*.45,$gy+$gr*.7),[Drawing.PointF]::new($gx,$gy+$gr),[Drawing.PointF]::new($gx-$gr*.45,$gy+$gr*.7),[Drawing.PointF]::new($gx-$gr*.64,$gy-$gr*.2))
  $p=[Drawing.Drawing2D.GraphicsPath]::new();$p.AddPolygon($poly)
  $brush=[Drawing.Drawing2D.LinearGradientBrush]::new([Drawing.PointF]::new($gx-$gr,$gy-$gr),[Drawing.PointF]::new($gx+$gr,$gy+$gr),[Drawing.Color]::FromArgb(242,224,201,255),[Drawing.Color]::FromArgb(233,89,64,171))
  $g.FillPath($brush,$p);$brush.Dispose()
  $pen=[Drawing.Pen]::new([Drawing.Color]::FromArgb(221,232,255,244),[single]($gr*.055))
  $g.DrawPath($pen,$p);$g.DrawLine($pen,[single]$gx,[single]($gy-$gr),[single]$gx,[single]($gy+$gr))
  $pen.Dispose();$p.Dispose()
 }
 $g.Dispose()
 $canvas.Save("$out\$id-five-traits.png",[Drawing.Imaging.ImageFormat]::Png)
 $col=$index%4;$row=[Math]::Floor($index/4);$factor=[Math]::Min(280/$canvas.Width,290/$canvas.Height)
 $sg.DrawString("$id / five traits",$font,[Drawing.Brushes]::White,[single]($col*300+10),[single]($row*350+5))
 $sg.DrawImage($canvas,[Drawing.RectangleF]::new($col*300+(300-$canvas.Width*$factor)/2,$row*350+30,$canvas.Width*$factor,$canvas.Height*$factor));$canvas.Dispose()
 $records+=[ordered]@{id=$id;anchorAlpha=$alphas;note='Illustrative placement preview with enlarged material traits; not Android screenshot. Glow and SRC_ATOP compositing are Android runtime effects. Android uses creature alpha compositing and Jupiter brightness-alpha ink.'};$index++
}
$sg.Dispose();$sheet.Save("$out\all-13.png",[Drawing.Imaging.ImageFormat]::Png);$sheet.Dispose();$font.Dispose();$genesis.Dispose();$jupiter.Dispose()
$records|ConvertTo-Json -Depth 6|Set-Content "$out\anchors.json"
Write-Output ("Verified all four anchor sites on "+$index+" bodies; five visual traits share the core/active channel.")


