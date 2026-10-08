package app.chainling
import org.json.JSONObject

object ResonancePersistence {
    fun decode(j:JSONObject?):ResonanceState {
        if(j==null)return ResonanceState()
        fun time(key:String)=if(j.isNull(key))null else j.optLong(key).takeIf{it>0}
        return ResonanceState(j.optBoolean("earned"),time("first"),time("verified"),
            if(j.isNull("active"))null else j.optBoolean("active"),time("since"),time("dormant"))
    }
    fun encode(r:ResonanceState)=JSONObject()
        .put("earned",r.earned).put("first",r.firstObservedAt ?: JSONObject.NULL)
        .put("verified",r.lastVerifiedAt ?: JSONObject.NULL).put("active",r.lastKnownActive ?: JSONObject.NULL)
        .put("since",r.observedStakeSince ?: JSONObject.NULL).put("dormant",r.dormantSince ?: JSONObject.NULL)
}

