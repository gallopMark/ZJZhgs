package com.uroad.zhgs.common

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import com.uroad.zhgs.R
import kotlinx.android.synthetic.main.activity_base_refreshrv.*

/**
 *Created by MFB on 2018/7/26.
 */
abstract class BaseRefreshRvActivity : BaseActivity() {

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_base_refreshrv)
        val layoutManager = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.VERTICAL }
        recyclerView.layoutManager = layoutManager
        initViewData()
        refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                pullToRefresh()
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
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