package com.uroad.zhgs.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.TextView
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R


/**
 * 自定义形状的TextView 圆形 椭圆形
 * Created by cjy on 16/11/30.
 */
class CustomShapeTextView : TextView {
    /**
     * 画笔
     */
    private lateinit var mPaint: Paint
    /**
     * 画笔颜色 默认灰色
     */
    private var mPaintNormalColor = -0x232324
    /**
     * 画笔颜色 选中时的颜色,默认灰色
     */
    private var mPaintSelectColor = -0x232324
    /**
     * 是否填充颜色
     */
    private var isFillColor = false

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initPaint(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        initPaint(context, attrs)
    }

    /**
     * 初始化画笔和自定义属性
     * @param context
     * @param attrs
     */
    private fun initPaint(context: Context, attrs: AttributeSet) {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.CustomShapeTextView)
        mPaintNormalColor = typeArray.getColor(R.styleable.CustomShapeTextView_paintNormalColor, mPaintNormalColor)
        mPaintSelectColor = typeArray.getColor(R.styleable.CustomShapeTextView_paintSelectColor, mPaintSelectColor)
        isFillColor = typeArray.getBoolean(R.styleable.CustomShapeTextView_isFillColor, false)
        typeArray.recycle()
        mPaint = Paint().apply { strokeWidth = context.resources.getDimension(R.dimen.margin_1) }
    }


    /**
     * 调用onDraw绘制边框
     * @param canvas
     */
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        //抗锯齿
        mPaint.isAntiAlias = true
        if (isFillColor) {
            //画笔颜色
            mPaint.color = mPaintSelectColor
            mPaint.style = Paint.Style.FILL
        } else {
            //画笔颜色
            mPaint.color = mPaintNormalColor
            //画笔样式:空心
            mPaint.style = Paint.Style.STROKE
        }
        //创建一个区域,限制圆弧范围
        val rectF = RectF()
        //设置半径,比较长宽,取最大值
        val radius = if (measuredWidth > measuredHeight) measuredWidth else measuredHeight
        //设置Padding 不一致,绘制出的是椭圆;一致的是圆形
        rectF.set(paddingLeft.toFloat(), paddingTop.toFloat(), (radius - paddingRight).toFloat(), (radius - paddingBottom).toFloat())
        //绘制圆弧
        canvas.drawArc(rectF, 0f, 360f, false, mPaint)
        //最后调用super方法,解决文本被所绘制的圆圈背景锁覆盖的问题
        super.onDraw(canvas)
    }

    /**
     * 设置是否填充颜色
     * @param isFill
     */
    fun setFillColor(isFill: Boolean) {
        this.isFillColor = isFill
        invalidate()
    }
}