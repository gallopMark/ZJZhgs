package com.uroad.zhgs.adapteRv

import android.content.Context
import com.uroad.zhgs.R
import com.uroad.zhgs.model.ServiceAreaMDL
import com.uroad.zhgs.model.ServiceMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/21.
 */
class ServiceAdapter(context: Context, mDatas: MutableList<ServiceMDL>)
    : BaseArrayRecyclerAdapter<ServiceMDL>(context, mDatas) {
    override fun bindView(viewType: Int): Int = R.layout.item_servicearea_child

    override fun onBindHoder(holder: RecyclerHolder, t: ServiceMDL, position: Int) {
        holder.setText(R.id.tvName, t.name)
    }
}