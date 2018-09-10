package com.uroad.zhgs.adapteRv

import android.content.Context
import com.uroad.zhgs.R
import com.uroad.zhgs.model.ServiceMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/22.
 */
class NearByService2Adapter(context: Context, mDatas: MutableList<ServiceMDL>)
    : BaseArrayRecyclerAdapter<ServiceMDL>(context, mDatas) {
    override fun bindView(viewType: Int): Int = R.layout.item_nearby_service2

    override fun onBindHoder(holder: RecyclerHolder, t: ServiceMDL, position: Int) {
        holder.setText(R.id.tvName, t.name)
        holder.setText(R.id.tvDistance, "${t.distance}km")
        holder.setText(R.id.tvShortName, t.shortname)
        holder.bindChildClick(R.id.ivNav)
    }
}