package com.uroad.zhgs.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.library.utils.ScreenUtils
import com.uroad.zhgs.R

class CurrencyLoadView : FrameLayout {
    private lateinit var loadingView: View
    private lateinit var errorView: View
    private lateinit var emptyView: View
    private lateinit var mLoadingTv: TextView
    private lateinit var ivPic: ImageView
    private lateinit var tvErrorTips: TextView
    private lateinit var tvReload: TextView
    private lateinit var mEmptyIv: ImageView
    private lateinit var mEmptyTv: TextView
    private var onRetryListener: OnRetryListener? = null

    companion object {
        const val STATE_IDEA = 0
        const val STATE_LOADING = 1
        const val STATE_NONETWORK = 2
        const val STATE_ERROR = 3
        const val STATE_EMPTY = 4
        const val STATE_GONE = 5
    }

    constructor(context: Context) : super(context) {
        init(context)
    }


    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        loadingView = LayoutInflater.from(context).inflate(R.layout.layout_loading, LinearLayout(context), false)
        errorView = LayoutInflater.from(context).inflate(R.layout.layout_error, LinearLayout(context), false)
        emptyView = LayoutInflater.from(context).inflate(R.layout.layout_empty, LinearLayout(context), false)
        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.CENTER
        addView(loadingView, params)
        addView(errorView)
        addView(emptyView, params)
        setState(STATE_IDEA)
        findViews()
    }

    private fun findViews() {
        mLoadingTv = findViewById(R.id.mLoadingTv)
        ivPic = findViewById(R.id.ivPic)
        tvErrorTips = findViewById(R.id.tvErrorTips)
        tvReload = findViewById(R.id.tvReload)
        mEmptyIv = findViewById(R.id.mEmptyIv)
        mEmptyTv = findViewById(R.id.mEmptyTv)
        val imageSize = ScreenUtils.getScreenWidth(context) / 3
        val picParams = LinearLayout.LayoutParams(imageSize, imageSize)
        ivPic.layoutParams = picParams
        mEmptyIv.layoutParams = picParams
        tvReload.setOnClickListener {
            setState(STATE_IDEA)
            onRetryListener?.onRetry(this@CurrencyLoadView)
        }
    }

    fun setLoadingText(text: CharSequence) {
        mLoadingTv.text = text
    }

    fun setEmptyIco(resId: Int) {
        mEmptyIv.setImageResource(resId)
    }

    fun setEmptyText(text: CharSequence) {
        mEmptyTv.text = text
    }

    fun setErrorText(text: CharSequence) {
        tvErrorTips.text = text
    }

    fun setErrorIcon(resId: Int) {
        ivPic.setImageResource(resId)
    }

    fun setState(state: Int) {
        when (state) {
            STATE_IDEA -> visibility = View.GONE
            STATE_LOADING -> {
                errorView.visibility = View.GONE
                emptyView.visibility = View.GONE
                loadingView.visibility = View.VISIBLE
                visibility = View.VISIBLE
            }
            STATE_NONETWORK -> {
                loadingView.visibility = View.GONE
                emptyView.visibility = View.GONE
                errorView.visibility = View.VISIBLE
                visibility = View.VISIBLE
                setErrorText(resources.getString(R.string.nonetwork))
                setErrorIcon(R.mipmap.ic_nonetwork)
            }
            STATE_ERROR -> {
                loadingView.visibility = View.GONE
                emptyView.visibility = View.GONE
                errorView.visibility = View.VISIBLE
                visibility = View.VISIBLE
                setErrorText(resources.getString(R.string.connect_error))
                setErrorIcon(R.mipmap.ic_connect_error)
            }
            STATE_EMPTY -> {
                loadingView.visibility = View.GONE
                errorView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
                visibility = View.VISIBLE
            }
            STATE_GONE -> {
                visibility = View.GONE
            }
        }
    }

    interface OnRetryListener {
        fun onRetry(view: View)
    }

    fun setOnRetryListener(onRetryListener: OnRetryListener) {
        this.onRetryListener = onRetryListener
    }
}