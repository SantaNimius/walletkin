package app.chainling

import android.content.Context
import org.json.JSONObject
import org.json.JSONArray

class ColonyStore(context: Context) {
    private val prefs = context.getSharedPreferences("colony-v1",Context.MODE_PRIVATE)
    fun takeMigrationNotice():Boolean {
        val pending=prefs.getBoolean("canonical-migration-notice",false)
        if(pending)prefs.edit().putBoolean("canonical-migration-notice",false).apply()
        return pending
    }
    fun addresses(): List<String> = prefs.getStringSet("addresses",emptySet())!!.sorted()
    fun load(address: String): Profile? = try {
        val j = JSONObject(prefs.getString("wallet:$address",null) ?: throw IllegalStateException("No profile"))
        val events = j.getJSONArray("events").objects().map { e -> Evidence(e.getString("signature"),if(e.isNull("time")) null else e.getLong("time"),(0 until e.getJSONArray("programs").length()).map { e.getJSONArray("programs").getString(it) }.toSet(),e.getBoolean("success"),e.getBoolean("outgoing"),e.getBoolean("incoming"),e.getBoolean("transfer"),e.getBoolean("memo"),e.getBoolean("nft"),e.optBoolean("skr")) }
        val snapshot = Snapshot(address,j.getDouble("sol"),j.nullDouble("skr"),j.nullDouble("stake"),if(j.isNull("seeker")) null else j.getBoolean("seeker"),if(j.isNull("tokens")) null else j.getInt("tokens"),events,j.getLong("scannedAt"),(0 until j.getJSONArray("warnings").length()).map { j.getJSONArray("warnings").getString(it) },j.getBoolean("limited"),j.nullDouble("jupStake"))
        Evolution.derive(snapshot,j.optBoolean("seekerborn"),ResonancePersistence.decode(j.optJSONObject("resonance"))).also { migrated -> if (j.optInt("dnaVersion",1) < 2) { prefs.edit().putBoolean("canonical-migration-notice",true).apply(); save(migrated) } }
    } catch(e: Exception) { null }
    fun save(p: Profile) {
        val s = p.snapshot
        val j = JSONObject().put("version",1).put("dnaVersion",p.dna.version).put("dnaSeed",p.dna.seed).put("speciesId",p.dna.species.id).put("paletteIndex",p.dna.palette).put("legacyGenesisSeed",Genesis.from(s.address).seed).put("sol",s.sol).put("skr",s.skrBalance ?: JSONObject.NULL).put("stake",s.stakedSkr ?: JSONObject.NULL).put("jupStake",s.stakedJup ?: JSONObject.NULL).put("seeker",s.seeker ?: JSONObject.NULL).put("seekerborn",p.seekerborn).put("tokens",s.tokenKinds ?: JSONObject.NULL).put("scannedAt",s.scannedAt).put("warnings",JSONArray(s.warnings)).put("limited",s.historyLimited)
        j.put("resonance",ResonancePersistence.encode(p.resonance))
        j.put("events",JSONArray(s.evidence.map { e -> JSONObject().put("signature",e.signature).put("time",e.time ?: JSONObject.NULL).put("programs",JSONArray(e.programs.toList())).put("success",e.success).put("outgoing",e.outgoing).put("incoming",e.incoming).put("transfer",e.tokenTransfer).put("memo",e.memo).put("nft",e.nft).put("skr",e.skr) }))
        check(prefs.edit().putString("wallet:${s.address}",j.toString()).putStringSet("addresses",(addresses() + s.address).toSet()).commit()) { "Could not save Colony" }
    }
    fun backgroundChoice(address: String): String? = prefs.getString("background:$address",null)
    fun chooseBackground(address: String, id: String?) {
        val editor=prefs.edit()
        if(id==null)editor.remove("background:$address") else editor.putString("background:$address",id)
        editor.apply()
    }
    fun remove(address: String) { prefs.edit().remove("wallet:$address").remove("background:$address").putStringSet("addresses",addresses().filter { it != address }.toSet()).apply() }
    var rpc: String
        get() = prefs.getString("rpc","https://api.mainnet-beta.solana.com")!!
        set(value) { prefs.edit().putString("rpc",value).apply() }
}
private fun JSONObject.nullDouble(key: String) = if(isNull(key)) null else getDouble(key)





