package app.chainling
import org.junit.Assert.*
import org.junit.Test
import org.json.JSONObject

class SkrResonanceTest {
    private fun snap(held:Double?=0.0,stake:Double?=0.0,time:Long=100,seeker:Boolean?=false,
        events:List<Evidence> = emptyList())=Snapshot("11111111111111111111111111111111",
        0.0,held,stake,seeker,0,events,time,emptyList(),false)
    @Test fun historicalHoldingSurvivesSaleAndUnstake(){
        val initial=Evolution.derive(snap(held=10.0))
        val next=Evolution.derive(snap(time=200),initial.seekerborn,initial.resonance)
        assertTrue(next.resonance.earned);assertEquals(100L,next.resonance.firstObservedAt)
        assertEquals(ResonanceMode.DORMANT,next.resonance.mode)
        assertTrue(next.traits.first{it.id=="skr"}.enabled)
        assertFalse(next.traits.first{it.id=="guardian"}.enabled)
    }
    @Test fun stakingAwakensCoreForFreeAndNeverChangesDna(){
        val a=Evolution.derive(snap(stake=1.0))
        val b=Evolution.derive(snap(stake=1000000.0))
        assertEquals(a.dna,b.dna);assertEquals(a.resonance.mode,b.resonance.mode);assertEquals(a.resonance.earned,b.resonance.earned)
        assertEquals(MembershipEligibility.FREE,a.resonance.eligibility);assertEquals(MembershipEligibility.ELIGIBLE,b.resonance.eligibility)
    }
    @Test fun seekerStakingIsGenesisAndUnstakePreservesLegacy(){
        val a=Evolution.derive(snap(stake=1.0,seeker=true))
        assertEquals(ResonanceMode.GENESIS,a.resonance.mode)
        val b=Evolution.derive(snap(time=200),a.seekerborn,a.resonance)
        assertTrue(b.seekerborn);assertTrue(b.resonance.earned)
        assertEquals(ResonanceMode.DORMANT,b.resonance.mode)
        assertEquals(MembershipEligibility.FREE,b.resonance.eligibility)
    }
    @Test fun unavailableRpcDoesNotInventUnstakeOrEraseMarks(){
        val a=Evolution.derive(snap(stake=1.0))
        val b=Evolution.derive(snap(held=null,stake=null,time=200),false,a.resonance)
        assertEquals(ResonanceMode.UNKNOWN,b.resonance.mode)
        assertTrue(b.resonance.earned);assertEquals(true,b.resonance.lastKnownActive)
        assertNull(b.resonance.dormantSince)
        assertEquals(a.resonance.observedStakeSince,b.resonance.observedStakeSince)
        assertEquals(MembershipEligibility.UNKNOWN,b.resonance.eligibility)
        assertFalse(b.resonance.active)
    }
    @Test fun serializationAndRestartKeepPermanentHistory(){
        val a=Evolution.derive(snap(stake=1.0))
        val decoded=ResonancePersistence.decode(JSONObject(ResonancePersistence.encode(a.resonance).toString()))
        val b=Evolution.derive(snap(time=300),false,decoded)
        assertTrue(b.resonance.earned);assertEquals(100L,b.resonance.firstObservedAt)
        assertEquals(300L,b.resonance.dormantSince)
    }
    @Test fun failedSkrInteractionDoesNotEarnMutation(){
        val failed=Evidence("failed",1,setOf(Evolution.SKR_PROGRAM),false,false,false,false,false,false,true)
        assertFalse(Evolution.derive(snap(events=listOf(failed))).resonance.earned)
    }
    @Test fun confirmedUnstakeThenRestakeStartsNewObservation(){
        val a=Evolution.derive(snap(stake=1.0))
        val b=Evolution.derive(snap(time=200),false,a.resonance)
        val c=Evolution.derive(snap(stake=1.0,time=300),false,b.resonance)
        assertEquals(300L,c.resonance.observedStakeSince);assertEquals(100L,c.resonance.firstObservedAt)
        assertNull(c.resonance.dormantSince)
    }
    @Test fun corruptNegativeStakeStaysUnknown(){
        assertEquals(ResonanceMode.UNKNOWN,Evolution.derive(snap(stake=-1.0)).resonance.mode)
        assertEquals(ResonanceMode.UNKNOWN,Evolution.derive(snap(stake=Double.NaN)).resonance.mode)
    }
    @Test fun repeatScanIsIdempotent(){
        val a=Evolution.derive(snap(stake=1.0))
        assertEquals(a,Evolution.derive(a.snapshot,a.seekerborn,a.resonance))
    }
    @Test fun everyCanonicalSpeciesHasDistinctAnatomicalTraitAnchors(){
        assertEquals(CanonicalWorld.species.map{it.id}.toSet(),TraitAnatomy.all.keys)
        for(a in TraitAnatomy.all.values)for(p in listOf(a.core,a.genesis,a.navigator,a.relic)){
            assertTrue(p.x>0 && p.x<a.width);assertTrue(p.y>0 && p.y<a.height)
        }
    }
}


