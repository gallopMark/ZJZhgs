package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.zhgs.R
import com.uroad.zhgs.widget.CircularProgressView
import com.uroad.library.utils.DisplayUtils


/**
 *Created by MFB on 2018/7/28.
 */
class LoadingDialog(private val mContext: Activity) : AlertDialog(mContext, R.style.translucentDialog) {
    private val contentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_loading, LinearLayout(mContext), false)
    private val progressBar: CircularProgressView
    private val mLoadingTv: TextView

    init {
        progressBar = contentView.findViewById(R.id.progressBar)
        mLoadingTv = contentView.findViewById(R.id.mLoadingTv)
        setCanceledOnTouchOutside(false)
        setCancelable(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val width = DisplayUtils.getWindowWidth(mContext) / 3 * 2
        val params = LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
        }
        setContentView(contentView, params)
    }

    fun setMsg(msg: CharSequence?): LoadingDialog {
        if (TextUtils.isEmpty(msg)) {
            mLoadingTv.text = mContext.resources.getString(R.string.dialog_loading_tips)
        } else {
            mLoadingTv.text = msg
        }
        return this
    }
}