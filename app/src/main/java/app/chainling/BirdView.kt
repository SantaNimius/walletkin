package app.chainling

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import kotlin.math.min
import kotlin.math.sin

class BirdView(context:Context):View(context),OrganicMaterialHost {
    private val sampledSource=Rect()
    override var materials=OrganicTraitSelection()
    private val organic=OrganicBodyMaterials(context,"bird")
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val frame=RectF(0f,0f,1536f,1024f)
    private val leftSource=Rect(52,88,1492,940)
    private val rightSource=Rect(176,28,1420,1012)
    private val tailSource=Rect(92,104,1468,972)
    private val leftDestination=RectF(50f,135f,830f,625f)
    private val rightDestination=RectF(1040f,245f,1480f,685f)
    private val tailDestination=RectF(360f,610f,910f,900f)
    private val started=SystemClock.uptimeMillis()
    private var attached=false
    private var tappedAt=-10000L
    init {contentDescription="Bird. Tap to interact.";isClickable=true}
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
        val art=CanonicalSpriteCache.get(context,"bird",parts.filter{materials.hasMaterials || !it.startsWith("organic-")} )
        val scale=min(width/1536f,height/1024f)*0.86f
        c.save();c.translate(width/2f,height/2f);c.scale(scale,scale);c.translate(-768f,-512f)
        c.scale(1f,1f+(sin(time*1.5)*0.005).toFloat(),768f,940f)
        val motion=(sin(time*(if(excited)7.0 else 1.1))*(if(excited)7.0 else 1.8)).toFloat()
        drawPart(c,art.getValue("tail"),tailSource,tailDestination,875f,670f,motion*0.8f)
        drawPart(c,art.getValue("wing_left"),leftSource,leftDestination,810f,350f,-motion)
        drawPart(c,art.getValue("wing_right"),rightSource,rightDestination,1100f,365f,motion)
        c.drawBitmap(art.getValue("body"),null,frame,paint)
        if(materials.hasMaterials)organic.draw(c,art.getValue("organic-atlas"),art.getValue("organic-master"),art.getValue("body"),frame,materials,time)
        if(time>0.5 && time%5.1<0.16){c.save();c.clipRect(999f,80f,1058f,139f);c.drawBitmap(art.getValue("blink"),null,frame,paint);c.restore()}
        c.restore()
        if(attached && windowVisibility==VISIBLE && isShown)postInvalidateDelayed(40)
    }
    private fun drawPart(c:Canvas,image:Bitmap,source:Rect,destination:RectF,x:Float,y:Float,angle:Float,originalWidth:Int=1536,originalHeight:Int=1024) {
        sampledSource.set(source.left*image.width/originalWidth,source.top*image.height/originalHeight,source.right*image.width/originalWidth,source.bottom*image.height/originalHeight)
        c.save();c.rotate(angle,x,y);c.drawBitmap(image,sampledSource,destination,paint);c.restore()
    }
    companion object {private val parts=listOf("body","wing_left","wing_right","tail","blink","organic-atlas","organic-master")}
}



