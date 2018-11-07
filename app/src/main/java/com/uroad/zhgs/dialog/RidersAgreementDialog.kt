package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R

/**
 * @author MFB
 * @create 2018/10/17
 * @describe 车友组队协议对话框
 */
class RidersAgreementDialog(private val context: Activity)
    : Dialog(context, R.style.supportDialog) {
    private var onViewClickListener: OnViewClickListener? = null
    private var message: String? = null

    fun message(message: String?): RidersAgreementDialog {
        this.message = message
        return this
    }

    fun setOnViewClickListener(onViewClickListener: OnViewClickListener): RidersAgreementDialog {
        this.onViewClickListener = onViewClickListener
        return this
    }

    override fun show() {
        super.show()
        initView()
    }

    private fun initView() {
        window?.let { window ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_riders_agreement, LinearLayout(context), false)
            val tvTitle = contentView.findViewById<TextView>(R.id.tvTitle)
            val tvMessage = contentView.findViewById<TextView>(R.id.tvMessage)
            val btDisagree = contentView.findViewById<Button>(R.id.btDisagree)
            val btAgree = contentView.findViewById<Button>(R.id.btAgree)
            tvTitle.text = context.resources.getString(R.string.dialog_riders_agreement_title)
            message?.let { tvMessage.text = Html.fromHtml(it) }
            tvMessage.movementMethod = ScrollingMovementMethod.getInstance()
            btDisagree.setOnClickListener { onViewClickListener?.onViewClick(1, this@RidersAgreementDialog) }
            btAgree.setOnClickListener { onViewClickListener?.onViewClick(2, this@RidersAgreementDialog) }
            window.setContentView(contentView)
            window.setLayout(DisplayUtils.getWindowWidth(context) / 4 * 3, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER)
        }
    }

    interface OnViewClickListener {
        fun onViewClick(type: Int, dialog: RidersAgreementDialog)
    }
}