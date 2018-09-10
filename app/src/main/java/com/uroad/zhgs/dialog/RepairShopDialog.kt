package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.model.RepairShopMDL

/**
 *Created by MFB on 2018/8/23.
 */
class RepairShopDialog(private val context: Activity, private val dataMDL: RepairShopMDL)
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
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_repairshop, LinearLayout(context), false)
            window.setContentView(contentView)
            val ivClose = contentView.findViewById<ImageView>(R.id.ivClose)
            val ivIcon = contentView.findViewById<ImageView>(R.id.ivIcon)
            val tvName = contentView.findViewById<TextView>(R.id.tvName)
            val tvDistance = contentView.findViewById<TextView>(R.id.tvDistance)
            val tvAddress = contentView.findViewById<TextView>(R.id.tvAddress)
            val llDetail = contentView.findViewById<LinearLayout>(R.id.llDetail)
            val llNavigation = contentView.findViewById<LinearLayout>(R.id.llNavigation)
            ivClose.setOnClickListener { dismiss() }
            ivIcon.setImageResource(R.mipmap.ic_menu_jtss_repair_p)
            tvName.text = dataMDL.name
            var distance = ""
            dataMDL.distance?.let { distance += it }
            distance += "km"
            tvDistance.text = distance
            tvAddress.text = dataMDL.address
            llDetail.setOnClickListener { onButtonClickListener?.onDetail(dataMDL) }
            llNavigation.setOnClickListener { onButtonClickListener?.onNavigation(dataMDL) }
            window.setLayout(DisplayUtils.getWindowWidth(context), WindowManager.LayoutParams.WRAP_CONTENT)
            window.setWindowAnimations(R.style.dialog_anim)
            window.setGravity(Gravity.BOTTOM)
        }
    }

    interface OnButtonClickListener {
        fun onDetail(dataMDL: RepairShopMDL)
        fun onNavigation(dataMDL: RepairShopMDL)
    }
}