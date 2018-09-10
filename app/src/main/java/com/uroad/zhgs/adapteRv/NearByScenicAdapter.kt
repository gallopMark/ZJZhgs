package com.uroad.zhgs.adapteRv

import android.app.Activity
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.widget.ImageView
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.model.ScenicMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/22.
 */
class NearByScenicAdapter(private val context: Activity, mDatas: MutableList<ScenicMDL>)
    : BaseArrayRecyclerAdapter<ScenicMDL>(context, mDatas) {
    private val itemWidth = (DisplayUtils.getWindowWidth(context) * 0.6).toInt()
    private val itemHeight = (itemWidth * 0.35).toInt()
    override fun bindView(viewType: Int): Int = R.layout.item_nearby_scenic

    override fun onBindHoder(holder: RecyclerHolder, t: ScenicMDL, position: Int) {
        holder.itemView.layoutParams = holder.itemView.layoutParams.apply {
            width = itemWidth
            height = itemHeight
        }
        holder.itemView.layoutParams = (holder.itemView.layoutParams as RecyclerView.LayoutParams).apply {
            width = itemWidth
            height = itemHeight
            if (position > 0) {
                leftMargin = DisplayUtils.dip2px(context, 10f)
            } else {
                leftMargin = 0
            }
        }
        val ivPic = holder.obtainView<ImageView>(R.id.ivPic)
        ImageLoaderV4.getInstance().displayImage(context, t.picurls, ivPic, ContextCompat.getColor(context, R.color.transparent))
        holder.setText(R.id.tvName, t.name)
        holder.setText(R.id.tvDistance, "${t.distance}km")
    }
}