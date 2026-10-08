package app.chainling

import android.graphics.*
import android.util.LruCache

/** Runtime mesh only: original approved PNGs remain unchanged. No duplicate limb passes. */
class FlexibleSprite {
    private val vertices=FloatArray((COLUMNS+1)*(ROWS+1)*2)
    fun draw(canvas:Canvas, bitmap:Bitmap, source:Rect, destination:RectF,
             rootX:Float, rootY:Float, hold:Float, reach:Float, wave:Float, paint:Paint) {
        val key=Key(bitmap,source.left,source.top,source.right,source.bottom)
        val cropped=synchronized(crops){ crops.get(key) ?: Bitmap.createBitmap(bitmap,
            source.left,source.top,source.width(),source.height()).also{crops.put(key,it)} }
        var index=0
        for(row in 0..ROWS) for(column in 0..COLUMNS) {
            val x=destination.left+destination.width()*column/COLUMNS
            val y=destination.top+destination.height()*row/ROWS
            val bend=JointMotion.weight(x,y,rootX,rootY,hold,reach)
            vertices[index++]=x
            vertices[index++]=y+wave*bend
        }
        canvas.drawBitmapMesh(cropped,COLUMNS,ROWS,vertices,0,null,0,paint)
    }
    private data class Key(val image:Bitmap,val left:Int,val top:Int,val right:Int,val bottom:Int)
    companion object {
        private const val COLUMNS=16
        private const val ROWS=12
        private val crops=object:LruCache<Key,Bitmap>(12*1024*1024){
            override fun sizeOf(key:Key,value:Bitmap)=value.allocationByteCount
        }
    }
}

