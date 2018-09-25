package com.uroad.zhgs.adapteRv

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import com.uroad.zhgs.R
import com.uroad.zhgs.R.id.*
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.model.SubscribeMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.utils.TypefaceUtils

/**
 *Created by MFB on 2018/8/15.
 * 订阅列表适配器
 */
class UserSubscribeAdapter(private val context: Context, mDatas: MutableList<SubscribeMDL>)
    : BaseArrayRecyclerAdapter<SubscribeMDL>(context, mDatas) {
    private val typeface = TypefaceUtils.dinCondensed(context)

    companion object {
        const val TYPE_EVENT = 1
        const val TYPE_TRAFFIC = 2
        const val TYPE_RESCUE_PROGRESS = 3
        const val TYPE_RESCUE_PAY = 4
    }

    override fun getItemViewType(position: Int): Int {
        val subType = mDatas[position].subtype
        if (!TextUtils.isEmpty(subType)) {
            if (subType == SubscribeMDL.SubType.TrafficJam.code)
                return TYPE_TRAFFIC
            if (subType == SubscribeMDL.SubType.RescuePay.code)
                return TYPE_RESCUE_PAY
            if (subType == SubscribeMDL.SubType.RescueProgress.code)
                return TYPE_RESCUE_PROGRESS
        }
        return TYPE_EVENT
    }

    override fun bindView(viewType: Int): Int {
        return when (viewType) {
            TYPE_TRAFFIC -> R.layout.item_user_subscribe_trafficjam
            TYPE_RESCUE_PROGRESS -> R.layout.item_user_subscribe_rescueprogress
            TYPE_RESCUE_PAY -> R.layout.item_user_subscribe_rescuepay
            else -> R.layout.item_user_subscribe_event
        }
    }

    override fun onBindHoder(holder: RecyclerHolder, t: SubscribeMDL, position: Int) {
        val itemType = holder.itemViewType
        when (itemType) {
            TYPE_TRAFFIC -> convertTraffic(holder, t)
            TYPE_RESCUE_PROGRESS -> convertRescueProgress(holder, t)
            TYPE_RESCUE_PAY -> convertRescuePay(holder, t)
            else -> convertEvent(holder, t)
        }
    }

    /*事件类型*/
    private fun convertEvent(holder: BaseRecyclerAdapter.RecyclerHolder, t: SubscribeMDL) {
        holder.setImageResource(R.id.ivIcon, t.getIcon())
        holder.setText(R.id.tvEventName, t.eventtypename)
        holder.setText(R.id.tvTitle, t.roadtitle)
        holder.setText(R.id.tvContent, t.reportout)
        holder.setTypeface(R.id.tvOccTime, typeface)
        holder.setTypeface(R.id.tvUpdateTime, typeface)
        holder.setTypeface(R.id.tvEndTime, typeface)
        if (TextUtils.isEmpty(t.getOccTime())) {
            holder.setText(R.id.tvOccTime, "--")
        } else {
            holder.setText(R.id.tvOccTime, t.getOccTime())
        }
        if (TextUtils.isEmpty(t.getUpdateTime())) {
            holder.setText(R.id.tvUpdateTime, "--")
        } else {
            holder.setText(R.id.tvUpdateTime, t.getUpdateTime())
        }
        if (t.getSubType() == SubscribeMDL.SubType.Planned.code) {
            holder.setText(R.id.tvEndTimeTips, context.resources.getString(R.string.usersubscribe_planEndTime))
        } else {
            holder.setText(R.id.tvEndTimeTips, context.resources.getString(R.string.usersubscribe_endTime))
        }
        holder.setText(R.id.tvEndTime, t.getRealoverTime())
    }

    /*拥堵类型*/
    private fun convertTraffic(holder: BaseRecyclerAdapter.RecyclerHolder, t: SubscribeMDL) {
        holder.setImageResource(R.id.ivIcon, R.mipmap.ic_menu_event_yd_p)
        holder.setText(R.id.tvEventName, t.eventstatus)
        if (TextUtils.isEmpty(t.statusname)) {
            holder.setVisibility(R.id.tvStatus, false)
        } else {
            holder.setVisibility(R.id.tvStatus, true)
            holder.setText(R.id.tvStatus, t.statusname)
            holder.setBackgroundColor(R.id.tvStatus, t.getStatusColor(context))
        }
        holder.setText(R.id.tvUpdateTime, t.getUpdateTime())
        holder.setText(R.id.tvTitle, t.roadtitle)
        holder.setText(R.id.tvContent, t.content)
        holder.setTypeface(R.id.tvOccTime, typeface)
        holder.setTypeface(R.id.tvJamSpeed, typeface)
        holder.setTypeface(R.id.tvDistance, typeface)
        holder.setTypeface(R.id.tvDuration, typeface)
        if (TextUtils.isEmpty(t.getPubTime())) {
            holder.setText(R.id.tvOccTime, "--")
        } else {
            holder.setText(R.id.tvOccTime, t.getPubTime())
        }
        var jamSpeed = ""
        t.jamspeed?.let { jamSpeed += it }
        jamSpeed += "km/h"
        holder.setText(R.id.tvJamSpeed, SpannableString(jamSpeed).apply { setSpan(AbsoluteSizeSpan(18, true), 0, jamSpeed.indexOf("k"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) })
        var distance = ""
        t.jamdist?.let { distance += it }
        distance += "km"
        holder.setText(R.id.tvDistance, SpannableString(distance).apply { setSpan(AbsoluteSizeSpan(18, true), 0, distance.indexOf("k"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) })
        holder.setText(R.id.tvDuration, t.getLongTime())
    }

    /*救援进展类型*/
    private fun convertRescueProgress(holder: RecyclerHolder, t: SubscribeMDL) {
        holder.setText(R.id.tvTips, "救援进展")
        holder.setText(R.id.tvTime, t.getCreateTime())
        holder.setText(R.id.tvRoadname, t.roadname)
        holder.setText(R.id.tvContent, t.content)
        holder.setTypeface(R.id.tvStartTime, typeface)
        holder.setTypeface(R.id.tvArriveTime, typeface)
        holder.setTypeface(R.id.tvOverTime, typeface)
        if (TextUtils.isEmpty(t.getStartTime())) {
            holder.setText(R.id.tvStartTime, "--")
        } else {
            holder.setText(R.id.tvStartTime, t.getStartTime())
        }
        if (TextUtils.isEmpty(t.getArriveTime())) {
            holder.setText(R.id.tvArriveTime, "--")
        } else {
            holder.setText(R.id.tvArriveTime, t.getArriveTime())
        }
        if (TextUtils.isEmpty(t.overtime)) {
            holder.setText(R.id.tvOverTime, "--")
        } else {
            holder.setText(R.id.tvOverTime, t.getOverTime())
        }
    }

    /*救援资费类型*/
    private fun convertRescuePay(holder: RecyclerHolder, t: SubscribeMDL) {
        holder.setText(R.id.tvTips, "救援资费尚未支付")
        holder.setText(R.id.tvTime, t.getCreateTime())
        holder.setText(R.id.tvMsg, t.msg)
    }
}