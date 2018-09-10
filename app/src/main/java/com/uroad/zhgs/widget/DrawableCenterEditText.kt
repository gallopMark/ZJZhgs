package com.uroad.zhgs.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.EditText


/**
 *Created by MFB on 2018/8/9.
 * 自定义控件让TextView的drawableLeft或者drawableRight与文本一起居中显示
 */
class DrawableCenterEditText : EditText {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onDraw(canvas: Canvas) {
        val drawables = compoundDrawables
        if (drawables != null) {
            val leftDrawable = drawables[0] //drawableLeft
            val rightDrawable = drawables[2]//drawableRight
            if (leftDrawable != null || rightDrawable != null) {
                //1,获取text的width
                val textWidth = paint.measureText(hint.toString())
                //2,获取padding
                val drawablePadding = compoundDrawablePadding
                val drawableWidth: Int
                val bodyWidth: Float
                if (leftDrawable != null) {
                    //3,获取drawable的宽度
                    drawableWidth = leftDrawable.intrinsicWidth
                    //4,获取绘制区域的总宽度
                    bodyWidth = textWidth + drawablePadding.toFloat() + drawableWidth.toFloat()
                } else {
                    drawableWidth = rightDrawable.intrinsicWidth
                    bodyWidth = textWidth + drawablePadding.toFloat() + drawableWidth.toFloat()
                    //图片居右设置padding
                    setPadding(0, 0, (width - bodyWidth).toInt(), 0)
                }
                canvas.translate((width - bodyWidth) / 2, 0f)
            }
        }
        super.onDraw(canvas)
    }
}