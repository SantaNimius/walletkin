$ErrorActionPreference='Stop'
$env:TEMP='D:\Chainling\tmp';$env:TMP=$env:TEMP
Add-Type -AssemblyName System.Drawing
Add-Type -ReferencedAssemblies System.Drawing.Common,System.Drawing.Primitives,System.Private.Windows.GdiPlus,System.Private.Windows.Core,System.Runtime -TypeDefinition @'
using System;
using System.Drawing;
using System.Drawing.Imaging;
public static class AstraAudit {
 public static void Render(string root,string output,string state){
  using(var baseImage=new Bitmap(root+@"\art\motion-audit\astrafox-rest.png"))
  using(var atlas=new Bitmap(root+@"\art\canonical\astrafox\organic-traits-v3\body-atlas.png"))
  using(var body=new Bitmap(root+@"\app\src\main\assets\canonical\astrafox\body.png"))
  using(var overlay=new Bitmap(1254,1254)){
   if(state=="all" || state=="skr" || state=="active")Patch(overlay,atlas,body,885,425,1080,702,state=="skr"?225:255);
   if(state=="all" || state=="genesis")Patch(overlay,atlas,body,790,202,928,315,255);
   if(state=="all" || state=="jupiter")Patch(overlay,atlas,body,535,530,795,875,255);
   if(state=="all" || state=="nft")Patch(overlay,atlas,body,900,680,1020,835,255);
   using(var g=Graphics.FromImage(baseImage)){g.DrawImage(overlay,0,0,1254,1254);}
   baseImage.Save(output,ImageFormat.Png);
  }
 }
 static void Patch(Bitmap overlay,Bitmap atlas,Bitmap body,int x0,int y0,int x1,int y1,int opacity){
  double cx=(x0+x1)/2.0,cy=(y0+y1)/2.0,rx=(x1-x0)/2.0,ry=(y1-y0)/2.0;
  for(int y=y0;y<y1;y++)for(int x=x0;x<x1;x++){
   double distance=Math.Sqrt(Math.Pow((x-cx)/rx,2)+Math.Pow((y-cy)/ry,2));
   double feather=distance<=.78?1:Math.Max(0,(1-distance)/.22);
   Color p=atlas.GetPixel(x,y),b=body.GetPixel(x,y);
   int alpha=(int)Math.Round(p.A*(b.A/255.0)*feather*opacity/255.0);
   if(alpha>0)overlay.SetPixel(x,y,Color.FromArgb(alpha,p.R,p.G,p.B));
  }
 }
}
'@
$out='D:\Chainling\art\canonical\astrafox\organic-traits-v3'
foreach($state in @('none','skr','active','genesis','jupiter','nft','all')){
 [AstraAudit]::Render('D:\Chainling',"$out\assembled-$state.png",$state)
}
Write-Output 'Seven independent material states rendered from runtime coordinates. Desktop diagnostic, not APK screenshot.'



