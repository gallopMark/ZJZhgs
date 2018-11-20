package com.uroad.zhgs.fragment

import android.os.Bundle
import android.view.View
import com.amap.api.col.sln3.va
import com.uroad.zhgs.activity.LocationWebViewActivity
import com.uroad.zhgs.adapteRv.ServiceAreaAdapter
import com.uroad.zhgs.common.BaseRefreshRvFragment
import com.uroad.zhgs.model.ServiceAreaMDL
import com.uroad.zhgs.model.ServiceMDL
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService

/**
 *Created by MFB on 2018/8/21.
 */
class ServiceAreaFragment : BaseRefreshRvFragment() {
    private var index = 1
    private val size = 10
    private var isFirstLoad = true
    private val mDatas = ArrayList<ServiceAreaMDL>()
    private lateinit var adapter: ServiceAreaAdapter
    private var onItemOpenCloseListener: OnItemOpenCloseListener? = null
    override fun initViewData() {
        adapter = ServiceAreaAdapter(context, mDatas)
        recyclerView.adapter = adapter
        adapter.setOnItemOptionListener(object : ServiceAreaAdapter.OnItemOptionListener {
            override fun itemOpenClose(position: Int, isOpen: Boolean) {
                onItemOpenCloseListener?.onItemOpenClose(position, mDatas[position].getServiceList(), isOpen)
            }

            override fun itemClick(position: Int, service: ServiceMDL) {
                val url = "http://zhgs.u-road.com/ZJAppView/serviceDetailTest.html?dataid=331101"
                openActivity(LocationWebViewActivity::class.java, Bundle().apply {
                    putString(LocationWebViewActivity.WEB_URL, url)
                    putString(LocationWebViewActivity.WEB_TITLE, service.name)
                    putBoolean("isService", true)
                })
            }
        })
    }

    override fun initData() {
        doRequest(WebApiService.SERVICE_LIST, WebApiService.serviceListParams(index, size, ""), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                if (isFirstLoad) setPageLoading()
            }

            override fun onSuccess(data: String?) {
                finishLoad()
                if (isFirstLoad) isFirstLoad = false
                finishLoad()
                setPageEndLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdLs = GsonUtils.fromDataToList(data, ServiceAreaMDL::class.java)
                    updateData(mdLs)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                finishLoad()
                if (isFirstLoad) {
                    setPageError()
                } else
                    onHttpError(e)
            }
        })
    }

    private fun updateData(mdLs: MutableList<ServiceAreaMDL>) {
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
        } else index += 1
    }

    override fun pullToRefresh() {
        index = 1
        initData()
    }

    override fun pullToLoadMore() {
        initData()
    }

    override fun onReLoad(view: View) {
        initData()
    }

    interface OnItemOpenCloseListener {
        fun onItemOpenClose(position: Int, serviceList: MutableList<ServiceMDL>, isOpen: Boolean)
    }

    fun setOnItemOpenCloseListener(onItemOpenCloseListener: OnItemOpenCloseListener) {
        this.onItemOpenCloseListener = onItemOpenCloseListener
    }
}