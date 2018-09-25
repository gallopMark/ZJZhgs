package com.uroad.zhgs.adapteRv

import android.app.Activity
import android.support.v4.util.ArrayMap
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.zhgs.R
import com.uroad.zhgs.model.ServiceAreaMDL
import com.uroad.zhgs.model.ServiceMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.rv.BaseRecyclerAdapter

/**
 *Created by MFB on 2018/8/21.
 */
class ServiceAreaAdapter(private val context: Activity, mDatas: MutableList<ServiceAreaMDL>)
    : BaseArrayRecyclerAdapter<ServiceAreaMDL>(context, mDatas) {
    private val map = ArrayMap<Int, Boolean>()
    private var onItemOptionListener: OnItemOptionListener? = null

    fun clear() {
        map.clear()
    }

    override fun bindView(viewType: Int): Int = R.layout.item_servicearea

    override fun onBindHoder(holder: RecyclerHolder, t: ServiceAreaMDL, position: Int) {
        val ivIcon = holder.obtainView<ImageView>(R.id.ivIcon)
        ImageLoaderV4.getInstance().displayImage(context, t.picurl, ivIcon)
        holder.setText(R.id.tvShortname, t.shortname)
        holder.setText(R.id.tvCount, "${t.getServiceList().size}")
        val recyclerView = holder.obtainView<RecyclerView>(R.id.recyclerView)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
        val adapter = ServiceAdapter(context, t.getServiceList())
        adapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(adapter: BaseRecyclerAdapter, holder: RecyclerHolder, view: View, position: Int) {
                onItemOptionListener?.itemClick(position, t.getServiceList()[position])
            }
        })
        recyclerView.adapter = adapter
        if (map[position] != null && map[position] == true) {
            holder.setImageResource(R.id.ivArrow, R.mipmap.ic_arrow_down)
            recyclerView.visibility = View.VISIBLE
        } else {
            holder.setImageResource(R.id.ivArrow, R.mipmap.ic_arrow_right)
            recyclerView.visibility = View.GONE
        }
        holder.itemView.setOnClickListener {
            if (map[position] != null && map[position] == true) {
                holder.setImageResource(R.id.ivArrow, R.mipmap.ic_arrow_right)
                recyclerView.visibility = View.GONE
                map[position] = false
                onItemOptionListener?.itemOpenClose(position, false)
            } else {
                holder.setImageResource(R.id.ivArrow, R.mipmap.ic_arrow_down)
                recyclerView.visibility = View.VISIBLE
                map[position] = true
                onItemOptionListener?.itemOpenClose(position, true)
            }
        }
    }

    interface OnItemOptionListener {
        fun itemOpenClose(position: Int, isOpen: Boolean)
        fun itemClick(position: Int, service: ServiceMDL)
    }

    fun setOnItemOptionListener(onItemOptionListener: OnItemOptionListener) {
        this.onItemOptionListener = onItemOptionListener
    }
}