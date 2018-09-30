package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.model.TollGateMDL

/**
 *Created by MFB on 2018/8/23.
 */
class TollGateDialog(private val context: Activity, private val dataMDL: TollGateMDL)
    : Dialog(context, R.style.transparentDialog) {
    private var onButtonClickListener: OnButtonClickListener? = null

    fun setOnButtonClickListener(onButtonClickListener: OnButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener
    }

    override fun show() {
        super.show()
        initView()
    }

    private fun initView() {
        window?.let { window ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_tollgate, LinearLayout(context), false)
            window.setContentView(contentView)
            val ivClose = contentView.findViewById<ImageView>(R.id.ivClose)
            val ivIcon = contentView.findViewById<ImageView>(R.id.ivIcon)
            val tvName = contentView.findViewById<TextView>(R.id.tvName)
            val tvStatus = contentView.findViewById<TextView>(R.id.tvStatus)
            val tvDistance = contentView.findViewById<TextView>(R.id.tvDistance)
            val tvAddress = contentView.findViewById<TextView>(R.id.tvAddress)
            val llDetail = contentView.findViewById<LinearLayout>(R.id.llDetail)
            val llNavigation = contentView.findViewById<LinearLayout>(R.id.llNavigation)
            ivClose.setOnClickListener { dismiss() }
            ivIcon.setImageResource(R.mipmap.ic_menu_jtss_toll_p)
            if (dataMDL.poistatus == 1) {
                tvStatus.text = "正常"
                tvStatus.setBackgroundResource(R.drawable.bg_status_normal_corners)
            } else {
                tvStatus.text = "关闭"
                tvStatus.setBackgroundResource(R.drawable.bg_status_close_corners)
            }
            tvName.text = dataMDL.name
            var distance = ""
            dataMDL.distance?.let { distance += it }
            distance += "km"
            tvDistance.text = distance
            tvAddress.text = dataMDL.shortname
            llDetail.setOnClickListener { onButtonClickListener?.onDetail(dataMDL) }
            llNavigation.setOnClickListener { onButtonClickListener?.onNavigation(dataMDL) }
            window.setLayout(DisplayUtils.getWindowWidth(context), WindowManager.LayoutParams.WRAP_CONTENT)
            window.setWindowAnimations(R.style.dialog_anim)
            window.setGravity(Gravity.BOTTOM)
        }
    }

    interface OnButtonClickListener {
        fun onDetail(dataMDL: TollGateMDL)
        fun onNavigation(dataMDL: TollGateMDL)
    }
}