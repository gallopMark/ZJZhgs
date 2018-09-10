package com.uroad.zhgs.adapteRv

import android.content.Context
import android.text.TextUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.enumeration.MapDataType
import com.uroad.zhgs.model.EventMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/21.
 * 高速事件列表适配器
 */
class HighwayEventListAdapter(private val context: Context, mDatas: MutableList<EventMDL>)
    : BaseArrayRecyclerAdapter<EventMDL>(context, mDatas) {
    override fun bindView(viewType: Int): Int = R.layout.item_highway_event_detail
    override fun onBindHoder(holder: RecyclerHolder, t: EventMDL, position: Int) {
        holder.setImageResource(R.id.ivIcon, t.getIcon())
        holder.setText(R.id.tvEventName, t.eventtypename)
        holder.setText(R.id.tvTitle, t.roadtitle)
        holder.setText(R.id.tvContent, t.reportout)
        if (TextUtils.isEmpty(t.getOccTime())) {
            holder.setText(R.id.tvOccTime, "--")
        } else {
            holder.setText(R.id.tvOccTime, t.getOccTime())
        }
        if (t.eventtype == MapDataType.CONSTRUCTION.code) {
            holder.setText(R.id.tvEndTimeTips, context.getString(R.string.usersubscribe_planEndTime))
        } else {
            holder.setText(R.id.tvEndTimeTips, context.getString(R.string.usersubscribe_endTime))
        }
        holder.setText(R.id.tvEndTime, t.getRealoverTime())
        if (TextUtils.isEmpty(t.getUpdateTime())) {
            holder.setText(R.id.tvUpdateTime, "--")
        } else {
            holder.setText(R.id.tvUpdateTime, t.getUpdateTime())
        }
        if (position == itemCount - 1) {
            holder.setVisibility(R.id.vUnderLine, false)
        } else {
            holder.setVisibility(R.id.vUnderLine, true)
        }
    }
}