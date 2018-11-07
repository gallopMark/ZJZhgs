package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.zhgs.R

/**
 * @author MFB
 * @create 2018/11/5
 * @describe 分享对话框
 */
class ShareDialog(private val context: Activity) : Dialog(context, R.style.supportDialog) {

    private var onShareListener: OnShareListener? = null

    fun shareListener(onShareListener: OnShareListener?): ShareDialog {
        this.onShareListener = onShareListener
        return this
    }

    override fun show() {
        super.show()
        initView()
    }

    private fun initView() {
        window?.let { window ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_share, LinearLayout(context), false)
            val tvWeChatMsg = contentView.findViewById<TextView>(R.id.tvWeChatMsg)
            val tvWeChatFriends = contentView.findViewById<TextView>(R.id.tvWeChatFriends)
            val tvQQ = contentView.findViewById<TextView>(R.id.tvQQ)
            val tvCancel = contentView.findViewById<TextView>(R.id.tvCancel)
            tvWeChatMsg.setOnClickListener { onShareListener?.share(1, this@ShareDialog) }
            tvWeChatFriends.setOnClickListener { onShareListener?.share(2, this@ShareDialog) }
            tvQQ.setOnClickListener { onShareListener?.share(3, this@ShareDialog) }
            tvCancel.setOnClickListener { dismiss() }
            window.setContentView(contentView)
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setWindowAnimations(R.style.dialog_anim)
            window.setGravity(Gravity.BOTTOM)
        }
    }

    interface OnShareListener {
        fun share(type: Int, dialog: ShareDialog)
    }
}