package com.uroad.zhgs.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import android.view.ViewGroup
import android.animation.ValueAnimator
import android.annotation.SuppressLint


/**
 * @author MFB
 * @create 2018/9/19
 * @describe Android实现下拉放大图片，松手自动反弹效果
 */
class HeadZoomView : RelativeLayout {
    private var mZoomView: View? = null
    private var mZoomViewWidth: Int = 0
    private var mZoomViewHeight: Int = 0

    private var firstPosition: Float = 0.toFloat()//记录第一次按下的位置
    private var isScrolling: Boolean = false//是否正在缩放
    private var mScrollRate = 0.3f//缩放系数，缩放系数越大，变化的越大
    private var mReplyRate = 0.5f//回调系数，越大，回调越慢

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setZoomView(mZoomView: View) {
        this.mZoomView = mZoomView
    }

    fun setScrollRate(mScrollRate: Float) {
        this.mScrollRate = mScrollRate
    }

    fun setReplyRate(mReplyRate: Float) {
        this.mReplyRate = mReplyRate
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        init()
    }

    private fun init() {
        overScrollMode = View.OVER_SCROLL_NEVER
        if (getChildAt(0) != null) {
            mZoomView = getChildAt(0)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        mZoomView?.let {
            if (mZoomViewWidth <= 0 || mZoomViewHeight <= 0) {
                mZoomViewWidth = it.measuredWidth
                mZoomViewHeight = it.measuredHeight
            }
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    //手指离开后恢复图片
                    isScrolling = false
                    replyImage(it)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!isScrolling) {
                        if (scrollY == 0) {
                            firstPosition = event.y// 滚动到顶部时记录位置，否则正常返回
                        } else {
                            return true
                        }
                    }
                    val distance = ((event.y - firstPosition) * mScrollRate) // 滚动距离乘以一个系数
                    if (distance < 0) { // 当前位置比记录位置要小，正常返回
                        return true
                    }
                    // 处理放大
                    isScrolling = true
                    setZoom(it, distance)
                    return true // 返回true表示已经完成触摸事件，不再处理
                }
            }
        }
        return true
    }

    //回弹动画
    private fun replyImage(mZoomView: View) {
        val distance = mZoomView.measuredWidth - mZoomViewWidth
        val valueAnimator = ValueAnimator.ofFloat(distance.toFloat(), 0f).setDuration((distance * mReplyRate).toLong())
        valueAnimator.addUpdateListener { animation -> setZoom(mZoomView, animation.animatedValue as Float) }
        valueAnimator.start()
    }

    private fun setZoom(mZoomView: View, zoom: Float) {
        if (mZoomViewWidth <= 0 || mZoomViewHeight <= 0) {
            return
        }
        val lp = mZoomView.layoutParams
        lp.width = (mZoomViewWidth + zoom).toInt()
        lp.height = (mZoomViewHeight * ((mZoomViewWidth + zoom) / mZoomViewWidth)).toInt()
        (lp as ViewGroup.MarginLayoutParams).setMargins(-(lp.width - mZoomViewWidth) / 2, 0, 0, 0)
        mZoomView.layoutParams = lp
    }
}