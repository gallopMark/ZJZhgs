package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.model.SnapShotMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.widget.GridSpacingItemDecoration

/**
 *Created by MFB on 2018/9/5.
 */
class CCTVDetailRvDialog(private val context: Activity, private val mDatas: MutableList<SnapShotMDL>)
    : Dialog(context, R.style.transparentDialog) {

    private var onPhotoClickListener: OnPhotoClickListener? = null

    fun setOnPhotoClickListener(onPhotoClickListener: OnPhotoClickListener) {
        this.onPhotoClickListener = onPhotoClickListener
    }

    override fun show() {
        super.show()
        initView()
    }

    private fun initView() {
        window?.let { window ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_mapdata_rv, LinearLayout(context), false)
            val ivClose = contentView.findViewById<ImageView>(R.id.ivClose)
            val recyclerView = contentView.findViewById<RecyclerView>(R.id.recyclerView)
            val tvEmpty = contentView.findViewById<TextView>(R.id.tvEmpty)
            if (mDatas.size > 0) {
                recyclerView.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.HORIZONTAL }
                val helper = PagerSnapHelper()
                helper.attachToRecyclerView(recyclerView)
                recyclerView.adapter = SnapShotAdapter(context, mDatas)
                recyclerView.visibility = View.VISIBLE
                tvEmpty.visibility = View.GONE
            } else {
                recyclerView.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
            }
            ivClose.setOnClickListener { dismiss() }
            window.setContentView(contentView)
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setWindowAnimations(R.style.dialog_anim)
            window.setGravity(Gravity.BOTTOM)
        }
    }

    private inner class SnapShotAdapter(private val context: Activity, mDatas: MutableList<SnapShotMDL>)
        : BaseArrayRecyclerAdapter<SnapShotMDL>(context, mDatas) {
        override fun bindView(viewType: Int): Int = R.layout.dialog_snapshot_detail

        override fun onBindHoder(holder: RecyclerHolder, t: SnapShotMDL, position: Int) {
            holder.setVisibility(R.id.ivClose, false)
            holder.setImageResource(R.id.ivIcon, R.mipmap.ic_menu_jtss_spot_p)
            holder.setText(R.id.tvEventName, context.resources.getString(R.string.monitor_video))
            holder.setText(R.id.tvTitle, t.shortname)
            val recyclerView = holder.obtainView<RecyclerView>(R.id.recyclerView)
            recyclerView.isNestedScrollingEnabled = false
            val urls = t.getPicUrls()
            if (urls.size > 0) {
                holder.setVisibility(R.id.tvEmpty, false)
                recyclerView.visibility = View.VISIBLE
                recyclerView.addItemDecoration(GridSpacingItemDecoration(urls.size, DisplayUtils.dip2px(context, 10f), false))
                recyclerView.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.HORIZONTAL }
                val adapter = PicAdapter(context, urls)
                recyclerView.adapter = adapter
                adapter.setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
                    override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                        onPhotoClickListener?.onPhotoClick(position, t)
                    }
                })
            } else {
                holder.setVisibility(R.id.tvEmpty, true)
                recyclerView.visibility = View.GONE
            }
        }
    }

    private inner class PicAdapter(context: Activity, mDatas: MutableList<String>)
        : BaseArrayRecyclerAdapter<String>(context, mDatas) {
        val mWidth: Int
        val mHeight: Int

        init {
            if (mDatas.size == 1) {
                mWidth = DisplayUtils.getWindowWidth(context) - DisplayUtils.dip2px(context, 40f)
                mHeight = mWidth / 5 * 3
            } else {
                if (mDatas.size <= 3) {
                    mWidth = (DisplayUtils.getWindowWidth(context) - DisplayUtils.dip2px(context, 40f) - DisplayUtils.dip2px(context, 10f) * (mDatas.size - 1)) / mDatas.size
                    mHeight = mWidth
                } else {
                    mWidth = (DisplayUtils.getWindowWidth(context) - DisplayUtils.dip2px(context, 40f) - DisplayUtils.dip2px(context, 10f) * (mDatas.size - 1)) / 3
                    mHeight = mWidth
                }
            }
        }

        override fun bindView(viewType: Int): Int = R.layout.item_snapshot

        override fun onBindHoder(holder: RecyclerHolder, t: String, position: Int) {
            holder.itemView.layoutParams = holder.itemView.layoutParams.apply {
                this.width = mWidth
                this.height = mHeight
            }
            holder.displayImage(R.id.ivPic, t, R.color.whitesmoke)
        }
    }

    interface OnPhotoClickListener {
        fun onPhotoClick(position: Int, mdl: SnapShotMDL)
    }
}