package app.chainling

import org.junit.Assert.*
import org.junit.Test
import java.math.BigInteger

class DomainTest {
    private val address="11111111111111111111111111111111"
    private fun snapshot(events:List<Evidence> = emptyList(),skr:Double?=null,stake:Double?=null,seeker:Boolean?=null)=Snapshot(address,0.0,skr,stake,seeker,0,events,100,emptyList(),false)
    private fun event(id:String="a",success:Boolean=true,programs:Set<String> = emptySet(),time:Long?=1)=Evidence(id,time,programs,success,false,false,false,false,false,false)
    @Test fun genesisIsStableAndUsesPublicKeyBytes(){val a=Genesis.from(address);assertEquals(a,Genesis.from(address));assertEquals(8,SpeciesRegistry.active.size);assertEquals(14,(SpeciesRegistry.active.map{it.id}+SpeciesRegistry.reserved).distinct().size);assertEquals(32,Base58.decode(address).size)}
    @Test fun base58RoundTripsLeadingZeroes(){for(i in 0..100){val key=ByteArray(32){((it*17+i)%256).toByte()};assertArrayEquals(key,Base58.decode(Base58.encode(key)))}}
    @Test(expected=IllegalArgumentException::class) fun invalidAddressRejected(){Genesis.from("invalid")}
    @Test fun failedTransactionsNeverGrantTraitsOrMemories(){val p=Evolution.derive(snapshot(listOf(event(success=false,programs=setOf(Evolution.JUPITER)))));assertEquals(16,p.traits.size);assertFalse(p.traits.any{it.enabled});assertTrue(p.memories.isEmpty())}
    @Test fun jupiterRequiresExactProgram(){val fake=Evolution.derive(snapshot(listOf(event(programs=setOf("Jupiter")))));assertFalse(fake.traits.first{it.id=="jupiter"}.enabled);val real=Evolution.derive(snapshot(listOf(event(programs=setOf(Evolution.JUPITER)))));assertTrue(real.traits.first{it.id=="jupiter"}.enabled)}
    @Test fun unknownBalanceDoesNotInventResonance(){val p=Evolution.derive(snapshot());assertFalse(p.traits.first{it.id=="skr"}.enabled);assertFalse(p.traits.first{it.id=="guardian"}.enabled)}
    @Test fun seekerbornPersistsAfterOwnershipChanges(){val initial=Evolution.derive(snapshot(seeker=true));val next=Evolution.derive(snapshot(seeker=false),initial.seekerborn);assertTrue(next.seekerborn)}
    @Test fun rescanIsIdempotentAndMemoriesHaveStableIds(){val old=Evolution.derive(snapshot(listOf(event())));assertTrue(Evolution.newMemories(old,old).isEmpty());val next=Evolution.derive(snapshot(listOf(event(),event("b",time=2))));assertEquals(listOf("tx:b"),Evolution.newMemories(old,next).map{it.id});assertEquals(old.dna,next.dna)}
    @Test fun unknownTimestampsDoNotInventAge(){val p=Evolution.derive(snapshot(listOf(event(time=null))));assertNull(p.memories.first().time);assertFalse(p.traits.first{it.id=="roots"}.enabled)}
    @Test fun stakeLayoutIsUnsignedLittleEndianAndOwnerChecked(){val b=ByteArray(169);StakeDecoder.userDiscriminator.copyInto(b);Base58.decode(Analyzer.CONFIG).copyInto(b,9);Base58.decode(address).copyInto(b,41);b[105]=2;assertEquals(BigInteger.valueOf(2),StakeDecoder.shares(b,address,Analyzer.CONFIG));b[41]=1;try{StakeDecoder.shares(b,address,Analyzer.CONFIG);fail("Must reject wrong user")}catch(_:IllegalArgumentException){}}
    @Test fun configSharePriceOffsetIsVerified(){val b=ByteArray(153);StakeDecoder.configDiscriminator.copyInto(b);Base58.decode(Analyzer.SKR_MINT).copyInto(b,41);b[137]=7;assertEquals(BigInteger.valueOf(7),StakeDecoder.price(b));b[0]=0;try{StakeDecoder.price(b);fail("Must reject layout")}catch(_:IllegalArgumentException){}}
}

