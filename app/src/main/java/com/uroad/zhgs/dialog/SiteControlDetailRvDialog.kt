package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import com.uroad.zhgs.R
import com.uroad.zhgs.model.SiteControlMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 * @author MFB
 * @create 2018/12/4
 * @describe 简图站点管制弹窗
 */
class SiteControlDetailRvDialog(private val context: Activity,
                                private val mDatas: MutableList<SiteControlMDL>)
    : Dialog(context, R.style.transparentDialog) {
    override fun show() {
        super.show()
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
            recyclerView.adapter = SiteControlAdapter(context, mDatas)
            ivClose.setOnClickListener { dismiss() }
            window.setContentView(contentView)
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setWindowAnimations(R.style.dialog_anim)
            window.setGravity(Gravity.BOTTOM)
        }
    }

    private inner class SiteControlAdapter(context: Activity, mDatas: MutableList<SiteControlMDL>)
        : BaseArrayRecyclerAdapter<SiteControlMDL>(context, mDatas) {
        override fun bindView(viewType: Int): Int = R.layout.item_sitecontrol_detail

        override fun onBindHoder(holder: RecyclerHolder, t: SiteControlMDL, position: Int) {
            holder.displayImage(R.id.ivIcon, t.picurl, R.color.color_f2)
            holder.setText(R.id.tvName, t.name)
            holder.setText(R.id.tvDistance, t.distance)
            if (t.fx1rk == 1) {
                holder.setText(R.id.tvEnterStatus, "入口开启")
                holder.setTextColorRes(R.id.tvEnterStatus, R.color.status_normal)
            } else {
                holder.setText(R.id.tvEnterStatus, "入口关闭")
                holder.setTextColorRes(R.id.tvEnterStatus, R.color.status_close)
            }
            if (t.fx2rk == 1) {
                holder.setText(R.id.tvExitStatus, "入口开启")
                holder.setTextColorRes(R.id.tvExitStatus, R.color.status_normal)
            } else {
                holder.setText(R.id.tvExitStatus, "入口关闭")
                holder.setTextColorRes(R.id.tvExitStatus, R.color.status_close)
            }
            holder.setText(R.id.tvEnterDirection, t.direction1)
            holder.setText(R.id.tvExitDirection, t.direction2)
        }
    }
}