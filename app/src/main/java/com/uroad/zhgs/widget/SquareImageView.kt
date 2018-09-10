package com.uroad.zhgs.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView


/**
 *Created by MFB on 2018/8/7.
 */

class SquareImageView : ImageView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMS = widthMeasureSpec
        var heightMS = heightMeasureSpec
        setMeasuredDimension(View.getDefaultSize(0, widthMS), View.getDefaultSize(0, heightMS))
        val childWidthSize = measuredWidth
        //高度和宽度一样
        widthMS = View.MeasureSpec.makeMeasureSpec(childWidthSize, View.MeasureSpec.EXACTLY)
        heightMS = widthMeasureSpec
        super.onMeasure(widthMS, heightMS)
    }

}