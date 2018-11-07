package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.zhgs.R

/**
 * @author MFB
 * @create 2018/10/19
 * @describe 邀请好友弹窗
 */
class RidersInvitingDialog(private val context: Activity)
    : Dialog(context, R.style.translucentDialog) {

    private var token: CharSequence? = null
    private var onViewClickListener: OnViewClickListener? = null

    fun token(token: CharSequence?): RidersInvitingDialog {
        this.token = token
        return this
    }

    fun viewClickListener(onViewClickListener: OnViewClickListener?): RidersInvitingDialog {
        this.onViewClickListener = onViewClickListener
        return this
    }

    override fun show() {
        super.show()
        initView()
    }

    private fun initView() {
        window?.let { window ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_riders_inviting, LinearLayout(context), false)
            val tvJoinToken = contentView.findViewById<TextView>(R.id.tvJoinToken)
            val tvInToken = contentView.findViewById<TextView>(R.id.tvInToken)
            val tvInFriends = contentView.findViewById<TextView>(R.id.tvInFriends)
            val ivClose = contentView.findViewById<ImageView>(R.id.ivClose)
            var mToken = "${context.resources.getString(R.string.riders_inToken)}："
            token?.let { mToken += it }
            tvJoinToken.text = SpannableString(mToken).apply { setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.appTextColor)), mToken.indexOf("："), mToken.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE) }
            tvInToken.setOnClickListener { onViewClickListener?.onViewClick(1, this@RidersInvitingDialog) }
            tvInFriends.setOnClickListener { onViewClickListener?.onViewClick(2, this@RidersInvitingDialog) }
            ivClose.setOnClickListener { dismiss() }
            window.setContentView(contentView)
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER)
        }
    }

    interface OnViewClickListener {
        fun onViewClick(type: Int, dialogRiders: RidersInvitingDialog)
    }
}