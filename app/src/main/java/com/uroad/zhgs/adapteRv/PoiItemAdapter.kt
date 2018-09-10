package com.uroad.zhgs.adapteRv

import android.content.Context
import com.amap.api.services.core.PoiItem
import com.uroad.zhgs.R
import com.uroad.zhgs.model.PoiItemMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/27.
 */
class PoiItemAdapter(context: Context, mDatas: MutableList<PoiItemMDL>)
    : BaseArrayRecyclerAdapter<PoiItemMDL>(context, mDatas) {
    override fun bindView(viewType: Int): Int = R.layout.item_poiitem

    override fun onBindHoder(holder: RecyclerHolder, t: PoiItemMDL, position: Int) {
        holder.setText(R.id.tvName, t.title)
        holder.setText(R.id.tvAddress, t.snippet)
        holder.setText(R.id.tvDistance, "${t.distance()}km")
    }
}