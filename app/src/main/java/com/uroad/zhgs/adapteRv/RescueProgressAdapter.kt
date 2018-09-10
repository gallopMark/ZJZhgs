package com.uroad.zhgs.adapteRv

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import com.uroad.zhgs.R
import com.uroad.zhgs.model.RescueDetailMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/4.
 */
class RescueProgressAdapter(context: Context, mDatas: MutableList<RescueDetailMDL.Track>) :
        BaseArrayRecyclerAdapter<RescueDetailMDL.Track>(context, mDatas) {
    override fun onBindHoder(holder: RecyclerHolder, t: RescueDetailMDL.Track, position: Int) {
        val tvTime = holder.obtainView<TextView>(R.id.tvTime)
        val tvDate = holder.obtainView<TextView>(R.id.tvDate)
        val ivOval = holder.obtainView<ImageView>(R.id.ivOval)
        val tvStatus = holder.obtainView<TextView>(R.id.tvStatus)
        val tvContent = holder.obtainView<TextView>(R.id.tvContent)
        tvTime.text = t.getTime()
        tvDate.text = t.getDate()
        if (position == 0) {
            ivOval.setImageResource(R.mipmap.ic_oval_accent)
        } else {
            ivOval.setImageResource(R.mipmap.ic_oval_default)
        }
        tvStatus.text = t.status
        tvContent.text = t.content
    }

    override fun bindView(viewType: Int): Int {
        return R.layout.item_rescue_progress
    }
}