package com.uroad.zhgs.adapteRv

import android.content.Context
import android.util.TypedValue
import android.widget.TextView
import com.uroad.zhgs.model.RescueFeeMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.R

/**
 *Created by MFB on 2018/8/3.
 */
class RFCarTypeAdapter(context: Context, mDatas: MutableList<RescueFeeMDL.Type.SonType>) :
        BaseArrayRecyclerAdapter<RescueFeeMDL.Type.SonType>(context, mDatas) {

    private var onItemSelectedListener: OnItemSelectedListener? = null
    private var selectIndex = -1
    private val ts12 = context.resources.getDimension(R.dimen.font_12)
    override fun onBindHoder(holder: RecyclerHolder, t: RescueFeeMDL.Type.SonType, position: Int) {
        val tv = holder.obtainView<TextView>(R.id.tv)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts12)
        tv.text = t.dictname
        tv.isSelected = selectIndex == position
        holder.itemView.setOnClickListener { setSelectIndex(position) }
    }

    override fun bindView(viewType: Int): Int {
        return R.layout.item_rescue_car
    }

    fun setSelectIndex(position: Int) {
        onItemSelectedListener?.onItemSelected(position)
        selectIndex = position
        notifyDataSetChanged()
    }

    interface OnItemSelectedListener {
        fun onItemSelected(position: Int)
    }

    fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener
    }
}