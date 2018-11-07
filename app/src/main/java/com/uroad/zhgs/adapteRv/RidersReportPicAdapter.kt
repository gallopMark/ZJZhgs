package com.uroad.zhgs.adapteRv

import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.model.MutilItem
import com.uroad.zhgs.model.PicMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 * @author MFB
 * @create 2018/10/20
 * @describe 车友爆料 图片选择适配器
 */
class RidersReportPicAdapter(context: Activity, mDatas: MutableList<MutilItem>)
    : BaseArrayRecyclerAdapter<MutilItem>(context, mDatas) {
    private val size = (DisplayUtils.getWindowWidth(context) - DisplayUtils.dip2px(context, 44f)) / 3
    private var onItemOptionListener: OnItemOptionListener? = null
    override fun onBindHoder(holder: RecyclerHolder, t: MutilItem, position: Int) {
        holder.itemView.layoutParams = LinearLayout.LayoutParams(size, size)
        if (holder.itemViewType == 1) {
            holder.itemView.setOnClickListener { onItemOptionListener?.onAddPic() }
        } else {
            val mdl = t as PicMDL
            holder.displayImage(R.id.ivPic, mdl.path)
            holder.setOnClickListener(R.id.ivCancel, View.OnClickListener {
                mDatas.removeAt(position)
                notifyDataSetChanged()
                onItemOptionListener?.onItemRemove(mDatas)
            })
        }
    }

    override fun getItemViewType(position: Int): Int {
        return mDatas[position].getItemType()
    }

    override fun bindView(viewType: Int): Int {
        if (viewType == 1) return R.layout.item_addpic_button2
        return R.layout.item_addpic2
    }

    interface OnItemOptionListener {
        fun onAddPic()
        fun onItemRemove(mDatas: MutableList<MutilItem>)
    }

    fun setOnItemOptionListener(onItemOptionListener: OnItemOptionListener) {
        this.onItemOptionListener = onItemOptionListener
    }
}