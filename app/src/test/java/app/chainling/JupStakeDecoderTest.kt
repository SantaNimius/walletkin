package app.chainling
import org.junit.Assert.*
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
class JupStakeDecoderTest {
    private val owner="6y8jfXZHHSYYaVLaEDnokW3MEKg1JfzA9S1Ld6zCv4SF"
    private fun escrow(max:Boolean=true,start:Long=100,end:Long=200):ByteArray {
        val b=ByteArray(322);JupStakeDecoder.discriminator("Escrow").copyInto(b)
        Base58.decode(JupStakeDecoder.LOCKER).copyInto(b,8);Base58.decode(owner).copyInto(b,40)
        ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).putLong(105,100000000).putLong(113,start).putLong(121,end)
        b[161]=if(max)1 else 0;return b
    }
    @Test fun activeAndCooldownAreBounded(){
        assertEquals(100.0,JupStakeDecoder.activeAmount(escrow(),owner,300),0.0)
        assertEquals(100.0,JupStakeDecoder.activeAmount(escrow(false),owner,150),0.0)
        assertEquals(0.0,JupStakeDecoder.activeAmount(escrow(false),owner,200),0.0)
        assertEquals(0.0,JupStakeDecoder.activeAmount(escrow(false,180,220),owner,150),0.0)
    }
    @Test fun rejectsUnrelatedOrMalformedAccounts(){
        for(b in listOf(escrow().apply{this[0]=0},escrow().apply{this[40]=0},escrow().apply{this[8]=0},escrow().copyOf(161),escrow().apply{this[161]=2})){
            try{JupStakeDecoder.activeAmount(b,owner,150);fail("Accepted invalid escrow")}catch(_:IllegalArgumentException){}
        }
    }
}

