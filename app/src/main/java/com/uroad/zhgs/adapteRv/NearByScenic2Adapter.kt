package com.uroad.zhgs.adapteRv

import android.app.Activity
import com.uroad.zhgs.R
import com.uroad.zhgs.model.ScenicMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/22.
 */
class NearByScenic2Adapter(context: Activity, mDatas: MutableList<ScenicMDL>)
    : BaseArrayRecyclerAdapter<ScenicMDL>(context, mDatas) {
    override fun bindView(viewType: Int): Int = R.layout.item_nearby_scenic2

    override fun onBindHoder(holder: RecyclerHolder, t: ScenicMDL, position: Int) {
        holder.setText(R.id.tvName, t.name)
        holder.setText(R.id.tvDistance, "${t.distance}km")
        holder.setText(R.id.tvAddress, t.address)
        holder.bindChildClick(R.id.ivNav)
    }
}