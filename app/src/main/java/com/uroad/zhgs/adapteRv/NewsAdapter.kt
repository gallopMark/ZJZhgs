package com.uroad.zhgs.adapteRv

import android.app.Activity
import android.widget.ImageView
import android.widget.LinearLayout
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.imageloader_v4.listener.ImageSize
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
    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, t: NewsMDL, position: Int) {
        val ivPic = holder.obtainView<ImageView>(R.id.ivPic)
        val params = (ivPic.layoutParams as LinearLayout.LayoutParams).apply {
            width = imageWith
            height = imageHeight
        }
        ivPic.layoutParams = params
        ImageLoaderV4.getInstance().displayImage(context, t.jpgurl, ivPic)
        holder.setText(R.id.tvTitle, t.title)
        holder.setText(R.id.tvTypeName, t.newstypename)
        holder.setText(R.id.tvTime, t.getTime())
        holder.setBackgroundColor(R.id.tvTypeName,t.getBgColor(context))
        holder.setTextColor(R.id.tvTypeName,t.getTextColor(context))
    }

    override fun bindView(viewType: Int): Int {
        return R.layout.item_news
    }
}