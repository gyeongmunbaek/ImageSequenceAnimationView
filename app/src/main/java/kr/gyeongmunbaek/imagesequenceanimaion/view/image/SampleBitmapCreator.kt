package kr.gyeongmunbaek.imagesequenceanimaion.view.image

import android.content.Context

/**
 * Created by gyeongmunbaek on 2017-10-26.
 */

class SampleBitmapCreator : BitmapCreatorParent {

    override val fileCount: Int = 22
    // set prefix string of image file name
    override val fileNamePrefix: String = "output_"
    // set images start file index. ex) in output_0"0"
    override val startIndex: Int = 0

    constructor() : super()

    constructor(context: Context, index: Int) : super(context, index)
}
