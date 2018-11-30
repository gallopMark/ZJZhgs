package com.uroad.zhgs.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.zhgs.R

class CurrencyLoadView : FrameLayout {
    private val mContext: Context
    private lateinit var loadingView: View
    private lateinit var errorView: View
    private lateinit var emptyView: View
    private lateinit var mLoadingTv: TextView
    private lateinit var mErrorTv: TextView
    private lateinit var tvReload: TextView
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
        mContext = context
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mContext = context
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mContext = context
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
        addView(emptyView, params.apply {
            leftMargin = mContext.resources.getDimensionPixelOffset(R.dimen.margin_30)
            rightMargin = mContext.resources.getDimensionPixelOffset(R.dimen.margin_30)
        })
        setState(STATE_IDEA)
        findViews()
    }

    private fun findViews() {
        mLoadingTv = findViewById(R.id.mLoadingTv)
        mErrorTv = findViewById(R.id.mErrorTv)
        tvReload = findViewById(R.id.tvReload)
        mEmptyTv = findViewById(R.id.mEmptyTv)
        tvReload.setOnClickListener {
            setState(STATE_IDEA)
            onRetryListener?.onRetry(this@CurrencyLoadView)
        }
    }

    fun setLoadingText(text: CharSequence?) {
        mLoadingTv.text = text
    }

    fun setEmptyIco(resId: Int) {
        val drawableTop = ContextCompat.getDrawable(mContext, resId)
        mEmptyTv.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop, null, null)
    }

    fun setEmptyText(text: CharSequence?) {
        mEmptyTv.text = text
    }

    fun setErrorText(text: CharSequence?) {
        mErrorTv.text = text
    }

    fun setErrorIcon(resId: Int) {
        val drawableTop = ContextCompat.getDrawable(mContext, resId)
        mErrorTv.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop, null, null)
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