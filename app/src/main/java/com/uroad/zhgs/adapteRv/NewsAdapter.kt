package com.uroad.zhgs.adapteRv

import android.app.Activity
import android.view.Gravity
import android.widget.LinearLayout
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.model.NewsMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.rv.BaseRecyclerAdapter

/**
 *Created by MFB on 2018/8/7.
 * 资讯列表适配器
 */
class NewsAdapter(private val context: Activity, mDatas: MutableList<NewsMDL>)
    : BaseArrayRecyclerAdapter<NewsMDL>(context, mDatas) {
    private val imageWith = DisplayUtils.getWindowWidth(context) / 3
    private val imageHeight = imageWith * 3 / 4
    private val params = LinearLayout.LayoutParams(imageWith, imageHeight).apply { gravity = Gravity.CENTER }
    private val dp4 = DisplayUtils.dip2px(context, 4f)
    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, t: NewsMDL, position: Int) {
        holder.setLayoutParams(R.id.ivPic, params)
        holder.displayImage(R.id.ivPic, t.jpgurl, R.color.color_f2, RoundedCorners(dp4))
        holder.setText(R.id.tvTitle, t.title)
        holder.setText(R.id.tvTypeName, t.newstypename)
        holder.setText(R.id.tvTime, t.getTime())
        holder.setBackgroundColor(R.id.tvTypeName, t.getBgColor(context))
        holder.setTextColor(R.id.tvTypeName, t.getTextColor(context))
    }

    override fun bindView(viewType: Int): Int {
        return R.layout.item_news
    }
}