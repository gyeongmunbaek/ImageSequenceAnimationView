package kr.gyeongmunbaek.imagesequenceanimaion.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import kr.gyeongmunbaek.imagesequenceanimaion.view.image.BitmapCreatorParent;
import kr.gyeongmunbaek.imagesequenceanimaion.view.image.cache.CustomCache;

/**
 * Created by gyeongmunbaek on 2017-10-26.
 */

public class ImageSequenceAnimationView extends View {
    private static final int CACHE_COUNT = 5;
    private static final int MESSAGE_INVALIDATE = 1;
    private static final int MESSAGE_START_ANIMATION = 2;

    private static final int MESSAGE_DELAY = 10;

    private final Context mContext;

    private CustomCache mCustomCache = null;

    private int mCacheCount = CACHE_COUNT;
    private int mDelay = MESSAGE_DELAY;

    private BitmapCreatorParent mBitmapClass = null;

    private int mCurrentIndex = 0;
    private Bitmap mPreBitmap = null;
    private boolean mIsPlaying = false;

    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MESSAGE_INVALIDATE) {
                new Runnable() {
                    @Override
                    public void run() {
                        if (mIsPlaying) {
                            invalidate();
                            update(mCurrentIndex);
                        }
                    }
                }.run();
            } else if (msg.what == MESSAGE_START_ANIMATION) {
                mHandler.removeMessages(MESSAGE_INVALIDATE);
                new Runnable() {
                    @Override
                    public void run() {
                        setBitmapClass(mBitmapClass);
                    }
                }.run();
            }
            return false;
        }
    });

    public ImageSequenceAnimationView(Context context) {
        super(context);
        mContext = context;
    }

    public ImageSequenceAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public ImageSequenceAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public ImageSequenceAnimationView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas.getWidth() > 0 && canvas.getHeight() > 0) {
            if (mCustomCache != null && mCustomCache.get(mCurrentIndex) != null) {
                Bitmap lBitmap = mCustomCache.get(mCurrentIndex).getBitmap();

                if (lBitmap != null) {
                    mPreBitmap = lBitmap;
                    canvas.drawBitmap(
                            lBitmap,
                            null,
                            new RectF(0, 0, canvas.getWidth(), canvas.getHeight()),
                            null);

                    mCurrentIndex++;
                } else if (mPreBitmap != null && mPreBitmap.isRecycled() == false) {
                    canvas.drawBitmap(
                            mPreBitmap,
                            null,
                            new RectF(0, 0, canvas.getWidth(), canvas.getHeight()),
                            null);
                }
            }
        }
    }

    public ImageSequenceAnimationView setCacheCount(int pCount) {
        mCacheCount = pCount;
        return this;
    }

    public ImageSequenceAnimationView setDelay(int pDelay) {
        mDelay = pDelay;
        return this;
    }

    public ImageSequenceAnimationView setAnimationClazz(final BitmapCreatorParent pClass) throws NullPointerException {
        if (pClass == null) {
            throw new NullPointerException();
        }
        mBitmapClass = pClass;
        return this;
    }

    public void startAnimation() {
        if (mHandler != null) {
            mHandler.removeMessages(MESSAGE_START_ANIMATION);
            Message msg = mHandler.obtainMessage(MESSAGE_START_ANIMATION);
            mHandler.sendMessageDelayed(msg, mDelay);
        }
    }

    private void setBitmapClass(final BitmapCreatorParent pClass) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mIsPlaying = false;

                if (mHandler != null) {
                    mHandler.removeMessages(MESSAGE_INVALIDATE);
                    mHandler.removeMessages(MESSAGE_START_ANIMATION);
                }

                if (mCustomCache != null) {
                    mCustomCache.evictAll();
                    mCustomCache = null;
                }

                if (mPreBitmap != null) {
                    if (mPreBitmap.isRecycled() == false) {
                        mPreBitmap.recycle();
                    }
                    mPreBitmap = null;
                }

                mCustomCache = new CustomCache(mContext, mCacheCount, pClass);
                mCurrentIndex = 0;

                for (int index = 0; index < mCacheCount; index++) {
                    try {
                        mCustomCache.get(index).createBitmap();
                    } catch (OutOfMemoryError e) {

                    } catch (Resources.NotFoundException e1) {

                    }
                }

                mIsPlaying = true;
                if (mHandler != null) {
                    Message msg = mHandler.obtainMessage(MESSAGE_INVALIDATE);
                    mHandler.sendMessageDelayed(msg, mDelay);
                }
                return null;
            }
        }.execute();
    }

    public void destroy() {
        mIsPlaying = false;

        if (mHandler != null) {
            mHandler.removeMessages(MESSAGE_INVALIDATE);
            mHandler.removeMessages(MESSAGE_START_ANIMATION);
        }

        if (mCustomCache != null) {
            mCustomCache.evictAll();
            mCustomCache = null;
        }

        if (mPreBitmap != null) {
            if (mPreBitmap.isRecycled() == false) {
                mPreBitmap.recycle();
            }
            mPreBitmap = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        destroy();
    }

    private void update(final int pCurrentIndex) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (mIsPlaying) {
                    if (mCustomCache != null) {
                        for (int index = pCurrentIndex; index < pCurrentIndex + CACHE_COUNT; index++) {
                            if (mCustomCache != null && mCustomCache.get(index) != null) {
                                try {
                                    mCustomCache.get(index).createBitmap();
                                } catch (OutOfMemoryError e) {

                                } catch (Resources.NotFoundException e1) {

                                }
                            }
                        }
                    }
                    if (mHandler != null) {
                        mHandler.removeMessages(MESSAGE_INVALIDATE);
                        Message msg = mHandler.obtainMessage(MESSAGE_INVALIDATE);
                        mHandler.sendMessageDelayed(msg, mDelay);
                    }
                }
                return null;
            }
        }.execute();
    }
}
