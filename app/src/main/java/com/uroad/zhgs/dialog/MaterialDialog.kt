package com.uroad.zhgs.dialog

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView

import com.uroad.library.utils.ScreenUtils
import com.uroad.zhgs.R


/**
 * 创建日期：2017/4/6 on 17:24
 * 描述:统一风格的dialog
 * 作者:马飞奔 Administrator
 */
class MaterialDialog(mContext: Context) : AlertDialog(mContext, R.style.baseCustomDialog_margin30) {
    private val view: View = LayoutInflater.from(mContext).inflate(R.layout.dialog_material, null)
    private val tv_tips: TextView // 提示框标题
    private val tv_message: TextView // 提示内容
    private val bt_makesure: Button // 确定按钮
    private val bt_cancel: Button // 取消按钮

    init {
        tv_tips = view.findViewById(R.id.tv_tips)
        tv_message = view.findViewById(R.id.tv_message)
        bt_makesure = view.findViewById(R.id.bt_makesure)
        bt_cancel = view.findViewById(R.id.bt_cancel)
        val maxHeight = (ScreenUtils.getScreenHeight(mContext) * 0.6).toInt()
        tv_message.maxHeight = maxHeight
        tv_message.movementMethod = ScrollingMovementMethod.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view)
    }

    override fun setTitle(title: CharSequence?) {
        if (TextUtils.isEmpty(title)) {
            tv_tips.visibility = View.GONE
        } else {
            tv_tips.text = title
            tv_tips.visibility = View.VISIBLE
        }
    }

    override fun setMessage(message: CharSequence) {
        tv_message.text = message
    }

    /* 确定按钮 */
    fun setPositiveButton(text: CharSequence, listener: ButtonClickListener?) {
        if (!TextUtils.isEmpty(text)) {
            bt_makesure.text = text
            bt_makesure.visibility = View.VISIBLE
            bt_makesure.setOnClickListener {
                if (listener != null) listener.onClick(bt_makesure, this@MaterialDialog)
                else dismiss()
            }
        } else {
            bt_makesure.visibility = View.GONE
        }
    }

    /* 取消按钮 */
    fun setNegativeButton(text: CharSequence, listener: ButtonClickListener?) {
        if (!TextUtils.isEmpty(text)) {
            bt_cancel.text = text
            bt_cancel.visibility = View.VISIBLE
            bt_cancel.setOnClickListener {
                if (listener != null) listener.onClick(bt_cancel, this@MaterialDialog)
                else dismiss()
            }
        } else {
            bt_cancel.visibility = View.GONE
        }
    }

    fun setNegativeTextColor(color: Int) {
        bt_cancel.setTextColor(color)
    }

    fun setPositiveTextColor(color: Int) {
        bt_makesure.setTextColor(color)
    }

    interface ButtonClickListener {
        fun onClick(v: View, dialog: AlertDialog)
    }
}
