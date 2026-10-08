package app.chainling

import android.content.Context
import android.graphics.*
import android.view.View
import android.widget.FrameLayout
import kotlin.math.cos
import kotlin.math.sin

/** Shared canonical presentation for Legends, wallet views and native share. */
open class CanonicalSceneView(
    context:Context, val speciesId:String, var paletteIndex:Int?=null, private val portrait:Boolean=false
):FrameLayout(context) {
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private val tintPaint=Paint(Paint.ANTI_ALIAS_FLAG)
    private var lastPalette:Int?=null
    init {
        setWillNotDraw(false)
        addView(if(portrait)CanonicalPortraitView(context,CanonicalWorld.assetId(speciesId)) else animated(context,speciesId),
            LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT))
        contentDescription=(CanonicalWorld.species.firstOrNull{it.id==speciesId}?.name ?: "Anomaly")+". Tap to interact."
    }
    override fun onDraw(canvas:Canvas) {
        super.onDraw(canvas)
        val color=paletteIndex?.let{CanonicalWorld.colors[it]} ?: CanonicalWorld.species.firstOrNull{it.id==speciesId}?.color ?: 0xFFBD9DFF.toInt()
        drawHalo(canvas,width.toFloat(),height.toFloat(),color)
    }
    override fun dispatchDraw(canvas:Canvas) {
        val p=paletteIndex
        if(p!=lastPalette){
            tintPaint.colorFilter=p?.let{ColorMatrixColorFilter(hueMatrix(CanonicalWorld.hueRotation(speciesId,it),CanonicalWorld.colors[it]))}
            lastPalette=p
        }
        val save=canvas.saveLayer(0f,0f,width.toFloat(),height.toFloat(),tintPaint)
        super.dispatchDraw(canvas)
        canvas.restoreToCount(save)
    }
    private fun drawHalo(canvas:Canvas,w:Float,h:Float,color:Int) {
        if(w<=0 || h<=0)return
        val radius=minOf(w,h)*0.44f
        paint.style=Paint.Style.FILL
        paint.shader=RadialGradient(w*.5f,h*.52f,radius,intArrayOf(alpha(color,72),alpha(color,22),Color.TRANSPARENT),floatArrayOf(0f,.55f,1f),Shader.TileMode.CLAMP)
        canvas.drawCircle(w*.5f,h*.52f,radius,paint);paint.shader=null
        paint.style=Paint.Style.STROKE;paint.strokeWidth=resources.displayMetrics.density*.75f;paint.color=alpha(color,48)
        canvas.drawOval(w*.5f-radius*.92f,h*.52f-radius*.84f,w*.5f+radius*.92f,h*.52f+radius*.84f,paint)
        paint.style=Paint.Style.FILL
    }
    companion object {
        private fun alpha(c:Int,a:Int)=(c and 0xFFFFFF) or (a shl 24)
        fun animated(context:Context,id:String):View=when(id){
            "fox"->AstrafoxView(context)
            "dragon"->LayeredCreatureView(context,"dragon")
            "golem"->GolemView(context)
            "wisp"->WispView(context)
            "shark"->SharkView(context)
            "plant"->PlantView(context)
            "mech"->MechView(context)
            "bird"->BirdView(context)
            "degen_ape"->DegenKongView(context)
            else->LayeredCreatureView(context,CanonicalWorld.assetId(id))
        }
        // Runtime material hue rotation preserves luminance, texture and transparent alpha.
        private fun hueMatrix(degrees:Float,color:Int):ColorMatrix {
            val angle=Math.toRadians(degrees.toDouble());val c=cos(angle).toFloat();val s=sin(angle).toFloat()
            val values=floatArrayOf(
                .213f+.787f*c-.213f*s,.715f-.715f*c-.715f*s,.072f-.072f*c+.928f*s,0f,0f,
                .213f-.213f*c+.143f*s,.715f+.285f*c+.140f*s,.072f-.072f*c-.283f*s,0f,0f,
                .213f-.213f*c-.787f*s,.715f-.715f*c+.715f*s,.072f+.928f*c+.072f*s,0f,0f,
                0f,0f,0f,1f,0f)
            // A small ambient material tint also gives neutral stone/paper visible color forms.
            val channels=intArrayOf(Color.red(color),Color.green(color),Color.blue(color))
            for(row in 0..2){
                for(column in 0..3)values[row*5+column]*=.90f
                values[row*5+4]=channels[row]*.10f
            }
            return ColorMatrix(values)
        }
    }
}



