package com.uroad.zhgs.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.uroad.zhgs.R

/*任意拖拽 全屏移动 随手指移动的DragView*/
class DragView : ImageView {
    private var marginStart: Int = 0
    private var marginEnd: Int = 0
    private var marginTop: Int = 0
    private var marginBottom: Int = 0
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var isDrag = false
    private var startDownX = 0 //手指按下 记录坐标(X,Y)
    private var startDownY = 0
    private var onClickListener: OnClickListener? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        screenWidth = getScreenWidth()
        screenHeight = getScreeHeight()
        val ta = context.obtainStyledAttributes(attrs, R.styleable.DragView)
        marginStart = ta.getDimension(R.styleable.DragView_dragMarginStart, 0f).toInt()
        marginEnd = ta.getDimension(R.styleable.DragView_dragMarginEnd, 0f).toInt()
        marginTop = ta.getDimension(R.styleable.DragView_dragMarginTop, 0f).toInt()
        marginBottom = ta.getDimension(R.styleable.DragView_dragMarginBottom, 0f).toInt()
        screenWidth = screenWidth - marginStart - marginEnd
        screenHeight = screenHeight - marginTop - marginBottom
        ta.recycle()
    }

    fun setMargin(left: Int, top: Int, right: Int, bottom: Int) {
        this.marginStart = left
        this.marginEnd = right
        this.marginTop = top
        this.marginBottom = bottom
        screenWidth = getScreenWidth() - left - right
        screenHeight = getScreeHeight() - top - bottom
    }

    private fun getScreenWidth(): Int = resources.displayMetrics.widthPixels
    private fun getScreeHeight(): Int = resources.displayMetrics.heightPixels

    fun setOnClickListener(onClickListener: OnClickListener?) {
        this.onClickListener = onClickListener
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        val lastMoveX: Int // 用于记录手指滑动的 距离  (x,y)
        val lastMoveY: Int
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDrag = true
                startDownX = event.rawX.toInt() //记录手指按下坐标
                startDownY = event.rawY.toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                lastMoveX = event.rawX.toInt()  //滑动之后的坐标
                lastMoveY = event.rawY.toInt()
                val dx = lastMoveX - startDownX  //计算滑动的距离  dx  dy
                val dy = lastMoveY - startDownY
                if (Math.abs(dx) > 2 || Math.abs(dy) > 2) {  // 用时候手指想点击 但是会有抖动的情况  误以为是滑动这样popup不会弹出来
                    //  判断如果移动距离大于 2  则认为用户是移动 否则是点击
                    isDrag = false
                }
                var left = left + dx      //计算 控件的位置
                var top = top + dy
                var right = right + dx
                var bottom = bottom + dy
                if (left < 0) {         //判断是否越出屏幕边界
                    left = 0
                    right = left + width
                }
                if (right > screenWidth) { //判断是否越出屏幕边界
                    right = screenWidth
                    left = right - width
                }
                if (top < 0) { //判断是否越出屏幕边界
                    top = 0
                    bottom = top + height
                }
                if (bottom > screenHeight) { //判断是否越出屏幕边界
                    bottom = screenHeight
                    top = bottom - height
                }
                layout(left, top, right, bottom) //控件重新绘制
                invalidate()       //刷新页面
                startDownX = event.rawX.toInt()     //最后移动的位置  当做初始位置
                startDownY = event.rawY.toInt()
            }
            MotionEvent.ACTION_UP -> {
                if (isDrag) {     //手指离开 判断是点击还是滑动  弹出popup
                    onClickListener?.onClick(this)
                }
                isDrag = false
            }
        }
        return true
    }

    interface OnClickListener {
        fun onClick(view: View)
    }
}