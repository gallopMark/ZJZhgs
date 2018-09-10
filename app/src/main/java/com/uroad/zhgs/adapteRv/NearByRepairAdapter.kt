package com.uroad.zhgs.adapteRv

import android.content.Context
import com.uroad.zhgs.R
import com.uroad.zhgs.model.RepairShopMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/23.
 * 我的附近加油站列表适配器
 */
class NearByRepairAdapter(context: Context, mDatas: MutableList<RepairShopMDL>)
    : BaseArrayRecyclerAdapter<RepairShopMDL>(context, mDatas) {
    override fun bindView(viewType: Int): Int = R.layout.item_nearby_repair

    override fun onBindHoder(holder: RecyclerHolder, t: RepairShopMDL, position: Int) {
        holder.setText(R.id.tvName, t.name)
        holder.setText(R.id.tvDistance, "${t.distance}km")
        holder.setText(R.id.tvAddress, t.address)
        holder.bindChildClick(R.id.ivNav)
    }
}