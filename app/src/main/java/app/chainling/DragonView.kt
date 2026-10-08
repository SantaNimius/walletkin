package app.chainling

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import kotlin.math.min
import kotlin.math.sin

class DragonView(context:Context):View(context) {
    private val sampledSource=Rect()
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val frame=RectF(0f,0f,1254f,1254f)
    private val leftSource=Rect(64,10,1084,1242)
    private val rightSource=Rect(172,52,1212,1228)
    private val tailSource=Rect(164,88,1200,1194)
    private val leftDestination=RectF(20f,140f,540f,940f)
    private val rightDestination=RectF(620f,110f,1240f,870f)
    private val tailDestination=RectF(670f,660f,1215f,1180f)
    private val started=SystemClock.uptimeMillis()
    private var attached=false
    private var tappedAt=-10000L
    init {contentDescription="Dragon. Tap to interact.";isClickable=true}
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
        val art=CanonicalSpriteCache.get(context,"dragon",parts)
        val scale=min(width/1254f,height/1254f)*0.86f
        c.save();c.translate(width/2f,height/2f);c.scale(scale,scale);c.translate(-627f,-627f)
        c.scale(1f,1f+(sin(time*1.5)*0.005).toFloat(),627f,1100f)
        val motion=(sin(time*(if(excited)7.0 else 1.1))*(if(excited)7.0 else 1.8)).toFloat()
        drawPart(c,art.getValue("tail"),tailSource,tailDestination,750f,750f,motion*0.8f)
        drawPart(c,art.getValue("wing_left"),leftSource,leftDestination,420f,590f,-motion)
        drawPart(c,art.getValue("wing_right"),rightSource,rightDestination,660f,570f,motion)
        c.drawBitmap(art.getValue("body"),null,frame,paint)
        if(time>0.5 && time%5.1<0.16){c.save();c.clipRect(532f,165f,667f,234f);c.drawBitmap(art.getValue("blink"),null,frame,paint);c.restore()}
        c.restore()
        if(attached && windowVisibility==VISIBLE && isShown)postInvalidateDelayed(40)
    }
    private fun drawPart(c:Canvas,image:Bitmap,source:Rect,destination:RectF,x:Float,y:Float,angle:Float,originalWidth:Int=1254,originalHeight:Int=1254) {
        sampledSource.set(source.left*image.width/originalWidth,source.top*image.height/originalHeight,source.right*image.width/originalWidth,source.bottom*image.height/originalHeight)
        c.save();c.rotate(angle,x,y);c.drawBitmap(image,sampledSource,destination,paint);c.restore()
    }
    companion object {private val parts=listOf("body","wing_left","wing_right","tail","blink")}
}
