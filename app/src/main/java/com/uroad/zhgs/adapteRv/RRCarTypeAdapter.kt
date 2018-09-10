package com.uroad.zhgs.adapteRv

import android.content.Context
import android.widget.TextView
import com.uroad.zhgs.R
import com.uroad.zhgs.model.RescueRequestMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/3.
 */
class RRCarTypeAdapter(context: Context, mDatas: MutableList<RescueRequestMDL.Sontype>) :
        BaseArrayRecyclerAdapter<RescueRequestMDL.Sontype>(context, mDatas) {
    private var onItemSelectedListener: OnItemSelectedListener? = null
    private var selectIndex = 0
    override fun onBindHoder(holder: RecyclerHolder, t: RescueRequestMDL.Sontype, position: Int) {
        val tv = holder.obtainView<TextView>(R.id.tv)
        tv.textSize = 12f
        tv.text = t.dictname
        tv.isSelected = selectIndex == position
        holder.itemView.setOnClickListener { setSelectIndex(position) }
    }

    override fun bindView(viewType: Int): Int {
        return R.layout.item_rescue_car
    }

    private fun setSelectIndex(position: Int) {
        onItemSelectedListener?.onItemSelected(position)
        selectIndex = position
        notifyDataSetChanged()
    }

    fun getSelectIndex(): Int = selectIndex
    interface OnItemSelectedListener {
        fun onItemSelected(position: Int)
    }

    fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener
    }
}