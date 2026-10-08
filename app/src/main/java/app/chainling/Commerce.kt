package app.chainling
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CommerceApi(val endpoint:String) {
    init{val u=URL(endpoint);require(u.protocol=="https" && u.userInfo==null && u.query==null && u.ref==null){"Use a trusted HTTPS commerce endpoint"}}
    suspend fun request(path:String,body:JSONObject?=null):JSONObject=withContext(Dispatchers.IO){
        val c=URL(endpoint.trimEnd('/')+path).openConnection() as HttpsURLConnection
        try{
            c.instanceFollowRedirects=false;c.connectTimeout=15000;c.readTimeout=25000
            c.setRequestProperty("Accept","application/json")
            if(body!=null){c.requestMethod="POST";c.doOutput=true;c.setRequestProperty("Content-Type","application/json");c.outputStream.use{it.write(body.toString().toByteArray(Charsets.UTF_8))}}
            val status=c.responseCode
            val stream=if(status in 200..299)c.inputStream else c.errorStream
            val bytes=stream?.use{
                val out=ByteArrayOutputStream();val buffer=ByteArray(4096)
                while(out.size()<=131072){val count=it.read(buffer);if(count<0)break;out.write(buffer,0,count)}
                out.toByteArray()
            } ?: byteArrayOf()
            require(bytes.size<=131072){"Commerce response too large"}
            val json=JSONObject(bytes.toString(Charsets.UTF_8))
            require(status in 200..299){json.optString("error","Commerce service unavailable")}
            json
        }finally{c.disconnect()}
    }
}
data class CheckoutOrder(val id:String,val wallet:String,val item:String,val recipient:String,
    val source:String,val destination:String,val amountUnits:Long,val priceSkr:String,
    val blockhash:String,val expiresAt:Long){
    companion object{
        fun parse(j:JSONObject):CheckoutOrder{
            require(j.getString("mint")==Analyzer.SKR_MINT && j.getInt("decimals")==6){"Unexpected payment token"}
            return CheckoutOrder(j.getString("id"),j.getString("wallet"),j.getString("item"),
                j.getString("recipient"),j.getString("source"),j.getString("destination"),
                j.getString("amountUnits").toLong().also{require(it>0)},j.getString("priceSkr"),j.getString("blockhash"),j.getLong("expiresAt"))
        }
    }
}
/** Local construction; the server cannot insert arbitrary signing instructions into its quote. */
object SkrPayment {
    const val MEMO="MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr"
    fun transaction(o:CheckoutOrder):ByteArray{
        val keys=listOf(o.wallet,o.source,o.destination,Analyzer.SKR_MINT,Analyzer.TOKEN,MEMO)
        require(keys.toSet().size==6){"Payment accounts overlap"}
        val decoded=keys.map{Base58.decode(it).also{b->require(b.size==32)}}
        val block=Base58.decode(o.blockhash).also{require(it.size==32)}
        val memo=("walletkin:"+o.id).toByteArray(Charsets.UTF_8);require(memo.size in 1..120)
        val transfer=ByteBuffer.allocate(10).order(ByteOrder.LITTLE_ENDIAN).put(12.toByte()).putLong(o.amountUnits).put(6.toByte()).array()
        val out=ByteArrayOutputStream()
        fun compact(n:Int){var v=n;do{val b=v and 127;v=v ushr 7;out.write(b or if(v>0)128 else 0)}while(v>0)}
        out.write(1);out.write(ByteArray(64)) // one empty signature for MWA
        out.write(byteArrayOf(1,0,3));compact(6);decoded.forEach{out.write(it)};out.write(block)
        compact(2)
        out.write(4);compact(4);out.write(byteArrayOf(1,3,2,0));compact(10);out.write(transfer)
        out.write(5);compact(1);out.write(0);compact(memo.size);out.write(memo)
        return out.toByteArray().also{require(it.size<=1232)}
    }
    suspend fun validateAccounts(o:CheckoutOrder,rpc:Rpc){
        require(rpc.call("getGenesisHash",org.json.JSONArray())=="5eykt4UsFv8P8NJdTREpY1vzqKqZKvdpKuc147dw2N9d"){"Mainnet required"}
        suspend fun account(key:String,owner:String,minimum:Long){
            val r=rpc.call("getAccountInfo",org.json.JSONArray().put(key).put(JSONObject().put("encoding","jsonParsed").put("commitment","confirmed"))) as JSONObject
            val a=r.getJSONObject("value");require(a.getString("owner")==Analyzer.TOKEN)
            val info=a.getJSONObject("data").getJSONObject("parsed").getJSONObject("info")
            require(info.getString("owner")==owner && info.getString("mint")==Analyzer.SKR_MINT && info.getString("state")=="initialized")
            val amount=info.getJSONObject("tokenAmount")
            require(amount.getInt("decimals")==6 && amount.getString("amount").toBigInteger()>=minimum.toBigInteger())
        }
        account(o.source,o.wallet,o.amountUnits);account(o.destination,o.recipient,0)
    }
}
object CommerceSession {
    var endpoint:String?=null
    var config:JSONObject?=null
    val wallets=mutableMapOf<String,JSONObject>()
    fun reset(){endpoint=null;config=null;wallets.clear()}
    fun access(address:String):Boolean{
        val j=wallets[address] ?: return false
        val checked=j.optLong("checkedAt");val age=System.currentTimeMillis()/1000-checked
        if(age !in 0..300)return false
        val p=j.optJSONObject("premium") ?: return false
        val now=System.currentTimeMillis()/1000
        return p.optString("status") in setOf("ACTIVE","GRACE_PERIOD") &&
            p.optLong("expiresAt")>now && (p.optString("status")!="GRACE_PERIOD" || p.optLong("graceUntil")>now)
    }
    fun owned(address:String):Set<String>{
        val j=wallets[address] ?: return emptySet()
        return j.optJSONArray("inventory")?.objects()?.map{it.getString("item")}?.toSet() ?: emptySet()
    }
}


