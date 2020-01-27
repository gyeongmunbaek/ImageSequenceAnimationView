package kr.gyeongmunbaek.imagesequenceanimaion.view.image.cache

import android.content.Context
import android.util.Log
import android.util.SparseArray

import java.lang.reflect.InvocationTargetException

import kr.gyeongmunbaek.imagesequenceanimaion.view.image.BitmapCreatorParent

/**
 * Created by gyeongmunbaek on 2017-10-26.
 */


class CustomCache<T : BitmapCreatorParent>(private val context: Context,
                                           private val cacheCount: Int,
                                           private val realClass: BitmapCreatorParent) {
    private val map: SparseArray<T> by lazy { SparseArray<T>() }
    private var lastIndex = 0

    operator fun get(key: Int): T? {
        var tempT = map[key]

        if (tempT == null) {
            val lPreIndex = key - cacheCount

            if (lPreIndex >= 0) {
                map[Integer.valueOf(lPreIndex)]?.let {
                    it.recycle()
                    map.remove(Integer.valueOf(lPreIndex))
                }
            }
            tempT = create(key)
            tempT?.let {
                map.put(key, it)
                lastIndex = key
            }
        }

        return tempT
    }

    private fun create(key: Int): T? {
        var lResultClass: T? = null
        try {
            val clazz = realClass.javaClass as Class<*>
            lResultClass = clazz.getDeclaredConstructor(Context::class.java, Int::class.java).newInstance(context, key) as T?
        } catch (e: InstantiationException) {
            e.printStackTrace()
            Log.i("GMBAEK", "InstantiationException : " + e.message)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            Log.i("GMBAEK", "IllegalAccessException : " + e.message)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
            Log.i("GMBAEK", "NoSuchMethodException : " + e.message)
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
            Log.i("GMBAEK", "InvocationTargetException : " + e.message)
        }

        return lResultClass
    }

    fun evictAll() {
        for (index in lastIndex - cacheCount..lastIndex) {
            map[index]?.let {
                it.recycle()
                map.remove(index)
            }
        }
        map.clear()
        realClass.recycle()
    }

    fun evict(key: Int) {
        map[key]?.let {
           it.recycle()
            map.remove(key)
        }
    }

    fun size(): Int {
        return map.size()
    }
}
