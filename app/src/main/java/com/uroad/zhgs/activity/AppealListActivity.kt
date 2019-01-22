package com.uroad.zhgs.activity

import android.os.Bundle
import android.view.View
import com.uroad.zhgs.R
import com.uroad.zhgs.adapteRv.AppealListAdapter
import com.uroad.zhgs.common.BaseRefreshRvActivity
import com.uroad.zhgs.model.AppealMDL
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_base_refreshrv.*

/**
 * 申诉列表页面
 */
class AppealListActivity : BaseRefreshRvActivity() {

    private var isPageLoading = false
    private lateinit var data: MutableList<AppealMDL>
    private lateinit var adapter: AppealListAdapter

    override fun initViewData() {
        withTitle(getString(R.string.appeal_list_title))
        refreshLayout.isEnableLoadMore = false
        data = ArrayList()
        adapter = AppealListAdapter(this, data).apply {
            setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
                override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                    if (position in 0 until data.size) {
                        openActivity(AppealDetailActivity::class.java, Bundle().apply { putString("json", GsonUtils.fromObjectToJson(data[position])) })
                    }
                }
            })
        }
        recyclerView.adapter = adapter
    }

    override fun pullToRefresh() {
        initData()
    }

    override fun pullToLoadMore() {
    }

    override fun initData() {
        doRequest(WebApiService.APPEAL_LIST, WebApiService.appealListParams(getPhone()), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                if (!isPageLoading) setPageLoading()
            }

            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    if (!isPageLoading) isPageLoading = true
                    setPageEndLoading()
                    finishLoad()
                    val mdLs = GsonUtils.fromDataToList(data, AppealMDL::class.java)
                    updateUI(mdLs)
                } else {
                    setPageError()
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                setPageError()
            }
        })
    }

    private fun updateUI(mdLs: MutableList<AppealMDL>) {
        data.clear()
        data.addAll(mdLs)
        adapter.notifyDataSetChanged()
        if (data.isEmpty()) setPageNoData(getString(R.string.appeal_empty_list))
    }
}