package app.chainling

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import kotlin.math.min
import kotlin.math.sin

/** Modular basalt guardian: arms move around shoulder joints; blink changes eyes only. */
class GolemView(context:Context):View(context),OrganicMaterialHost {
    private val sampledSource=Rect()
    override var materials=OrganicTraitSelection()
    private val organic=OrganicBodyMaterials(context,"golem")
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val frame=RectF(0f,0f,1254f,1254f)
    private val leftSource=Rect(206,18,886,1254)
    private val rightSource=Rect(228,16,1144,1288)
    private val leftDestination=RectF(68f,204f,475f,1040f)
    private val rightDestination=RectF(750f,250f,1220f,1030f)
    private val started=SystemClock.uptimeMillis()
    private var attached=false
    private var tappedAt=-10000L
    init {contentDescription="Golem. Tap to interact.";isClickable=true}
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
        val excited=SystemClock.uptimeMillis()-tappedAt<1000
        val art=CanonicalSpriteCache.get(context,"golem",parts.filter{materials.hasMaterials || !it.startsWith("organic-")} )
        val scale=min(width/1254f,height/1254f)*0.88f
        c.save();c.translate(width/2f,height/2f);c.scale(scale,scale);c.translate(-627f,-627f)
        c.scale(1f,1f+(sin(time*1.2)*0.004).toFloat(),627f,1120f)
        val motion=(sin(time*(if(excited)5.0 else 0.9))*(if(excited)5.0 else 0.9)).toFloat()
        drawPart(c,art.getValue("arm_left"),leftSource,leftDestination,435f,285f,motion)
        drawPart(c,art.getValue("arm_right"),rightSource,rightDestination,920f,350f,-motion,1216,1294)
        c.drawBitmap(art.getValue("body"),null,frame,paint)
        if(materials.hasMaterials)organic.draw(c,art.getValue("organic-atlas"),art.getValue("organic-master"),art.getValue("body"),frame,materials,time)
        if(time>0.5 && time%5.6<0.18){c.save();c.clipRect(612f,164f,750f,204f);c.drawBitmap(art.getValue("blink"),null,frame,paint);c.restore()}
        c.restore()
        if(attached && windowVisibility==VISIBLE && isShown)postInvalidateDelayed(40)
    }
    private fun drawPart(c:Canvas,image:Bitmap,source:Rect,destination:RectF,x:Float,y:Float,angle:Float,originalWidth:Int=1254,originalHeight:Int=1254) {
        sampledSource.set(source.left*image.width/originalWidth,source.top*image.height/originalHeight,source.right*image.width/originalWidth,source.bottom*image.height/originalHeight)
        c.save();c.rotate(angle,x,y);c.drawBitmap(image,sampledSource,destination,paint);c.restore()
    }
    companion object {private val parts=listOf("body","arm_left","arm_right","blink","organic-atlas","organic-master")}
}




