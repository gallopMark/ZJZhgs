package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.enumeration.MapDataType
import com.uroad.zhgs.model.EventMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.rv.BaseRecyclerAdapter

/**
 *Created by MFB on 2018/9/5.
 */
class EventDetailRvDialog(private val context: Activity,
                          private val mDatas: MutableList<EventMDL>)
    : Dialog(context, R.style.transparentDialog) {

    private var onSubscribeListener: OnSubscribeListener? = null
    private var adapter: EventAdapter? = null

    fun setOnSubscribeListener(onSubscribeListener: OnSubscribeListener) {
        this.onSubscribeListener = onSubscribeListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        window?.let { window ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_mapdata_rv, LinearLayout(context), false)
            val ivClose = contentView.findViewById<ImageView>(R.id.ivClose)
            val recyclerView = contentView.findViewById<RecyclerView>(R.id.recyclerView)
            recyclerView.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.HORIZONTAL }
            val helper = PagerSnapHelper()
            helper.attachToRecyclerView(recyclerView)
            adapter = EventAdapter(context, mDatas).apply {
                setOnItemChildClickListener(object : BaseRecyclerAdapter.OnItemChildClickListener {
                    override fun onItemChildClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                        if (position in 0 until mDatas.size)
                            onSubscribeListener?.onSubscribe(mDatas[position], position)
                    }
                })
            }
            recyclerView.adapter = adapter
            ivClose.setOnClickListener { dismiss() }
            window.setContentView(contentView)
            window.setLayout(DisplayUtils.getWindowWidth(context), WindowManager.LayoutParams.WRAP_CONTENT)
            window.setWindowAnimations(R.style.dialog_anim)
            window.setGravity(Gravity.BOTTOM)
        }
    }

    fun notifyItemChanged(position: Int, mdL: EventMDL) {
        mDatas[position] = mdL
        adapter?.notifyDataSetChanged()
    }

    private inner class EventAdapter(private val context: Activity, mDatas: MutableList<EventMDL>)
        : BaseArrayRecyclerAdapter<EventMDL>(context, mDatas) {
        override fun bindView(viewType: Int) = R.layout.dialog_event_detail
        override fun onBindHoder(holder: RecyclerHolder, t: EventMDL, position: Int) {
            holder.setVisibility(R.id.ivClose, false)
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
                holder.setText(R.id.tvEndTimeTips, context.resources.getString(R.string.usersubscribe_planEndTime))
            } else {
                holder.setText(R.id.tvEndTimeTips, context.resources.getString(R.string.usersubscribe_endTime))
            }
            holder.setText(R.id.tvEndTime, t.getPlanOverTime())
            if (TextUtils.isEmpty(t.getUpdateTime())) {
                holder.setText(R.id.tvUpdateTime, "--")
            } else {
                holder.setText(R.id.tvUpdateTime, t.getUpdateTime())
            }
            if (t.subscribestatus == 1) {
                holder.setText(R.id.tvSubscribe, context.resources.getString(R.string.usersubscribe_hasSubscribe))
                holder.setEnabled(R.id.tvSubscribe, false)
            } else {
                holder.setText(R.id.tvSubscribe, context.resources.getString(R.string.usersubscribe_subscribe))
                holder.setEnabled(R.id.tvSubscribe, true)
            }
            holder.bindChildClick(R.id.tvSubscribe)
        }
    }

    interface OnSubscribeListener {
        fun onSubscribe(dataMDL: EventMDL, position: Int)
    }
}