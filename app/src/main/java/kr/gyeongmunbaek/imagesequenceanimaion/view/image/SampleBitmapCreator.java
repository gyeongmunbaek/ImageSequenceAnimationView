package kr.gyeongmunbaek.imagesequenceanimaion.view.image;

import android.content.Context;

/**
 * Created by gyeongmunbaek on 2017-10-26.
 */

public class SampleBitmapCreator extends BitmapCreatorParent {
    public SampleBitmapCreator() {
        super();
    }

    public SampleBitmapCreator(Context pContext, Integer pIndex) {
        super(pContext, pIndex);
    }

    @Override
    public int getFileCount() {
        return 22;
    }

    @Override
    public String getFileNamePrefix() {
        // set prefix string of image file name
        return "output_";
    }

    @Override
    public int getStartIndex() {
        // set images start file index. ex) in output_0"0"
        return 0;
    }
}
