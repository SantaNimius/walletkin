package app.chainling
import android.content.Context
import android.graphics.*
import kotlin.math.min
import kotlin.math.sin
/** Surface-bound organic inlays, with species-specific scale and material treatment. */
class CreatureTraits(context:Context) {
    private val material=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply{xfermode=PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)}
    private val path=Path()
    private val icons=CanonicalSpriteCache.get(context,"protocol",listOf("genesis","jupiter"))
    private val jupiterInk=ColorMatrixColorFilter(ColorMatrix(floatArrayOf(1f,0f,0f,0f,0f,0f,1f,0f,0f,0f,0f,0f,1f,0f,0f,.333f,.333f,.333f,0f,0f)))
    private fun vein(c:Canvas,x:Float,y:Float,r:Float,color:Int,scale:Float=1f){
        material.shader=null;material.style=Paint.Style.STROKE;material.strokeWidth=r*.13f;material.color=color
        for(i in -1..1){
            path.reset();path.moveTo(x-r*2.4f*scale,y+i*r*.65f)
            path.cubicTo(x-r,y-r*1.6f+i*r*.45f,x+r,y+r*1.4f+i*r*.45f,x+r*2.4f*scale,y+i*r*.65f)
            c.drawPath(path,material)
        }
        material.style=Paint.Style.FILL
    }
    private fun crystal(c:Canvas,x:Float,y:Float,r:Float,top:Int,bottom:Int){
        path.reset();path.moveTo(x,y-r);path.lineTo(x+r*.64f,y-r*.2f);path.lineTo(x+r*.45f,y+r*.7f)
        path.lineTo(x,y+r);path.lineTo(x-r*.45f,y+r*.7f);path.lineTo(x-r*.64f,y-r*.2f);path.close()
        material.style=Paint.Style.FILL
        material.shader=LinearGradient(x-r,y-r,x+r,y+r,top,bottom,Shader.TileMode.CLAMP)
        c.drawPath(path,material);material.shader=null
        material.style=Paint.Style.STROKE;material.strokeWidth=r*.055f;material.color=0xDDE8FFF4.toInt()
        c.drawPath(path,material)
        path.reset();path.moveTo(x-r*.64f,y-r*.2f);path.lineTo(x,y+r);path.lineTo(x+r*.64f,y-r*.2f)
        path.moveTo(x,y-r);path.lineTo(x,y+r);c.drawPath(path,material)
        material.style=Paint.Style.FILL
    }
    fun draw(c:Canvas,p:Profile,w:Float,h:Float,time:Double,preview:Boolean=false){
        val a=TraitAnatomy.forSpecies(p.dna.species.id)
        val scale=min(w/a.width,h/a.height)*a.scale
        c.save();c.translate(w/2,h/2);c.scale(scale,scale);c.translate(-a.width/2,-a.height/2)
        val active=p.traits.filter{it.enabled}.map{it.id}.toSet()
        val id=p.dna.species.id
        val r=a.width*(if(id in setOf("wisp","bird")) .019f else .024f)
        val organic=id in setOf("fox","plant","cat","shark")
        if(p.resonance.earned || preview){
            val x=a.core.x;val y=a.core.y
            val awake=p.resonance.active || preview
            val pulse=(.82+.18*sin(time*2)).toFloat()
            material.color=if(awake)Color.argb((100*pulse).toInt(),120,255,174) else 0x507E9F77
            material.shader=RadialGradient(x,y,r*2.3f,intArrayOf(material.color,Color.TRANSPARENT),null,Shader.TileMode.CLAMP)
            c.drawCircle(x,y,r*2.3f,material);material.shader=null
            vein(c,x,y,r,if(awake)0xB4B3F98E.toInt() else 0xA78BAD79.toInt(),if(organic)1.25f else .9f)
            if(organic){
                material.shader=RadialGradient(x-r*.2f,y-r*.3f,r,intArrayOf(0xF2F3FFCC.toInt(),if(awake)0xF058DFA1.toInt() else 0xDC7C9B76.toInt(),0xC51B5845.toInt()),floatArrayOf(0f,.4f,1f),Shader.TileMode.CLAMP)
                c.drawOval(x-r*.65f,y-r,x+r*.65f,y+r,material);material.shader=null
                material.style=Paint.Style.STROKE;material.color=0xCBD6FFB0.toInt();material.strokeWidth=r*.065f
                c.drawOval(x-r*.65f,y-r,x+r*.65f,y+r,material);material.style=Paint.Style.FILL
            }else crystal(c,x,y,r,if(awake)0xF2E8FFC4.toInt() else 0xDCB3C29B.toInt(),if(awake)0xED2DAC84.toInt() else 0xDB44634E.toInt())
        }
        if(p.seekerborn || preview){
            val x=a.genesis.x;val y=a.genesis.y;val rr=r*.75f
            material.color=0x8042EED0.toInt();material.shader=RadialGradient(x,y,rr*1.6f,intArrayOf(material.color,Color.TRANSPARENT),null,Shader.TileMode.CLAMP)
            c.drawCircle(x,y,rr*1.6f,material);material.shader=null
            path.reset();path.addOval(x-rr,y-rr,x+rr,y+rr,Path.Direction.CW)
            c.save();c.clipPath(path);material.color=Color.WHITE;material.alpha=240
            c.drawBitmap(icons.getValue("genesis"),null,RectF(x-rr,y-rr,x+rr,y+rr),material);c.restore();material.alpha=255
            material.style=Paint.Style.STROKE;material.strokeWidth=r*.06f;material.color=0xC3B9FFE7.toInt()
            c.drawCircle(x,y,rr,material);material.style=Paint.Style.FILL
        }
        if("jupiter" in active || preview){
            val x=a.navigator.x;val y=a.navigator.y
            vein(c,x,y,r,if(id=="dragon")0x9979CFB5.toInt() else 0xB99BE6BC.toInt(),1.15f)
            material.alpha=if(id=="dragon")155 else 220;material.colorFilter=jupiterInk
            c.drawBitmap(icons.getValue("jupiter"),null,RectF(x-r*.8f,y-r*.8f,x+r*.8f,y+r*.8f),material)
            material.alpha=255;material.colorFilter=null
        }
        if("nft" in active || preview){
            val x=a.relic.x;val y=a.relic.y
            material.shader=RadialGradient(x,y,r*1.6f,intArrayOf(0x709F7EFF,Color.TRANSPARENT),null,Shader.TileMode.CLAMP)
            c.drawCircle(x,y,r*1.6f,material);material.shader=null
            crystal(c,x-r*.63f,y+r*.18f,r*.42f,0xE2A7ECFF.toInt(),0xC76C54C7.toInt())
            crystal(c,x+r*.65f,y+r*.27f,r*.38f,0xE3FFA9DC.toInt(),0xCE9152B3.toInt())
            crystal(c,x,y,r*.87f,0xF2E0C9FF.toInt(),0xE95940AB.toInt())
        }
        material.shader=null;material.style=Paint.Style.FILL;material.alpha=255;c.restore()
    }
}
data class TraitPoint(val x:Float,val y:Float)
data class TraitAnatomy(val width:Float,val height:Float,val scale:Float,
    val core:TraitPoint,val genesis:TraitPoint,val navigator:TraitPoint,val relic:TraitPoint) {
    companion object {
        private fun rig(w:Int,h:Int,s:Float,vararg xy:Int)=TraitAnatomy(w.toFloat(),h.toFloat(),s,
            TraitPoint(xy[0].toFloat(),xy[1].toFloat()),TraitPoint(xy[2].toFloat(),xy[3].toFloat()),
            TraitPoint(xy[4].toFloat(),xy[5].toFloat()),TraitPoint(xy[6].toFloat(),xy[7].toFloat()))
        val all=mapOf(
            "fox" to rig(1254,1254,.94f,930,600,860,280,690,650,930,760),
            "dragon" to rig(1536,1100,.86f,615,670,697,385,815,685,660,750),
            "golem" to rig(1254,1254,.88f,640,530,690,240,630,610,570,780),
            "wisp" to rig(1374,1145,.87f,1010,650,1020,470,975,715,1020,720),
            "shark" to rig(1536,1024,.87f,630,545,305,450,795,530,780,600),
            "plant" to rig(1312,1199,.86f,865,700,910,530,790,805,660,930),
            "mech" to rig(1536,1024,.88f,790,450,550,295,850,330,805,540),
            "bird" to rig(1536,1024,.86f,1030,470,1025,110,920,530,975,580),
            "slime" to rig(1536,1024,.86f,920,630,1080,445,800,710,1120,660),
            "cosmic" to rig(1312,1199,.86f,750,620,770,460,770,720,580,680),
            "degen_ape" to rig(1312,1199,.86f,790,530,890,145,650,580,910,560),
            "cat" to rig(1240,1268,.86f,550,515,520,230,735,820,640,900),
            "lynx" to rig(1312,1199,.86f,580,620,510,260,895,640,765,800)
        )
        fun forSpecies(id:String)=requireNotNull(all[id]){"Missing canonical trait anatomy: "+id}
    }
}
