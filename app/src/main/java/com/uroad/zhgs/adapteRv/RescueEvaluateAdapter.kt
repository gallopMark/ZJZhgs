package com.uroad.zhgs.adapteRv

import android.app.Activity
import android.widget.TextView
import com.uroad.zhgs.R
import com.uroad.zhgs.model.EvaluateMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 * @author MFB
 * @create 2018/9/27
 * @describe 救援评价项
 */
class RescueEvaluateAdapter(context: Activity, mDatas: MutableList<EvaluateMDL.Type.SonType>)
    : BaseArrayRecyclerAdapter<EvaluateMDL.Type.SonType>(context, mDatas) {
    private val mSelected = ArrayList<EvaluateMDL.Type.SonType>()
    private var onSelectListener: OnSelectListener? = null

    fun setOnSelectListener(onSelectListener: OnSelectListener) {
        this.onSelectListener = onSelectListener
    }

    override fun bindView(viewType: Int): Int = R.layout.item_evaluate

    override fun onBindHoder(holder: RecyclerHolder, t: EvaluateMDL.Type.SonType, position: Int) {
        val tv = holder.obtainView<TextView>(R.id.tvEvaluate)
        tv.text = t.dictname
        tv.setOnClickListener {
            tv.isSelected = !tv.isSelected
            if (tv.isSelected) {
                mSelected.add(t)
            } else {
                mSelected.remove(t)
            }
            onSelected()
        }
    }

    private fun onSelected() {
        val sb = StringBuilder()
        for (i in 0 until mSelected.size) {
            sb.append(mSelected[i].dictcode)
            if (i < mSelected.size - 1) {
                sb.append(",")
            }
        }
        onSelectListener?.onSelected(sb.toString())
    }

    interface OnSelectListener {
        fun onSelected(evaluatetag: String)
    }
}