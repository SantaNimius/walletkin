package app.chainling

import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import java.math.BigInteger
import android.util.Base64

class Rpc(private val endpoint: String) {
    init { require(URL(endpoint).protocol == "https") { "RPC must use HTTPS" } }
    suspend fun call(method: String, params: Any): Any = withContext(Dispatchers.IO) {
        var last: Exception? = null
        for (attempt in 0..2) {
            ensureActive()
            val conn = URL(endpoint).openConnection() as HttpsURLConnection
            try {
                conn.requestMethod = "POST"; conn.connectTimeout = 12000; conn.readTimeout = 20000; conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                val body = JSONObject().put("jsonrpc", "2.0").put("id", 1).put("method", method).put("params", params)
                conn.outputStream.use { it.write(body.toString().toByteArray()) }
                val status = conn.responseCode
                if (status == 429 || status >= 500) throw java.io.IOException("RPC temporarily busy ($status)")
                require(status in 200..299) { "RPC rejected request ($status)" }
                val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                if (json.has("error")) throw IllegalStateException("RPC $method failed (${json.getJSONObject("error").optInt("code")})")
                return@withContext json.get("result")
            } catch(e: CancellationException) { throw e }
            catch(e: Exception) { last = e; if (e is IllegalStateException || e is IllegalArgumentException) break }
            finally { conn.disconnect() }
            delay(800L * (attempt + 1))
        }
        throw last ?: IllegalStateException("RPC unavailable")
    }
}
object StakeDecoder {
    val userDiscriminator = byteArrayOf(102,53,163.toByte(),107,9,138.toByte(),87,153.toByte())
    val configDiscriminator = byteArrayOf(238.toByte(),151.toByte(),43,3,11,151.toByte(),63,176.toByte())
    fun uint128(bytes: ByteArray, offset: Int): BigInteger { require(bytes.size >= offset + 16); return BigInteger(1, bytes.copyOfRange(offset, offset + 16).reversedArray()) }
    fun shares(bytes: ByteArray, user: String, config: String): BigInteger {
        require(bytes.size >= 169 && bytes.take(8).toByteArray().contentEquals(userDiscriminator)) { "Invalid UserStake layout" }
        require(Base58.encode(bytes.copyOfRange(9,41)) == config && Base58.encode(bytes.copyOfRange(41,73)) == user) { "Stake owner mismatch" }
        return uint128(bytes,105)
    }
    fun price(bytes: ByteArray): BigInteger {
        require(bytes.size >= 153 && bytes.take(8).toByteArray().contentEquals(configDiscriminator)) { "Invalid StakeConfig layout" }
        require(Base58.encode(bytes.copyOfRange(41,73)) == Analyzer.SKR_MINT) { "Unexpected staking mint" }
        return uint128(bytes,137)
    }
}
class Analyzer(private val rpc: Rpc) {
    companion object {
        const val SKR_MINT = "SKRbvo6Gf7GondiT3BbTfuRDPqLWei4j2Qy2NPGZhW3"
        const val TOKEN = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
        const val TOKEN22 = "TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb"
        const val SGT = "GT22s89nU4iWFkNXj1Bw6uYhJJWDRPpShHt4Bk8f99Te"
        const val CONFIG = "4HQy82s9CHTv1GsYKnANHMiHfhcqesYkK6sB3RDSYyqw"
    }
    private fun params(vararg values: Any) = JSONArray(values.toList())
    private suspend fun tokens(address: String, program: String): List<JSONObject> {
        val r = rpc.call("getTokenAccountsByOwner", params(address, JSONObject().put("programId", program), JSONObject().put("encoding", "jsonParsed").put("commitment", "confirmed"))) as JSONObject
        return r.getJSONArray("value").objects().map { it.getJSONObject("account").getJSONObject("data").getJSONObject("parsed").getJSONObject("info") }.filter { it.getJSONObject("tokenAmount").getString("amount") != "0" }
    }
    private suspend fun seeker(tokens: List<JSONObject>): Boolean {
        val candidates = tokens.filter { it.getJSONObject("tokenAmount").optInt("decimals") == 0 && it.getJSONObject("tokenAmount").getString("amount") == "1" }
        require(candidates.size <= 100) { "Seeker verification exceeds scan budget" }
        for (token in candidates) {
            val result = rpc.call("getAccountInfo", params(token.getString("mint"), JSONObject().put("encoding","jsonParsed").put("commitment","confirmed"))) as JSONObject
            val account = result.optJSONObject("value") ?: throw IllegalStateException("Mint unavailable")
            require(account.getString("owner") == TOKEN22)
            val info = account.getJSONObject("data").getJSONObject("parsed").getJSONObject("info")
            val extensions = info.optJSONArray("extensions")?.objects() ?: emptyList()
            val metadata = extensions.any { it.optString("extension") == "metadataPointer" && it.optJSONObject("state")?.optString("metadataAddress") == SGT }
            val group = extensions.any { it.optString("extension") == "tokenGroupMember" && it.optJSONObject("state")?.optString("group") == SGT }
            if (metadata && group) return true
        }
        return false
    }
    private suspend fun staked(address: String): Double {
        val cfg = rpc.call("getAccountInfo", params(CONFIG,JSONObject().put("encoding","base64").put("commitment","confirmed"))) as JSONObject
        val account = cfg.getJSONObject("value"); require(account.getString("owner") == Evolution.SKR_PROGRAM)
        val price = StakeDecoder.price(Base64.decode(account.getJSONArray("data").getString(0),Base64.DEFAULT))
        val filters = JSONArray().put(JSONObject().put("memcmp",JSONObject().put("offset",0).put("bytes",Base58.encode(StakeDecoder.userDiscriminator)))).put(JSONObject().put("memcmp",JSONObject().put("offset",41).put("bytes",address)))
        val stakes = rpc.call("getProgramAccounts", params(Evolution.SKR_PROGRAM,JSONObject().put("encoding","base64").put("commitment","confirmed").put("filters",filters))) as JSONArray
        val shares = stakes.objects().fold(BigInteger.ZERO) { total, s -> total + StakeDecoder.shares(Base64.decode(s.getJSONObject("account").getJSONArray("data").getString(0),Base64.DEFAULT),address,CONFIG) }
        return shares.multiply(price).toBigDecimal().divide(java.math.BigDecimal("1000000000000000"),6,java.math.RoundingMode.DOWN).toDouble()
    }
    private suspend fun jupStaked(address:String):Double {
        val config=rpc.call("getAccountInfo",params(JupStakeDecoder.LOCKER,JSONObject().put("encoding","base64").put("commitment","confirmed"))) as JSONObject
        val locker=config.getJSONObject("value")
        require(locker.getString("owner")==JupStakeDecoder.PROGRAM)
        JupStakeDecoder.verifyLocker(Base64.decode(locker.getJSONArray("data").getString(0),Base64.DEFAULT))
        val filters=JSONArray().put(JSONObject().put("memcmp",JSONObject().put("offset",0).put("bytes",Base58.encode(JupStakeDecoder.discriminator("Escrow")))))
            .put(JSONObject().put("memcmp",JSONObject().put("offset",8).put("bytes",JupStakeDecoder.LOCKER)))
            .put(JSONObject().put("memcmp",JSONObject().put("offset",40).put("bytes",address)))
        val accounts=rpc.call("getProgramAccounts",params(JupStakeDecoder.PROGRAM,JSONObject().put("encoding","base64").put("commitment","confirmed").put("filters",filters))) as JSONArray
        require(accounts.length()<=4){"JUP escrow scan budget exceeded"}
        val now=System.currentTimeMillis()/1000
        return accounts.objects().sumOf {
            val account=it.getJSONObject("account");require(account.getString("owner")==JupStakeDecoder.PROGRAM)
            JupStakeDecoder.activeAmount(Base64.decode(account.getJSONArray("data").getString(0),Base64.DEFAULT),address,now)
        }
    }
    suspend fun scan(address: String, prior: Snapshot?, progress: (String) -> Unit): Snapshot {
        Genesis.from(address)
        progress("Verifying Solana mainnet")
        require(rpc.call("getGenesisHash",JSONArray()) == "5eykt4UsFv8P8NJdTREpY1vzqKqZKvdpKuc147dw2N9d") { "Endpoint is not Solana mainnet" }
        val warnings = mutableListOf<String>()
        suspend fun <T> optional(label: String, action: suspend () -> T): T? = try { withTimeoutOrNull(12000) { action() } ?: run { warnings.add("$label timed out. Rescan can retry this check."); null } } catch(e: CancellationException){throw e} catch(e: Exception){warnings.add("$label unavailable. Rescan can retry this check."); null}
        progress("Reading wallet balances")
        val sol = (rpc.call("getBalance", params(address,JSONObject().put("commitment","confirmed"))) as JSONObject).getLong("value") / 1e9
        val spl = optional("SPL tokens") { tokens(address,TOKEN) }
        val t22 = optional("Token-2022 tokens") { tokens(address,TOKEN22) }
        val skr = if(spl != null && t22 != null) (spl + t22).filter { it.getString("mint") == SKR_MINT }.sumOf { it.getJSONObject("tokenAmount").optString("uiAmountString","0").toDouble() } else null
        val seeker = if(t22 != null) optional("Seeker Genesis verification") { seeker(t22) } else null
        progress("Checking SKR Resonance")
        val stake = optional("SKR staking") { staked(address) }
        progress("Checking JUP governance stake")
        val jupStake = optional("JUP governance staking") { jupStaked(address) }
        progress("Tracing confirmed memories")
        val history = optional("Recent history") { rpc.call("getSignaturesForAddress",params(address,JSONObject().put("limit",100).put("commitment","confirmed"))) as JSONArray }
        val sigs = history ?: JSONArray()
        val retained = prior?.evidence?.associateBy { it.signature }?.toMutableMap() ?: mutableMapOf()
        val missing = sigs.objects().filter { it.isNull("err") && !retained.containsKey(it.getString("signature")) }
        // Bounded RPC work: repeat rescan to fill older missing items without discarding previous evidence.
        var detailFailures = 0
        val detailStart = System.nanoTime()
        for ((index, s) in missing.take(20).withIndex()) {
            if(System.nanoTime() - detailStart > 60_000_000_000L) { warnings.add("History detail scan budget reached. Rescan to continue."); break }
            progress("Reading memory ${index + 1}/${minOf(20,missing.size)}")
            val tx = optional("Transaction detail") { rpc.call("getTransaction",params(s.getString("signature"),JSONObject().put("encoding","jsonParsed").put("commitment","confirmed").put("maxSupportedTransactionVersion",0))) }
            if(tx is JSONObject) { retained[s.getString("signature")] = parseTransaction(address,s.getString("signature"),tx); detailFailures = 0 }
            else { detailFailures++; if(tx == JSONObject.NULL) warnings.add("A transaction detail was not available yet.") }
            if(detailFailures >= 3) { warnings.add("History details paused after repeated RPC failures. Rescan to continue."); break }
            delay(500)
        }
        return Snapshot(address,sol,skr,stake,seeker,if(spl == null || t22 == null) null else (spl + t22).filter { it.getJSONObject("tokenAmount").optInt("decimals") > 0 }.map { it.getString("mint") }.toSet().size,retained.values.toList(),System.currentTimeMillis()/1000,warnings.distinct(),history == null || sigs.length() >= 100 || missing.any { !retained.containsKey(it.getString("signature")) },jupStake)
    }
    internal fun parseTransaction(address: String, signature: String, tx: JSONObject): Evidence {
        val meta = tx.getJSONObject("meta"); val message = tx.getJSONObject("transaction").getJSONObject("message")
        val keys = message.getJSONArray("accountKeys").objects(); val idx = keys.indexOfFirst { it.getString("pubkey") == address }
        val instructions = message.getJSONArray("instructions").objects() + (meta.optJSONArray("innerInstructions")?.objects()?.flatMap { it.getJSONArray("instructions").objects() } ?: emptyList())
        val programs = instructions.mapNotNull { it.optString("programId").takeIf { p -> p.isNotEmpty() } }.toSet()
        val pre = meta.getJSONArray("preBalances"); val post = meta.getJSONArray("postBalances")
        val delta = if(idx >= 0) post.getLong(idx) - pre.getLong(idx) else 0
        val fee = if(idx == 0) meta.optLong("fee") else 0
        val isSigner = idx >= 0 && keys[idx].optBoolean("signer")
        val preSkr = meta.optJSONArray("preTokenBalances")?.objects().orEmpty().filter { it.optString("mint") == SKR_MINT && it.optString("owner") == address }.associate { it.getInt("accountIndex") to it.getJSONObject("uiTokenAmount").getString("amount") }
        val postSkr = meta.optJSONArray("postTokenBalances")?.objects().orEmpty().filter { it.optString("mint") == SKR_MINT && it.optString("owner") == address }.associate { it.getInt("accountIndex") to it.getJSONObject("uiTokenAmount").getString("amount") }
        val skr = (preSkr.keys + postSkr.keys).any { (preSkr[it] ?: "0") != (postSkr[it] ?: "0") }
        return Evidence(signature,if(tx.isNull("blockTime")) null else tx.getLong("blockTime"),programs,meta.isNull("err"),isSigner && instructions.any { it.optString("programId") == "11111111111111111111111111111111" && it.optJSONObject("parsed")?.optString("type") == "transfer" && it.optJSONObject("parsed")?.optJSONObject("info")?.optString("source") == address },delta > 0,instructions.any { it.optJSONObject("parsed")?.optString("type") in listOf("transfer","transferChecked") && it.optString("programId") in setOf(TOKEN,TOKEN22) },programs.any { it.startsWith("MemoSq4") || it.startsWith("Memo1U") },"metaqbxxUerdq28cj1RbAWkYQm3ybzjb6a8bt518x1s" in programs,skr)
    }
}
fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }







