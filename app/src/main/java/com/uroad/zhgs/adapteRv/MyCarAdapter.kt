package com.uroad.zhgs.adapteRv

import android.content.Context
import android.text.TextUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.enumeration.Carcategory
import com.uroad.zhgs.model.CarMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/9/2.
 */
class MyCarAdapter(context: Context, mDatas: MutableList<CarMDL>)
    : BaseArrayRecyclerAdapter<CarMDL>(context, mDatas) {
    override fun onBindHoder(holder: RecyclerHolder, t: CarMDL, position: Int) {
        if (TextUtils.equals(t.carcategory, Carcategory.COACH.code)) {
            holder.setImageResource(R.id.ivIcon, R.mipmap.ic_car_coach)
        } else {
            holder.setImageResource(R.id.ivIcon, R.mipmap.ic_car_truck)
        }
        holder.setText(R.id.tvCarNo, t.carno)
        var lb = "车辆类型："
        t.carcategoryname?.let { lb += it }
        holder.setText(R.id.tvLeiBie, lb)
        var lx = "车辆类型："
        t.cartypename?.let { lx += it }
        holder.setText(R.id.tvLeiXing, lx)
        if (t.isdefault == 1) holder.setVisibility(R.id.tvDefault, true)
        else holder.setVisibility(R.id.tvDefault, false)
    }

    override fun bindView(viewType: Int): Int {
        return R.layout.item_car
    }
}