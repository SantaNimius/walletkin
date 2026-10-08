package app.chainling

enum class ResonanceMode(val title:String) {
    NONE("No Resonance yet"), DORMANT("Dormant Resonance"),
    ACTIVE("Active Resonance"), GENESIS("Genesis Resonance"), UNKNOWN("Staking check unavailable")
}
enum class MembershipEligibility(val title:String) {
    FREE("Premium Locked"), ELIGIBLE("Premium Eligible"), UNKNOWN("Premium check unavailable")
}
data class ResonanceState(
    val earned:Boolean=false,
    val firstObservedAt:Long?=null,
    val lastVerifiedAt:Long?=null,
    val lastKnownActive:Boolean?=null,
    val observedStakeSince:Long?=null,
    val dormantSince:Long?=null,
    val mode:ResonanceMode=ResonanceMode.NONE,
    val verifiedStakeAmount:Double?=null
) {
    val active get()=mode==ResonanceMode.ACTIVE || mode==ResonanceMode.GENESIS
    val eligibility get()=PremiumPolicy.eligibility(verifiedStakeAmount)
}
object SkrResonance {
    const val GRACE_DAYS=7
    val proposedRewards=listOf(30 to "Background",90 to "Special animation",180 to "Cosmetic artifact",365 to "Resonance visual effect")
    /** Times are verified scan observations, never inferred lifetime staking tenure. */
    fun evolve(s:Snapshot,seekerborn:Boolean,prior:ResonanceState=ResonanceState()):ResonanceState {
        val stake=s.stakedSkr?.takeIf{it.isFinite() && it>=0}
        val positive=stake!=null && stake>0
        val earned=prior.earned || positive || (s.skrBalance?.takeIf{it.isFinite()} ?: 0.0)>0 ||
            s.evidence.any{it.success && (it.skr || Evolution.SKR_PROGRAM in it.programs)}
        val first=prior.firstObservedAt ?: s.scannedAt.takeIf{earned && it>0}
        if(stake==null)return prior.copy(earned=earned,firstObservedAt=first,mode=ResonanceMode.UNKNOWN,verifiedStakeAmount=null)
        val mode=if(positive)if(seekerborn)ResonanceMode.GENESIS else ResonanceMode.ACTIVE
            else if(earned)ResonanceMode.DORMANT else ResonanceMode.NONE
        val since=if(positive){
            if(prior.lastKnownActive==true)prior.observedStakeSince ?: s.scannedAt else s.scannedAt
        }else null
        return ResonanceState(earned,first,s.scannedAt,positive,since,
            if(!positive && prior.lastKnownActive==true) s.scannedAt else if(positive)null else prior.dormantSince,mode,stake)
    }
    fun explanation(p:Profile):String = when(p.resonance.mode){
        ResonanceMode.GENESIS->"Verified active SKR staking awakens your permanent Genesis Core. Staking effects are included in Free."
        ResonanceMode.ACTIVE->"Verified active SKR staking awakens your Resonance Core. Staking effects are included in Free."
        ResonanceMode.DORMANT->"Your earned Resonance Mark and Core remain. The latest verified staking check is zero, so dynamic effects rest."
        ResonanceMode.UNKNOWN->"Current staking could not be verified. Your earned marks remain; active effects and Membership eligibility await a fresh check."
        ResonanceMode.NONE->"Confirmed SKR history can leave permanent marks. Active staking awakens Resonance."
    }
}



