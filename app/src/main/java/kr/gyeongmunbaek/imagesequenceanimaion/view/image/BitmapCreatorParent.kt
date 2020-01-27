package kr.gyeongmunbaek.imagesequenceanimaion.view.image

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Created by gyeongmunbaek on 2016-11-02.
 */

abstract class BitmapCreatorParent {
    private var context: Context? = null

    var bitmap: Bitmap? = null
    private var index = 0
    private var density = 2.0f

    abstract val fileCount: Int
    abstract val fileNamePrefix: String
    abstract val startIndex: Int

    constructor()

    constructor(context: Context, index: Int) {
        this.context = context
        this.index = index

        density = this.context!!.resources.displayMetrics.density
    }

    @Throws(Resources.NotFoundException::class, OutOfMemoryError::class)
    fun createBitmap() {
        if (bitmap == null) {
            val newIndex = startIndex + index % fileCount
            var fileName = fileNamePrefix

            if (fileCount >= 100) {
                if (newIndex < 100) {
                    fileName += "0"
                }
                if (newIndex < 10) {
                    fileName += "0"
                }
            } else {
                if (newIndex < 10) {
                    fileName += "0"
                }
            }

            fileName += newIndex.toString()
            val resID = context!!.resources.getIdentifier(fileName, "drawable",
                    context!!.packageName)
            if (resID == 0) {
                throw Resources.NotFoundException()
            }

            val maxMemory = Runtime.getRuntime().maxMemory()
            val allocationMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
            val realFreeMemory = maxMemory - allocationMemory

            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeResource(context!!.resources, resID, options)

            val needsMemory = options.outWidth * density * (options.outHeight * density) * 4

            if (realFreeMemory - needsMemory > 0) {
                try {
                    bitmap = BitmapFactory.decodeResource(context!!.resources, resID)
                } catch (e: OutOfMemoryError) {
                    throw OutOfMemoryError()
                }
            }
        }
    }

    fun recycle() {
        bitmap?.recycle()
        bitmap = null
    }
}
