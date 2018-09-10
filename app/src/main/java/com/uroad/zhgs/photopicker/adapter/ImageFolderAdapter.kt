package com.uroad.zhgs.photopicker.adapter

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.zhgs.R
import com.uroad.zhgs.photopicker.model.ImageFolder
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/7/30.
 */
class ImageFolderAdapter(private val context: Context, mDatas: MutableList<ImageFolder>)
    : BaseArrayRecyclerAdapter<ImageFolder>(context, mDatas) {
    private var mSelectItem: Int = 0

    fun setSelectedItem(position: Int) {
        mSelectItem = position
        notifyDataSetChanged()
    }

    override fun bindView(viewType: Int): Int {
        return R.layout.item_photopicker_imagefolder
    }

    override fun onBindHoder(holder: RecyclerHolder, t: ImageFolder, position: Int) {
        val ivImage = holder.obtainView<ImageView>(R.id.ivImage)
        val tvName = holder.obtainView<TextView>(R.id.tvName)
        val tvCount = holder.obtainView<TextView>(R.id.tvCount)
        val ivSelect = holder.obtainView<ImageView>(R.id.ivSelect)
        ImageLoaderV4.getInstance().displayImage(context, t.firstImagePath, ivImage)
        tvName.text = t.name
        val textCount = "${t.mediaItems.size}张"
        tvCount.text = textCount
        ivSelect.visibility = if (mSelectItem == position) View.VISIBLE else View.GONE
    }
}