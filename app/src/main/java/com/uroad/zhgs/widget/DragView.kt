package com.uroad.zhgs.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import com.uroad.zhgs.R

/*任意拖拽 全屏移动 随手指移动的DragView*/
class DragView : ImageView {
    private var lastX: Int = 0
    private var lastY: Int = 0
    private var marginStart: Int = 0
    private var marginEnd: Int = 0
    private var marginTop: Int = 0
    private var marginBottom: Int = 0
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var isDrag = false

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

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.action
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = ev.rawX.toInt()//设定移动的初始位置相对位置
                lastY = ev.rawY.toInt()
            }
            MotionEvent.ACTION_MOVE -> { //移动
                //event.getRawX()事件点距离屏幕左上角的距离
                val dx = ev.rawX.toInt() - lastX
                val dy = ev.rawY.toInt() - lastY
                var left = this.left + dx
                var top = this.top + dy
                var right = this.right + dx
                var bottom = this.bottom + dy
                if (left < marginStart) { //最左边
                    left = marginStart
                    right = left + this.width
                }
                if (right > screenWidth) { //最右边
                    right = screenWidth
                    left = right - this.width
                }
                if (top < marginTop) {  //最上边
                    top = marginTop
                    bottom = top + this.height
                }
                if (bottom > screenHeight) {//最下边
                    bottom = screenHeight
                    top = bottom - this.height
                }
                this.layout(left, top, right, bottom)//设置控件的新位置
                isDrag = true
                lastX = ev.rawX.toInt()//再次将滑动其实位置定位
                lastY = ev.rawY.toInt()
            }
            MotionEvent.ACTION_UP -> {
                if (isDrag) {
                    isDrag = false
                    return true
                }
            }
        }
        return super.onTouchEvent(ev)
    }
}