package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.activity.ShowImageActivity
import com.uroad.zhgs.model.SnapShotMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.widget.GridSpacingItemDecoration

/**
 *Created by MFB on 2018/8/22.
 */
class SnapShotDialog(private val context: Activity, private val dataMDL: SnapShotMDL)
    : Dialog(context, R.style.transparentDialog) {

    override fun show() {
        super.show()
        initView()
    }

    private fun initView() {
        window?.let { window ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_snapshot_detail, LinearLayout(context), false)
            window.setContentView(contentView)
            val ivClose = contentView.findViewById<ImageView>(R.id.ivClose)
            val ivIcon = contentView.findViewById<ImageView>(R.id.ivIcon)
            val tvEventName = contentView.findViewById<TextView>(R.id.tvEventName)
            val tvTitle = contentView.findViewById<TextView>(R.id.tvTitle)
            val tvEmpty = contentView.findViewById<TextView>(R.id.tvEmpty)
            val recyclerView = contentView.findViewById<RecyclerView>(R.id.recyclerView)
            ivClose.setOnClickListener { dismiss() }
            ivIcon.setImageResource(R.mipmap.ic_menu_jtss_spot_p)
            tvEventName.text = "监控"
            tvTitle.text = dataMDL.shortname
            if (dataMDL.getPicUrls().size > 0) {
                tvEmpty.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                recyclerView.addItemDecoration(GridSpacingItemDecoration(3, DisplayUtils.dip2px(context, 10f), false))
                recyclerView.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.HORIZONTAL }
                val adapter = PicAdapter(context, dataMDL.getPicUrls())
                recyclerView.adapter = adapter
                adapter.setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
                    override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                        val pics = ArrayList<String>()
                        for (i in 0 until dataMDL.getPicUrls().size) {
                            if (!TextUtils.isEmpty(dataMDL.getPicUrls()[i])) {
                                pics.add(dataMDL.getPicUrls()[i])
                            }
                        }
                        context.startActivity(Intent(context, ShowImageActivity::class.java).apply {
                            putExtra("position", position)
                            putStringArrayListExtra("photos", pics)
                        })
                    }
                })
            } else {
                tvEmpty.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
            window.setLayout(DisplayUtils.getWindowWidth(context), WindowManager.LayoutParams.WRAP_CONTENT)
            window.setWindowAnimations(R.style.dialog_anim)
            window.setGravity(Gravity.BOTTOM)
        }
    }

    private class PicAdapter(context: Activity, mDatas: MutableList<String>)
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
}