package kr.gyeongmunbaek.imagesequenceanimaion.view.image.cache;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import kr.gyeongmunbaek.imagesequenceanimaion.view.image.BitmapCreatorParent;

/**
 * Created by gyeongmunbaek on 2017-10-26.
 */


public class CustomCache<T extends BitmapCreatorParent> {
    private int mCacheCount = 0;
    private Context mContext = null;
    private BitmapCreatorParent mRealClass = null;

    private Map<Integer, T> mMap = null;
    private int mLastIndex = 0;

    public CustomCache(Context pContext, int pCacheCount, BitmapCreatorParent pRealClass) {
        mCacheCount = pCacheCount;
        mContext = pContext;
        mRealClass = pRealClass;

        mMap = new HashMap<>();
    }

    public T get(Integer pKey) {
        T lTemp = mMap.get(pKey);

        if (lTemp == null) {
            int lIndex = pKey.intValue();
            int lPreIndex = (lIndex - mCacheCount);

            if (lPreIndex >= 0) {
                if (mMap.get(Integer.valueOf(lPreIndex)) != null) {
                    mMap.get(Integer.valueOf(lPreIndex)).recycle();
                    mMap.remove(Integer.valueOf(lPreIndex));
                }
            }
            lTemp = create(pKey);
            mMap.put(pKey, lTemp);
            mLastIndex = pKey.intValue();
        }

        return lTemp;
    }

    private T create(Integer pKey) {
        T lResultClass = null;
        try {
            Class<T> Clazz = (Class<T>) mRealClass.getClass();
            lResultClass = Clazz.getDeclaredConstructor(Context.class, Integer.class).newInstance(mContext, pKey);
        } catch (InstantiationException e) {
            e.printStackTrace();
            Log.i("GMBAEK", "InstantiationException : " + e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.i("GMBAEK", "IllegalAccessException : " + e.getMessage());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Log.i("GMBAEK", "NoSuchMethodException : " + e.getMessage());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            Log.i("GMBAEK", "InvocationTargetException : " + e.getMessage());
        }

        return lResultClass;
    }

    public void evictAll() {
        for (int index = mLastIndex - mCacheCount; index <= mLastIndex; index++) {
            Integer lKey = Integer.valueOf(index);
            if (mMap.get(lKey) != null) {
                mMap.get(lKey).recycle();
                mMap.remove(lKey);
            }
        }
        mMap.clear();
        mMap = null;

        if (mRealClass != null) {
            mRealClass.recycle();
        }
    }

    public void evict(Integer pKey) {
        if (mMap.get(pKey) != null) {
            mMap.get(pKey).recycle();
            mMap.remove(pKey);
        }
    }

    public int size() {
        return mMap.size();
    }
}
