package app.chainling
import android.graphics.*
import kotlin.math.sin
/** Painted materials from the approved master, selected independently; no invented evidence. */
class SharkMaterials {
    private val imagePaint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val maskPaint=Paint(Paint.ANTI_ALIAS_FLAG).apply{xfermode=PorterDuffXfermode(PorterDuff.Mode.DST_IN)}
    private val frame=RectF(0f,0f,1536f,1024f)
    private val skull=RectF(1175f,475f,1325f,620f)
    private val chest=RectF(715f,330f,975f,650f)
    private val flank=RectF(500f,335f,750f,720f)
    private val memory=RectF(940f,480f,1120f,675f)
    private fun patch(c:Canvas,atlas:Bitmap,body:Bitmap,roi:RectF,alpha:Int=255){
        val layer=c.saveLayer(roi,null)
        c.clipRect(roi);imagePaint.alpha=alpha
        c.drawBitmap(atlas,null,RectF(21f,-10f,1445f,957f),imagePaint);imagePaint.alpha=255
        c.drawBitmap(body,null,frame,maskPaint)
        val radius=roi.width()/2
        maskPaint.shader=RadialGradient(roi.centerX(),roi.centerY(),radius,
            intArrayOf(Color.WHITE,Color.WHITE,Color.TRANSPARENT),floatArrayOf(0f,.78f,1f),Shader.TileMode.CLAMP).apply{
            setLocalMatrix(Matrix().apply{setScale(1f,roi.height()/roi.width(),roi.centerX(),roi.centerY())})
        }
        c.drawRect(roi,maskPaint);maskPaint.shader=null;c.restoreToCount(layer)
    }
    fun draw(c:Canvas,atlas:Bitmap,body:Bitmap,state:OrganicTraitSelection,time:Double,stakingAtlas:Bitmap){
        if(state.skr)patch(c,atlas,body,chest,if(state.active)(236+19*(.5+.5*sin(time*2))).toInt() else 225)
        if(state.genesis)patch(c,atlas,body,skull)
        if(state.jupiter)patch(c,atlas,body,flank)
        if(state.nft)patch(c,atlas,body,memory)
        if(state.jupStaking)patch(c,stakingAtlas,body,RectF(350f,350f,520f,550f))
    }
}



