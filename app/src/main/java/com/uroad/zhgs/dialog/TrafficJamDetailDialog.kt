package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.model.TrafficJamMDL
import com.uroad.zhgs.utils.TypefaceUtils

/**
 *Created by MFB on 2018/8/22.
 */
class TrafficJamDetailDialog(private val context: Activity, private var dataMDL: TrafficJamMDL)
    : Dialog(context, R.style.transparentDialog) {

    private var onViewClickListener: OnViewClickListener? = null
    private var tvSubscribe: TextView? = null
    fun setOnViewClickListener(onViewClickListener: OnViewClickListener) {
        this.onViewClickListener = onViewClickListener
    }

    interface OnViewClickListener {
        fun onViewClick(dataMDL: TrafficJamMDL)
    }

    override fun show() {
        super.show()
        initView()
    }

    private fun initView() {
        window?.let { window ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_trafficjam_detail, LinearLayout(context), false)
            window.setContentView(contentView)
            val ivClose = contentView.findViewById<ImageView>(R.id.ivClose)
            val ivIcon = contentView.findViewById<ImageView>(R.id.ivIcon)
            val tvEventName = contentView.findViewById<TextView>(R.id.tvEventName)
            val tvTitle = contentView.findViewById<TextView>(R.id.tvTitle)
            val tvContent = contentView.findViewById<TextView>(R.id.tvContent)
            val tvOccTime = contentView.findViewById<TextView>(R.id.tvOccTime)
            val tvJamSpeed = contentView.findViewById<TextView>(R.id.tvJamSpeed)
            val tvDistance = contentView.findViewById<TextView>(R.id.tvDistance)
            val tvDuration = contentView.findViewById<TextView>(R.id.tvDuration)
            val tvSource = contentView.findViewById<TextView>(R.id.tvSource)
            tvSubscribe = contentView.findViewById(R.id.tvSubscribe)
            ivClose.setOnClickListener { dismiss() }
            ivIcon.setImageResource(R.mipmap.ic_menu_event_yd_p)
            tvEventName.text = "拥堵"
            tvTitle.text = dataMDL.roadtitle
            tvContent.text = dataMDL.content
            val typeface = TypefaceUtils.dinCondensed(context)
            tvOccTime.typeface = typeface
            tvJamSpeed.typeface = typeface
            tvDistance.typeface = typeface
            tvDuration.typeface = typeface
            if (TextUtils.isEmpty(dataMDL.getPubTime())) {
                tvOccTime.text = "--"
            } else {
                tvOccTime.text = dataMDL.getPubTime()
            }
            var jamSpeed = ""
            dataMDL.jamspeed?.let { jamSpeed += it }
            jamSpeed += "km/h"
            val ts18 = context.resources.getDimensionPixelOffset(R.dimen.font_18)
            tvJamSpeed.text = SpannableString(jamSpeed).apply { setSpan(AbsoluteSizeSpan(ts18, false), 0, jamSpeed.indexOf("k"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }
            var distance = ""
            dataMDL.jamdist?.let { distance += it }
            distance += "km"
            tvDistance.text = SpannableString(distance).apply { setSpan(AbsoluteSizeSpan(ts18, false), 0, distance.indexOf("k"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }
            tvDuration.text = dataMDL.getLongTime(ts18, false)
            tvSource.text = dataMDL.source
            updateSubscribe(dataMDL)
            window.setLayout(DisplayUtils.getWindowWidth(context), WindowManager.LayoutParams.WRAP_CONTENT)
            window.setWindowAnimations(R.style.dialog_anim)
            window.setGravity(Gravity.BOTTOM)
        }
    }

    fun updateSubscribe(mdl: TrafficJamMDL) {
        dataMDL = mdl
        if (mdl.subscribestatus == 1) {
            tvSubscribe?.text = context.resources.getString(R.string.usersubscribe_hasSubscribe)
            tvSubscribe?.isEnabled = false
        } else {
            tvSubscribe?.text = context.resources.getString(R.string.usersubscribe_subscribe)
            tvSubscribe?.isEnabled = true
            tvSubscribe?.setOnClickListener { onViewClickListener?.onViewClick(dataMDL) }
        }
    }
}