package com.uroad.zhgs.adapteRv

import android.content.Context
import android.widget.TextView
import com.uroad.zhgs.R
import com.uroad.zhgs.model.RescueRequestMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/3.
 */
class RRFaultTypeAdapter(context: Context, mDatas: MutableList<RescueRequestMDL.Sontype>)
    : BaseArrayRecyclerAdapter<RescueRequestMDL.Sontype>(context, mDatas) {
    private var onItemSelectedListener: OnItemSelectedListener? = null
    private var selectIndex = -1
    override fun onBindHoder(holder: RecyclerHolder, t: RescueRequestMDL.Sontype, position: Int) {
        val tv = holder.obtainView<TextView>(R.id.tv)
        tv.text = t.dictname
        tv.isSelected = selectIndex == position
        holder.itemView.setOnClickListener {
            tv.isSelected = !tv.isSelected
            if (tv.isSelected)
                setSelectIndex(position)
            else
                setSelectIndex(-1)
        }
    }

    private fun setSelectIndex(position: Int) {
        onItemSelectedListener?.onItemSelected(position)
        selectIndex = position
        notifyDataSetChanged()
    }

    fun getSelectIndex(): Int = selectIndex
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