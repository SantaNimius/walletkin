package app.chainling
import org.junit.Assert.*
import org.junit.Test
import org.json.JSONObject
import java.io.File

class CanonicalGenesisTest {
    private val address="6y8jfXZHHSYYaVLaEDnokW3MEKg1JfzA9S1Ld6zCv4SF"
    @Test fun legacySeedRemainsAuditable() {
        assertEquals("1fd26241bed8331e",Genesis.from(address).seed)
        assertEquals(1,Genesis.from(address).version)
        assertEquals(2,CanonicalWorld.from(address).version)
        assertNotEquals(Genesis.from(address).seed,CanonicalWorld.from(address).seed)
    }
    @Test fun canonicalAssignmentAndColorsAreStable() {
        val first=CanonicalWorld.from(address)
        repeat(10){assertEquals(first,CanonicalWorld.from(address))}
        assertTrue(first.species.id in CanonicalWorld.species.map{it.id})
        assertTrue(first.palette in CanonicalWorld.palettes.indices)
    }
    @Test fun allApprovedLineagesAndColorsAreReachable() {
        val found=mutableSetOf<String>();val colors=mutableSetOf<Int>()
        repeat(1024){n->
            val key=ByteArray(32);key[30]=(n shr 8).toByte();key[31]=n.toByte()
            val dna=CanonicalWorld.from(Base58.encode(key));found+=dna.species.id;colors+=dna.palette
        }
        assertEquals(CanonicalWorld.species.map{it.id}.toSet(),found)
        assertEquals(CanonicalWorld.palettes.indices.toSet(),colors)
        assertFalse(found.any{it in SpeciesRegistry.active.map{legacy->legacy.id}})
        assertFalse("anomaly" in found)
    }
    @Test fun legendsAndWalletNamesCannotDiverge() {
        val candidates=listOf(File("src/main/assets/legends.json"),File("app/src/main/assets/legends.json"))
        val catalog=JSONObject(candidates.first{it.exists()}.readText()).getJSONArray("entries")
        val entries=(0 until catalog.length()).map{catalog.getJSONObject(it)}
        assertEquals(14,entries.size)
        CanonicalWorld.species.forEach{s->assertEquals(s.name,entries.first{it.getString("id")==s.id}.getString("name"))}
    }
}
