package com.uroad.zhgs.adaptervp

import android.app.Activity
import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.uroad.library.utils.BitmapUtils
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R

/**
 *Created by MFB on 2018/9/4.
 */
class SplashGuideAdapter(private val context: Activity, private val pictures: MutableList<Int>)
    : PagerAdapter() {

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun getCount(): Int = pictures.size

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.item_splash_guide, container, false)
        val ivPic = view.findViewById<ImageView>(R.id.ivPic)
        ivPic.setImageResource(pictures[position])
        container.addView(ivPic)
        return ivPic
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}