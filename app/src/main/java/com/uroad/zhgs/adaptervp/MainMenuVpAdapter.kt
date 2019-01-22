package com.uroad.zhgs.adaptervp

import android.app.Activity
import android.support.v4.util.ArrayMap
import android.support.v4.view.PagerAdapter
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.uroad.zhgs.R
import com.uroad.zhgs.adapteRv.MainMenuAdapter
import com.uroad.zhgs.model.sys.MainMenuMDL
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.widget.GridSpacingItemDecoration

class MainMenuVpAdapter(private val context: Activity,
                        private val arrayMap: ArrayMap<Int, MutableList<MainMenuMDL>>) : PagerAdapter() {

    private var onPageItemClickListener: OnPageItemClickListener? = null
    fun setOnPageItemClickListener(onPageItemClickListener: OnPageItemClickListener) {
        this.onPageItemClickListener = onPageItemClickListener
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
    override fun getCount(): Int = arrayMap.size

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.item_main_menu_rv, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.isNestedScrollingEnabled = false
        val page = position
        val mdLs = arrayMap[page]
        val data = ArrayList<MainMenuMDL>()
        if (mdLs != null && mdLs.size > 0) {
            data.addAll(mdLs)
        }
        recyclerView.adapter = MainMenuAdapter(context, data).apply {
            setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
                override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                    if (position in 0 until data.size)
                        onPageItemClickListener?.onPageItemClick(page, position, data[position])
                }
            })
        }
        container.addView(recyclerView)
        return recyclerView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    interface OnPageItemClickListener {
        fun onPageItemClick(page: Int, itemPos: Int, mdl: MainMenuMDL)
    }
}