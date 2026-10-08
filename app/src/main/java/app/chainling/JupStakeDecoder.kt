package app.chainling
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest

/** Layout from the official vote.jup.ag locked_voter IDL, checked 2026-10-07. */
object JupStakeDecoder {
    const val PROGRAM="voTpe3tHQ7AjQHMapgSue2HJFAh2cGsdokqN3XqmVSj"
    const val LOCKER="CVMdMd79no569tjc5Sq7kzz8isbfCcFyBS5TLGsrZ5dN"
    const val MINT="JUPyiwrYJFskUPiHa7hkeR8VUtAeFoSYbKedZNsDvCN"
    fun discriminator(name:String)=MessageDigest.getInstance("SHA-256").digest(("account:"+name).toByteArray()).copyOf(8)
    private fun key(b:ByteArray,offset:Int)=Base58.encode(b.copyOfRange(offset,offset+32))
    private fun signed(b:ByteArray,offset:Int)=ByteBuffer.wrap(b,offset,8).order(ByteOrder.LITTLE_ENDIAN).long
    fun verifyLocker(b:ByteArray){
        require(b.size>=73 && b.copyOf(8).contentEquals(discriminator("Locker"))){"Invalid JUP Locker"}
        require(key(b,41)==MINT){"Unexpected governance mint"}
    }
    fun activeAmount(b:ByteArray,owner:String,now:Long):Double {
        require(b.size>=322 && b.copyOf(8).contentEquals(discriminator("Escrow"))){"Invalid JUP Escrow"}
        require(key(b,8)==LOCKER && key(b,40)==owner){"JUP escrow owner/locker mismatch"}
        require(b[161].toInt() in 0..1){"Invalid max-lock flag"}
        val amount=BigInteger(1,b.copyOfRange(105,113).reversedArray())
        val start=signed(b,113);val end=signed(b,121)
        // Partial unstaking has already left the escrow amount; separate pending accounts are excluded.
        val active=b[161].toInt()==1 || (start>0 && start<=now && end>now)
        return if(active)amount.toBigDecimal().movePointLeft(6).toDouble() else 0.0
    }
}

