package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.zhgs.R

/**
 * @author MFB
 * @create 2018/9/21
 * @describe 第一次进入路况导航显示
 */
@Suppress("DEPRECATION")
class NewFunctionDialog(private val context: Activity,
                        private val message: String?)
    : Dialog(context, R.style.translucentDialog) {

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
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_newfunctions, LinearLayout(context), false)
            window.setContentView(contentView)
            val ivPic = contentView.findViewById<ImageView>(R.id.ivPic)
            val tvMessage = contentView.findViewById<TextView>(R.id.tvMessage)
            val tvIKnow = contentView.findViewById<TextView>(R.id.tvIKnow)
            ivPic.setImageResource(R.mipmap.ic_new_functions_bg)
            message?.let {
                tvMessage.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY)
                else
                    Html.fromHtml(it)
            }
            tvIKnow.setOnClickListener { dismiss() }
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER)
        }
    }
}