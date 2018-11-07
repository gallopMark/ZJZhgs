package com.uroad.zhgs.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class CustomView : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val measuredWidth = measuredWidth
        val measuredHeight = measuredHeight
        val max = Math.max(measuredWidth, measuredHeight)
        setMeasuredDimension(max, max)
    }
}
