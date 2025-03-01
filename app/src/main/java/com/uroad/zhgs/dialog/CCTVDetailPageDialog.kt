package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.library.utils.DisplayUtils
import com.uroad.library.widget.banner.BannerView
import com.uroad.zhgs.R
import com.uroad.zhgs.model.SnapShotMDL

/**
 *Created by MFB on 2018/8/25.
 */
class CCTVDetailPageDialog(private val context: Activity,
                           private val mDatas: MutableList<SnapShotMDL>)
    : Dialog(context, R.style.translucentDialog) {

    private var onItemClickListener: OnItemClickListener? = null
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener): CCTVDetailPageDialog {
        this.onItemClickListener = onItemClickListener
        return this
    }

    override fun show() {
        super.show()
        initView()
    }

    private fun initView() {
        window?.let { window ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_eventdetail_page, LinearLayout(context), false)
            val bannerView = contentView.findViewById<BannerView>(R.id.bannerView)
            val tvCount = contentView.findViewById<TextView>(R.id.tvCount)
            val ivClose = contentView.findViewById<ImageView>(R.id.ivClose)
            val adapter = CCTVAdapter(context, mDatas)
            bannerView.setAdapter(adapter)
            val count = "1/${mDatas.size}"
            tvCount.text = count
            bannerView.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(p0: Int) {

                }

                override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                }

                override fun onPageSelected(position: Int) {
                    val text = "${position + 1}/${mDatas.size}"
                    tvCount.text = text
                    if (position in 0 until mDatas.size) {

                    }
                }
            })
            ivClose.setOnClickListener { dismiss() }
            window.setContentView(contentView)
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER)
        }
    }

    private inner class CCTVAdapter(private val context: Activity, private val mDatas: MutableList<SnapShotMDL>)
        : PagerAdapter() {
        override fun getCount(): Int = mDatas.size

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = LayoutInflater.from(context).inflate(R.layout.item_cctv_detail_page, container, false)
            val flCCTV = view.findViewById<FrameLayout>(R.id.flCCTV)
            val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)
            val tvName = view.findViewById<TextView>(R.id.tvName)
            flCCTV.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DisplayUtils.getWindowHeight(context) / 3)
            ImageLoaderV4.getInstance().displayImage(context, mDatas[position].getLastPicUrl(), ivIcon, R.color.white)
            tvName.text = mDatas[position].resname
            view.setOnClickListener { onItemClickListener?.onItemClick(position, mDatas[position]) }
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, mdl: SnapShotMDL)
    }
}