package com.uroad.zhgs.fragment

import android.view.View
import com.uroad.zhgs.adapteRv.HighwayEventListAdapter
import com.uroad.zhgs.common.BasePageRefreshRvFragment
import com.uroad.zhgs.model.EventMDL
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService

/**
 *Created by MFB on 2018/8/16.
 * 高速事件预览
 */
class HighwayEventFragment : BasePageRefreshRvFragment() {
    private val mDatas = ArrayList<EventMDL>()
    private lateinit var adapter: HighwayEventListAdapter
    private var index = 1
    private val size = 10
    private var isFirstLoad = true
    private var roadoldid: String = ""
    override fun initViewData(view: View) {
        arguments?.getString("roadoldid")?.let { roadoldid = it }
        adapter = HighwayEventListAdapter(context, mDatas)
        recyclerView.adapter = adapter
    }

    override fun initData() {
        doRequest(WebApiService.HIGHWAY_EVENT_LIST, WebApiService.eventListParams(roadoldid, "", "", index.toString(), size.toString()), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                if (isFirstLoad) setPageLoading()
            }

            override fun onSuccess(data: String?) {
                finishLoad()
                if (GsonUtils.isResultOk(data)) {
                    setPageEndLoading()
                    val dataMDLs = GsonUtils.fromDataToList(data, EventMDL::class.java)
                    updateData(dataMDLs)
                } else {
                    setPageError()
                }
                if (isFirstLoad) isFirstLoad = false
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                finishLoad()
                if (isFirstLoad) setPageError()
                else onHttpError(e)
            }
        })
    }

    private fun updateData(mdLs: MutableList<EventMDL>) {
        if (index == 1) mDatas.clear()
        mDatas.addAll(mdLs)
        adapter.notifyDataSetChanged()
        if (mdLs.size < size) {
            refreshLayout.setNoMoreData(true)
        } else {
            refreshLayout.setNoMoreData(false)
        }
        if (index == 1 && mDatas.size == 0) {
            setPageNoData()
        } else {
            index += 1
        }
    }

    override fun pullToRefresh() {
        index = 1
        initData()
    }

    override fun pullToLoadMore() {
        initData()
    }
}