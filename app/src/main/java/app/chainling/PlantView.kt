package app.chainling

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import kotlin.math.min
import kotlin.math.sin

/** Approved colored botanical engraving with independently swaying leafy branches. */
class PlantView(context:Context):View(context),OrganicMaterialHost {
    override var materials=OrganicTraitSelection()
    private val organic=OrganicBodyMaterials(context,"plant")
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val sampledSource=Rect()
    private val frame=RectF(0f,0f,1312f,1199f)
    private val leftSource=Rect(76,44,1476,992)
    private val rightSource=Rect(12,16,1528,1012)
    private val leftDestination=RectF(40f,350f,580f,720f)
    private val rightDestination=RectF(900f,500f,1290f,860f)
    private val started=SystemClock.uptimeMillis()
    private var attached=false
    private var tappedAt=-10000L
    init {contentDescription="Plant. Tap to interact.";isClickable=true}
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
        val now=SystemClock.uptimeMillis()
        val time=(now-started)/1000.0
        val excited=now-tappedAt<1100
        val art=CanonicalSpriteCache.get(context,"plant",parts.filter{materials.hasMaterials || !it.startsWith("organic-")} )
        val scale=min(width/1312f,height/1199f)*0.86f
        c.save();c.translate(width/2f,height/2f);c.scale(scale,scale);c.translate(-656f,-599.5f)
        c.scale(1f,1f+(sin(time*1.1)*0.004).toFloat(),656f,1110f)
        val motion=(sin(time*(if(excited)8.0 else 1.2))*(if(excited)5.0 else 1.8)).toFloat()
        drawPart(c,art.getValue("branch_left"),leftSource,leftDestination,535f,670f,motion)
        drawPart(c,art.getValue("branch_right"),rightSource,rightDestination,975f,740f,-motion)
        c.drawBitmap(art.getValue("body"),null,frame,paint)
        if(materials.hasMaterials)organic.draw(c,art.getValue("organic-atlas"),art.getValue("organic-master"),art.getValue("body"),frame,materials,time)
        if(time>0.5 && time%5.8<0.18){c.save();c.clipRect(790f,520f,989f,605f);c.drawBitmap(art.getValue("blink"),null,frame,paint);c.restore()}
        c.restore()
        if(attached && windowVisibility==VISIBLE && isShown)postInvalidateDelayed(40)
    }
    private fun drawPart(c:Canvas,image:Bitmap,source:Rect,destination:RectF,x:Float,y:Float,angle:Float) {
        sampledSource.set(source.left*image.width/1536,source.top*image.height/1024,source.right*image.width/1536,source.bottom*image.height/1024)
        c.save();c.rotate(angle,x,y);c.drawBitmap(image,sampledSource,destination,paint);c.restore()
    }
    companion object {private val parts=listOf("body","branch_left","branch_right","blink","organic-atlas","organic-master")}
}


