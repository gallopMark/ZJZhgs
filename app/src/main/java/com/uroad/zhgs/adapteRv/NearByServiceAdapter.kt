package com.uroad.zhgs.adapteRv

import android.app.Activity
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.model.ServiceMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/22.
 */
class NearByServiceAdapter(private val context: Activity, mDatas: MutableList<ServiceMDL>)
    : BaseArrayRecyclerAdapter<ServiceMDL>(context, mDatas) {
    override fun bindView(viewType: Int): Int = R.layout.item_nearby_service

    override fun onBindHoder(holder: RecyclerHolder, t: ServiceMDL, position: Int) {
        val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
        if (position > 0) {
            params.leftMargin = DisplayUtils.dip2px(context, 10f)
        } else {
            params.leftMargin = 0
        }
        holder.itemView.layoutParams = params
        if (!TextUtils.isEmpty(t.closestatus)) {
            holder.setVisibility(R.id.tvStatus, View.VISIBLE)
            holder.setText(R.id.tvStatus, t.closestatus)
        } else {
            if (!TextUtils.isEmpty(t.oilstatusnews)) {
                holder.setVisibility(R.id.tvStatus, View.VISIBLE)
                holder.setText(R.id.tvStatus, t.oilstatusnews)
            } else {
                holder.setVisibility(R.id.tvStatus, View.GONE)
            }
        }
        holder.displayImage(R.id.ivPic, t.picurl, R.color.color_f7)
        holder.setText(R.id.tvName, t.name)
        holder.setText(R.id.tvDistance, "${t.distance}km")
    }
}