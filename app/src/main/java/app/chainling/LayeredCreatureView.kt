package app.chainling

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.min
import kotlin.math.sin

/** Artist-approved layers are placed in a shared art coordinate system. */
class LayeredCreatureView(context: Context, private val id: String) : View(context), OrganicMaterialHost {
    override var materials=OrganicTraitSelection()
    private val organic=if(id in setOf("dragon","slime","cosmic","cat","lynx"))OrganicBodyMaterials(context,id) else null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val sampledSource = Rect()
    private val flexible = FlexibleSprite()
    private val rig = context.assets.open("canonical/$id/rig.json").bufferedReader()
        .use { JSONObject(it.readText()) }
    private val artWidth = rig.getDouble("width").toFloat()
    private val artHeight = rig.getDouble("height").toFloat()
    private val eye = rect(rig.getJSONArray("eye"))
    private val bodyBlink = rig.optBoolean("blinkBody", true)
    private val blinkDestination = rig.optJSONArray("blinkDestination")?.let(::rect)
    private val blinkPatches = rig.optJSONArray("blinkPatches")?.let { patches ->
        List(patches.length()) { index -> readFrame(patches.getJSONObject(index)) }
    }.orEmpty()
    private val mode = rig.optString("mode", "breath")
    private val layers = rig.getJSONArray("layers").let { items ->
        List(items.length()) { index ->
            val item = items.getJSONObject(index)
            val baseFrame = readFrame(item)
            val pivot = item.getJSONArray("pivot")
            Layer(baseFrame, item.optJSONObject("blink")?.let { readFrame(it, baseFrame.destination) },
                pivot.getDouble(0).toFloat(), pivot.getDouble(1).toFloat(),
                item.optDouble("motion", 0.0).toFloat(), item.optDouble("phase", 0.0),
                item.optDouble("restAngle", 0.0).toFloat(),
                item.optJSONArray("clip")?.let(::rect), item.optJSONObject("flex"), item.optJSONArray("cutouts"))
        }
    }
    private val parts = (layers.flatMap { layer ->
        listOfNotNull(layer.frame.name, layer.blink?.name)
    } + (if (bodyBlink) listOf("blink") else emptyList()) + (if(organic!=null)listOf("organic-atlas","organic-master") else emptyList())).distinct()
    private val started = SystemClock.uptimeMillis()
    private var attached = false
    private var tappedAt = -10000L

