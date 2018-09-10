package com.uroad.zhgs.adaptervp

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.uroad.zhgs.R
import com.uroad.zhgs.R.id.tvOverTime
import com.uroad.zhgs.model.SubscribeMDL

/**
 *Created by MFB on 2018/8/22.
 */
class UserSubscribePageAdapter(private val context: Context,
                               private val mDatas: MutableList<SubscribeMDL>) : PagerAdapter() {
    private var mDownTime: Long = 0
    private var mListener: OnPageTouchListener? = null
    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun getCount(): Int {
        return if (mDatas.size == 0) 0 else Integer.MAX_VALUE
    }

    private fun getItem(position: Int): SubscribeMDL {
        return if (position >= mDatas.size) mDatas[0] else mDatas[position]
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var pos = 0
        if (mDatas.size != 0) {
            pos = position % mDatas.size
        }
        val subType = mDatas[pos].subtype
        val view: View
        view = if (!TextUtils.isEmpty(subType) && subType == SubscribeMDL.SubType.TrafficJam.code) {
            LayoutInflater.from(context).inflate(R.layout.item_user_subscribe_trafficjam_page, container, false)
        } else if (!TextUtils.isEmpty(subType) && subType == SubscribeMDL.SubType.RescueProgress.code) {
            LayoutInflater.from(context).inflate(R.layout.item_user_subscribe_rescueprogress_page, container, false)
        } else if (!TextUtils.isEmpty(subType) && subType == SubscribeMDL.SubType.RescuePay.code) {
            LayoutInflater.from(context).inflate(R.layout.item_user_subscribe_rescuepay_page, container, false)
        } else {
            LayoutInflater.from(context).inflate(R.layout.item_user_subscribe_event_page, container, false)
        }
        // 处理视图和数据
        convert(view, getItem(pos), subType)
        view.isClickable = true
        // 处理条目的触摸事件
        view.setOnTouchListener(View.OnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    mDownTime = System.currentTimeMillis()
                    mListener?.onPageDown()
                }
                MotionEvent.ACTION_UP -> {
                    val upTime = System.currentTimeMillis()
                    mListener?.onPageUp()
                    if (upTime - mDownTime < 500) {
                        // 500毫秒以内就算单击
                        mListener?.onPageClick(pos, getItem(pos))
                    }
                }
            }
            return@OnTouchListener false
        })
        container.addView(view)
        return view
    }

    private fun convert(view: View, item: SubscribeMDL, subType: String?) {
        if (!TextUtils.isEmpty(subType) && subType == SubscribeMDL.SubType.TrafficJam.code) {
            convertTraffic(view, item)
        } else if (!TextUtils.isEmpty(subType) && subType == SubscribeMDL.SubType.RescueProgress.code) {
            convertRescueProgress(view, item)
        } else if (!TextUtils.isEmpty(subType) && subType == SubscribeMDL.SubType.RescuePay.code) {
            convertRescuePay(view, item)
        } else {
            convertEvent(view, item)
        }
    }

    /*事件类型*/
    private fun convertEvent(view: View, item: SubscribeMDL) {
        val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)
        val tvEventName = view.findViewById<TextView>(R.id.tvEventName)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvContent = view.findViewById<TextView>(R.id.tvContent)
        val tvOccTime = view.findViewById<TextView>(R.id.tvOccTime)
        val tvEndTimeTips = view.findViewById<TextView>(R.id.tvEndTimeTips)
        val tvEndTime = view.findViewById<TextView>(R.id.tvEndTime)
        val tvUpdateTime = view.findViewById<TextView>(R.id.tvUpdateTime)
        ivIcon.setImageResource(item.getIcon())
        tvEventName.text = item.eventtypename
        tvTitle.text = item.roadtitle
        tvContent.text = item.reportout
        if (TextUtils.isEmpty(item.getOccTime())) {
            tvOccTime.text = "--"
        } else {
            tvOccTime.text = item.getOccTime()
        }
        if (item.getSubType() == SubscribeMDL.SubType.Planned.code) {
            tvEndTimeTips.text = context.resources.getString(R.string.usersubscribe_planEndTime)
        } else {
            tvEndTimeTips.text = context.resources.getString(R.string.usersubscribe_endTime)
        }
        tvEndTime.text = item.getRealoverTime()
        if (TextUtils.isEmpty(item.getUpdateTime())) {
            tvUpdateTime.text = "--"
        } else {
            tvUpdateTime.text = item.getUpdateTime()
        }
    }

    /*拥堵类型*/
    private fun convertTraffic(view: View, item: SubscribeMDL) {
        val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)
        val tvEventName = view.findViewById<TextView>(R.id.tvEventName)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
        val tvUpdateTime = view.findViewById<TextView>(R.id.tvUpdateTime)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvContent = view.findViewById<TextView>(R.id.tvContent)
        val tvOccTime = view.findViewById<TextView>(R.id.tvOccTime)
        val tvJamSpeed = view.findViewById<TextView>(R.id.tvJamSpeed)
        val tvDistance = view.findViewById<TextView>(R.id.tvDistance)
        val tvDuration = view.findViewById<TextView>(R.id.tvDuration)
        ivIcon.setImageResource(R.mipmap.ic_menu_event_yd_p)
        tvEventName.text = item.eventstatus
        if (TextUtils.isEmpty(item.statusname)) {
            tvStatus.visibility = View.GONE
        } else {
            tvStatus.visibility = View.VISIBLE
            tvStatus.text = item.statusname
            tvStatus.setBackgroundColor(item.getStatusColor(context))
        }
        tvUpdateTime.text = item.getUpdateTime()
        tvTitle.text = item.roadtitle
        tvContent.text = item.content
        if (TextUtils.isEmpty(item.getPubTime())) {
            tvOccTime.text = "--"
        } else {
            tvOccTime.text = item.getPubTime()
        }
        var jamSpeed = ""
        item.jamspeed?.let { jamSpeed += it }
        jamSpeed += "km/h"
        tvJamSpeed.text = SpannableString(jamSpeed).apply { setSpan(AbsoluteSizeSpan(18, true), 0, jamSpeed.indexOf("k"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }
        var distance = ""
        item.jamdist?.let { distance += it }
        distance += "km"
        tvDistance.text = SpannableString(distance).apply { setSpan(AbsoluteSizeSpan(18, true), 0, distance.indexOf("k"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }
        tvDuration.text = item.getLongTime()
    }

    /*救援进展类型*/
    private fun convertRescueProgress(view: View, item: SubscribeMDL) {
        val tvTips = view.findViewById<TextView>(R.id.tvTips)
        val tvTime = view.findViewById<TextView>(R.id.tvTime)
        val tvRoadname = view.findViewById<TextView>(R.id.tvRoadname)
        val tvContent = view.findViewById<TextView>(R.id.tvContent)
        val tvAcceptTime = view.findViewById<TextView>(R.id.tvAcceptTime)
        val tvStartTime = view.findViewById<TextView>(R.id.tvStartTime)
        val tvArriveTime = view.findViewById<TextView>(R.id.tvArriveTime)
        tvTips.text = "救援进展"
        tvTime.text = item.getCreateTime()
        tvRoadname.text = item.roadname
        tvContent.text = if (TextUtils.isEmpty(item.content)) "待定"
        else item.content
        tvAcceptTime.text = item.getAcceptTime()
        tvStartTime.text = item.getStartTime()
        tvArriveTime.text = item.getArriveTime()
    }

    /*救援资费类型*/
    private fun convertRescuePay(view: View, item: SubscribeMDL) {
        val tvTips = view.findViewById<TextView>(R.id.tvTips)
        val tvTime = view.findViewById<TextView>(R.id.tvTime)
        val tvMsg = view.findViewById<TextView>(R.id.tvMsg)
        tvTips.text = "救援资费尚未支付"
        tvTime.text = item.getCreateTime()
        tvMsg.text = item.msg
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    /**
     * 条目页面的触摸事件
     */
    interface OnPageTouchListener {
        fun onPageClick(position: Int, mdl: SubscribeMDL)

        fun onPageDown()

        fun onPageUp()
    }

    fun setOnPageTouchListener(listener: OnPageTouchListener) {
        this.mListener = listener
    }
}