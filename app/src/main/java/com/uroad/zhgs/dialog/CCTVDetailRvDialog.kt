package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        window?.let { window ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_mapdata_rv, LinearLayout(context), false)
            val ivClose = contentView.findViewById<ImageView>(R.id.ivClose)
            val recyclerView = contentView.findViewById<RecyclerView>(R.id.recyclerView)
            recyclerView.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.HORIZONTAL }
            val helper = PagerSnapHelper()
            helper.attachToRecyclerView(recyclerView)
            recyclerView.adapter = SnapShotAdapter(context, mDatas)
            ivClose.setOnClickListener { dismiss() }
            window.setContentView(contentView)
            window.setLayout(DisplayUtils.getWindowWidth(context), WindowManager.LayoutParams.WRAP_CONTENT)
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
            holder.setText(R.id.tvEventName, "监控")
            holder.setText(R.id.tvTitle, t.shortname)
            val recyclerView = holder.obtainView<RecyclerView>(R.id.recyclerView)
            recyclerView.isNestedScrollingEnabled = false
            if (t.getPicUrls().size > 0) {
                holder.setVisibility(R.id.tvEmpty, false)
                recyclerView.visibility = View.VISIBLE
                recyclerView.addItemDecoration(GridSpacingItemDecoration(3, DisplayUtils.dip2px(context, 10f), false))
                recyclerView.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.HORIZONTAL }
                val adapter = PicAdapter(context, t.getPicUrls())
                recyclerView.adapter = adapter
                adapter.setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
                    override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
//                        val pics = ArrayList<String>()
//                        for (i in 0 until t.getPicUrls().size) {
//                            if (!TextUtils.isEmpty(t.getPicUrls()[i])) {
//                                pics.add(t.getPicUrls()[i])
//                            }
//                        }
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
        val size = (DisplayUtils.getWindowWidth(context) - DisplayUtils.dip2px(context, 10f) * 4) / 3
        override fun bindView(viewType: Int): Int = R.layout.item_snapshot

        override fun onBindHoder(holder: RecyclerHolder, t: String, position: Int) {
            holder.itemView.layoutParams = holder.itemView.layoutParams.apply {
                width = size
                height = size
            }
            holder.displayImage(R.id.ivPic, t, R.color.whitesmoke)
        }
    }

    interface OnPhotoClickListener {
        fun onPhotoClick(position: Int, mdl: SnapShotMDL)
    }
}