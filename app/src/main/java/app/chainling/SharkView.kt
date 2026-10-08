package app.chainling

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import kotlin.math.min
import kotlin.math.sin

/** Approved cel-shaded shark rig, with independent tail and pectoral fin joints. */
class SharkView(context:Context):View(context) {
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val sampledSource=Rect()
    private val flexible=FlexibleSprite()
    var materials=OrganicTraitSelection()
    private val organic=SharkMaterials()
    private val bodyDestination=RectF(0f,25f,1382f,947f)
    private val tailSource=Rect(344,20,1344,1004)
    private val farSource=Rect(300,100,1236,960)
    private val nearSource=Rect(450,310,1450,996)
    // Keep the entire overlapping peduncle fixed; only the free tail lobes bend.
    private val tailDestination=RectF(1130f,180f,1520f,770f)
    private val farDestination=RectF(205f,520f,490f,895f)
    private val nearDestination=RectF(570f,560f,1030f,930f)
    // Feather the closed extraction edge at the tail joint; source artwork remains unchanged.
    private val jointMask=Paint().apply {
        shader=LinearGradient(1230f,0f,1285f,0f,Color.WHITE,Color.TRANSPARENT,Shader.TileMode.CLAMP)
        xfermode=PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }
    private val gillPaint=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=Color.rgb(62,240,224);style=Paint.Style.STROKE;strokeWidth=4f}
    private val gills=listOf(
        Path().apply{moveTo(532f,386f);cubicTo(577f,445f,573f,541f,547f,606f)},
        Path().apply{moveTo(573f,385f);cubicTo(620f,454f,612f,551f,587f,624f)},
        Path().apply{moveTo(615f,445f);cubicTo(649f,502f,640f,561f,613f,611f)}
    )
    private val started=SystemClock.uptimeMillis()
    private var attached=false
    private var tappedAt=-10000L
    init {contentDescription="Shark. Tap to interact.";isClickable=true}
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
        val art=CanonicalSpriteCache.get(context,"shark",parts)
        val scale=min(width/1536f,height/1024f)*0.87f
        c.save();c.translate(width/2f,height/2f);c.scale(scale,scale);c.translate(-768f,-512f)
        c.translate(0f,(sin(time*1.3)*5).toFloat())
        c.rotate((sin(time*0.9)*0.6).toFloat(),768f,512f)
        if(excited)c.translate((-sin((now-tappedAt)/900.0*Math.PI)*32).toFloat(),0f)
        val motion=(sin(time*(if(excited)7.0 else 1.8))*(if(excited)3.0 else 1.5)).toFloat()
        val tail=art.getValue("tail")
        sampledSource.set(tailSource.left*tail.width/1536,tailSource.top*tail.height/1024,tailSource.right*tail.width/1536,tailSource.bottom*tail.height/1024)
        flexible.draw(c,tail,sampledSource,tailDestination,1230f,545f,190f,340f,
            (sin(time*(if(excited)4.0 else 1.8))*(if(excited)24.0 else 10.0)).toFloat(),paint)
        drawPart(c,art.getValue("fin_far"),farSource,farDestination,430f,650f,-motion*0.6f)
        drawPart(c,art.getValue("fin_near"),nearSource,nearDestination,620f,620f,motion*0.6f)
        val layer=c.saveLayer(bodyDestination,null)
        c.drawBitmap(art.getValue("body"),null,bodyDestination,paint)
        if(time>0.5 && time%5.3<0.16){c.save();c.clipRect(260f,450f,345f,505f);c.drawBitmap(art.getValue("blink"),null,bodyDestination,paint);c.restore()}
        c.save();c.translate(bodyDestination.left,bodyDestination.top);c.scale(bodyDestination.width()/1536f,bodyDestination.height()/1024f)
        organic.draw(c,art.getValue("organic-atlas"),art.getValue("body"),materials,time,art.getValue("jup-atlas"));c.restore()
        gillPaint.alpha=(12+10*sin(time*1.7)).toInt()
        gills.forEach{c.drawPath(it,gillPaint)}
        c.drawRect(1230f,25f,1382f,947f,jointMask)
        c.restoreToCount(layer)
        c.restore()
        if(attached && windowVisibility==VISIBLE && isShown)postInvalidateDelayed(40)
    }
    private fun drawPart(c:Canvas,image:Bitmap,source:Rect,destination:RectF,x:Float,y:Float,angle:Float) {
        sampledSource.set(source.left*image.width/1536,source.top*image.height/1024,source.right*image.width/1536,source.bottom*image.height/1024)
        c.save();c.rotate(angle,x,y);c.drawBitmap(image,sampledSource,destination,paint);c.restore()
    }
    companion object {private val parts=listOf("body","tail","fin_near","fin_far","blink","organic-atlas","jup-atlas")}
}




