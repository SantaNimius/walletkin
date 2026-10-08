package app.chainling
import org.junit.Assert.*
import org.junit.Test
class KongMotionTest {
    @Test fun vestFaceAndShoulderAnchorsStayStationary(){
        for(p in listOf(390f to 350f,600f to 400f,890f to 170f,1050f to 440f,650f to 580f)){
            assertEquals(0f,KongMotion.handWeight(p.first,p.second,false),0f)
            assertEquals(0f,KongMotion.handWeight(p.first,p.second,true),0f)
        }
    }
    @Test fun bothHandsMoveWithinBoundedRange(){
        assertTrue(KongMotion.handWeight(240f,1080f,false)>.8f)
        assertTrue(KongMotion.handWeight(1190f,1130f,true)>.8f)
        for(x in 0..1312 step 32)for(y in 0..1199 step 32){
            assertTrue(KongMotion.handWeight(x.toFloat(),y.toFloat(),false) in 0f..1f)
            assertTrue(KongMotion.handWeight(x.toFloat(),y.toFloat(),true) in 0f..1f)
        }
    }
}

