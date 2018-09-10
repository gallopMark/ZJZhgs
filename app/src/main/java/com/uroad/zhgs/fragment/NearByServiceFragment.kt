package com.uroad.zhgs.fragment

import android.view.View
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Poi
import com.uroad.zhgs.R
import com.uroad.zhgs.adapteRv.NearByService2Adapter
import com.uroad.zhgs.common.BasePageRefreshRvFragment
import com.uroad.zhgs.enumeration.MapDataType
import com.uroad.zhgs.model.ServiceMDL
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService

/**
 *Created by MFB on 2018/8/23.
 */
class NearByServiceFragment : BasePageRefreshRvFragment() {
    private var longitude: Double = 120.2
    private var latitude: Double = 30.3
    private var isFirstLoad = true
    private val mDatas = ArrayList<ServiceMDL>()
    private lateinit var adapter: NearByService2Adapter

    override fun initViewData(view: View) {
//        arguments?.let {
//            longitude = it.getDouble("longitude")
//            latitude = it.getDouble("latitude")
//        }
        refreshLayout.isEnableLoadMore = false
        adapter = NearByService2Adapter(context, mDatas)
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                if (position in 0 until mDatas.size) {
                    openLocationWebActivity(mDatas[position].detailurl, mDatas[position].name)
                }
            }
        })
        adapter.setOnItemChildClickListener(object : BaseRecyclerAdapter.OnItemChildClickListener {
            override fun onItemChildClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                when (view.id) {
                    R.id.ivNav -> {
                        if (position in 0 until mDatas.size) {   //进入导航页面
                            var poiName = ""
                            mDatas[position].name?.let { poiName = it }
                            val end = Poi(poiName, LatLng(mDatas[position].latitude(), mDatas[position].longitude()), "")
                            openNaviPage(null, end)
                        }
                    }
                }
            }
        })
    }

    override fun initData() {
        doRequest(WebApiService.MAP_DATA, WebApiService.mapDataByTypeParams(MapDataType.SERVICE_AREA.code,
                longitude, latitude, "", ""), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                if (isFirstLoad) setPageLoading()
            }

            override fun onSuccess(data: String?) {
                finishLoad()
                setPageEndLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdLs = GsonUtils.fromDataToList(data, ServiceMDL::class.java)
                    updateData(mdLs)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
                if (isFirstLoad) isFirstLoad = false
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                finishLoad()
                if (isFirstLoad) setPageError()
                else {
                    onHttpError(e)
                }
            }
        })
    }

    private fun updateData(mdLs: MutableList<ServiceMDL>) {
        mDatas.clear()
        mDatas.addAll(mdLs)
        adapter.notifyDataSetChanged()
        if (mDatas.size == 0) {
            setPageNoData()
        }
    }

    override fun onReLoad(view: View) {
        initData()
    }

    override fun pullToRefresh() {
        initData()
    }

    override fun pullToLoadMore() {
    }

}