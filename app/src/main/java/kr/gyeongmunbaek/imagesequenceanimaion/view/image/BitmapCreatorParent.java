package kr.gyeongmunbaek.imagesequenceanimaion.view.image;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by gyeongmunbaek on 2016-11-02.
 */

abstract public class BitmapCreatorParent {
    private Context mContext = null;

    private int mIndex = 0;
    private Bitmap mBitMap = null;
    private float mDensity = 2.0f;

    public BitmapCreatorParent() {
    }

    public BitmapCreatorParent(Context pContext, Integer pIndex) {
        mContext = pContext;
        mIndex = pIndex.intValue();

        mDensity = mContext.getResources().getDisplayMetrics().density;
    }

    public void createBitmap() throws Resources.NotFoundException, OutOfMemoryError{
        if (mBitMap == null) {
            int lIndex = getStartIndex() + (mIndex % getFileCount());
            String lFileName = getFileNamePrefix();

            if (getFileCount() >= 100) {
                if (lIndex < 100) {
                    lFileName += "0";
                }
                if (lIndex < 10) {
                    lFileName += "0";
                }
            } else {
                if (lIndex < 10) {
                    lFileName += "0";
                }
            }

            lFileName += String.valueOf(lIndex);
            int resID = mContext.getResources().getIdentifier(lFileName, "drawable",
                    mContext.getPackageName());
            if (resID == 0) {
                throw new Resources.NotFoundException();
            }

            long lMaxMemory = Runtime.getRuntime().maxMemory();
            long lAllocationMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long lRealfreeMemory = lMaxMemory - lAllocationMemory;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(mContext.getResources(), resID, options);

            float lNeedsMemory = (options.outWidth * mDensity) * (options.outHeight * mDensity) * 4;

            if (lRealfreeMemory - lNeedsMemory > 0) {
                try {
                    mBitMap = BitmapFactory.decodeResource(mContext.getResources(), resID);
                } catch (OutOfMemoryError e) {
                    throw new OutOfMemoryError();
                }
            }
        }
    }

    public Bitmap getBitmap() {
        return mBitMap;
    }

    public void recycle() {
        if (mBitMap != null) {
            mBitMap.recycle();
            mBitMap = null;
        }
    }

    abstract public int getFileCount();

    abstract public String getFileNamePrefix();

    abstract public int getStartIndex();
}
