package app.chainling
import org.junit.Assert.*
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
class SkrPaymentTest {
    private val wallet="6y8jfXZHHSYYaVLaEDnokW3MEKg1JfzA9S1Ld6zCv4SF"
    private fun order()=CheckoutOrder("00000000-0000-0000-0000-000000000001",wallet,"premium",
        "FMj38thjSJXCic8CvDXEiusiXgYG1g1EF7zrzMKxrLmC","11111111111111111111111111111111",
        Analyzer.CONFIG,100000000L,"100","11111111111111111111111111111111",9999999999)
    @Test fun signingRequestContainsOnlyCheckedSkrTransferAndOrderMemo(){
        val tx=SkrPayment.transaction(order());assertTrue(tx.size<=1232)
        assertEquals(1,tx[0].toInt());assertTrue(tx.copyOfRange(1,65).all{it==0.toByte()})
        assertArrayEquals(byteArrayOf(1,0,3,6),tx.copyOfRange(65,69))
        val offset=69+6*32+32
        assertArrayEquals(byteArrayOf(2,4,4,1,3,2,0,10,12),tx.copyOfRange(offset,offset+9))
        assertEquals(100000000L,ByteBuffer.wrap(tx,offset+9,8).order(ByteOrder.LITTLE_ENDIAN).long)
        assertEquals(6,tx[offset+17].toInt())
        assertTrue(tx.toString(Charsets.ISO_8859_1).endsWith("walletkin:"+order().id))
    }
    @Test fun accountOverlapAndInvalidAddressesCannotReachSigning(){
        assertThrows(IllegalArgumentException::class.java){SkrPayment.transaction(order().copy(source=wallet))}
        assertThrows(IllegalArgumentException::class.java){SkrPayment.transaction(order().copy(destination="bad0"))}
    }
    @Test fun productThresholdCanIncreaseWithoutNewApp(){
        assertEquals(MembershipEligibility.FREE,PremiumPolicy.eligibility(150.0,200.0))
        assertEquals(MembershipEligibility.ELIGIBLE,PremiumPolicy.eligibility(200.0,200.0))
        assertEquals(MembershipEligibility.UNKNOWN,PremiumPolicy.eligibility(200.0,Double.NaN))
    }
}

