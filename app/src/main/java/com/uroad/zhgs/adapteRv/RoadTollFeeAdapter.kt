package com.uroad.zhgs.adapteRv

import android.content.Context
import android.text.TextUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.model.RoadTollFeeMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 * @author MFB
 * @create 2018/9/21
 * @describe
 */
class RoadTollFeeAdapter(context: Context, mDatas: MutableList<RoadTollFeeMDL.Fee>)
    : BaseArrayRecyclerAdapter<RoadTollFeeMDL.Fee>(context, mDatas) {
    override fun bindView(viewType: Int): Int = R.layout.item_roadtoll_fee

    override fun onBindHoder(holder: RecyclerHolder, t: RoadTollFeeMDL.Fee, position: Int) {
        var price = "¥"
        t.price?.let { price += it }
        holder.setText(R.id.tvPrice, price)
        holder.displayImage(R.id.ivPic, t.car_icon, R.color.color_f7)
        holder.setText(R.id.tvTitle, t.car_title)
    }
}