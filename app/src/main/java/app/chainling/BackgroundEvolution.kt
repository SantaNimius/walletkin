package app.chainling

data class BackgroundTrait(val id: String, val name: String, val reason: String, val artKey: String, val requiredTrait: String?)
object BackgroundEvolution {
    val all = listOf(
        BackgroundTrait("genesis", "Genesis Night", "Your birth realm. Always available.", "genesis", null),
        BackgroundTrait("roots", "Deep Roots", "Confirmed activity spans at least 30 days in the observed history.", "roots", "roots"),
        BackgroundTrait("scribe", "Memory Archive", "A successful on-chain memo was observed.", "scribe", "scribe"),
        BackgroundTrait("jupiter", "Jupiter Routes", "A confirmed successful Jupiter v6 interaction was observed.", "jupiter", "jupiter"),
        BackgroundTrait("skr", "SKR Resonance", "Verified positive SKR holding or a confirmed SKR balance change.", "skr", "skr"),
        BackgroundTrait("guardian", "Guardian Sanctuary", "Verified positive SKR staking shares.", "skr", "guardian")
    )
    fun unlocked(profile: Profile): List<BackgroundTrait> {
        val active = profile.traits.filter { it.enabled }.map { it.id }.toSet()
        return all.filter { it.requiredTrait == null || it.requiredTrait in active }
    }
    fun active(profile: Profile?, preferred: String? = null): BackgroundTrait {
        if (profile == null) return all.first()
        val available = unlocked(profile)
        return available.firstOrNull { it.id == preferred } ?: available.last()
    }
}