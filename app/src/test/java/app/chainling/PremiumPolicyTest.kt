package app.chainling
import org.junit.Assert.*
import org.junit.Test
class PremiumPolicyTest {
    @Test fun thresholdUsesStakedAmountNotHoldings(){
        assertEquals(MembershipEligibility.FREE,PremiumPolicy.eligibility(0.0))
        assertEquals(MembershipEligibility.FREE,PremiumPolicy.eligibility(99.999999))
        assertEquals(MembershipEligibility.ELIGIBLE,PremiumPolicy.eligibility(100.0))
        assertEquals(MembershipEligibility.ELIGIBLE,PremiumPolicy.eligibility(150.0))
    }
    @Test fun unavailableAndCorruptStakingNeverUnlockPremium(){
        for(stake in listOf(null,-1.0,Double.NaN,Double.POSITIVE_INFINITY))
            assertEquals(MembershipEligibility.UNKNOWN,PremiumPolicy.eligibility(stake))
    }
    @Test fun partialUnstakeLosesEligibilityWithoutLosingResonance(){
        val s=Snapshot("11111111111111111111111111111111",0.0,100000.0,150.0,false,0,emptyList(),100,emptyList(),false)
        val a=Evolution.derive(s)
        val b=Evolution.derive(s.copy(stakedSkr=90.0,scannedAt=200),false,a.resonance)
        assertEquals(MembershipEligibility.FREE,b.resonance.eligibility)
        assertTrue(b.resonance.active);assertTrue(b.resonance.earned);assertEquals(a.dna,b.dna)
    }
    @Test fun cachedEligibilityRecomputesFromSnapshotAfterRestart(){
        val s=Snapshot("11111111111111111111111111111111",0.0,0.0,100.0,false,0,emptyList(),100,emptyList(),false)
        val original=Evolution.derive(s)
        val decoded=ResonancePersistence.decode(ResonancePersistence.encode(original.resonance))
        assertEquals(original.resonance.eligibility,Evolution.derive(s,false,decoded).resonance.eligibility)
    }
}

