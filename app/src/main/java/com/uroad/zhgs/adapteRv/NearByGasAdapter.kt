package com.uroad.zhgs.adapteRv

import android.content.Context
import com.uroad.zhgs.R
import com.uroad.zhgs.model.GasStationMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/23.
 * 我的附近加油站列表适配器
 */
class NearByGasAdapter(context: Context, mDatas: MutableList<GasStationMDL>)
    : BaseArrayRecyclerAdapter<GasStationMDL>(context, mDatas) {
    override fun bindView(viewType: Int): Int = R.layout.item_nearby_gas

    override fun onBindHoder(holder: RecyclerHolder, t: GasStationMDL, position: Int) {
        holder.setText(R.id.tvName, t.name)
        holder.setText(R.id.tvDistance, "${t.getDistance()}km")
        holder.setText(R.id.tvAddress, t.address)
        holder.bindChildClick(R.id.ivNav)
    }
}