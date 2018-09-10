package com.uroad.zhgs.adapteRv

import android.content.Context
import com.uroad.zhgs.R
import com.uroad.zhgs.model.HotLineMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/14.
 */
class HighwayHotlineAdapter(context: Context, mDatas: MutableList<HotLineMDL>) :
        BaseArrayRecyclerAdapter<HotLineMDL>(context, mDatas) {
    override fun onBindHoder(holder: RecyclerHolder, t: HotLineMDL, position: Int) {
        holder.displayImage(R.id.ivIcon, t.picurl)
        holder.setText(R.id.tvPhone, t.phone)
        holder.setText(R.id.tvPhoneName, t.phonename)
        holder.bindChildClick(R.id.ivCall)
    }

    override fun bindView(viewType: Int): Int {
        return R.layout.item_highway_hotline
    }
}