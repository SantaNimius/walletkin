package app.chainling

import kotlin.math.sqrt

/** A soft bend leaves an entire attachment zone stationary, including tap reactions. */
object JointMotion {
    fun weight(x: Float, y: Float, rootX: Float, rootY: Float, hold: Float, reach: Float): Float {
        require(hold >= 0f && reach > hold)
        val distance = sqrt((x-rootX)*(x-rootX) + (y-rootY)*(y-rootY))
        val t = ((distance-hold)/(reach-hold)).coerceIn(0f,1f)
        return t*t*(3f-2f*t)
    }
}

