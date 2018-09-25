package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.enumeration.MapDataType
import com.uroad.zhgs.model.EventMDL
import com.uroad.zhgs.utils.TypefaceUtils

/**
 *Created by MFB on 2018/8/15.
 */
class EventDetailDialog(private val context: Activity, private val dataMDL: EventMDL) : Dialog(context, R.style.transparentDialog) {

    private var onSubscribeListener: OnSubscribeListener? = null

    fun setOnSubscribeListener(onSubscribeListener: OnSubscribeListener) {
        this.onSubscribeListener = onSubscribeListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        window?.let { window ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_event_detail, LinearLayout(context), false)
            window.setContentView(contentView)
            val ivClose = contentView.findViewById<ImageView>(R.id.ivClose)
            val ivIcon = contentView.findViewById<ImageView>(R.id.ivIcon)
            val tvEventName = contentView.findViewById<TextView>(R.id.tvEventName)
            val tvTitle = contentView.findViewById<TextView>(R.id.tvTitle)
            val tvContent = contentView.findViewById<TextView>(R.id.tvContent)
            val tvOccTime = contentView.findViewById<TextView>(R.id.tvOccTime)
            val tvEndTime = contentView.findViewById<TextView>(R.id.tvEndTime)
            val tvEndTimeTips = contentView.findViewById<TextView>(R.id.tvEndTimeTips)
            val tvUpdateTime = contentView.findViewById<TextView>(R.id.tvUpdateTime)
            val tvSubscribe = contentView.findViewById<TextView>(R.id.tvSubscribe)
            ivClose.setOnClickListener { dismiss() }
            ivIcon.setImageResource(dataMDL.getIcon())
            tvEventName.text = dataMDL.eventtypename
            tvTitle.text = dataMDL.roadtitle
            tvContent.text = dataMDL.reportout
            val typeface = TypefaceUtils.dinCondensed(context)
            tvOccTime.typeface = typeface
            tvUpdateTime.typeface = typeface
            tvEndTime.typeface = typeface
            if (TextUtils.isEmpty(dataMDL.getOccTime())) {
                tvOccTime.text = "--"
            } else {
                tvOccTime.text = dataMDL.getOccTime()
            }
            tvEndTimeTips.text = if (dataMDL.eventtype == MapDataType.CONSTRUCTION.code)
                context.resources.getString(R.string.usersubscribe_planEndTime)
            else context.resources.getString(R.string.usersubscribe_endTime)
            tvEndTime.text = dataMDL.getRealoverTime()
            if (TextUtils.isEmpty(dataMDL.getUpdateTime())) {
                tvUpdateTime.text = "--"
            } else {
                tvUpdateTime.text = dataMDL.getUpdateTime()
            }
            if (dataMDL.subscribestatus == 1) {
                tvSubscribe.text = context.resources.getString(R.string.usersubscribe_hasSubscribe)
                tvSubscribe.isEnabled = false
            } else {
                tvSubscribe.text = context.resources.getString(R.string.usersubscribe_subscribe)
                tvSubscribe.isEnabled = true
                tvSubscribe.setOnClickListener { onSubscribeListener?.onSubscribe(dataMDL) }
            }
            window.setLayout(DisplayUtils.getWindowWidth(context), WindowManager.LayoutParams.WRAP_CONTENT)
            window.setWindowAnimations(R.style.dialog_anim)
            window.setGravity(Gravity.BOTTOM)
        }
    }

    interface OnSubscribeListener {
        fun onSubscribe(dataMDL: EventMDL)
    }
}