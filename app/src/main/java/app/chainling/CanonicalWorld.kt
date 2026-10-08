package app.chainling

import java.security.MessageDigest

/** Versioned canonical roster; legacy Genesis v1 remains unchanged for audit. */
object CanonicalWorld {
    val species = listOf(
        Species("fox","Astrafox","Cosmic · Curious",0xFF638DFF.toInt(),0),
        Species("dragon","Riftdrake","Mineral · Rift",0xFF53D5DF.toInt(),1),
        Species("golem","Jadewarden","Basalt · Jade",0xFF69D7A0.toInt(),2),
        Species("wisp","Lumenwisp","Pearl · Mint",0xFF98EFE0.toInt(),3),
        Species("shark","Abyssfin","Abyss · Graphite",0xFF72C8E8.toInt(),4),
        Species("plant","Fernroot","Roots · Fern",0xFF98CE71.toInt(),5),
        Species("mech","Byteforge","Iron · Ceramic",0xFFFFB37B.toInt(),6),
        Species("bird","Foldwing","Paper · Midnight",0xFF839DFF.toInt(),7),
        Species("slime","Chromagel","Glass · Amber",0xFFEEB260.toInt(),8),
        Species("cosmic","Orbitron","Orbit · Retro",0xFFE9BE6D.toInt(),9),
        Species("degen_ape","Degen Kong","Street · Cyberpunk",0xFFE293F2.toInt(),10),
        Species("cat","Glyphcat","Nouveau · Neon",0xFFFFB566.toInt(),11),
        Species("lynx","Prismlynx","Facet · Wild",0xFFEBC082.toInt(),12)
    )
    val palettes = listOf("Azure","Emerald","Crimson","Violet","Amber","Ice")
    val hues = listOf(220f,135f,355f,280f,40f,180f)
    val colors = listOf(0xFF739EFF.toInt(),0xFF72EBA4.toInt(),0xFFFF778F.toInt(),
        0xFFCA92FF.toInt(),0xFFFFC573.toInt(),0xFF79E4EB.toInt())
    private val baseHues = listOf(220f,195f,150f,165f,210f,95f,30f,220f,70f,45f,30f,30f,35f)
    fun assetId(id:String)=if(id=="fox")"astrafox" else id
    fun byId(id:String)=species.first{it.id==id}
    fun hueRotation(id:String,palette:Int):Float {
        val index=species.indexOfFirst{it.id==id}
        return if(index<0)0f else hues[palette.coerceIn(hues.indices)]-baseHues[index]
    }
    fun from(address:String):DNA {
        val key=Base58.decode(address);require(key.size==32){"Solana public key must be 32 bytes"}
        val h=MessageDigest.getInstance("SHA-256").digest("walletkin:genesis:v2:".toByteArray()+key)
        val palette=(h[3].toInt() and 255)%palettes.size
        val origin=species[(h[0].toInt() and 255)%species.size].copy(color=colors[palette])
        return DNA(origin,h.take(8).joinToString(""){"%02x".format(it)},(h[1].toInt() and 255)%3,
            (h[2].toInt() and 255)%5,version=2,palette=palette)
    }
}

