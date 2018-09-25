package com.uroad.zhgs.adapteRv

import android.app.Activity
import android.content.Context
import android.support.v4.content.ContextCompat
import android.widget.RelativeLayout
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.model.NewsTabMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/6.
 */
class NewsTabAdapter(context: Activity, mDatas: MutableList<NewsTabMDL.Type>)
    : BaseArrayRecyclerAdapter<NewsTabMDL.Type>(context, mDatas) {
    private val width = if (mDatas.size >= 4) DisplayUtils.getWindowWidth(context) / 4
    else DisplayUtils.getWindowHeight(context) / mDatas.size
    private val colorDefault = ContextCompat.getColor(context, R.color.gainsboro)
    private val colorSelected = ContextCompat.getColor(context, R.color.appTextColor)
    private var selectPos = 0
    private var onSelectedListener: OnSelectedListener? = null

    override fun onBindHoder(holder: RecyclerHolder, t: NewsTabMDL.Type, position: Int) {
        holder.itemView.layoutParams = RelativeLayout.LayoutParams(width, RelativeLayout.LayoutParams.WRAP_CONTENT)
        if (selectPos == position) {
            holder.setTextSize(R.id.tvTabName, 16f)
            holder.setTextColor(R.id.tvTabName, colorSelected)
            holder.setVisibility(R.id.vUnderLine, true)
        } else {
            holder.setTextSize(R.id.tvTabName, 14f)
            holder.setTextColor(R.id.tvTabName, colorDefault)
            holder.setVisibility(R.id.vUnderLine, false)
        }
        holder.setText(R.id.tvTabName, t.dictname)
        holder.itemView.setOnClickListener {
            setSelectPos(position)
            onSelectedListener?.onSelected(position)
        }
    }

    fun setSelectPos(position: Int) {
        this.selectPos = position
        notifyDataSetChanged()
    }

    override fun bindView(viewType: Int): Int {
        return R.layout.item_news_tab
    }

    interface OnSelectedListener {
        fun onSelected(position: Int)
    }

    fun setOnSelectedListener(onSelectedListener: OnSelectedListener) {
        this.onSelectedListener = onSelectedListener
    }
}