package com.uroad.zhgs.photopicker.adapter

import android.app.Activity
import android.view.View
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.imageloader_v4.listener.ImageSize
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.photopicker.model.ImageItem
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.rv.BaseRecyclerAdapter

/**
 *Created by MFB on 2018/7/30.
 */
class ImageGridAdapter(private val context: Activity,
                       mDatas: MutableList<ImageItem>,
                       private val isMutily: Boolean,
                       private val limit: Int)
    : BaseArrayRecyclerAdapter<ImageItem>(context, mDatas) {
    companion object {
        private const val ITEM_IMAGE = 1
        private const val ITEM_CAMERA = 2
    }

    private val mImageSize: Int
    private var mSelects = ArrayList<ImageItem>()
    private var onImageItemClickListener: OnImageItemClickListener? = null

    init {
        val screenWidth = DisplayUtils.getWindowWidth(context)
        val columnSpace = DisplayUtils.dip2px(context, 2f)
        mImageSize = (screenWidth - columnSpace * 2) / 3
    }

    fun setSelects(images: MutableList<ImageItem>) {
        mSelects.clear()
        mSelects.addAll(images)
        notifyDataSetChanged()
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, t: ImageItem, position: Int) {
        val itemType = holder.itemViewType
        holder.itemView.layoutParams = FrameLayout.LayoutParams(mImageSize, mImageSize)
        if (itemType == ITEM_IMAGE) {
            val ivPic = holder.obtainView<ImageView>(R.id.ivPic)
            val checkBox = holder.obtainView<CheckBox>(R.id.checkBox)
            ImageLoaderV4.getInstance().displayImage(context, t.path,
                    ivPic, R.color.darkgrey,
                    ImageSize(mImageSize, mImageSize))
            if (isMutily) {
                checkBox.visibility = View.VISIBLE
            } else {
                checkBox.visibility = View.GONE
            }
            holder.itemView.setOnClickListener {
                if (isMutily) {
                    if (checkBox.isChecked) {
                        mSelects.remove(t)
                        checkBox.isChecked = false
                    } else {
                        if (mSelects.contains(t)) {
                            mSelects.remove(t)
                        }
                        if (mSelects.size < limit) {
                            mSelects.add(t)
                            checkBox.isChecked = true
                        } else {
                            checkBox.isChecked = false
                            onImageItemClickListener?.onOverSelected(limit)
                        }
                    }
                } else {
                    mSelects.clear()
                    mSelects.add(t)
                }
                onImageItemClickListener?.onSelected(mSelects)
            }
            checkBox.setOnClickListener {
                if (mSelects.contains(t)) {
                    checkBox.isChecked = false
                    mSelects.remove(t)
                } else {
                    if (mSelects.size < limit) {
                        mSelects.add(t)
                        checkBox.isChecked = true
                    } else {
                        checkBox.isChecked = false
                        onImageItemClickListener?.onOverSelected(limit)
                    }
                }
                onImageItemClickListener?.onSelected(mSelects)
            }
            checkBox.isChecked = mSelects.contains(t)
        } else {
            holder.itemView.setOnClickListener { onImageItemClickListener?.onCamera() }
        }
    }

    override fun bindView(viewType: Int): Int {
        if (viewType == ITEM_IMAGE) return R.layout.item_photopicker_imagegrid
        else return R.layout.item_photopicker_camera
    }

    override fun getItemViewType(position: Int): Int {
        if (mDatas[position].showCamera) return ITEM_CAMERA
        else return ITEM_IMAGE
    }

    interface OnImageItemClickListener {
        fun onCamera()
        fun onOverSelected(limit: Int)
        fun onSelected(mDatas: ArrayList<ImageItem>)
    }

    fun setOnImageItemClickListener(onImageItemClickListener: OnImageItemClickListener) {
        this.onImageItemClickListener = onImageItemClickListener
    }
}