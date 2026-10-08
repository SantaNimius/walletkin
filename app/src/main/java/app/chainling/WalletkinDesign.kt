package app.chainling

import android.content.Context
import android.graphics.*
import android.view.View
import org.json.JSONObject
import kotlin.math.max

class WalletkinBackdrop(context: Context, private val realm: String = "genesis"): View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    override fun onDraw(canvas: Canvas) { super.onDraw(canvas); draw(context, canvas, width.toFloat(), height.toFloat(), realm, paint) }
    companion object {
        private val cache = object: android.util.LruCache<String, Bitmap>(12 * 1024 * 1024) {
            override fun sizeOf(key: String, value: Bitmap) = value.byteCount
        }
        private fun bitmap(context: Context, realm: String): Bitmap {
            cache.get(realm)?.let { return it }
            val resource=when(realm) {
                "roots"->R.drawable.background_roots
                "scribe"->R.drawable.background_scribe
                "jupiter"->R.drawable.background_jupiter
                "skr"->R.drawable.background_skr
                else->R.drawable.walletkin_background
            }
            val bounds=BitmapFactory.Options().apply{inJustDecodeBounds=true}
            BitmapFactory.decodeResource(context.resources,resource,bounds)
            var sample=1
            while(maxOf(bounds.outWidth,bounds.outHeight)/sample>1200)sample*=2
            return requireNotNull(BitmapFactory.decodeResource(context.resources,resource,
                BitmapFactory.Options().apply{inSampleSize=sample;inScaled=false})).also{cache.put(realm,it)}
        }
        fun draw(context: Context, c: Canvas, w: Float, h: Float, realm: String = "genesis", paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)) {
            if (w <= 0 || h <= 0) return
            val image = bitmap(context, realm)
            val ratio = max(w / image.width, h / image.height)
            val imageW = image.width * ratio; val imageH = image.height * ratio
            paint.shader = null; paint.style = Paint.Style.FILL; paint.alpha = 255
            c.drawBitmap(image, null, RectF((w-imageW)/2, (h-imageH)/2, (w+imageW)/2, (h+imageH)/2), paint)
            paint.color = 0x30080B14; c.drawRect(0f, 0f, w, h, paint)
        }
    }
}

data class LegendEntry(val id: String, val name: String, val subtitle: String, val lore: String, val anomaly: Boolean, val forms: List<String>, val legacyId: String?)
object LegendsCatalog {
    fun read(context: Context): List<LegendEntry> {
        val json = context.assets.open("legends.json").bufferedReader().use { JSONObject(it.readText()) }
        val array = json.getJSONArray("entries")
        if (json.optBoolean("awaitingCanonicalList") && array.length()==0) return emptyList()
        val result = (0 until array.length()).map { index ->
            val entry = array.getJSONObject(index)
            val forms = entry.optJSONArray("forms")
            LegendEntry(entry.getString("id"), entry.getString("name"), entry.getString("subtitle"), entry.getString("lore"), entry.optBoolean("anomaly"),
                if (forms == null) emptyList() else (0 until forms.length()).map { forms.getString(it) }, entry.optString("legacyId").takeIf { it.isNotBlank() })
        }
        require(if(json.optBoolean("awaitingCanonicalList")) result.size<=14 && result.map{it.id}.toSet().size==result.size && result.count{it.anomaly}<=1 else result.size == 14 && result.count { it.anomaly } == 1 && result.map { it.id }.toSet().size == 14) { "Catalog must contain 13 lineages and one Anomaly" }
        return result
    }
}