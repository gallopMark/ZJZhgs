package com.uroad.zhgs.adapteRv

import android.content.Context
import android.support.v4.content.ContextCompat
import com.uroad.zhgs.R
import com.uroad.zhgs.model.RoadTollGSMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 * @author MFB
 * @create 2018/9/19
 * @describe 路径路费 站点列表适配器
 */
class RoadTollZDAdapter(context: Context, mDatas: MutableList<RoadTollGSMDL.Poi>)
    : BaseArrayRecyclerAdapter<RoadTollGSMDL.Poi>(context, mDatas) {

    override fun bindView(viewType: Int): Int = R.layout.item_roadtoll_zd
    override fun onBindHoder(holder: RecyclerHolder, t: RoadTollGSMDL.Poi, position: Int) {
        holder.setText(R.id.tv, t.name)
    }
}