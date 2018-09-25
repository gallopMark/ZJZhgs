package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.library.widget.banner.BannerView
import com.uroad.zhgs.R
import com.uroad.zhgs.enumeration.MapDataType
import com.uroad.zhgs.model.EventMDL
import com.uroad.zhgs.utils.TypefaceUtils

/**
 *Created by MFB on 2018/8/25.
 */
class EventDetailPageDialog(private val context: Activity,
                            private val mDatas: MutableList<EventMDL>)
    : Dialog(context, R.style.translucentDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        window?.let { window ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_eventdetail_page, LinearLayout(context), false)
            val bannerView = contentView.findViewById<BannerView>(R.id.bannerView)
            val tvCount = contentView.findViewById<TextView>(R.id.tvCount)
            val ivClose = contentView.findViewById<ImageView>(R.id.ivClose)
            val adapter = EventPageAdapter(context, mDatas)
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
                }
            })
            ivClose.setOnClickListener { dismiss() }
            window.setContentView(contentView)
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER)
        }
    }

    private class EventPageAdapter(private val context: Activity, private val mDatas: MutableList<EventMDL>)
        : PagerAdapter() {
        override fun getCount(): Int = mDatas.size

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = LayoutInflater.from(context).inflate(R.layout.item_user_subscribe_event_page, container, false)
            convert(view, mDatas[position])
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        private fun convert(view: View, data: EventMDL) {
            val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)
            val tvEventName = view.findViewById<TextView>(R.id.tvEventName)
            val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
            val tvContent = view.findViewById<TextView>(R.id.tvContent)
            val tvOccTime = view.findViewById<TextView>(R.id.tvOccTime)
            val tvEndTime = view.findViewById<TextView>(R.id.tvEndTime)
            val tvEndTimeTips = view.findViewById<TextView>(R.id.tvEndTimeTips)
            val tvUpdateTime = view.findViewById<TextView>(R.id.tvUpdateTime)
            ivIcon.setImageResource(data.getIcon())
            tvEventName.text = data.eventtypename
            tvTitle.text = data.roadtitle
            tvContent.text = data.reportout
            val typeface = TypefaceUtils.dinCondensed(context)
            tvOccTime.typeface = typeface
            tvUpdateTime.typeface = typeface
            tvEndTime.typeface = typeface
            if (TextUtils.isEmpty(data.getOccTime())) {
                tvOccTime.text = "--"
            } else {
                tvOccTime.text = data.getOccTime()
            }
            tvEndTimeTips.text = if (data.eventtype == MapDataType.CONSTRUCTION.code)
                context.resources.getString(R.string.usersubscribe_planEndTime)
            else context.resources.getString(R.string.usersubscribe_endTime)
            tvEndTime.text = data.getRealoverTime()
            if (TextUtils.isEmpty(data.getUpdateTime())) {
                tvUpdateTime.text = "--"
            } else {
                tvUpdateTime.text = data.getUpdateTime()
            }
        }
    }
}