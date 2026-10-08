package app.chainling

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import kotlin.math.min
import kotlin.math.sin

/** Approved Astrafox layers. Tail motion and blink are independent of the body. */
class AstrafoxView(context: Context): View(context) {
    var materials=OrganicTraitSelection()
    private val organic=AstrafoxMaterials()
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val sampledTail=Rect()
    private val flexible=FlexibleSprite()
    private val tailSource=Rect(178,86,1056,1206)
    private val tailDestination=RectF(140f,450f,645f,1120f)
    private val bodyDestination=RectF(0f,0f,1254f,1254f)
    private val started=SystemClock.uptimeMillis()
    private var attached=false
    private var tappedAt=-10000L
    init { contentDescription="Astrafox, cosmic fox. Tap to interact.";isClickable=true }
    override fun onAttachedToWindow(){super.onAttachedToWindow();attached=true;invalidate()}
    override fun onDetachedFromWindow(){attached=false;super.onDetachedFromWindow()}
    override fun onWindowVisibilityChanged(visibility:Int){super.onWindowVisibilityChanged(visibility);if(visibility==VISIBLE)invalidate()}
    override fun performClick():Boolean{super.performClick();tappedAt=SystemClock.uptimeMillis();invalidate();return true}
    override fun onTouchEvent(event:MotionEvent):Boolean {
        when(event.actionMasked){MotionEvent.ACTION_DOWN->return true;MotionEvent.ACTION_UP->{performClick();return true}}
        return super.onTouchEvent(event)
    }
    override fun onDraw(c:Canvas) {
        super.onDraw(c)
        val time=(SystemClock.uptimeMillis()-started)/1000.0
        val excited=SystemClock.uptimeMillis()-tappedAt<900
        val art=Art.get(context)
        val scale=min(width/1254f,height/1254f)*0.94f
        c.save();c.translate(width/2f,height/2f);c.scale(scale,scale);c.translate(-627f,-627f)
        val breath=1f+(sin(time*1.8)*0.006).toFloat()
        c.scale(1f,breath,627f,1060f)
        if(excited)c.translate(0f,(-sin((SystemClock.uptimeMillis()-tappedAt)/900.0*Math.PI)*20).toFloat())
        c.save()
        val tail=art.getValue("tail")
        sampledTail.set(tailSource.left*tail.width/1254,tailSource.top*tail.height/1254,tailSource.right*tail.width/1254,tailSource.bottom*tail.height/1254)
        flexible.draw(c,tail,sampledTail,tailDestination,620f,650f,110f,540f,(sin(time*(if(excited)4.0 else 1.6))*(if(excited)22.0 else 10.0)).toFloat(),paint);c.restore()
        val frame=bodyDestination
        c.drawBitmap(art.getValue("body"),null,frame,paint)
        val blink=time>0.5 && time%4.6<0.16
        if(blink){
            // Only replace the eye area, avoiding generative drift elsewhere in the closed frame.
            c.save();c.clipRect(768f,293f,953f,390f);c.drawBitmap(art.getValue("blink"),null,frame,paint);c.restore()
        }
        organic.draw(c,art.getValue("organic-atlas"),art.getValue("body"),materials,time,art.getValue("jup-atlas"))
        c.restore()
        if(attached && windowVisibility==VISIBLE && isShown)postInvalidateDelayed(40)
    }
    private object Art {
        private val parts=listOf("body","tail","blink","organic-atlas","jup-atlas")
        fun get(context:Context)=CanonicalSpriteCache.get(context,"astrafox",parts)
    }
}



