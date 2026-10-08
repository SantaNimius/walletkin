package app.chainling

import android.content.Context
import android.graphics.*
import android.view.View
import kotlin.math.min

/** Complete accepted appearance in catalogue; detailed rigs animate after opening a card. */
class CanonicalPortraitView(context:Context,private val id:String):View(context) {
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val destination=RectF()
    override fun onDraw(canvas:Canvas) {
        super.onDraw(canvas)
        val portrait=CanonicalSpriteCache.get(context,id,parts).getValue("portrait")
        val scale=min(width.toFloat()/portrait.width,height.toFloat()/portrait.height)*0.90f
        val w=portrait.width*scale;val h=portrait.height*scale
        destination.set((width-w)/2f,(height-h)/2f,(width+w)/2f,(height+h)/2f)
        canvas.drawBitmap(portrait,null,destination,paint)
    }
    companion object {private val parts=listOf("portrait")}
}
