package com.uroad.zhgs.fragment

import android.view.View
import com.uroad.zhgs.adapteRv.ServiceAdapter
import com.uroad.zhgs.common.BaseRefreshRvFragment
import com.uroad.zhgs.model.ServiceMDL
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService

/**
 *Created by MFB on 2018/8/21.
 */
class ServiceFragment : BaseRefreshRvFragment() {
    private var index = 1
    private val size = 10
    private var isFirstLoad = true
    private val mDatas = ArrayList<ServiceMDL>()
    private lateinit var adapter: ServiceAdapter
    private var keyword: String = ""
    private var onDataSetChangedListener: OnDataSetChangedListener? = null

    override fun initViewData() {
        arguments?.getString("keyword")?.let { keyword = it }
        adapter = ServiceAdapter(context, mDatas)
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                if (position in 0 until mDatas.size)
                    openLocationWebActivity(mDatas[position].detailurl, mDatas[position].name)
            }
        })
    }

    override fun initData() {
        doRequest(WebApiService.SERVICE_LIST, WebApiService.serviceListParams(index, size, keyword), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                if (isFirstLoad) setPageLoading()
            }

            override fun onSuccess(data: String?) {
                finishLoad()
                setPageEndLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdls = GsonUtils.fromDataToList(data, ServiceMDL::class.java)
                    updateData(mdls)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
                if (isFirstLoad) isFirstLoad = false
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

    private fun updateData(mdls: MutableList<ServiceMDL>) {
        if (index == 1) mDatas.clear()
        mDatas.addAll(mdls)
        adapter.notifyDataSetChanged()
        onDataSetChangedListener?.dataSetChanged(mdls)
        if (mdls.size < size) {
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

    interface OnDataSetChangedListener {
        fun dataSetChanged(mdls: MutableList<ServiceMDL>)
    }

    fun setOnDataSetChangedListener(onDataSetChangedListener: OnDataSetChangedListener) {
        this.onDataSetChangedListener = onDataSetChangedListener
    }
}