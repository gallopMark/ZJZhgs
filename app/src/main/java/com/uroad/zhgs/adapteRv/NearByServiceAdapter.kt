package com.uroad.zhgs.adapteRv

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.widget.ImageView
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.model.ServiceMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/22.
 */
class NearByServiceAdapter(private val context: Context, mDatas: MutableList<ServiceMDL>)
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
        val ivPic = holder.obtainView<ImageView>(R.id.ivPic)
        ImageLoaderV4.getInstance().displayImage(context, t.picurl, ivPic, ContextCompat.getColor(context, R.color.transparent))
        holder.setText(R.id.tvName, t.name)
        holder.setText(R.id.tvDistance, "${t.distance}km")
    }
}