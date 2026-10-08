$ErrorActionPreference='Stop'
$env:TEMP='D:\Chainling\tmp';$env:TMP=$env:TEMP
Add-Type -ReferencedAssemblies System.Drawing.Common,System.Drawing.Primitives,System.Private.Windows.GdiPlus,System.Private.Windows.Core,System.Runtime -TypeDefinition @'
using System;
using System.Drawing;
using System.Drawing.Imaging;
public static class OrganicAudit {
 public static Bitmap Open(string path){return new Bitmap(path);}
 public static Bitmap Copy(Bitmap source){var b=new Bitmap(source.Width,source.Height);using(var g=Graphics.FromImage(b)){g.DrawImage(source,0,0);}return b;}
 public static void Patch(Bitmap output,Bitmap atlas,Bitmap master,Bitmap body,double[] rr,double[] source,bool bodyMask,bool basePatch,double[][] exclusions,int alpha){
 int w=body.Width,h=body.Height;var r=new RectangleF((float)(rr[0]*w),(float)(rr[1]*h),(float)((rr[2]-rr[0])*w),(float)((rr[3]-rr[1])*h));
 using(var layer=new Bitmap(w,h)){
 using(var g=Graphics.FromImage(layer)){g.InterpolationMode=System.Drawing.Drawing2D.InterpolationMode.HighQualityBicubic;
 if(source!=null && source.Length==4)g.DrawImage(master,r,new RectangleF((float)(source[0]*master.Width),(float)(source[1]*master.Height),(float)((source[2]-source[0])*master.Width),(float)((source[3]-source[1])*master.Height)),GraphicsUnit.Pixel);
 else g.DrawImage(atlas,new Rectangle(0,0,w,h));}
 for(int y=Math.Max(0,(int)r.Top);y<Math.Min(h,(int)Math.Ceiling(r.Bottom));y++)for(int x=Math.Max(0,(int)r.Left);x<Math.Min(w,(int)Math.Ceiling(r.Right));x++){
 Color p=layer.GetPixel(x,y);double dx=(x-(r.Left+r.Width/2))/(r.Width/2),dy=(y-(r.Top+r.Height/2))/(r.Height/2);
 double dist=Math.Sqrt(dx*dx+dy*dy),fade=Math.Max(0,Math.Min(1,(1-dist)/.18));
 if(bodyMask)fade*=body.GetPixel(x,y).A/255.0;
 if(basePatch && exclusions!=null)foreach(var e in exclusions){double ex=(x-(e[0]+e[2])*w/2)/((e[2]-e[0])*w/2),ey=(y-(e[1]+e[3])*h/2)/((e[3]-e[1])*h/2);fade*=1-Math.Max(0,Math.Min(1,(1-Math.Sqrt(ex*ex+ey*ey))/.18));}
 layer.SetPixel(x,y,Color.FromArgb((int)(p.A*fade*alpha/255.0),p.R,p.G,p.B));}
 using(var g=Graphics.FromImage(output)){g.DrawImage(layer,r,r,GraphicsUnit.Pixel);}
 }
 }
 public static void Save(Bitmap b,string path){b.Save(path,ImageFormat.Png);}
 public static void Board(string[] paths,string output){
 var board=new Bitmap(1200,((paths.Length+2)/3)*440);
 using(var g=Graphics.FromImage(board)){g.Clear(Color.FromArgb(14,18,30));for(int i=0;i<paths.Length;i++)using(var b=new Bitmap(paths[i])){
 float s=Math.Min(380f/b.Width,400f/b.Height);g.DrawImage(b,i%3*400+(400-b.Width*s)/2,i/3*440+20,b.Width*s,b.Height*s);
 g.DrawString(System.IO.Path.GetFileNameWithoutExtension(paths[i]),new Font("Arial",12),Brushes.White,i%3*400+12,i/3*440+418);}}
 Save(board,output);board.Dispose();
 }
}

'@
$root='D:\Chainling\app\src\main\assets\canonical'
$out='D:\Chainling\art\runtime-materials-audit'
New-Item -ItemType Directory -Path $out -Force | Out-Null
$paths=@()
foreach($id in @('dragon','golem','wisp','plant','mech','bird','slime','cosmic','degen_ape','cat','lynx')){
 $base=if($id -eq 'degen_ape'){'portrait'}else{'body'}
 $body=[OrganicAudit]::Open("$root\$id\$base.png")
 $atlas=[OrganicAudit]::Open("$root\$id\organic-atlas.png")
 $master=[OrganicAudit]::Open("$root\$id\organic-master.png")
 $cfg=Get-Content -LiteralPath "$root\$id\materials.json" -Raw | ConvertFrom-Json
 $excluded=[double[][]]@($cfg.regions|Where-Object trait -ne 'base'|ForEach-Object {,[double[]]$_.rect})
 foreach($state in @('none','skr','genesis','jupiter','jup_staking','nft','all')){
  $frame=[OrganicAudit]::Copy($body)
  foreach($r in $cfg.regions){
   if($r.trait -ne 'base' -and $state -ne 'all' -and $r.trait -ne $state){continue}
   $source=if($r.source){[double[]]$r.source}else{[double[]]@()}
   [OrganicAudit]::Patch($frame,$atlas,$master,$body,[double[]]$r.rect,$source,[bool]$r.bodyMask,($r.trait -eq 'base'),$excluded,255)
  }
  $path="$out\$id-$state.png";[OrganicAudit]::Save($frame,$path);$frame.Dispose()
  if($state -eq 'all'){$paths+=$path}
 }
 $body.Dispose();$atlas.Dispose();$master.Dispose()
}
[OrganicAudit]::Board([string[]]$paths,"$out\all-bodies.png")
Write-Output '77 independent body-material diagnostics rendered. These are source-coordinate diagnostics, not Seeker screenshots.'



