package app.chainling
/** Local motion on the accepted whole illustration: vest, shoulders and face stay fixed. */
object KongMotion {
    fun handWeight(x:Float,y:Float,right:Boolean):Float {
        val vertical=((y-(if(right)520f else 430f))/650f).coerceIn(0f,1f)
        val lateral=if(right)((x-1015f)/100f).coerceIn(0f,1f)
            else ((535f-x)/150f).coerceIn(0f,1f)
        return vertical*vertical*(3f-2f*vertical)*lateral
    }
}

