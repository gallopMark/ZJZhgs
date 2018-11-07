package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.zhgs.R
import com.uroad.zhgs.model.RidersDetailMDL
import com.uroad.zhgs.model.RidersMsgMDL

class RidersInTokenDialog(private val context: Activity)
    : Dialog(context, R.style.translucentDialog) {

    private var data: Any? = null
    private var onViewClickListener: OnViewClickListener? = null

    fun withData(data: Any?): RidersInTokenDialog {
        this.data = data
        return this
    }

    fun setOnViewClickListener(onViewClickListener: OnViewClickListener?): RidersInTokenDialog {
        this.onViewClickListener = onViewClickListener
        return this
    }

    override fun show() {
        super.show()
        initView()
    }

    private fun initView() {
        window?.let { window ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_riders_intoken, LinearLayout(context), false)
            val tvTitle = contentView.findViewById<TextView>(R.id.tvTitle)
            val tvMessage = contentView.findViewById<TextView>(R.id.tvMessage)
            val tvClose = contentView.findViewById<TextView>(R.id.tvClose)
            val tvJoin = contentView.findViewById<TextView>(R.id.tvJoin)
            var title = ""
            var message = "此次组队的目的地为"
            val start = message.length
            var end = start
            if (data is RidersDetailMDL.TeamData) {
                val mdl = data as RidersDetailMDL.TeamData
                mdl.teamname?.let { title += it }
                mdl.toplace?.let {
                    message += it
                    end = message.length
                }
            } else if (data is RidersMsgMDL.Content) {
                val mdl = data as RidersMsgMDL.Content
                mdl.username?.let { title += it }
                mdl.toplace?.let {
                    message += it
                    end = message.length
                }
            }
            title += "\u2000邀请你组队"
            tvTitle.text = title
            message += "\n${context.resources.getString(R.string.dialog_riders_multiInviting_title)}"
            val ss = SpannableString(message)
            ss.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.appTextColor)), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            val start1 = message.indexOf("《")
            val end1 = message.length - 1
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(view: View) {
                    onViewClickListener?.onViewClick(1, this@RidersInTokenDialog)
                }

                override fun updateDrawState(ds: TextPaint?) {
                    ds?.isUnderlineText = false
                }
            }
            ss.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorAccent)), start1, end1, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            ss.setSpan(clickableSpan, start1, end1, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            tvMessage.text = ss
            tvMessage.movementMethod = LinkMovementMethod.getInstance()
            tvClose.setOnClickListener { onViewClickListener?.onViewClick(2, this@RidersInTokenDialog) }
            tvJoin.setOnClickListener { onViewClickListener?.onViewClick(3, this@RidersInTokenDialog) }
            window.setContentView(contentView)
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER)
        }
    }

    interface OnViewClickListener {
        fun onViewClick(type: Int, dialog: RidersInTokenDialog)
    }
}