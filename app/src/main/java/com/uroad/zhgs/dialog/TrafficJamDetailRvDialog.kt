package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.zhgs.R
import com.uroad.zhgs.model.TrafficJamMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.utils.TypefaceUtils

class TrafficJamDetailRvDialog(private val context: Activity,
                               private val mdLs: MutableList<TrafficJamMDL>)
    : Dialog(context, R.style.transparentDialog) {

    private var adapter: TrafficJamAdapter? = null
    private var onViewClickListener: OnViewClickListener? = null

    fun setOnViewClickListener(onViewClickListener: OnViewClickListener?): TrafficJamDetailRvDialog {
        this.onViewClickListener = onViewClickListener
        return this
    }

    override fun show() {
        super.show()
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
            adapter = TrafficJamAdapter(context, mdLs)
            recyclerView.adapter = adapter
            ivClose.setOnClickListener { dismiss() }
            window.setContentView(contentView)
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setWindowAnimations(R.style.dialog_anim)
            window.setGravity(Gravity.BOTTOM)
        }
    }

    private inner class TrafficJamAdapter(context: Activity, mdLs: MutableList<TrafficJamMDL>)
        : BaseArrayRecyclerAdapter<TrafficJamMDL>(context, mdLs) {
        override fun bindView(viewType: Int): Int = R.layout.dialog_trafficjam_detail

        override fun onBindHoder(holder: RecyclerHolder, t: TrafficJamMDL, position: Int) {
            holder.setVisibility(R.id.ivClose, View.GONE)
            val tvUseful = holder.obtainView<TextView>(R.id.tvUseful)
            val tvUseless = holder.obtainView<TextView>(R.id.tvUseless)
            val tvSubscribe = holder.obtainView<TextView>(R.id.tvSubscribe)
            holder.setImageResource(R.id.ivIcon, R.mipmap.ic_menu_event_yd_p)
            holder.setText(R.id.tvEventName, "拥堵")
            holder.setText(R.id.tvTitle, t.roadtitle)
            holder.setText(R.id.tvContent, t.content)
            val typeface = TypefaceUtils.dinCondensed(context)
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
            val ts18 = context.resources.getDimensionPixelOffset(R.dimen.font_18)
            holder.setText(R.id.tvJamSpeed, SpannableString(jamSpeed).apply { setSpan(AbsoluteSizeSpan(ts18, false), 0, jamSpeed.indexOf("k"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) })
            var distance = ""
            t.jamdist?.let { distance += it }
            distance += "km"
            holder.setText(R.id.tvDistance, SpannableString(distance).apply { setSpan(AbsoluteSizeSpan(ts18, false), 0, distance.indexOf("k"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) })
            holder.setText(R.id.tvDuration, t.getLongTime(ts18, false))
            if (t.isuseful == 1) {
                tvUseful.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.mipmap.ic_useful_pressed), null, null, null)
            } else {
                tvUseful.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.mipmap.ic_useful_default), null, null, null)
            }
            if (t.isuseful == 2) {
                tvUseless.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.mipmap.ic_useless_pressed), null, null, null)
            } else {
                tvUseless.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.mipmap.ic_useless_default), null, null, null)
            }
            if (t.isuseful == 1 || t.isuseful == 2) {
                tvUseful.isEnabled = false
                tvUseless.isEnabled = false
            } else {
                tvUseful.setOnClickListener { onViewClickListener?.onViewClick(t, position, 1, this@TrafficJamDetailRvDialog) }
                tvUseless.setOnClickListener { onViewClickListener?.onViewClick(t, position, 2, this@TrafficJamDetailRvDialog) }
            }
            if (t.subscribestatus == 1) {
                tvSubscribe.text = context.resources.getString(R.string.usersubscribe_hasSubscribe)
                tvSubscribe.isEnabled = false
            } else {
                tvSubscribe.text = context.resources.getString(R.string.usersubscribe_subscribe)
                tvSubscribe.isEnabled = true
                tvSubscribe.setOnClickListener { onViewClickListener?.onViewClick(t, position, 3, this@TrafficJamDetailRvDialog) }
            }
        }
    }

    fun notifyItemChanged(position: Int, mdL: TrafficJamMDL) {
        mdLs[position] = mdL
        adapter?.notifyDataSetChanged()
    }

    interface OnViewClickListener {
        fun onViewClick(mdl: TrafficJamMDL, position: Int, type: Int, dialog: TrafficJamDetailRvDialog)
    }
}