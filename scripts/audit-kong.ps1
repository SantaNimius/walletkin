$ErrorActionPreference='Stop'
$env:TEMP='D:\Chainling\tmp';$env:TMP=$env:TEMP
Add-Type -AssemblyName System.Drawing
$root='D:\Chainling\app\src\main\assets\canonical\degen_ape'
$out='D:\Chainling\art\motion-audit'
function Weight([double]$x,[double]$y,[bool]$right){
 $v=[Math]::Clamp(($y-$(if($right){520}else{430}))/650,0,1)
 $h=if($right){[Math]::Clamp(($x-1015)/100,0,1)}else{[Math]::Clamp((535-$x)/150,0,1)}
 return $v*$v*(3-2*$v)*$h
}
function Point($x,$y,$amplitude){
 $l=Weight $x $y $false;$r=Weight $x $y $true
 return [Drawing.PointF]::new(($x+$amplitude*$l-$amplitude*$r),($y+.25*$amplitude*($l+$r)))
}
$art=[Drawing.Bitmap]::new("$root\portrait.png")
$blink=[Drawing.Bitmap]::new("$root\blink.png")
$sheet=[Drawing.Bitmap]::new(1800,330);$sg=[Drawing.Graphics]::FromImage($sheet)
$sg.Clear([Drawing.Color]::FromArgb(14,18,30))
$font=[Drawing.Font]::new('Arial',12)
$states=@('rest','idle-minus','idle-plus','tap-minus','tap-plus','blink')
$amplitudes=@(0,-3,3,-8,8,0)
for($i=0;$i -lt 6;$i++){
 $canvas=[Drawing.Bitmap]::new(1312,1199);$g=[Drawing.Graphics]::FromImage($canvas)
 $g.Clear([Drawing.Color]::FromArgb(14,18,30))
 $g.InterpolationMode=[Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
 if($amplitudes[$i] -eq 0){$g.DrawImage($art,0,0,1312,1199)}else{
 for($row=0;$row -lt 28;$row++){for($col=0;$col -lt 32;$col++){
 $x0=1312*$col/32;$x1=1312*($col+1)/32;$y0=1199*$row/28;$y1=1199*($row+1)/28
 $a=Point $x0 $y0 $amplitudes[$i];$b=Point $x1 $y0 $amplitudes[$i]
 $c=Point $x0 $y1 $amplitudes[$i];$d=Point $x1 $y1 $amplitudes[$i]
 $src=[Drawing.RectangleF]::new(($x0*$art.Width/1312),($y0*$art.Height/1199),(($x1-$x0)*$art.Width/1312),(($y1-$y0)*$art.Height/1199))
 $saved=$g.Save();$path=[Drawing.Drawing2D.GraphicsPath]::new()
 $path.AddPolygon([Drawing.PointF[]]@($a,$b,$c));$g.SetClip($path)
 $g.DrawImage($art,[Drawing.PointF[]]@($a,$b,$c),$src,[Drawing.GraphicsUnit]::Pixel)
 $g.Restore($saved);$path.Dispose()
 $saved=$g.Save();$path=[Drawing.Drawing2D.GraphicsPath]::new()
 $path.AddPolygon([Drawing.PointF[]]@($b,$d,$c));$g.SetClip($path)
 $ghost=[Drawing.PointF]::new(($b.X+$c.X-$d.X),($b.Y+$c.Y-$d.Y))
 $g.DrawImage($art,[Drawing.PointF[]]@($ghost,$b,$c),$src,[Drawing.GraphicsUnit]::Pixel)
 $g.Restore($saved);$path.Dispose()
 }}
 }
 if($i -eq 5){$saved=$g.Save();$g.SetClip([Drawing.RectangleF]::new(795,150,171,68));$g.DrawImage($blink,0,0,1312,1199);$g.Restore($saved)}
 $g.Dispose();$canvas.Save("$out\degen_ape-$($states[$i]).png",[Drawing.Imaging.ImageFormat]::Png)
 $factor=[Math]::Min(284/1312,280/1199)
 $sg.DrawImage($canvas,[Drawing.RectangleF]::new(($i*300+(300-1312*$factor)/2),30,(1312*$factor),(1199*$factor)))
 $sg.DrawString("Degen Kong / $($states[$i])",$font,[Drawing.Brushes]::White,[single]($i*300+5),[single]8)
 $canvas.Dispose()
}
$sg.Dispose();$sheet.Save("$out\degen_ape-sheet.png",[Drawing.Imaging.ImageFormat]::Png)
$sheet.Dispose();$font.Dispose();$art.Dispose();$blink.Dispose()
Write-Output 'Accepted whole portrait: six mesh diagnostic states rendered. Desktop geometry preview, not a Seeker test.'

