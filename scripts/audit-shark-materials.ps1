$ErrorActionPreference='Stop'
$env:TEMP='D:\Chainling\tmp';$env:TMP=$env:TEMP
Add-Type -AssemblyName System.Drawing
Add-Type -ReferencedAssemblies System.Drawing.Common,System.Drawing.Primitives,System.Private.Windows.GdiPlus,System.Private.Windows.Core,System.Runtime -TypeDefinition @'
using System;
using System.Drawing;
using System.Drawing.Imaging;
public static class SharkAudit {
 public static void Render(string root,string output,string state){
  using(var baseImage=BaseFrame(root))
  using(var rawAtlas=new Bitmap(root+@"\art\canonical\shark\organic-traits-v3\body-atlas-tail-genesis.png"))
  using(var atlas=new Bitmap(1536,1024))
  using(var body=new Bitmap(root+@"\app\src\main\assets\canonical\shark\body.png"))
  using(var overlay=new Bitmap(1536,1024)){
   using(var register=Graphics.FromImage(atlas)){register.DrawImage(rawAtlas,new RectangleF(21,-10,1424,967));}
   if(state=="all" || state=="skr" || state=="active")Patch(overlay,atlas,body,715,330,975,650,state=="skr"?225:255);
   if(state=="all" || state=="genesis")Patch(overlay,atlas,body,1175,475,1325,620,255);
   if(state=="all" || state=="jupiter")Patch(overlay,atlas,body,500,335,750,720,255);
   if(state=="all" || state=="nft")Patch(overlay,atlas,body,940,480,1120,675,255);
using(var layer=new Bitmap(1536,1024)){
    using(var lg=Graphics.FromImage(layer)){lg.DrawImage(body,new RectangleF(0,25,1382,922));lg.DrawImage(overlay,new RectangleF(0,25,1382,922));}
    for(int y=25;y<947;y++)for(int x=1230;x<1382;x++){
      Color pixel=layer.GetPixel(x,y);double fade=Math.Max(0,Math.Min(1,(1285-x)/55.0));
      layer.SetPixel(x,y,Color.FromArgb((int)(pixel.A*fade),pixel.R,pixel.G,pixel.B));
    }
    using(var g=Graphics.FromImage(baseImage)){g.DrawImage(layer,0,0);}
   }
   baseImage.Save(output,ImageFormat.Png);
  }
 }
 static Bitmap BaseFrame(string root){
  var output=new Bitmap(1536,1024);
  using(var g=Graphics.FromImage(output)){
   g.Clear(Color.FromArgb(14,18,30));
   g.InterpolationMode=System.Drawing.Drawing2D.InterpolationMode.HighQualityBicubic;
   using(var tail=new Bitmap(root+@"\app\src\main\assets\canonical\shark\tail.png"))
    g.DrawImage(tail,new RectangleF(1130,180,390,590),new RectangleF(344,20,1000,984),GraphicsUnit.Pixel);
   using(var far=new Bitmap(root+@"\app\src\main\assets\canonical\shark\fin_far.png"))
    g.DrawImage(far,new RectangleF(205,520,285,375),new RectangleF(300,100,936,860),GraphicsUnit.Pixel);
   using(var near=new Bitmap(root+@"\app\src\main\assets\canonical\shark\fin_near.png"))
    g.DrawImage(near,new RectangleF(570,560,460,370),new RectangleF(450,310,1000,686),GraphicsUnit.Pixel);
  }
  return output;
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
$out='D:\Chainling\art\canonical\shark\organic-traits-v3'
foreach($state in @('none','skr','active','genesis','jupiter','nft','all')){
 [SharkAudit]::Render('D:\Chainling',"$out\assembled-$state.png",$state)
}
Write-Output 'Seven independent material states rendered from runtime coordinates. Desktop diagnostic, not APK screenshot.'

