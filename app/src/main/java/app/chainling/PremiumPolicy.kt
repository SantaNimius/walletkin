package app.chainling

/** Premium threshold is independent of free Resonance activation and natural DNA. */
object PremiumPolicy {
    const val MIN_STAKE_SKR=100.0
    const val PERIOD_DAYS=30
    const val GRACE_DAYS=7
    fun eligibility(stake:Double?,minimum:Double=MIN_STAKE_SKR):MembershipEligibility=when {
        !minimum.isFinite() || minimum<=0 || stake==null || !stake.isFinite() || stake<0 -> MembershipEligibility.UNKNOWN
        stake>=minimum -> MembershipEligibility.ELIGIBLE
        else -> MembershipEligibility.FREE
    }
    val benefits=listOf(
        PremiumBenefit("Deep DNA","Expanded evidence-backed activity and protocol analytics."),
        PremiumBenefit("Evolution Timeline","A richer view of your saved evolution. Free memories remain free."),
        PremiumBenefit("Deep Chronicle","Extended biography grounded in verified wallet history."),
        PremiumBenefit("Advanced Animations","Additional emotions and reactions; earned traits remain free."),
        PremiumBenefit("Walletkin Wrapped","Weekly, monthly and annual activity summaries."),
        PremiumBenefit("Animated Share","Animated cards and GIF/video export. HD PNG and transparent PNG exports are already available."),
        PremiumBenefit("Customization","Additional cosmetic environments and animation packs."),
        PremiumBenefit("Home Screen Widgets","Your creature on the Android home screen.")
    )
}
data class PremiumBenefit(val name:String,val description:String)
data class CosmeticItem(val id:String,val name:String,val category:String,val description:String,
    val realm:String="genesis",val palette:Int?=null,val effect:String?=null)
/** Preview catalog only. No prices, ownership or purchased status are fabricated locally. */
object CosmeticCatalog {
    val items=listOf(
        CosmeticItem("neon-city","Neon City","Backgrounds","Electric skyline and neon atmospheric light.","explorer"),
        CosmeticItem("deep-space","Deep Space","Backgrounds","A quiet orbital sanctuary.","cosmic"),
        CosmeticItem("forest","Forest Sanctuary","Backgrounds","Botanical light and a jade horizon.","holder"),
        CosmeticItem("azure","Azure Neon","Skins","Blue cosmetic color treatment; Genesis DNA is unchanged.",palette=0),
        CosmeticItem("emerald","Emerald Glass","Skins","Green cosmetic color treatment; rarity is unchanged.",palette=1),
        CosmeticItem("crimson","Crimson Signal","Skins","Red cosmetic color treatment; earned traits remain intact.",palette=2),
        CosmeticItem("orbit-motes","Orbit Motes","Effects","Soft particles around the creature.",effect="motes"),
        CosmeticItem("portal","Prismatic Portal","Effects","A luminous portal behind the creature.",effect="portal")
    )
}



object CosmeticEquipment {
    fun item(context:android.content.Context,address:String,category:String):CosmeticItem? {
        val id=context.getSharedPreferences("cosmetics",android.content.Context.MODE_PRIVATE).getString("$address:$category",null)
        return CosmeticCatalog.items.firstOrNull{it.id==id && id in CommerceSession.owned(address)}
    }
}


