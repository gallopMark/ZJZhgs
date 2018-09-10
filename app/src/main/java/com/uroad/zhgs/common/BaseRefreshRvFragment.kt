package com.uroad.zhgs.common

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import com.uroad.zhgs.R

/**
 *Created by MFB on 2018/7/25.
 */
abstract class BaseRefreshRvFragment : BaseFragment() {

    open lateinit var refreshLayout: SmartRefreshLayout
    open lateinit var recyclerView: RecyclerView
    override fun setBaseLayoutResID(): Int {
        return R.layout.fragment_base_refreshrv
    }

    override fun setUp(view: View, savedInstanceState: Bundle?) {
        refreshLayout = view.findViewById(R.id.refreshLayout)
        recyclerView = view.findViewById(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
        recyclerView.layoutManager = layoutManager
        initViewData()
        refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout?) {
                pullToRefresh()
            }

            override fun onLoadMore(refreshLayout: RefreshLayout?) {
                pullToLoadMore()
            }
        })
    }

    abstract fun initViewData()

    abstract fun pullToRefresh()

    abstract fun pullToLoadMore()
    fun finishLoad() {
        refreshLayout.finishRefresh()
        refreshLayout.finishLoadMore()
    }
}