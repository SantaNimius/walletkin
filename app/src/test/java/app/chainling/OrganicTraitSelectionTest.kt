package app.chainling
import org.junit.Assert.*
import org.junit.Test
class OrganicTraitSelectionTest {
    private fun profile(stake:Double?=0.0,seeker:Boolean?=false)=Evolution.derive(
        Snapshot("11111111111111111111111111111111",0.0,0.0,stake,seeker,0,emptyList(),100,emptyList(),false))
    @Test fun zeroEvidenceDoesNotShowAnySyntheticMaterial(){
        assertEquals(OrganicTraitSelection(),OrganicTraitSelection.from(profile()))
    }
    @Test fun stakingAndGenesisNeverInventProtocolOrCollectorTraits(){
        val s=OrganicTraitSelection.from(profile(1.0,true))
        assertTrue(s.skr);assertTrue(s.active);assertTrue(s.genesis);assertFalse(s.jupiter);assertFalse(s.nft)
    }
    @Test fun illustrativePreviewIsExplicitAndDoesNotChangeProfile(){
        val p=profile();val state=OrganicTraitSelection.from(p,true)
        assertEquals(OrganicTraitSelection(true,true,true,true,true,true),state)
        assertEquals(OrganicTraitSelection(),OrganicTraitSelection.from(p))
    }
    @Test fun unknownStakingKeepsEarnedCoreButCannotClaimActive(){
        val p=profile(1.0)
        val next=Evolution.derive(p.snapshot.copy(stakedSkr=null,scannedAt=200),false,p.resonance)
        val s=OrganicTraitSelection.from(next)
        assertTrue(s.skr);assertFalse(s.active)
    }
    @Test fun jupStakeDoesNotInventSwapEvidence(){
        val p=profile();val next=Evolution.derive(p.snapshot.copy(stakedJup=10.0))
        val state=OrganicTraitSelection.from(next)
        assertTrue(state.jupStaking);assertFalse(state.jupiter)
        assertFalse(OrganicTraitSelection.from(Evolution.derive(p.snapshot.copy(stakedJup=null))).jupStaking)
    }
}


