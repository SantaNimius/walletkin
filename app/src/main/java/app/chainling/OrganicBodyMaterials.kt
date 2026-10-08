package app.chainling
import android.content.Context
import android.graphics.*
import kotlin.math.sin

interface OrganicMaterialHost { var materials:OrganicTraitSelection }

/** Paint regions are composited in the SAME body transform as the animation, never screen decals. */
class OrganicBodyMaterials(context:Context,private val id:String) {
    private val cfg=context.assets.open("canonical/"+id+"/materials.json").bufferedReader().use { org.json.JSONObject(it.readText()) }
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val mask=Paint(Paint.ANTI_ALIAS_FLAG).apply{xfermode=PorterDuffXfermode(PorterDuff.Mode.DST_IN)}
    private fun rect(a:org.json.JSONArray,w:Float,h:Float)=RectF(a.getDouble(0).toFloat()*w,a.getDouble(1).toFloat()*h,a.getDouble(2).toFloat()*w,a.getDouble(3).toFloat()*h)
    fun draw(c:Canvas,atlas:Bitmap,master:Bitmap,body:Bitmap,frame:RectF,state:OrganicTraitSelection,time:Double){
        val regions=cfg.getJSONArray("regions")
        for(i in 0 until regions.length()){
            val r=regions.getJSONObject(i);val kind=r.getString("trait")
            val show=when(kind){"base"->true;"skr"->state.skr;"genesis"->state.genesis;"jupiter"->state.jupiter;"jup_staking"->state.jupStaking;"nft"->state.nft;else->false}
            if(!show)continue
            val roi=rect(r.getJSONArray("rect"),frame.width(),frame.height()).apply{offset(frame.left,frame.top)}
            val layer=c.saveLayer(roi,null);c.clipRect(roi)
            paint.alpha=if(kind=="skr")if(state.active)(236+19*(.5+.5*sin(time*2))).toInt() else 225 else 255
            val source=r.optJSONArray("source")
            if(source==null)c.drawBitmap(atlas,null,frame,paint)
            else {
                val s=rect(source,master.width.toFloat(),master.height.toFloat())
                c.drawBitmap(master,Rect(s.left.toInt(),s.top.toInt(),s.right.toInt(),s.bottom.toInt()),roi,paint)
            }
            paint.alpha=255
            if(r.optBoolean("bodyMask",true))c.drawBitmap(body,null,frame,mask)
            if(kind=="base")for(j in 0 until regions.length()){
                val other=regions.getJSONObject(j)
                if(other.getString("trait")=="base")continue
                val cut=rect(other.getJSONArray("rect"),frame.width(),frame.height()).apply{offset(frame.left,frame.top)}
                mask.xfermode=PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                mask.shader=RadialGradient(cut.centerX(),cut.centerY(),cut.width()/2,
                    intArrayOf(Color.WHITE,Color.WHITE,Color.TRANSPARENT),floatArrayOf(0f,.82f,1f),Shader.TileMode.CLAMP).apply{
                    setLocalMatrix(Matrix().apply{setScale(1f,cut.height()/cut.width(),cut.centerX(),cut.centerY())})
                }
                c.drawRect(cut,mask);mask.shader=null
                mask.xfermode=PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            }
            val feather=r.optDouble("feather",.82).toFloat()
            mask.shader=RadialGradient(roi.centerX(),roi.centerY(),roi.width()/2,intArrayOf(Color.WHITE,Color.WHITE,Color.TRANSPARENT),floatArrayOf(0f,feather,1f),Shader.TileMode.CLAMP).apply{
                setLocalMatrix(Matrix().apply{setScale(1f,roi.height()/roi.width(),roi.centerX(),roi.centerY())})
            }
            c.drawRect(roi,mask);mask.shader=null;c.restoreToCount(layer)
        }
    }
}


