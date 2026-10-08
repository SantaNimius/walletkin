package app.chainling

import java.security.MessageDigest
import java.math.BigInteger

object Base58 {
    private const val alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
    fun encode(bytes: ByteArray): String {
        var n = BigInteger(1, bytes); val out = StringBuilder()
        while (n.signum() > 0) { val qr = n.divideAndRemainder(BigInteger.valueOf(58)); out.append(alphabet[qr[1].toInt()]); n = qr[0] }
        bytes.takeWhile { it == 0.toByte() }.forEach { out.append('1') }
        return out.reverse().toString()
    }
    fun decode(s: String): ByteArray {
        require(s.isNotEmpty() && s.all { it in alphabet }) { "Invalid Solana address" }
        var n = BigInteger.ZERO
        for (c in s) n = n.multiply(BigInteger.valueOf(58)).add(BigInteger.valueOf(alphabet.indexOf(c).toLong()))
        val raw = if (n == BigInteger.ZERO) byteArrayOf() else n.toByteArray().let { if (it[0] == 0.toByte()) it.drop(1).toByteArray() else it }
        return ByteArray(s.takeWhile { it == '1' }.length) + raw
    }
}
data class Species(val id: String, val name: String, val subtitle: String, val color: Int, val silhouette: Int)
object SpeciesRegistry {
    val active = listOf(
        Species("ember", "Emberkin", "A spark that remembers", 0xFFFF986D.toInt(), 0),
        Species("tide", "Tidekeeper", "Every current leaves a trace", 0xFF77D7EB.toInt(), 1),
        Species("moss", "Mosswarden", "Patience grows into power", 0xFFB4E58A.toInt(), 2),
        Species("orbit", "Orbitling", "Drawn to unexplored worlds", 0xFFC5A6FF.toInt(), 3),
        Species("crystal", "Prismhorn", "Many paths, one identity", 0xFF98BFFF.toInt(), 4),
        Species("shade", "Duskwing", "A quiet keeper of secrets", 0xFFD6A4CE.toInt(), 5),
        Species("volt", "Voltfox", "Curiosity moves at light speed", 0xFFF4DE81.toInt(), 6),
        Species("coral", "Coralune", "Small encounters become a reef", 0xFFFFADC1.toInt(), 7)
    )
    // Reserved IDs never participate in DNA v1 selection. Future versions opt in explicitly.
    val reserved = listOf("aurora", "iron", "sands", "frost", "bloom", "echo")
}
data class DNA(val species: Species, val seed: String, val eye: Int, val markings: Int, val version: Int = 1, val palette: Int = 0)
object Genesis {
    fun from(address: String): DNA {
        val key = Base58.decode(address); require(key.size == 32) { "Solana public key must be 32 bytes" }
        val h = MessageDigest.getInstance("SHA-256").digest("chainling:genesis:v1:".toByteArray() + key)
        return DNA(SpeciesRegistry.active[(h[0].toInt() and 255) % 8], h.take(8).joinToString("") { "%02x".format(it) }, (h[1].toInt() and 255) % 3, (h[2].toInt() and 255) % 5)
    }
}
data class Evidence(val signature: String, val time: Long?, val programs: Set<String>, val success: Boolean, val outgoing: Boolean, val incoming: Boolean, val tokenTransfer: Boolean, val memo: Boolean, val nft: Boolean, val skr: Boolean)
data class Snapshot(val address: String, val sol: Double, val skrBalance: Double?, val stakedSkr: Double?, val seeker: Boolean?, val tokenKinds: Int?, val evidence: List<Evidence>, val scannedAt: Long, val warnings: List<String>, val historyLimited: Boolean, val stakedJup: Double? = null)
data class Trait(val id: String, val name: String, val reason: String, val enabled: Boolean)
data class Memory(val id: String, val title: String, val time: Long?, val signature: String?)
data class Profile(val snapshot: Snapshot, val dna: DNA, val traits: List<Trait>, val memories: List<Memory>, val seekerborn: Boolean, val resonance: ResonanceState = ResonanceState())
object Evolution {
    const val JUPITER = "JUP6LkbZbjS1jKKwapdHNy74zcZ3tLUZoi5QNyVTaV4"
    const val SKR_PROGRAM = "SKRskrmtL83pcL4YqLWt6iPefDqwXQWHSw9S9vz94BZ"
    fun derive(s: Snapshot, permanentSeeker: Boolean = false, priorResonance: ResonanceState = ResonanceState()): Profile {
        val e = s.evidence.filter { it.success }; val programs = e.flatMap { it.programs }.toSet()
        val days = e.mapNotNull { it.time?.div(86400) }.toSet().size
        val born = permanentSeeker || s.seeker == true
        val resonance = SkrResonance.evolve(s,born,priorResonance)
        val rules = listOf(
            Trait("awakening", "First spark", "At least one confirmed successful transaction", e.isNotEmpty()),
            Trait("wanderer", "Wanderer", "10 successful transactions observed", e.size >= 10),
            Trait("veteran", "Chronicle keeper", "50 successful transactions observed", e.size >= 50),
            Trait("rhythm", "Daily pulse", "Activity on 3 distinct UTC days", days >= 3),
            Trait("roots", "Deep roots", "Observed activity spans at least 30 days", e.mapNotNull { it.time }.let { it.isNotEmpty() && it.max() - it.min() >= 30L * 86400 }),
            Trait("giver", "Giving hands", "Direct SOL transfer from this wallet in a signed transaction", e.any { it.outgoing }),
            Trait("receiver", "Open heart", "Incoming SOL balance change observed", e.any { it.incoming }),
            Trait("tokens", "Color collector", "3 positive fungible token balances", (s.tokenKinds ?: 0) >= 3),
            Trait("traveler", "Token trails", "Successful SPL token transfer observed", e.any { it.tokenTransfer }),
            Trait("jup_staking", "JUP Orbit Keeper", "Verified positive active Jupiter governance escrow", (s.stakedJup ?: 0.0) > 0.0),
            Trait("jupiter", "Jupiter navigator", "Jupiter v6 program invoked successfully", JUPITER in programs),
            Trait("skr", "Resonance Mark / Core", "Permanent: verified SKR holding, staking or successful SKR interaction observed", resonance.earned),
            Trait("guardian", "Active Resonance", "Current verified positive SKR stake; included in Free", resonance.active),
            Trait("seeker", "Seekerborn", "Verified Seeker Genesis Token; permanent locally", born),
            Trait("scribe", "Memory scribe", "Memo program invoked successfully", e.any { it.memo }),
            Trait("nft", "NFT Collector", "Successful Metaplex token-metadata interaction observed; NFT ownership/inventory not verified", e.any { it.nft })
        )
        val memories = e.sortedWith(compareBy<Evidence> { it.time ?: Long.MAX_VALUE }.thenBy { it.signature }).map {
            val title = when { JUPITER in it.programs -> "A path through Jupiter"; SKR_PROGRAM in it.programs -> "A Guardian encounter"; it.skr -> "An SKR encounter"; it.memo -> "A message left on-chain"; it.nft -> "An NFT encounter"; it.outgoing -> "A gift sent onward"; it.incoming -> "An arrival"; else -> "A new footprint" }
            Memory("tx:${it.signature}", title, it.time, it.signature)
        }
        return Profile(s, CanonicalWorld.from(s.address), rules, memories, born, resonance)
    }
    fun newMemories(old: Profile, next: Profile) = next.memories.filter { m -> old.memories.none { it.id == m.id } }
    fun story(p: Profile): String {
        val active = p.traits.filter { it.enabled }
        return SkrResonance.explanation(p) + " " + "${p.dna.species.name} was born from this wallet's immutable Genesis DNA. " +
            if (p.memories.isEmpty()) "Its chronicle is still quiet in the scanned window. The next confirmed footprint can become a memory." else
                "${p.memories.size} confirmed footprints shape its chronicle. " + active.filter { it.id in setOf("jupiter", "guardian", "seeker", "roots", "scribe") }.joinToString(" ") { when(it.id) { "jupiter" -> "Jupiter's routes left an orbital compass."; "guardian" -> "Staked SKR kindled its Guardian aura."; "seeker" -> "Seekerborn light is woven permanently into its crest."; "roots" -> "Its roots reach across a month of observed activity."; else -> "On-chain messages glow in its markings." } }
    }
}







