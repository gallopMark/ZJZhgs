package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.support.v4.content.ContextCompat
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
class EventDetailDialog(private val context: Activity, private var dataMDL: EventMDL) : Dialog(context, R.style.transparentDialog) {

    private var onViewClickListener: OnViewClickListener? = null
    private var tvUseful: TextView? = null
    private var tvUseless: TextView? = null
    private var tvSubscribe: TextView? = null

    fun setOnViewClickListener(onViewClickListener: OnViewClickListener) {
        this.onViewClickListener = onViewClickListener
    }

    override fun show() {
        super.show()
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
            tvUseful = contentView.findViewById(R.id.tvUseful)
            tvUseless = contentView.findViewById(R.id.tvUseless)
            tvSubscribe = contentView.findViewById(R.id.tvSubscribe)
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
            if (dataMDL.eventtype == MapDataType.CONSTRUCTION.code) {
                tvEndTimeTips.text = context.resources.getString(R.string.usersubscribe_planEndTime)
            } else {
                tvEndTimeTips.text = context.resources.getString(R.string.usersubscribe_endTime)
            }
            tvEndTime.text = dataMDL.getPlanOverTime()
            if (TextUtils.isEmpty(dataMDL.getUpdateTime())) {
                tvUpdateTime.text = "--"
            } else {
                tvUpdateTime.text = dataMDL.getUpdateTime()
            }
            updateMDL(dataMDL)
            updateSubscribe(dataMDL)
            window.setLayout(DisplayUtils.getWindowWidth(context), WindowManager.LayoutParams.WRAP_CONTENT)
            window.setWindowAnimations(R.style.dialog_anim)
            window.setGravity(Gravity.BOTTOM)
        }
    }

    fun updateMDL(mdl: EventMDL) {
        dataMDL = mdl
        if (mdl.isuseful == 1) {
            tvUseful?.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.mipmap.ic_useful_pressed), null, null, null)
        } else {
            tvUseful?.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.mipmap.ic_useful_default), null, null, null)
        }
        if (mdl.isuseful == 2) {
            tvUseless?.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.mipmap.ic_useless_pressed), null, null, null)
        } else {
            tvUseless?.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.mipmap.ic_useless_default), null, null, null)
        }
        if (mdl.isuseful == 1 || mdl.isuseful == 2) {
            tvUseful?.isEnabled = false
            tvUseless?.isEnabled = false
        } else {
            tvUseful?.setOnClickListener { onViewClickListener?.onViewClick(mdl, 1) }
            tvUseless?.setOnClickListener { onViewClickListener?.onViewClick(mdl, 2) }
        }
    }

    fun updateSubscribe(mdl: EventMDL) {
        dataMDL = mdl
        if (mdl.subscribestatus == 1) {
            tvSubscribe?.text = context.resources.getString(R.string.usersubscribe_hasSubscribe)
            tvSubscribe?.isEnabled = false
        } else {
            tvSubscribe?.text = context.resources.getString(R.string.usersubscribe_subscribe)
            tvSubscribe?.isEnabled = true
            tvSubscribe?.setOnClickListener { onViewClickListener?.onViewClick(dataMDL, 3) }
        }
    }

    interface OnViewClickListener {
        fun onViewClick(dataMDL: EventMDL, type: Int)
    }
}