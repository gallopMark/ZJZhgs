package com.uroad.zhgs.adapteRv

import android.app.Activity
import android.content.Context
import android.widget.ImageView
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.model.MutilItem
import com.uroad.zhgs.model.PicMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/3.
 */
class AddPicAdapter(private val context: Activity, mDatas: MutableList<MutilItem>,
                    var count: Int, margin: Int) : BaseArrayRecyclerAdapter<MutilItem>(context, mDatas) {
    val size = (DisplayUtils.getWindowWidth(context) - margin) / count

    private var onItemOptionListener: OnItemOptionListener? = null
    override fun onBindHoder(holder: RecyclerHolder, t: MutilItem, position: Int) {
        holder.itemView.layoutParams = holder.itemView.layoutParams.apply {
            width = size
            height = size
        }
        if (holder.itemViewType == 1) {
            holder.itemView.setOnClickListener { onItemOptionListener?.onAddPic() }
        } else {
            val mdl = t as PicMDL
            val ivPic = holder.obtainView<ImageView>(R.id.ivPic)
            val ivCancel = holder.obtainView<ImageView>(R.id.ivCancel)
            ImageLoaderV4.getInstance().displayImage(context, mdl.path, ivPic)
            ivCancel.setOnClickListener {
                mDatas.removeAt(position)
                notifyDataSetChanged()
                onItemOptionListener?.onItemRemove(mDatas)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return mDatas[position].getItemType()
    }

    override fun bindView(viewType: Int): Int {
        if (viewType == 1) return R.layout.item_addpic_button
        return R.layout.item_addpic
    }

    interface OnItemOptionListener {
        fun onAddPic()
        fun onItemRemove(mDatas: MutableList<MutilItem>)
    }

    fun setOnItemOptionListener(onItemOptionListener: OnItemOptionListener) {
        this.onItemOptionListener = onItemOptionListener
    }
}