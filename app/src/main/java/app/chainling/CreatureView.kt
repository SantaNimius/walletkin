package app.chainling

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import kotlin.math.cos
import kotlin.math.sin

/** Same canonical body and material traits across reveal, profile, Colony, mutation and share. */
class CreatureView(context:Context,var profile:Profile,private val traitPreview:Boolean=false,private val applyCosmetics:Boolean=true):
    CanonicalSceneView(context,profile.dna.species.id,profile.dna.palette) {
    init {
        if(!traitPreview && applyCosmetics){
            CosmeticEquipment.item(context,profile.snapshot.address,"Skins")?.palette?.let{paletteIndex=it}
            CosmeticEquipment.item(context,profile.snapshot.address,"Effects")?.effect?.let{addView(CosmeticEffect(context,it),LayoutParams(-1,-1))}
        }
    }
    private val effects=Paint(Paint.ANTI_ALIAS_FLAG)
    private val surface=CreatureTraits(context)
    private val start=SystemClock.uptimeMillis()
    override fun dispatchDraw(canvas:Canvas) {
        (getChildAt(0) as? AstrafoxView)?.materials=OrganicTraitSelection.from(profile,traitPreview)
        (getChildAt(0) as? SharkView)?.materials=OrganicTraitSelection.from(profile,traitPreview)
        (getChildAt(0) as? OrganicMaterialHost)?.materials=OrganicTraitSelection.from(profile,traitPreview)
        val time=(SystemClock.uptimeMillis()-start)/1000.0
        val w=width.toFloat();val h=height.toFloat()
        val materialLayer=canvas.saveLayer(0f,0f,w,h,null)
        super.dispatchDraw(canvas)
        // Painted material hosts replace the rejected screen-space procedural overlays.
        canvas.restoreToCount(materialLayer)
        val radius=minOf(w,h)*.44f
        effects.style=Paint.Style.FILL
        val phase=profile.dna.seed.take(4).toInt(16)/65535.0*Math.PI*2
        for(i in 0 until minOf(profile.memories.size,24)){
            val angle=phase+i*Math.PI*2/24+time*.1
            effects.color=profile.dna.species.color
            canvas.drawCircle(w*.5f+(cos(angle)*radius).toFloat(),h*.52f+(sin(angle)*radius*.84).toFloat(),resources.displayMetrics.density*1.4f,effects)
        }
        if(profile.resonance.active || traitPreview){
            effects.color=if(profile.resonance.mode==ResonanceMode.GENESIS || traitPreview)0x80A6FFF0.toInt() else 0x70D6FF91
            effects.style=Paint.Style.STROKE;effects.strokeWidth=resources.displayMetrics.density*1.2f
            canvas.drawOval(w*.5f-radius,h*.52f-radius*.9f,w*.5f+radius,h*.52f+radius*.9f,effects)
            effects.style=Paint.Style.FILL
            for(i in 0..5){
                val angle=phase+time*.25+i*Math.PI/3
                canvas.drawCircle(w*.5f+(cos(angle)*radius*.95).toFloat(),
                    h*.52f+(sin(angle)*radius*.85).toFloat(),resources.displayMetrics.density*1.7f,effects)
            }
        }
        if(isAttachedToWindow && windowVisibility==VISIBLE && isShown &&
            (profile.resonance.active || traitPreview || profile.memories.isNotEmpty()))postInvalidateDelayed(40)
    }
    fun render(canvas:Canvas,p:Profile,w:Float,h:Float,time:Double=0.0) {
        if(w<=0 || h<=0)return
        require(p.dna==profile.dna){"Share must use the displayed wallet DNA"}
        measure(MeasureSpec.makeMeasureSpec(w.toInt(),MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(h.toInt(),MeasureSpec.EXACTLY))
        layout(0,0,w.toInt(),h.toInt());draw(canvas)
    }
}






