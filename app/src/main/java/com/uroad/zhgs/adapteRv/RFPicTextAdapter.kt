package com.uroad.zhgs.adapteRv

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.zhgs.R
import com.uroad.zhgs.model.RescueFeeMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/3.
 * 拖车、吊车、轮胎维修（图片加文字）
 */
class RFPicTextAdapter(private val context: Context, mDatas: MutableList<RescueFeeMDL.WorkType>) :
        BaseArrayRecyclerAdapter<RescueFeeMDL.WorkType>(context, mDatas) {
    private var onItemSelectedListener: OnItemSelectedListener? = null
    private var selectIndex = -1
    override fun onBindHoder(holder: RecyclerHolder, t: RescueFeeMDL.WorkType, position: Int) {
        val ivPic = holder.obtainView<ImageView>(R.id.ivPic)
        val tv = holder.obtainView<TextView>(R.id.tv)
        if (position == selectIndex) {
            ImageLoaderV4.getInstance().displayImage(context, t.getPic1(), ivPic)
            tv.isSelected = true
        } else {
            ImageLoaderV4.getInstance().displayImage(context, t.getPic0(), ivPic)
            tv.isSelected = false
        }
        tv.text = t.dictname
        holder.itemView.setOnClickListener { setSelectIndex(position) }
    }

    override fun bindView(viewType: Int): Int {
        return R.layout.item_rescue_feepic
    }

    private fun setSelectIndex(position: Int) {
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
