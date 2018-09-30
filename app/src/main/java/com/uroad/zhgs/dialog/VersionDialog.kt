package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.model.VersionMDL

/**
 * @author MFB
 * @create 2018/9/21
 * @describe 版本更新对话框
 */
class VersionDialog(private val context: Activity, private val mdl: VersionMDL)
    : Dialog(context, R.style.translucentDialog) {

    private var onConfirmClickListener: OnConfirmClickListener? = null

    fun setOnConfirmClickListener(onConfirmClickListener: OnConfirmClickListener): VersionDialog {
        this.onConfirmClickListener = onConfirmClickListener
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCanceledOnTouchOutside(false)
    }

    override fun show() {
        super.show()
        initView()
    }

    private fun initView() {
        window?.let { window ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_version, LinearLayout(context), false)
            window.setContentView(contentView)
            val tvContent = contentView.findViewById<TextView>(R.id.tvContent)
            val btCancel = contentView.findViewById<Button>(R.id.btCancel)
            val btConfirm = contentView.findViewById<Button>(R.id.btConfirm)
            if (TextUtils.isEmpty(mdl.content)) {
                tvContent.text = context.getString(R.string.version_update_defContent)
            } else {
                tvContent.text = mdl.content
            }
            if (mdl.isforce == 1) {  //强制更新   隐藏取消 按钮
                btCancel.visibility = View.GONE
                setCancelable(false)
            } else {
                btCancel.visibility = View.VISIBLE
                setCancelable(true)
            }
            btCancel.setOnClickListener { dismiss() }
            btConfirm.setOnClickListener { onConfirmClickListener?.onConfirm(mdl, this@VersionDialog) }
            window.setLayout((DisplayUtils.getWindowWidth(context) * 0.75).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER)
        }
    }

    interface OnConfirmClickListener {
        fun onConfirm(mdl: VersionMDL, dialog: VersionDialog)
    }
}