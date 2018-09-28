package com.uroad.zhgs.adapteRv

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.model.HighwayMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.rv.BaseRecyclerAdapter

/**
 *Created by MFB on 2018/8/16.
 * 高速列表适配器
 */
class HighwayListAdapter(private val context: Activity, mDatas: MutableList<HighwayMDL>)
    : BaseArrayRecyclerAdapter<HighwayMDL>(context, mDatas) {
    private var onItemClickCallBack: OnItemClickCallBack? = null
    fun setOnItemClickCallBack(onItemClickCallBack: OnItemClickCallBack) {
        this.onItemClickCallBack = onItemClickCallBack
    }

    override fun bindView(viewType: Int): Int {
        return R.layout.item_highway_list
    }

    override fun onBindHoder(holder: RecyclerHolder, t: HighwayMDL, position: Int) {
        val pos = position
        val ivIcon = holder.obtainView<ImageView>(R.id.ivIcon)
        ImageLoaderV4.getInstance().displayImage(context, t.picurl, ivIcon, ContextCompat.getColor(context, R.color.white))
        holder.setText(R.id.tvShortname, t.shortname)
        holder.setText(R.id.tvPoiname, t.poiname)
        val rvEvent = holder.obtainView<RecyclerView>(R.id.rvEvent)
        rvEvent.isNestedScrollingEnabled = false
        rvEvent.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.HORIZONTAL }
        if (t.getEventList().size > 0) {
            rvEvent.visibility = View.VISIBLE
        } else {
            rvEvent.visibility = View.GONE
        }
        rvEvent.adapter = EventNumAdapter(context, t.getEventList()).apply {
            setOnItemClickListener(object : OnItemClickListener {
                override fun onItemClick(adapter: BaseRecyclerAdapter, holder: RecyclerHolder, view: View, position: Int) {
                    onItemClickCallBack?.callback(pos)
                }
            })
        }
        val rvSite = holder.obtainView<RecyclerView>(R.id.rvSite)
        rvSite.isNestedScrollingEnabled = false
        rvSite.layoutManager = object : LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) {
            override fun canScrollHorizontally(): Boolean = false
        }
        rvSite.adapter = SiteAdapter(context, t.getRoadUp()).apply {
            setOnItemClickListener(object : OnItemClickListener {
                override fun onItemClick(adapter: BaseRecyclerAdapter, holder: RecyclerHolder, view: View, position: Int) {
                    onItemClickCallBack?.callback(pos)
                }
            })
        }
        val rvUp = holder.obtainView<RecyclerView>(R.id.rvUp)
        rvUp.isNestedScrollingEnabled = false
        rvUp.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.HORIZONTAL }
        rvUp.adapter = SiteAdapter.ColorAdapter(context, t.getRoadUp()).apply {
            setOnItemClickListener(object : OnItemClickListener {
                override fun onItemClick(adapter: BaseRecyclerAdapter, holder: RecyclerHolder, view: View, position: Int) {
                    onItemClickCallBack?.callback(pos)
                }
            })
        }
        val rvDown = holder.obtainView<RecyclerView>(R.id.rvDown)
        rvDown.isNestedScrollingEnabled = false
        rvDown.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.HORIZONTAL }
        rvDown.adapter = SiteAdapter.ColorAdapter(context, t.getRoadDown()).apply {
            setOnItemClickListener(object : OnItemClickListener {
                override fun onItemClick(adapter: BaseRecyclerAdapter, holder: RecyclerHolder, view: View, position: Int) {
                    onItemClickCallBack?.callback(pos)
                }
            })
        }
        holder.bindChildClick(R.id.flViewDetail)
    }

    class EventNumAdapter(context: Context, mDatas: MutableList<HighwayMDL.EventNum>) :
            BaseArrayRecyclerAdapter<HighwayMDL.EventNum>(context, mDatas) {
        override fun bindView(viewType: Int): Int {
            return R.layout.item_highway_eventnum
        }

        override fun onBindHoder(holder: RecyclerHolder, t: HighwayMDL.EventNum, position: Int) {
            holder.setImageResource(R.id.ivIcon, t.getIcon())
            holder.setText(R.id.tvCount, "${t.num}")
        }
    }

    class SiteAdapter(context: Activity, mDatas: MutableList<HighwayMDL.State>)
        : BaseArrayRecyclerAdapter<HighwayMDL.State>(context, mDatas) {
        var width: Int
        private val dp5 = DisplayUtils.dip2px(context, 5f)
        private var lastName = ""

        init {
            var count = 0
            var content = ""
            for (item in mDatas) {
                if (item.isshow == 1) {
                    count++
                    item.name?.let {
                        content += it
                        lastName = it
                    }
                }
            }
            val paint = Paint().apply { textSize = context.resources.getDimension(R.dimen.font_12) }
            val w = (DisplayUtils.getWindowWidth(context) - dp5 * 2) - paint.measureText(content) - dp5 * (count - 1)
            width = if (w > 0) (w / (mDatas.size - count)).toInt()
            else DisplayUtils.dip2px(context, 2f)
        }

        override fun bindView(viewType: Int): Int {
            return R.layout.item_highway_site
        }

        override fun onBindHoder(holder: RecyclerHolder, t: HighwayMDL.State, position: Int) {
            val ivIcon = holder.obtainView<ImageView>(R.id.ivIcon)
            val tvText = holder.obtainView<TextView>(R.id.tvText)
            val params0 = ivIcon.layoutParams as LinearLayout.LayoutParams
            holder.setLayoutParams(R.id.ivIcon, params0)
            when (position) {
                0 -> params0.gravity = Gravity.START
                itemCount - 1 -> params0.gravity = Gravity.END
                else -> params0.gravity = Gravity.CENTER
            }
            ivIcon.layoutParams = params0
            if (t.isHinge()) {
                ivIcon.setImageResource(R.mipmap.ic_highway_interflow)
            } else {
                ivIcon.setImageResource(R.mipmap.ic_highway_site)
            }
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            if (TextUtils.equals(t.name, lastName)) {
                if (t.isshow == 1) {
                    tvText.text = t.name
                    ivIcon.visibility = View.VISIBLE
                } else {
                    tvText.text = ""
                    ivIcon.visibility = View.INVISIBLE
                }
                params.width = LinearLayout.LayoutParams.WRAP_CONTENT
                params.rightMargin = 0
            } else {
                if (t.isshow == 1) {
                    tvText.text = t.name
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
            holder.itemView.layoutParams = params
        }

        class ColorAdapter(private val context: Activity, mDatas: MutableList<HighwayMDL.State>)
            : BaseArrayRecyclerAdapter<HighwayMDL.State>(context, mDatas) {
            private val size = DisplayUtils.getWindowWidth(context) / mDatas.size

            override fun bindView(viewType: Int): Int {
                return R.layout.item_highway_linecolor
            }

            override fun onBindHoder(holder: RecyclerHolder, t: HighwayMDL.State, position: Int) {
                holder.itemView.layoutParams = holder.itemView.layoutParams.apply { width = size }
                holder.setBackgroundColor(R.id.vColor, t.getColor(context))
            }
        }
    }

    interface OnItemClickCallBack {
        fun callback(position: Int)
    }
}