package app.chainling
import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import kotlin.math.min
import kotlin.math.sin

/** One approved silhouette; soft local arm motion cannot produce duplicated sleeves or shoulders. */
class DegenKongView(context:Context):View(context),OrganicMaterialHost {
    override var materials=OrganicTraitSelection()
    private val organic=OrganicBodyMaterials(context,"degen_ape")
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val vertices=FloatArray((COLS+1)*(ROWS+1)*2)
    private val frame=RectF(0f,0f,1312f,1199f)
    private val started=SystemClock.uptimeMillis()
    private var tappedAt=-10000L
    init{isClickable=true;contentDescription="Degen Kong. Tap to interact."}
    override fun performClick():Boolean{
        super.performClick();tappedAt=SystemClock.uptimeMillis();invalidate();return true
    }
    override fun onTouchEvent(e:MotionEvent):Boolean=when(e.actionMasked){
        MotionEvent.ACTION_DOWN->true
        MotionEvent.ACTION_UP->{performClick();true}
        else->super.onTouchEvent(e)
    }
    override fun onDraw(c:Canvas){
        super.onDraw(c)
        val now=SystemClock.uptimeMillis();val t=(now-started)/1000.0;val age=now-tappedAt
        val tap=age in 0..899
        val art=CanonicalSpriteCache.get(context,"degen_ape",parts.filter{materials.hasMaterials || !it.startsWith("organic-")} )
        val scale=min(width/1312f,height/1199f)*.86f
        c.save();c.translate(width/2f,height/2f);c.scale(scale,scale);c.translate(-656f,-599.5f)
        c.scale(1f,1f+(sin(t*1.3)*.004).toFloat(),656f,1100f)
        if(tap)c.translate(0f,(-sin(age/900.0*Math.PI)*8).toFloat())
        val amplitude=if(tap)8f else 3f
        val a=(sin(t*(if(tap)4.0 else 1.1)) * amplitude).toFloat()
        val b=(sin(t*(if(tap)4.0 else 1.1)+1.4) * amplitude).toFloat()
        var index=0
        for(row in 0..ROWS)for(col in 0..COLS){
            val x=1312f*col/COLS;val y=1199f*row/ROWS
            val left=KongMotion.handWeight(x,y,false);val right=KongMotion.handWeight(x,y,true)
            vertices[index++]=x+a*left-b*right
            vertices[index++]=y+(a*left+b*right)*.25f
        }
        c.drawBitmapMesh(art.getValue("portrait"),COLS,ROWS,vertices,0,null,0,paint)
        if(materials.hasMaterials)organic.draw(c,art.getValue("organic-atlas"),art.getValue("organic-master"),art.getValue("portrait"),frame,materials,t)
        if(t>.5 && t%5.2<.17){
            c.save();c.clipRect(795f,150f,966f,218f)
            c.drawBitmap(art.getValue("blink"),null,frame,paint);c.restore()
        }
        c.restore()
        if(isAttachedToWindow && windowVisibility==VISIBLE && isShown)postInvalidateDelayed(40)
    }
    companion object{
        private const val COLS=32
        private const val ROWS=28
        private val parts=listOf("portrait","blink","organic-atlas","organic-master")
    }
}



