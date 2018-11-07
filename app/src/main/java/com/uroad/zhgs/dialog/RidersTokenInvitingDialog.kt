package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.zhgs.R

/**
 * @author MFB
 * @create 2018/10/22
 * @describe 口令邀请好友对话框
 */
class RidersTokenInvitingDialog(private val context: Activity)
    : Dialog(context, R.style.translucentDialog) {
    private var inToken: CharSequence? = null
    private var onViewClickListener: OnViewClickListener? = null
    fun token(inToken: CharSequence?): RidersTokenInvitingDialog {
        this.inToken = inToken
        return this
    }

    fun onViewClickListener(onViewClickListener: OnViewClickListener): RidersTokenInvitingDialog {
        this.onViewClickListener = onViewClickListener
        return this
    }

    override fun show() {
        super.show()
        initView()
    }

    private fun initView() {
        window?.let { window ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_riders_tokeninviting, LinearLayout(context), false)
            val tvTitle = contentView.findViewById<TextView>(R.id.tvTitle)
            val tvMessage = contentView.findViewById<TextView>(R.id.tvMessage)
            val tvShare = contentView.findViewById<TextView>(R.id.tvShare)
            val tvShareToWeChat = contentView.findViewById<TextView>(R.id.tvShareToWeChat)
            val tvShareToQQ = contentView.findViewById<TextView>(R.id.tvShareToQQ)
            val ivClose = contentView.findViewById<ImageView>(R.id.ivClose)
            tvTitle.text = "口令已复制"
            tvMessage.text = inToken
            val share = "—— 分享至 ——"
            tvShare.text = share
            tvShareToWeChat.setOnClickListener { onViewClickListener?.onViewClick(1, this@RidersTokenInvitingDialog) }
            tvShareToQQ.setOnClickListener { onViewClickListener?.onViewClick(2, this@RidersTokenInvitingDialog) }
            ivClose.setOnClickListener { dismiss() }
            window.setContentView(contentView)
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER)
        }
    }

    interface OnViewClickListener {
        fun onViewClick(type: Int, dialog: RidersTokenInvitingDialog)
    }
}