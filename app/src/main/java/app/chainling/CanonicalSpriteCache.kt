package app.chainling

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache

/** One bounded cache for the growing canonical roster, shared across gallery and detail views. */
object CanonicalSpriteCache {
    private val cache=object:LruCache<String,Map<String,Bitmap>>(64*1024*1024) {
        override fun sizeOf(key:String,value:Map<String,Bitmap>)=value.values.sumOf{it.byteCount}
    }
    @Synchronized fun get(context:Context,id:String,parts:List<String>):Map<String,Bitmap> {
        val key=id+":"+parts.joinToString(",")
        cache.get(key)?.let{return it}
        val result=parts.associateWith{name->context.assets.open("canonical/$id/$name.png").use{
            val decoded=requireNotNull(BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply{inScaled=false}))
            val factor=minOf(1f,768f/maxOf(decoded.width,decoded.height))
            if(factor==1f)decoded else Bitmap.createScaledBitmap(decoded,(decoded.width*factor).toInt(),(decoded.height*factor).toInt(),true).also{decoded.recycle()}
        }}
        cache.put(key,result);return result
    }
}



