package app.chainling
data class OrganicTraitSelection(val skr:Boolean=false,val active:Boolean=false,
    val genesis:Boolean=false,val jupiter:Boolean=false,val nft:Boolean=false,val jupStaking:Boolean=false){
    val hasMaterials:Boolean get()=skr || genesis || jupiter || nft || jupStaking
    companion object{
        fun from(p:Profile,preview:Boolean=false):OrganicTraitSelection {
            val traits=p.traits.filter{it.enabled}.map{it.id}.toSet()
            return OrganicTraitSelection(p.resonance.earned||preview,p.resonance.active||preview,
                p.seekerborn||preview,"jupiter" in traits||preview,"nft" in traits||preview,"jup_staking" in traits||preview)
        }
    }
}



