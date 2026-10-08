package app.chainling
import org.junit.Assert.*
import org.junit.Test

class JointMotionTest {
    @Test fun attachmentZoneDoesNotMoveEvenAtTapExtremes() {
        for (x in 80..120 step 5) for(y in 180..220 step 5) {
            val weight=JointMotion.weight(x.toFloat(),y.toFloat(),100f,200f,30f,150f)
            assertEquals(0f,weight,0f)
            assertEquals(y.toFloat(),y+35f*weight,0f)
        }
    }
    @Test fun bendIsContinuousBoundedAndReachesTip() {
        var previous=0f
        for(distance in 0..180) {
            val weight=JointMotion.weight(distance.toFloat(),0f,0f,0f,30f,150f)
            assertTrue(weight in 0f..1f)
            assertTrue(weight>=previous)
            assertTrue(weight-previous<.02f)
            previous=weight
        }
        assertEquals(1f,previous,0f)
    }
}

