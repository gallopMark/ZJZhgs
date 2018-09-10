package com.uroad.zhgs.photopicker.adapter

import android.content.Context
import android.support.v4.util.ArrayMap
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.zhgs.R
import com.uroad.zhgs.photopicker.model.ImageItem
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/1.
 */
class ImageRvAdapter(private val context: Context, images: MutableList<ImageItem>)
    : BaseArrayRecyclerAdapter<ImageItem>(context, images) {
    private var selectIndex: Int = 0
    private val checkItems = ArrayMap<Int, Boolean>()

    init {
        for (i in 0 until mDatas.size) {
            checkItems[i] = true
        }
    }

    fun setSelectIndex(selectIndex: Int) {
        this.selectIndex = selectIndex
        notifyDataSetChanged()
    }

    fun setUnCheckItem(position: Int, isCheck: Boolean) {
        checkItems[position] = isCheck
        notifyDataSetChanged()
    }

    fun getUnCheckItems(): ArrayMap<Int, Boolean> {
        return checkItems
    }

    override fun onBindHoder(holder: RecyclerHolder, t: ImageItem, position: Int) {
        val flSelect = holder.obtainView<FrameLayout>(R.id.flSelect)
        val ivPic = holder.obtainView<ImageView>(R.id.ivPic)
        val vUnSelect = holder.obtainView<View>(R.id.vUnSelect)
        ImageLoaderV4.getInstance().displayImage(context, t.path, ivPic)
        if (position == selectIndex) {
            flSelect.setBackgroundResource(R.drawable.bg_photopicker_corner_2dp)
        } else {
            flSelect.setBackgroundResource(0)
        }
        if (checkItems[position] == true) {
            vUnSelect.visibility = View.GONE
        } else {
            vUnSelect.visibility = View.VISIBLE
        }
    }

    override fun bindView(viewType: Int): Int {
        return R.layout.item_photopicker_rvpic
    }
}