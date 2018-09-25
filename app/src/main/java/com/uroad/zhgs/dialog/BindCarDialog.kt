package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R

/**
 * @author MFB
 * @create 2018/9/21
 * @describe 用户暂未绑定车辆提示框
 */
class BindCarDialog(private val context: Activity) : Dialog(context) {
    private var onConfirmClickListener: OnConfirmClickListener? = null

    fun setOnConfirmClickListener(onConfirmClickListener: OnConfirmClickListener): BindCarDialog {
        this.onConfirmClickListener = onConfirmClickListener
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCanceledOnTouchOutside(false)
        initView()
    }

    private fun initView() {
        window?.let { window ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_bindcar, LinearLayout(context), false)
            window.setContentView(contentView)
            val btConfirm = contentView.findViewById<Button>(R.id.btConfirm)
            val btCancel = contentView.findViewById<Button>(R.id.btCancel)
            btConfirm.setOnClickListener { onConfirmClickListener?.onConfirm(this@BindCarDialog) }
            btCancel.setOnClickListener { dismiss() }
            window.setBackgroundDrawableResource(R.drawable.bg_corners_white_10dp)
            window.setLayout((DisplayUtils.getWindowWidth(context) * 0.75).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER)
        }
    }

    interface OnConfirmClickListener {
        fun onConfirm(dialog: BindCarDialog)
    }
}