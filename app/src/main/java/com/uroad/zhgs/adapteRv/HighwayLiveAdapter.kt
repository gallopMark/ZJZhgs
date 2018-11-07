package com.uroad.zhgs.adapteRv

import android.app.Activity
import android.widget.LinearLayout
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.model.SnapShotMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 * @author MFB
 * @create 2018/10/8
 * @describe 高速直播列表适配器
 */
class HighwayLiveAdapter(context: Activity, mDatas: MutableList<SnapShotMDL>)
    : BaseArrayRecyclerAdapter<SnapShotMDL>(context, mDatas) {
    private val params1: LinearLayout.LayoutParams
    private val params2: LinearLayout.LayoutParams

    init {
        val width1 = DisplayUtils.getWindowWidth(context) - DisplayUtils.dip2px(context, 12f) * 2
        params1 = LinearLayout.LayoutParams(width1, width1 / 2)
        val width2 = DisplayUtils.getWindowWidth(context) - DisplayUtils.dip2px(context, 12f) * 3
        params2 = LinearLayout.LayoutParams(width2 / 2, width2 / 2)
    }

    override fun bindView(viewType: Int): Int = R.layout.item_highway_live

    override fun onBindHoder(holder: RecyclerHolder, t: SnapShotMDL, position: Int) {
        if (position == 0) {
            holder.setLayoutParams(R.id.flIv, params1)
        } else {
            holder.setLayoutParams(R.id.flIv, params2)
        }
        holder.displayImage(R.id.ivPic, t.picurl)
        holder.setText(R.id.tvShortName, t.shortname)
        holder.setText(R.id.tvContent, t.resname)
    }
}