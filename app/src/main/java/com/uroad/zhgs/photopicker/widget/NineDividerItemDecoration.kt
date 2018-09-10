package com.uroad.zhgs.photopicker.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * RecyclerView 九宫格分割线
 */
class NineDividerItemDecoration : RecyclerView.ItemDecoration {
    companion object {
        private val ATTRS = intArrayOf(android.R.attr.listDivider)
    }

    private var spanColumnpan: Int = 0

    private var mDivider: Drawable? = null

    //几列
    constructor(context: Context, spanColumn: Int) {
        val a = context.obtainStyledAttributes(ATTRS)
        mDivider = a.getDrawable(0)
        a.recycle()
        this.spanColumnpan = spanColumn
    }

    constructor(context: Context, spanColumn: Int, resID: Int) {
        mDivider = ContextCompat.getDrawable(context, resID)
        this.spanColumnpan = spanColumn
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        drawHorizontal(c, parent)
        drawVertical(c, parent)
    }

    // 平行线
    private fun drawVertical(c: Canvas, parent: RecyclerView) {
        mDivider?.let {
            val left = parent.paddingLeft
            val right = parent.width - parent.paddingRight
            val childCount = parent.childCount
            var top: Int
            var i = 0
            while (i < childCount) {
                val child = parent.getChildAt(i)
                val params = child.layoutParams as RecyclerView.LayoutParams
                top = child.bottom + params.bottomMargin
                val bottom = top + it.intrinsicHeight
                it.setBounds(left, top, right, bottom)
                it.draw(c)
                i += spanColumnpan
            }
        }
    }

    // 竖线
    private fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        mDivider?.let {
            var top: Int
            val childCount = parent.childCount
            var left: Int
            var bottom: Int
            var i = 0
            while (i < childCount) {
                val child = parent.getChildAt(i)
                val params = child.layoutParams as RecyclerView.LayoutParams
                left = child.right + params.rightMargin
                bottom = child.bottom + params.bottomMargin
                top = child.top + params.topMargin
                for (j in 0..spanColumnpan) {
                    val mLeft = left * j
                    val right = mLeft + it.intrinsicHeight
                    it.setBounds(mLeft, top, right, bottom)
                    it.draw(c)
                }
                i = i + spanColumnpan
            }
        }
    }
}
