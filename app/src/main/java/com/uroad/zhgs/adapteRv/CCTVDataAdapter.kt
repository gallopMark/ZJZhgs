package com.uroad.zhgs.adapteRv

import android.app.Activity
import android.widget.LinearLayout
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.model.SnapShotMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/16.
 */
class CCTVDataAdapter(context: Activity, mDatas: MutableList<SnapShotMDL>)
    : BaseArrayRecyclerAdapter<SnapShotMDL>(context, mDatas) {
    private val size = (DisplayUtils.getWindowWidth(context) - DisplayUtils.dip2px(context, 10f) * 3) / 2
    override fun bindView(viewType: Int): Int {
        return R.layout.item_cctv_data
    }

    override fun onBindHoder(holder: RecyclerHolder, t: SnapShotMDL, position: Int) {
        holder.itemView.layoutParams = LinearLayout.LayoutParams(size, size)
        holder.displayImage(R.id.ivIcon, t.getLastPicUrl())
        holder.setText(R.id.tvName, t.resname)
    }
}