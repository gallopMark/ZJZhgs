package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.model.ServiceMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.widget.GridSpacingItemDecoration

/**
 *Created by MFB on 2018/8/23.
 */
class ServiceAreaDialog(private val context: Activity, private val dataMDL: ServiceMDL)
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
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_servicearea, LinearLayout(context), false)
            window.setContentView(contentView)
            val ivClose = contentView.findViewById<ImageView>(R.id.ivClose)
            val ivIcon = contentView.findViewById<ImageView>(R.id.ivIcon)
            val tvName = contentView.findViewById<TextView>(R.id.tvName)
            val tvDistance = contentView.findViewById<TextView>(R.id.tvDistance)
            val tvAddress = contentView.findViewById<TextView>(R.id.tvAddress)
            val recyclerView = contentView.findViewById<RecyclerView>(R.id.recyclerView)
            val tvEmptyFacilities = contentView.findViewById<TextView>(R.id.tvEmptyFacilities)
            val tvOilInfo = contentView.findViewById<TextView>(R.id.tvOilInfo)
            val tvParkInfo = contentView.findViewById<TextView>(R.id.tvParkInfo)
            val llDetail = contentView.findViewById<LinearLayout>(R.id.llDetail)
            val llNavigation = contentView.findViewById<LinearLayout>(R.id.llNavigation)
            ivClose.setOnClickListener { dismiss() }
            ivIcon.setImageResource(R.mipmap.ic_menu_jtss_service_p)
            tvName.text = dataMDL.name
            var distance = ""
            dataMDL.distance?.let { distance += it }
            distance += "km"
            tvDistance.text = distance
            tvAddress.text = dataMDL.shortname
            recyclerView.layoutManager = GridLayoutManager(context, 4).apply { orientation = GridLayoutManager.VERTICAL }
            recyclerView.addItemDecoration(GridSpacingItemDecoration(4, DisplayUtils.dip2px(context, 5f), false))
            if (dataMDL.getServiceArr().size > 0) {
                recyclerView.visibility = View.VISIBLE
                recyclerView.adapter = MAdapter(context, dataMDL.getServiceArr())
                tvEmptyFacilities.visibility = View.GONE
            } else {
                recyclerView.visibility = View.GONE
                tvEmptyFacilities.visibility = View.VISIBLE
            }
//            var text = ""
//            if (!TextUtils.isEmpty(dataMDL.oil1) && !TextUtils.equals(dataMDL.oil1, "0")) {
//                text += context.resources.getString(R.string.service_area_oil92)
//            }
//            if (!TextUtils.isEmpty(dataMDL.oil2) && !TextUtils.equals(dataMDL.oil2, "0")) {
//                text += if (!TextUtils.isEmpty(text)) "；${context.resources.getString(R.string.service_area_oil95)}"
//                else context.resources.getString(R.string.service_area_oil95)
//            }
//            if (!TextUtils.isEmpty(dataMDL.oil3) && !TextUtils.equals(dataMDL.oil3, "0")) {
//                text += if (!TextUtils.isEmpty(text)) "；${context.resources.getString(R.string.service_area_oil98)}"
//                else context.resources.getString(R.string.service_area_oil98)
//            }
//            if (!TextUtils.isEmpty(dataMDL.oil4) && !TextUtils.equals(dataMDL.oil4, "0")) {
//                text += if (!TextUtils.isEmpty(text)) "；${context.resources.getString(R.string.service_area_oil0)}"
//                else context.resources.getString(R.string.service_area_oil0)
//            }
            tvOilInfo.text = dataMDL.getOilText()
            tvParkInfo.text = dataMDL.parkstatusname
            llDetail.setOnClickListener { onButtonClickListener?.onDetail(dataMDL) }
            llNavigation.setOnClickListener { onButtonClickListener?.onNavigation(dataMDL) }
            window.setLayout(DisplayUtils.getWindowWidth(context), WindowManager.LayoutParams.WRAP_CONTENT)
            window.setWindowAnimations(R.style.dialog_anim)
            window.setGravity(Gravity.BOTTOM)
        }
    }

    private class MAdapter(context: Context, mDatas: MutableList<String>)
        : BaseArrayRecyclerAdapter<String>(context, mDatas) {
        override fun bindView(viewType: Int): Int = R.layout.item_servicearea_facilities

        override fun onBindHoder(holder: RecyclerHolder, t: String, position: Int) {
            holder.setText(R.id.tv, t)
        }
    }

    interface OnButtonClickListener {
        fun onDetail(dataMDL: ServiceMDL)
        fun onNavigation(dataMDL: ServiceMDL)
    }
}