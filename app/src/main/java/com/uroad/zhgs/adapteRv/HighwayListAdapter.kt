package com.uroad.zhgs.adapteRv

import android.app.Activity
import android.graphics.Paint
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.model.HighwayMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import java.math.BigDecimal

/**
 *Created by MFB on 2018/8/16.
 * 高速列表适配器
 */
class HighwayListAdapter(private val context: Activity, mDatas: MutableList<HighwayMDL>)
    : BaseArrayRecyclerAdapter<HighwayMDL>(context, mDatas) {

    override fun bindView(viewType: Int): Int {
        return R.layout.item_highway_list
    }

    override fun onBindHoder(holder: RecyclerHolder, t: HighwayMDL, position: Int) {
        val llEvent = holder.obtainView<LinearLayout>(R.id.llEvent)
        val llUp = holder.obtainView<LinearLayout>(R.id.llUp)
        val llDown = holder.obtainView<LinearLayout>(R.id.llDown)
        val llSite = holder.obtainView<LinearLayout>(R.id.llSite)
        holder.displayImage(R.id.ivIcon, t.picurl, R.color.white)
        holder.setText(R.id.tvShortname, t.shortname)
        holder.setText(R.id.tvPoiname, t.poiname)
        llEvent.removeAllViews()
        llUp.removeAllViews()
        llDown.removeAllViews()
        llSite.removeAllViews()
        if (t.getEventList().size > 0) {
            llEvent.visibility = View.VISIBLE
            createEventView(llEvent, t.getEventList())
        } else {
            llEvent.visibility = View.GONE
        }
        createUpLine(llUp, t.getRoadUp())
        createDownLine(llDown, t.getRoadDown())
        createSite(llSite, t.getRoadUp())
    }

    private fun createEventView(llEvent: LinearLayout, eventList: MutableList<HighwayMDL.EventNum>) {
        for (item in eventList) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_highway_eventnum, llEvent, false)
            val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)
            val tvCount = view.findViewById<TextView>(R.id.tvCount)
            ivIcon.setImageResource(item.getIcon())
            tvCount.text = "${item.num}"
            llEvent.addView(view)
        }
    }

    private fun createUpLine(llUp: LinearLayout, up: MutableList<HighwayMDL.State>) {
        val width = (DisplayUtils.getWindowWidth(context) - DisplayUtils.dip2px(context, 10f)) / up.size
        val params = LinearLayout.LayoutParams(width, DisplayUtils.dip2px(context, 4f))
        for (item in up) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_highway_linecolor, llUp, false)
            view.layoutParams = params
            view.setBackgroundColor(item.getColor(context))
            llUp.addView(view)
        }
    }

    private fun createDownLine(llDown: LinearLayout, down: MutableList<HighwayMDL.State>) {
        val width = (DisplayUtils.getWindowWidth(context) - DisplayUtils.dip2px(context, 10f)) / down.size
        val params = LinearLayout.LayoutParams(width, DisplayUtils.dip2px(context, 4f))
        for (item in down) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_highway_linecolor, llDown, false)
            view.layoutParams = params
            view.setBackgroundColor(item.getColor(context))
            llDown.addView(view)
        }
    }

    private fun createSite(llSite: LinearLayout, roadUp: MutableList<HighwayMDL.State>) {
        val dp5 = DisplayUtils.dip2px(context, 5f)
        var lastName = ""
        var count = 0
        var content = ""
        for (item in roadUp) {
            if (item.isshow == 1) {
                count++
                item.name?.let {
                    content += it
                    lastName = it
                }
            }
        }
        val paint = Paint().apply { textSize = context.resources.getDimension(R.dimen.font_12) }
        val w = ((DisplayUtils.getWindowWidth(context) - dp5 * 2) - paint.measureText(content) - dp5 * (count - 1))
        val width = if (w > 0) Math.floor(w.toDouble() / (roadUp.size - count)).toInt()
        else DisplayUtils.dip2px(context, 2f)
        for (i in 0 until roadUp.size) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_highway_site, llSite, false)
            val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)
            val tvText = view.findViewById<TextView>(R.id.tvText)
            val params0 = ivIcon.layoutParams as LinearLayout.LayoutParams
            ivIcon.layoutParams = params0
            when {
                i == 0 -> params0.gravity = Gravity.START
                TextUtils.equals(roadUp[i].name, lastName) -> params0.gravity = Gravity.END
                else -> params0.gravity = Gravity.CENTER
            }
            if (roadUp[i].isHinge()) {
                ivIcon.setImageResource(R.mipmap.ic_highway_interflow)
            } else {
                ivIcon.setImageResource(R.mipmap.ic_highway_site)
            }
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            if (TextUtils.equals(roadUp[i].name, lastName)) {
                if (roadUp[i].isshow == 1) {
                    tvText.text = roadUp[i].name
                    ivIcon.visibility = View.VISIBLE
                } else {
                    tvText.text = ""
                    ivIcon.visibility = View.INVISIBLE
                }
                params.width = LinearLayout.LayoutParams.WRAP_CONTENT
                params.rightMargin = 0
            } else {
                if (roadUp[i].isshow == 1) {
                    tvText.text = roadUp[i].name
                    ivIcon.visibility = View.VISIBLE
                    params.rightMargin = dp5
                    params.width = LinearLayout.LayoutParams.WRAP_CONTENT
                } else {
                    tvText.text = ""
                    ivIcon.visibility = View.INVISIBLE
                    params.width = width
                    params.rightMargin = 0
                }
            }
            view.layoutParams = params
            llSite.addView(view)
        }
    }
}