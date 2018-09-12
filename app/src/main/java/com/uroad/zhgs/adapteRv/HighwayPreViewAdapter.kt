package com.uroad.zhgs.adapteRv

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.FrameLayout
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.model.HighwayPreViewMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/21.
 * 高速快览适配器
 */
class HighwayPreViewAdapter(private val context: Context, mDatas: MutableList<HighwayPreViewMDL.Traffic>)
    : BaseArrayRecyclerAdapter<HighwayPreViewMDL.Traffic>(context, mDatas) {
    private var onEventClickListener: OnEventClickListener? = null
    override fun bindView(viewType: Int): Int {
        return R.layout.item_highway_preview
    }

    override fun onBindHoder(holder: RecyclerHolder, t: HighwayPreViewMDL.Traffic, position: Int) {
        if (t.isHinge()) {
            holder.setImageResource(R.id.ivStatus, R.mipmap.ic_road_traffic_interflow)
            if (!TextUtils.isEmpty(t.getRoadName())) {
                holder.setText(R.id.tvRoadName, t.getRoadName())
                holder.setVisibility(R.id.tvRoadName, true)
            } else {
                holder.setVisibility(R.id.tvRoadName, false)
            }
        } else {
            if (t.isNormal()) holder.setImageResource(R.id.ivStatus, R.mipmap.ic_road_traffic_normal)
            else holder.setImageResource(R.id.ivStatus, R.mipmap.ic_road_traffic_close)
            holder.setVisibility(R.id.tvRoadName, false)
        }
        holder.setText(R.id.tvName, t.name)
        if (position in 1 until itemCount) {
            holder.setBackgroundColor(R.id.vColorTop, mDatas[position - 1].getColor(context))
        } else {
            holder.setBackgroundColor(R.id.vColorTop, ContextCompat.getColor(context, R.color.transparent))
        }
        holder.setBackgroundColor(R.id.vColor, t.getColor(context))
        val rvEvent = holder.obtainView<RecyclerView>(R.id.rvEvent)
        rvEvent.isNestedScrollingEnabled = false
        rvEvent.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.HORIZONTAL }
        rvEvent.adapter = EventAdapter(context, t.getEventList())
    }

    private inner class EventAdapter(context: Context, mDatas: MutableList<HighwayPreViewMDL.Traffic.Event>) :
            BaseArrayRecyclerAdapter<HighwayPreViewMDL.Traffic.Event>(context, mDatas) {
        override fun bindView(viewType: Int): Int = R.layout.item_highway_event_pic

        override fun onBindHoder(holder: RecyclerHolder, t: HighwayPreViewMDL.Traffic.Event, position: Int) {
            if (!TextUtils.isEmpty(t.cctvids)) {
                holder.setImageResource(R.id.ivIcon, t.cctvIco)
            } else {
                holder.setImageResource(R.id.ivIcon, t.getIcon())
            }
            holder.itemView.setOnClickListener {
                if (!TextUtils.isEmpty(t.cctvids))
                    onEventClickListener?.onCCTVClickListener(t.cctvids)
                else
                    onEventClickListener?.onEventClickListener(t.eventIds)
            }
        }
    }

    interface OnEventClickListener {
        fun onEventClickListener(eventIds: String)
        fun onCCTVClickListener(cctvIds: String)
    }

    fun setOnEventClickListener(onEventClickListener: OnEventClickListener) {
        this.onEventClickListener = onEventClickListener
    }
}