    init {
        contentDescription = rig.getString("name") + ". Tap to interact."
        isClickable = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attached = true
        invalidate()
    }
    override fun onDetachedFromWindow() {
        attached = false
        super.onDetachedFromWindow()
    }
    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == VISIBLE) invalidate()
    }
    override fun performClick(): Boolean {
        super.performClick()
        tappedAt = SystemClock.uptimeMillis()
        invalidate()
        return true
    }
    override fun onTouchEvent(event: MotionEvent): Boolean = when (event.actionMasked) {
        MotionEvent.ACTION_DOWN -> true
        MotionEvent.ACTION_UP -> { performClick(); true }
        else -> super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = SystemClock.uptimeMillis()
        val time = (now - started) / 1000.0
        val age = now - tappedAt
        val excited = age in 0..999
        val blinking = time > 0.5 && time % 5.2 < 0.17
        val art = CanonicalSpriteCache.get(context, id, parts.filter{id=="cat" || materials.hasMaterials || !it.startsWith("organic-")})
        val scale = min(width / artWidth, height / artHeight) * 0.86f
        canvas.save()
        canvas.translate(width / 2f, height / 2f)
        canvas.scale(scale, scale)
        canvas.translate(-artWidth / 2f, -artHeight / 2f)
        when (mode) {
            "squish" -> {
                val wave = sin(time * 1.6) * 0.012 +
                    if (excited) sin(age / 1000.0 * Math.PI) * 0.055 else 0.0
                canvas.scale((1 + wave).toFloat(), (1 - wave).toFloat(),
                    artWidth / 2f, artHeight * 0.86f)
            }
            "hover" -> {
                canvas.translate(0f, (sin(time * 1.1) * 9).toFloat())
                canvas.rotate((sin(time * 0.7) * 0.6).toFloat(),
                    artWidth / 2f, artHeight / 2f)
            }
            else -> {
                canvas.scale(1f, 1f + (sin(time * 1.3) * 0.004).toFloat(),
                    artWidth / 2f, artHeight * 0.92f)
                if (excited) canvas.translate(0f, (-sin(age / 1000.0 * Math.PI) * 12).toFloat())
            }
        }
        for (layer in layers) {
            val frame = if (blinking) layer.blink ?: layer.frame else layer.frame
            val bitmap = art.getValue(frame.name)
            val frequency = if (excited) 5.5 else 1.1
            val strength = if (excited) 3.0 else 1.0
            val angle = layer.restAngle + (sin(time * frequency + layer.phase) * layer.motion * strength).toFloat()
            canvas.save()
            layer.clip?.let { canvas.clipRect(it) }
            layer.cutouts?.let { cuts ->
                for (index in 0 until cuts.length()) {
                    val cut=cuts.getJSONObject(index)
                    val bounds=rect(cut.getJSONArray("rect"))
                    val path=android.graphics.Path().apply { addOval(bounds,android.graphics.Path.Direction.CW) }
                    val transform=android.graphics.Matrix().apply {
                        setRotate(cut.optDouble("angle",0.0).toFloat(),bounds.centerX(),bounds.centerY())
                    }
                    path.transform(transform)
                    canvas.clipOutPath(path)
                }
            }
            if (layer.flex == null) canvas.rotate(angle, layer.x, layer.y)
            sampledSource.set(
                frame.source.left * bitmap.width / frame.width,
                frame.source.top * bitmap.height / frame.height,
                frame.source.right * bitmap.width / frame.width,
                frame.source.bottom * bitmap.height / frame.height)
            paint.alpha = when {
                mode == "squish" && layer.frame.name == "body" ->
                    (246 + 9 * sin(time * 1.7)).toInt().coerceIn(237, 255)
                mode == "glimmer" && layer.frame.name == "body" ->
                    (244 + 11 * sin(time * 1.4)).toInt().coerceIn(233, 255)
                else -> 255
            }
            if (layer.flex != null) {
                val flex = layer.flex
                val root = flex.getJSONArray("root")
                val wave = sin(time * frequency + layer.phase).toFloat() *
                    flex.getDouble("amplitude").toFloat() * if (excited) 2f else 1f
                flexible.draw(canvas, bitmap, sampledSource, frame.destination,
                    root.getDouble(0).toFloat(), root.getDouble(1).toFloat(),
                    flex.getDouble("hold").toFloat(), flex.getDouble("reach").toFloat(), wave, paint)
            } else canvas.drawBitmap(bitmap, sampledSource, frame.destination, paint)
            if (bodyBlink && layer.frame.name == "body" && blinking) {
                if (blinkPatches.isEmpty()) {
                    canvas.save()
                    canvas.clipRect(eye)
                    canvas.drawBitmap(art.getValue("blink"), null,
                        blinkDestination ?: layer.frame.destination, paint)
                    canvas.restore()
                } else {
                    val closed = art.getValue("blink")
                    blinkPatches.forEach { patch ->
                        sampledSource.set(
                            patch.source.left * closed.width / patch.width,
                            patch.source.top * closed.height / patch.height,
                            patch.source.right * closed.width / patch.width,
                            patch.source.bottom * closed.height / patch.height)
                        canvas.drawBitmap(closed, sampledSource, patch.destination, paint)
                    }
                }
            }
            if(layer.frame.name=="body" && organic!=null && (id=="cat" || materials.hasMaterials)){
                canvas.save()
                val dest=frame.destination
                canvas.clipRect(dest)
                val sx=dest.width()/frame.source.width();val sy=dest.height()/frame.source.height()
                canvas.translate(dest.left-frame.source.left*sx,dest.top-frame.source.top*sy);canvas.scale(sx,sy)
                organic.draw(canvas,art.getValue("organic-atlas"),art.getValue("organic-master"),art.getValue("body"),
                    RectF(0f,0f,frame.width.toFloat(),frame.height.toFloat()),materials,time)
                canvas.restore()
            }
            canvas.restore()
        }
        paint.alpha = 255
        canvas.restore()
        if (attached && windowVisibility == VISIBLE && isShown) postInvalidateDelayed(40)
    }

    private data class Frame(
        val name: String, val width: Int, val height: Int,
        val source: Rect, val destination: RectF)
    private data class Layer(
        val frame: Frame, val blink: Frame?, val x: Float, val y: Float,
        val motion: Float, val phase: Double, val restAngle: Float, val clip: RectF?, val flex: JSONObject?, val cutouts: JSONArray?)

    companion object {
        private fun rect(a: JSONArray) = RectF(
            a.getDouble(0).toFloat(), a.getDouble(1).toFloat(),
            a.getDouble(2).toFloat(), a.getDouble(3).toFloat())
        private fun readFrame(item: JSONObject, destination: RectF? = null): Frame {
            val size = item.getJSONArray("size")
            val source = item.getJSONArray("source")
            return Frame(item.getString("name"), size.getInt(0), size.getInt(1),
                Rect(source.getInt(0), source.getInt(1), source.getInt(2), source.getInt(3)),
                item.optJSONArray("destination")?.let(::rect)
                    ?: requireNotNull(destination))
        }
    }
}






