package com.uroad.zhgs.adapteRv

import android.app.Activity
import android.content.Context
import android.support.v4.content.ContextCompat
import android.widget.ImageView
import android.widget.LinearLayout
import com.amap.api.col.sln3.ho
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.R.id.ivIcon
import com.uroad.zhgs.model.CCTVMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/16.
 */
class CCTVDataAdapter(context: Activity, mDatas: MutableList<CCTVMDL>)
    : BaseArrayRecyclerAdapter<CCTVMDL>(context, mDatas) {
    private val size = (DisplayUtils.getWindowWidth(context) - DisplayUtils.dip2px(context, 10f) * 3) / 2
    override fun bindView(viewType: Int): Int {
        return R.layout.item_cctv_data
    }

    override fun onBindHoder(holder: RecyclerHolder, t: CCTVMDL, position: Int) {
        holder.itemView.layoutParams = LinearLayout.LayoutParams(size, size)
        holder.displayImage(R.id.ivIcon, t.getLastPicUrl())
        holder.setText(R.id.tvName, t.resname)
    }
}