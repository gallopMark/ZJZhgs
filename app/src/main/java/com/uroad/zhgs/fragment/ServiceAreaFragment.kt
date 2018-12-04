package com.uroad.zhgs.fragment

import android.view.View
import com.amap.api.location.AMapLocation
import com.uroad.zhgs.adapteRv.ServiceAreaAdapter
import com.uroad.zhgs.common.BaseRefreshRvFragment
import com.uroad.zhgs.common.BaseRefreshRvLocationFragment
import com.uroad.zhgs.model.ServiceAreaMDL
import com.uroad.zhgs.model.ServiceMDL
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService

/**
 *Created by MFB on 2018/8/21.
 */
class ServiceAreaFragment : BaseRefreshRvLocationFragment() {
    private var longitude: Double = 0.toDouble()
    private var latitude: Double = 0.toDouble()
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
                openLocationWebActivity(service.detailurl, service.name)
            }
        })
        requestLocationPermissions(object : RequestLocationPermissionCallback {
            override fun doAfterGrand() {
                openLocation()
            }

            override fun doAfterDenied() {
                onRequestService()
            }
        })
    }

    override fun afterLocation(location: AMapLocation) {
        this.longitude = location.longitude
        this.latitude = location.latitude
        onRequestService()
        closeLocation()
    }

    override fun locationFailure() {
        onRequestService()
    }

    private fun onRequestService() {
        doRequest(WebApiService.SERVICE_LIST, WebApiService.serviceListParams(index, size, "",longitude, latitude), object : HttpRequestCallback<String>() {
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
        onRequestService()
    }

    override fun pullToLoadMore() {
        onRequestService()
    }

    override fun onReLoad(view: View) {
        onRequestService()
    }

    interface OnItemOpenCloseListener {
        fun onItemOpenClose(position: Int, serviceList: MutableList<ServiceMDL>, isOpen: Boolean)
    }

    fun setOnItemOpenCloseListener(onItemOpenCloseListener: OnItemOpenCloseListener) {
        this.onItemOpenCloseListener = onItemOpenCloseListener
    }
}