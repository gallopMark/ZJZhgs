package com.uroad.zhgs.adapteRv

import android.app.Activity
import com.uroad.zhgs.R
import com.uroad.zhgs.model.HarvestMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 * @author MFB
 * @create 2018/11/24
 * @describe 我的成果(奖励列表适配器)
 */
class MyHarvestPrizeAdapter(context: Activity, mDatas: MutableList<HarvestMDL.Prize>)
    : BaseArrayRecyclerAdapter<HarvestMDL.Prize>(context, mDatas) {
    override fun bindView(viewType: Int): Int = R.layout.item_harvestprize
    override fun onBindHoder(holder: RecyclerHolder, t: HarvestMDL.Prize, position: Int) {
        holder.setText(R.id.tvTitle, t.title)
        var text = "发放时间："
        t.sendtime?.let { text += it }
        holder.setText(R.id.tvTime, text)
    }
}