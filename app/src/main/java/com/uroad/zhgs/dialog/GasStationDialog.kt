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
import com.amap.api.col.sln3.it
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.model.GasStationMDL

/**
 *Created by MFB on 2018/8/23.
 */
class GasStationDialog(private val context: Activity, private val dataMDL: GasStationMDL)
    : Dialog(context, R.style.transparentDialog) {

    private var onNavigationListener: OnNavigationListener? = null
    fun setOnNavigationListener(onNavigationListener: OnNavigationListener) {
        this.onNavigationListener = onNavigationListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            val llNavigation = contentView.findViewById<LinearLayout>(R.id.llNavigation)
            ivClose.setOnClickListener { dismiss() }
            ivIcon.setImageResource(R.mipmap.ic_menu_jtss_gas_p)
            tvName.text = dataMDL.name
            val distance = "${dataMDL.getDistance()}km"
            tvDistance.text = distance
            tvAddress.text = dataMDL.address
            llNavigation.setOnClickListener { onNavigationListener?.onNavigation(dataMDL) }
            window.setLayout(DisplayUtils.getWindowWidth(context), WindowManager.LayoutParams.WRAP_CONTENT)
            window.setWindowAnimations(R.style.dialog_anim)
            window.setGravity(Gravity.BOTTOM)
        }
    }

    interface OnNavigationListener {
        fun onNavigation(dataMDL: GasStationMDL)
    }
}