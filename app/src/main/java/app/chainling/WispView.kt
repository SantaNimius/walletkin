package app.chainling

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import kotlin.math.min
import kotlin.math.sin

/** Approved translucent spirit: two independent trails and an eye-only blink. */
class WispView(context:Context):View(context),OrganicMaterialHost {
    override var materials=OrganicTraitSelection()
    private val organic=OrganicBodyMaterials(context,"wisp")
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val sampledSource=Rect()
    private val frame=RectF(0f,0f,1374f,1145f)
    private val lowerSource=Rect(48,280,1344,1096)
    private val upperSource=Rect(88,40,1488,1000)
    private val lowerDestination=RectF(76f,540f,1120f,1130f)
    private val upperDestination=RectF(570f,25f,1170f,465f)
    private val started=SystemClock.uptimeMillis()
    private var attached=false
    private var tappedAt=-10000L
    init {contentDescription="Wisp. Tap to interact.";isClickable=true}
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
        val excited=now-tappedAt<900
        val art=CanonicalSpriteCache.get(context,"wisp",parts.filter{materials.hasMaterials || !it.startsWith("organic-")} )
        val scale=min(width/1374f,height/1145f)*0.87f
        c.save();c.translate(width/2f,height/2f);c.scale(scale,scale);c.translate(-687f,-572.5f)
        c.translate(0f,(sin(time*1.4)*7).toFloat())
        if(excited)c.translate(0f,(-sin((now-tappedAt)/900.0*Math.PI)*28).toFloat())
        val motion=(sin(time*(if(excited)6.0 else 1.1))*(if(excited)6.0 else 2.4)).toFloat()
        paint.alpha=255
        drawPart(c,art.getValue("trail_upper"),upperSource,upperDestination,1080f,390f,-motion,1536,1024)
        drawPart(c,art.getValue("trail_lower"),lowerSource,lowerDestination,1030f,750f,motion,1374,1145)
        paint.alpha=(242+13*sin(time*1.7)).toInt().coerceIn(229,255)
        c.drawBitmap(art.getValue("body"),null,frame,paint)
        if(materials.hasMaterials)organic.draw(c,art.getValue("organic-atlas"),art.getValue("organic-master"),art.getValue("body"),frame,materials,time)
        if(time>0.5 && time%4.9<0.17){c.save();c.clipRect(899f,465f,1125f,565f);c.drawBitmap(art.getValue("blink"),null,frame,paint);c.restore()}
        paint.alpha=255
        c.restore()
        if(attached && windowVisibility==VISIBLE && isShown)postInvalidateDelayed(40)
    }
    private fun drawPart(c:Canvas,image:Bitmap,source:Rect,destination:RectF,x:Float,y:Float,angle:Float,originalWidth:Int,originalHeight:Int) {
        sampledSource.set(source.left*image.width/originalWidth,source.top*image.height/originalHeight,source.right*image.width/originalWidth,source.bottom*image.height/originalHeight)
        c.save();c.rotate(angle,x,y);c.drawBitmap(image,sampledSource,destination,paint);c.restore()
    }
    companion object {private val parts=listOf("body","trail_lower","trail_upper","blink","organic-atlas","organic-master")}
}


