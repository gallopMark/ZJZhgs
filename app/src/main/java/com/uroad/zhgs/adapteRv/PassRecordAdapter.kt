package com.uroad.zhgs.adapteRv

import android.app.Activity
import com.uroad.zhgs.R
import com.uroad.zhgs.model.PassRecordMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 * @author MFB
 * @create 2018/11/1
 * @describe 通行记录列表适配器
 */
class PassRecordAdapter(context: Activity, mDatas: MutableList<PassRecordMDL>)
    : BaseArrayRecyclerAdapter<PassRecordMDL>(context, mDatas) {
    override fun bindView(viewType: Int): Int = R.layout.item_passrecord

    override fun onBindHoder(holder: RecyclerHolder, t: PassRecordMDL, position: Int) {
        holder.setText(R.id.tvEnterStation, t.n_en_station_name)
        holder.setText(R.id.tvExitStation, t.n_ex_station_name)
        holder.setText(R.id.tvEnterTime, t.getEnDateTime())
        holder.setText(R.id.tvExitTime, t.getExDateTime())
        holder.setText(R.id.tvMoney, t.getMoney())
    }
}