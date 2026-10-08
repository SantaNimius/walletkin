package app.chainling
import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.view.View
import android.widget.FrameLayout
import kotlin.math.cos
import kotlin.math.sin

/** Temporary product preview, never saved as wallet DNA, earned traits or inventory. */
class CosmeticPreview(context:Context,profile:Profile,item:CosmeticItem):FrameLayout(context) {
    init {
        addView(CosmeticScene(context,item),LayoutParams(-1,-1))
        val preview=if(item.palette==null)profile else profile.copy(dna=profile.dna.copy(palette=item.palette))
        addView(CreatureView(context,preview,applyCosmetics=false),LayoutParams(-1,-1))
        if(item.effect!=null)addView(CosmeticEffect(context,item.effect),LayoutParams(-1,-1))
        contentDescription="Cosmetic preview: "+item.name+". Not purchased or equipped."
    }
}
internal class CosmeticScene(context:Context,private val item:CosmeticItem):View(context){
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(c:Canvas){
        val w=width.toFloat();val h=height.toFloat()
        if(w<=0 || h<=0)return
        val colors=when(item.realm){
            "explorer"->intArrayOf(0xFF090D25.toInt(),0xFF381950.toInt())
            "holder"->intArrayOf(0xFF061E1B.toInt(),0xFF245844.toInt())
            else->intArrayOf(0xFF080C20.toInt(),0xFF252344.toInt())
        }
        paint.shader=LinearGradient(0f,0f,w,h,colors,null,Shader.TileMode.CLAMP)
        c.drawRect(0f,0f,w,h,paint);paint.shader=null
        when(item.realm){
            "explorer"->{
                for(i in 0..10){
                    val x=w*i/10f;val top=h*(.48f+((i*7)%5)*.055f)
                    paint.color=0xFF151A35.toInt();c.drawRect(x,top,x+w*.12f,h,paint)
                    paint.color=if(i%2==0)0x806BFCE5.toInt() else 0x80F462E4.toInt()
                    c.drawRect(x+2,top,x+4,h,paint)
                    for(j in 0..4)c.drawRect(x+w*.035f,top+12+j*22,x+w*.065f,top+16+j*22,paint)
                }
            }
            "holder"->{
                for(i in 0..8){
                    paint.color=0x402FEAAA;val x=w*i/8
                    c.drawOval(x-w*.1f,h*.1f,x+w*.1f,h*.8f,paint)
                    paint.color=0x505CECB0;c.drawCircle(x,h*.8f,w*.035f,paint)
                }
            }
            else->{
                for(i in 0..55){
                    paint.color=0x90BCE3FF.toInt()
                    c.drawCircle(w*((i*37%101)/101f),h*((i*53%103)/103f),if(i%3==0)2f else 1f,paint)
                }
                paint.color=0x309889FF;c.drawCircle(w*.8f,h*.18f,w*.18f,paint)
            }
        }
    }
}
internal class CosmeticEffect(context:Context,private val kind:String):View(context){
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private val start=SystemClock.uptimeMillis()
    override fun onDraw(c:Canvas){
        val w=width.toFloat();val h=height.toFloat();val t=(SystemClock.uptimeMillis()-start)/1000.0
        paint.color=0x80BEA5FF.toInt();paint.style=Paint.Style.STROKE;paint.strokeWidth=2f
        if(kind=="portal")c.drawOval(w*.12f,h*.12f,w*.88f,h*.88f,paint)
        paint.style=Paint.Style.FILL
        for(i in 0..11){
            val a=t*.2+i*Math.PI/6
            c.drawCircle(w*.5f+(cos(a)*w*.4f).toFloat(),h*.5f+(sin(a)*h*.4f).toFloat(),3f,paint)
        }
        if(isAttachedToWindow && isShown)postInvalidateDelayed(40)
    }
}



