package com.uroad.zhgs.widget


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

import com.uroad.zhgs.R

class StarBar : View {
    private var starDistance = 0 // 星星间距
    private var starCount = 0 // 星星个数
    private var starSize: Int = 0 // 星星高度大小，星星一般正方形，宽度等于高度
    /**
     * 获取显示星星的数目
     *
     * @return starMark
     */
    /**
     * 设置显示的星星的分数
     */
    // 调用监听接口
    var starMark = 0.00f
        set(mark) {
            field = if (integerMark) {
                Math.ceil(mark.toDouble()).toInt().toFloat()
            } else {
                Math.round(mark * 100) * 1.0f / 100
            }
            this.onStarChangeListener?.onStarChange(this.starMark)
            invalidate()
        } // 评分星星

    private var starFillBitmap: Bitmap? = null // 亮星星
    private var starEmptyDrawable: Drawable? = null// 暗星星
    private var onStarChangeListener: OnStarChangeListener? = null// 监听星星变化接口
    private lateinit var paint: Paint // 绘制星星画笔
    private var integerMark = false
    private var canEdit: Boolean = false

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    /**
     * 初始化UI组件
     *
     * @param context
     * @param attrs
     */
    private fun init(context: Context, attrs: AttributeSet) {
        isClickable = true
        val mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.StarBar)
        this.starDistance = mTypedArray.getDimension(
                R.styleable.StarBar_starDistance, 0f).toInt()
        this.starSize = mTypedArray.getDimension(
                R.styleable.StarBar_starSize, 0f).toInt()
        this.starCount = mTypedArray.getInteger(
                R.styleable.StarBar_starNumber, 0)
        this.starEmptyDrawable = mTypedArray.getDrawable(R.styleable.StarBar_starEmpty)
        this.starFillBitmap = drawableToBitmap(mTypedArray.getDrawable(R.styleable.StarBar_starFill))
        this.canEdit = mTypedArray.getBoolean(R.styleable.StarBar_isEdit, true)
        this.integerMark = mTypedArray.getBoolean(R.styleable.StarBar_isIntMark, true)
        mTypedArray.recycle()
        paint = Paint().apply {
            isAntiAlias = true
            starFillBitmap?.let {
                shader = BitmapShader(it, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            }
        }
    }

    /**
     * 设置是否需要整数评分
     *
     * @param integerMark
     */
    fun setIntegerMark(integerMark: Boolean) {
        this.integerMark = integerMark
    }

    fun setCanEdit(canEdit: Boolean) {
        this.canEdit = canEdit
        invalidate()
    }

    fun setStarCount(starCount: Int) {
        this.starCount = starCount
        invalidate()
    }

    /**
     * 定义星星点击的监听接口
     */
    interface OnStarChangeListener {
        fun onStarChange(mark: Float)
    }

    /**
     * 设置监听
     *
     * @param onStarChangeListener
     */
    fun setOnStarChangeListener(
            onStarChangeListener: OnStarChangeListener) {
        this.onStarChangeListener = onStarChangeListener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(starSize * starCount + starDistance * (starCount - 1), starSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in 0 until starCount) {
            starEmptyDrawable?.let {
                it.setBounds((starDistance + starSize) * i, 0,
                        (starDistance + starSize) * i + starSize, starSize)
                it.draw(canvas)
            }
        }
        if (this.starMark > 1) {
            canvas.drawRect(0f, 0f, starSize.toFloat(), starSize.toFloat(), paint)
            if (this.starMark - this.starMark.toInt() == 0f) {
                var i = 1
                while (i < this.starMark) {
                    canvas.translate((starDistance + starSize).toFloat(), 0f)
                    canvas.drawRect(0f, 0f, starSize.toFloat(), starSize.toFloat(), paint)
                    i++
                }
            } else {
                var i = 1
                while (i < this.starMark - 1) {
                    canvas.translate((starDistance + starSize).toFloat(), 0f)
                    canvas.drawRect(0f, 0f, starSize.toFloat(), starSize.toFloat(), paint)
                    i++
                }
                canvas.translate((starDistance + starSize).toFloat(), 0f)
                canvas.drawRect(
                        0f,
                        0f,
                        starSize * (Math.round((this.starMark - this.starMark.toInt()) * 10) * 1.0f / 10),
                        starSize.toFloat(), paint)
            }
        } else {
            canvas.drawRect(0f, 0f, starSize * this.starMark, starSize.toFloat(), paint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!canEdit) {
            return false
        }
        var x = event.x.toInt()
        if (x < 0)
            x = 0
        if (x > measuredWidth)
            x = measuredWidth
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                starMark = x * 1.0f / (measuredWidth * 1.0f / starCount)
            }
            MotionEvent.ACTION_MOVE -> {
                starMark = x * 1.0f / (measuredWidth * 1.0f / starCount)
            }
            MotionEvent.ACTION_UP -> {
            }
        }
        invalidate()
        return super.onTouchEvent(event)
    }

    /**
     * drawable转bitmap
     *
     * @param drawable
     * @return
     */
    private fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) return null
        val bitmap = Bitmap.createBitmap(starSize, starSize,
                Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, starSize, starSize)
        drawable.draw(canvas)
        return bitmap
    }
}
