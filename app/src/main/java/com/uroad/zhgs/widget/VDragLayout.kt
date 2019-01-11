package com.uroad.zhgs.widget

import android.annotation.SuppressLint
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.uroad.zhgs.R

class VDragLayout : ConstraintLayout {

    private lateinit var dragView: View
    private lateinit var dragHelper: ViewDragHelper
    private var dragViewWidth: Int = 0
    private var dragViewHeight: Int = 0

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        dragHelper = ViewDragHelper.create(this, 1.0f, object : ViewDragHelper.Callback() {
            override fun tryCaptureView(child: View, pointerId: Int): Boolean {
                return child == dragView // 只处理dragView
            }

            override fun onEdgeDragStarted(edgeFlags: Int, pointerId: Int) {
                dragHelper.captureChildView(dragView, pointerId)
            }

            override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
                var l = left
                if (left > width - dragViewWidth) { //右边边界
                    l = width - dragViewWidth
                } else if (left < 0) {    //左边边界
                    l = 0
                }
                return l
            }

            override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
                var t = top
                if (top > height - dragViewHeight) { // 底部边界
                    t = height - dragViewHeight
                } else if (top < 0) {
                    t = 0 // 顶部边界
                }
                return t
            }

            override fun getViewVerticalDragRange(child: View): Int {
                return measuredHeight - child.measuredHeight
            }

            override fun getViewHorizontalDragRange(child: View): Int {
                return measuredWidth - child.measuredWidth
            }
        })
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let { return dragHelper.shouldInterceptTouchEvent(it) }
        return super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            dragHelper.processTouchEvent(it)
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun computeScroll() {
        if (dragHelper.continueSettling(true)) invalidate()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        dragView = findViewById(R.id.dragView)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        dragViewWidth = dragView.measuredWidth
        dragViewHeight = dragView.measuredHeight
    }
}