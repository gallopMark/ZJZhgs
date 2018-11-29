package com.uroad.zhgs.dialog

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

import com.uroad.zhgs.R


/**
 * 创建日期：2017/4/6 on 17:24
 * 描述:统一风格的dialog
 * 作者:马飞奔 Administrator
 */
class MaterialDialog(mContext: Context) : AlertDialog(mContext, R.style.materialDialog) {
    private val view: View = LayoutInflater.from(mContext).inflate(R.layout.dialog_material, LinearLayout(mContext), false)
    private val tvTips: TextView // 提示框标题
    private val tvMessage: TextView // 提示内容
    private val btMakeSure: Button // 确定按钮
    private val tvDivider: TextView
    private val btCancel: Button // 取消按钮

    init {
        tvTips = view.findViewById(R.id.tv_tips)
        tvMessage = view.findViewById(R.id.tv_message)
        btMakeSure = view.findViewById(R.id.bt_makesure)
        tvDivider = view.findViewById(R.id.tvDivider)
        btCancel = view.findViewById(R.id.bt_cancel)
        tvMessage.movementMethod = ScrollingMovementMethod.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view)
    }

    override fun setTitle(title: CharSequence?) {
        if (TextUtils.isEmpty(title)) {
            tvTips.visibility = View.GONE
        } else {
            tvTips.text = title
            tvTips.visibility = View.VISIBLE
        }
    }

    override fun setMessage(message: CharSequence?) {
        tvMessage.text = message
    }

    fun hideDivider() {
        tvDivider.visibility = View.GONE
    }

    /* 确定按钮 */
    fun setPositiveButton(text: CharSequence?, listener: ButtonClickListener?) {
        if (!TextUtils.isEmpty(text)) {
            btMakeSure.text = text
            btMakeSure.visibility = View.VISIBLE
            btMakeSure.setOnClickListener {
                if (listener != null) listener.onClick(btMakeSure, this@MaterialDialog)
                else dismiss()
            }
        } else {
            btMakeSure.visibility = View.GONE
        }
    }

    /* 取消按钮 */
    fun setNegativeButton(text: CharSequence?, listener: ButtonClickListener?) {
        if (!TextUtils.isEmpty(text)) {
            btCancel.text = text
            btCancel.visibility = View.VISIBLE
            btCancel.setOnClickListener {
                if (listener != null) listener.onClick(btCancel, this@MaterialDialog)
                else dismiss()
            }
        } else {
            btCancel.visibility = View.GONE
        }
    }

    fun setNegativeTextColor(color: Int) {
        btCancel.setTextColor(color)
    }

    fun setPositiveTextColor(color: Int) {
        btMakeSure.setTextColor(color)
    }

    interface ButtonClickListener {
        fun onClick(v: View, dialog: AlertDialog)
    }
}
