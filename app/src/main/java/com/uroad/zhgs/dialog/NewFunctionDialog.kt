package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import com.uroad.library.utils.BitmapUtils
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.helper.AppLocalHelper

/**
 * @author MFB
 * @create 2018/9/21
 * @describe 第一次进入路况导航显示
 */
class NewFunctionDialog(private val context: Activity)
    : Dialog(context, R.style.translucentDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCanceledOnTouchOutside(false)
        setOnDismissListener { AppLocalHelper.saveNav(context, false) }
    }

    override fun show() {
        super.show()
        initView()
    }

    private fun initView() {
        window?.let { window ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_newfunctions, LinearLayout(context), false)
            window.setContentView(contentView)
            val ivPic = contentView.findViewById<ImageView>(R.id.ivPic)
            val btIKnow = contentView.findViewById<Button>(R.id.btIKnow)
            ivPic.setImageResource(R.mipmap.ic_new_functions_bg)
            btIKnow.setOnClickListener { dismiss() }
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER)
        }
    }
}