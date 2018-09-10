package com.uroad.zhgs.adapteRv

import android.content.Context
import android.widget.TextView
import com.uroad.zhgs.R
import com.uroad.zhgs.model.RescueFeeMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/3.
 * 车辆类型适配器
 */
class RFCarCategoryAdapter(context: Context, mDatas: MutableList<RescueFeeMDL.Type>)
    : BaseArrayRecyclerAdapter<RescueFeeMDL.Type>(context, mDatas) {

    private var onItemSelectedListener: OnItemSelectedListener? = null
    private var selectIndex = 0
    override fun onBindHoder(holder: RecyclerHolder, t: RescueFeeMDL.Type, position: Int) {
        val tv = holder.obtainView<TextView>(R.id.tv)
        tv.text = t.dictname
        tv.isSelected = selectIndex == position
        holder.itemView.setOnClickListener { setSelectIndex(position) }
    }

    private fun setSelectIndex(position: Int) {
        onItemSelectedListener?.onItemSelected(position)
        selectIndex = position
        notifyDataSetChanged()
    }

    override fun bindView(viewType: Int): Int {
        return R.layout.item_rescue_car
    }

    interface OnItemSelectedListener {
        fun onItemSelected(position: Int)
    }

    fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener
    }
}