package app.chainling

import org.junit.Assert.*
import org.junit.Test

class BackgroundEvolutionTest {
    private fun profile(events: List<Evidence> = emptyList(), skr: Double? = null, stake: Double? = null): Profile =
        Evolution.derive(Snapshot("11111111111111111111111111111111", 0.0, skr, stake, null, 0, events, 100, emptyList(), false))
    private fun event(id: String, success: Boolean = true, programs: Set<String> = emptySet(), time: Long? = 1, memo: Boolean = false) =
        Evidence(id, time, programs, success, false, false, false, memo, false, false)
    @Test fun unknownBalancesAndLockedPreferenceCannotGrantPaidOrProtocolRealms() {
        val p=profile()
        assertEquals(listOf("genesis"),BackgroundEvolution.unlocked(p).map{it.id})
        assertEquals("genesis",BackgroundEvolution.active(p,"guardian").id)
        assertEquals("genesis",BackgroundEvolution.active(p,"jupiter").id)
    }
    @Test fun failedProtocolAndMemoTransactionsDoNotUnlockBackgrounds() {
        val p=profile(listOf(event("failed",false,setOf(Evolution.JUPITER),memo=true)))
        assertEquals("genesis",BackgroundEvolution.active(p).id)
        assertEquals(1,BackgroundEvolution.unlocked(p).size)
    }
    @Test fun confirmedRescanChangesRealmWithoutChangingGenesis() {
        val old=profile()
        val next=profile(listOf(event("real",programs=setOf(Evolution.JUPITER))))
        assertEquals("genesis",BackgroundEvolution.active(old).id)
        assertEquals("jupiter",BackgroundEvolution.active(next).id)
        assertEquals(old.dna,next.dna)
        assertEquals(1,Evolution.newMemories(old,next).size)
    }
    @Test fun manualChoiceIsHonoredOnlyWhileItsEvidenceRemains() {
        val p=profile(listOf(event("jupiter",programs=setOf(Evolution.JUPITER))),skr=2.0,stake=3.0)
        assertEquals("guardian",BackgroundEvolution.active(p).id)
        assertEquals("jupiter",BackgroundEvolution.active(p,"jupiter").id)
        assertEquals("genesis",BackgroundEvolution.active(profile(),"jupiter").id)
    }
    @Test fun RootsRequiresKnownThirtyDaySpanAndMemoHasItsOwnRealm() {
        assertFalse(BackgroundEvolution.unlocked(profile(listOf(event("unknown",time=null)))).any{it.id=="roots"})
        val roots=profile(listOf(event("first",time=1),event("last",time=30L*86400+1)))
        assertEquals("roots",BackgroundEvolution.active(roots).id)
        assertEquals("scribe",BackgroundEvolution.active(profile(listOf(event("memo",memo=true)))).id)
    }
}