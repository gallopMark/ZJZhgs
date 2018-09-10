package com.uroad.zhgs.common

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import com.uroad.zhgs.R

/**
 *Created by MFB on 2018/8/7.
 * viewpage 懒加载
 */
abstract class BasePageRefreshRvFragment : BasePageFragment() {
    open lateinit var refreshLayout: SmartRefreshLayout
    open lateinit var recyclerView: RecyclerView
    override fun setBaseLayoutResID(): Int {
        return R.layout.fragment_base_refreshrv
    }

    override fun setUp(view: View) {
        refreshLayout = view.findViewById(R.id.refreshLayout)
        recyclerView = view.findViewById(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
        recyclerView.layoutManager = layoutManager
        initViewData(view)
        refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout?) {
                pullToRefresh()
            }

            override fun onLoadMore(refreshLayout: RefreshLayout?) {
                pullToLoadMore()
            }
        })
    }

    abstract fun initViewData(view: View)

    abstract fun pullToRefresh()

    abstract fun pullToLoadMore()

    fun finishLoad() {
        if (refreshLayout.isRefreshing)
            refreshLayout.finishRefresh()
        if (refreshLayout.isLoading)
            refreshLayout.finishLoadMore()
    }
}