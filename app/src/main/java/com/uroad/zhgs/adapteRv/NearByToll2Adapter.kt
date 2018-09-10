package com.uroad.zhgs.adapteRv

import android.content.Context
import com.uroad.zhgs.R
import com.uroad.zhgs.model.TollGateMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/22.
 */
class NearByToll2Adapter(context: Context, mDatas: MutableList<TollGateMDL>)
    : BaseArrayRecyclerAdapter<TollGateMDL>(context, mDatas) {
    override fun bindView(viewType: Int): Int = R.layout.item_nearby_toll2

    override fun onBindHoder(holder: RecyclerHolder, t: TollGateMDL, position: Int) {
        holder.setText(R.id.tvName, t.name)
        holder.setText(R.id.tvDistance, "${t.distance}km")
        holder.setText(R.id.tvAddress, t.shortname)
        if (t.poistatus == 1) {
            holder.setText(R.id.tvStatus, "正常")
            holder.setBackgroundResource(R.id.tvStatus, R.drawable.bg_status_normal_corners)
        } else {
            holder.setText(R.id.tvStatus, "关闭")
            holder.setBackgroundResource(R.id.tvStatus, R.drawable.bg_status_close_corners)
        }
        holder.bindChildClick(R.id.ivNav)
    }
}