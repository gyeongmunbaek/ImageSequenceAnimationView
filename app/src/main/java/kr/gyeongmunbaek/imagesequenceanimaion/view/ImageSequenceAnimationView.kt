package kr.gyeongmunbaek.imagesequenceanimaion.view

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.os.AsyncTask
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.View

import kr.gyeongmunbaek.imagesequenceanimaion.view.image.BitmapCreatorParent
import kr.gyeongmunbaek.imagesequenceanimaion.view.image.cache.CustomCache

/**
 * Created by gyeongmunbaek on 2017-10-26.
 */

class ImageSequenceAnimationView : View {
    private var customCache: CustomCache<BitmapCreatorParent>? = null
    private var cacheCount = CACHE_COUNT
    private var delay = MESSAGE_DELAY

    private var bitmapClass: BitmapCreatorParent? = null

    private var currentIndex = 0
    private var preBitmap: Bitmap? = null
    private var isPlaying = false

    private var area: RectF = RectF(0.0f, 0.0f, width.toFloat(), height.toFloat())
    private val handler = CustomHandler(this)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onDraw(canvas: Canvas) {
        if (width > 0 && height > 0) {
            if (customCache != null && customCache!![currentIndex] != null) {
                customCache!![currentIndex]?.bitmap?.let {
                    preBitmap = it
                    canvas.drawBitmap(
                            it, null,
                            area, null)

                    currentIndex++
                } ?: run {
                    preBitmap?.let { bitmap ->
                        if (!bitmap.isRecycled) {
                            canvas.drawBitmap(
                                    bitmap, null,
                                    area, null)
                        }
                    }
                }
            }
        }
    }

    fun setCacheCount(pCount: Int): ImageSequenceAnimationView {
        cacheCount = pCount
        return this
    }

    fun setDelay(delay: Long): ImageSequenceAnimationView {
        this.delay = delay
        return this
    }

    @Throws(NullPointerException::class)
    fun setAnimationClazz(pClass: BitmapCreatorParent?): ImageSequenceAnimationView {
        if (pClass == null) {
            throw NullPointerException()
        }
        bitmapClass = pClass
        return this
    }

    fun startAnimation() {
        handler.removeMessages(MESSAGE_START_ANIMATION)
        val msg = handler.obtainMessage(MESSAGE_START_ANIMATION)
        handler.sendMessageDelayed(msg, delay)
    }

    private fun setBitmapClass(bitmapCreatorParent: BitmapCreatorParent?) {
        val bitmapTask = BitmapClassAsyncTask(this, bitmapCreatorParent)
        bitmapTask.execute()
    }

    fun destroy() {
        isPlaying = false

        handler.removeMessages(MESSAGE_INVALIDATE)
        handler.removeMessages(MESSAGE_START_ANIMATION)

        if (customCache != null) {
            customCache!!.evictAll()
            customCache = null
        }

        if (preBitmap != null) {
            if (!preBitmap!!.isRecycled) {
                preBitmap!!.recycle()
            }
            preBitmap = null
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        destroy()
    }

    private fun update(currentIndex: Int) {
        val asyncTask = UpdateAsyncTask(this, currentIndex)
        asyncTask.execute()
    }

    companion object {
        private const val CACHE_COUNT = 5
        private const val MESSAGE_INVALIDATE = 1
        private const val MESSAGE_START_ANIMATION = 2

        private const val MESSAGE_DELAY = 10L
    }

    class CustomHandler(private val view: ImageSequenceAnimationView) : Handler() {
        override fun handleMessage(msg: Message) {
            view.run {
                if (msg.what == MESSAGE_INVALIDATE) {
                    Runnable {
                        if (isPlaying) {
                            area = RectF(0.0f, 0.0f, width.toFloat(), height.toFloat())
                            invalidate()
                            update(currentIndex)
                        }
                    }.run()
                } else if (msg.what == MESSAGE_START_ANIMATION) {
                    handler.removeMessages(MESSAGE_INVALIDATE)
                    Runnable { setBitmapClass(bitmapClass) }.run()
                }
            }
        }
    }

    class BitmapClassAsyncTask(private val view: ImageSequenceAnimationView, var bitmapCreatorParent: BitmapCreatorParent?) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            view.run {
                isPlaying = false

                handler.removeMessages(MESSAGE_INVALIDATE)
                handler.removeMessages(MESSAGE_START_ANIMATION)

                if (customCache != null) {
                    customCache!!.evictAll()
                    customCache = null
                }

                preBitmap?.let {
                    if (!it.isRecycled) {
                        it.recycle()
                    }
                    preBitmap = null
                }

                bitmapCreatorParent?.let {
                    customCache = CustomCache(context, cacheCount, it)
                    currentIndex = 0
                }

                for (index in 0 until cacheCount) {
                    try {
                        customCache!![index]?.createBitmap()
                    } catch (e: OutOfMemoryError) {

                    } catch (e1: Resources.NotFoundException) {

                    }
                }

                isPlaying = true
                val msg = handler.obtainMessage(MESSAGE_INVALIDATE)
                handler.sendMessageDelayed(msg, delay)
                return null
            }
        }
    }

    class UpdateAsyncTask(private val view: ImageSequenceAnimationView, private val index: Int) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? {
            view.run {
                if (isPlaying) {
                    if (customCache != null) {
                        for (index in index until index + CACHE_COUNT) {
                            if (customCache != null && customCache!![index] != null) {
                                try {
                                    customCache!![index]?.createBitmap()
                                } catch (e: OutOfMemoryError) {

                                } catch (e1: Resources.NotFoundException) {

                                }
                            }
                        }
                    }
                    handler.removeMessages(MESSAGE_INVALIDATE)
                    val msg = handler.obtainMessage(MESSAGE_INVALIDATE)
                    handler.sendMessageDelayed(msg, delay)
                }
                return null
            }
        }
    }
}
