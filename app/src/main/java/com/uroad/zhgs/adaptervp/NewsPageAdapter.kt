package com.uroad.zhgs.adaptervp

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.library.utils.DisplayUtils
import com.uroad.library.widget.banner.BannerBaseAdapter
import com.uroad.zhgs.R
import com.uroad.zhgs.model.NewsMDL

/**
 *Created by MFB on 2018/8/22.
 */
@Deprecated("never use")
class NewsPageAdapter(private val context: Activity) : BannerBaseAdapter<NewsMDL>(context) {
    private val imageWith = DisplayUtils.getWindowWidth(context) / 3
    private val imageHeight = imageWith * 3 / 4
    override fun getLayoutResID(): Int = R.layout.item_news_page

    override fun convert(convertView: View, data: NewsMDL, position: Int) {
        val ivPic = convertView.findViewById<ImageView>(R.id.ivPic)
        val tvTitle = convertView.findViewById<TextView>(R.id.tvTitle)
        val tvTime = convertView.findViewById<TextView>(R.id.tvTime)
        val tvTypeName = convertView.findViewById<TextView>(R.id.tvTypeName)
        val params = (ivPic.layoutParams as LinearLayout.LayoutParams).apply {
            width = imageWith
            height = imageHeight
        }
        ivPic.layoutParams = params
        ImageLoaderV4.getInstance().displayImage(context, data.jpgurl, ivPic)
        tvTitle.text = data.title
        tvTypeName.text = data.newstypename
        tvTime.text = data.getTime()
        tvTypeName.setBackgroundColor(data.getBgColor(context))
        tvTypeName.setTextColor(data.getTextColor(context))
    }
}