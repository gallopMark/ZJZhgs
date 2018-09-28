package com.uroad.zhgs.fragment

import android.os.Bundle
import android.view.View
import com.uroad.zhgs.activity.HighwayPreViewActivity
import com.uroad.zhgs.adapteRv.HighwayListAdapter
import com.uroad.zhgs.common.BasePageRefreshRvFragment
import com.uroad.zhgs.model.HighwayMDL
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService

/**
 *Created by MFB on 2018/8/16.
 * 高速列表
 */
class HighwayListFragment : BasePageRefreshRvFragment() {
    private val mDatas = ArrayList<HighwayMDL>()
    private lateinit var adapter: HighwayListAdapter
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    private var type: Int = 1
    private var index: Int = 1
    private val size: Int = 10
    private var isFirstLoad = true

    override fun initViewData(view: View) {
        arguments?.let {
            type = it.getInt("type")
            longitude = it.getDouble("longitude")
            latitude = it.getDouble("latitude")
        }
        recyclerView.isNestedScrollingEnabled = false
        adapter = HighwayListAdapter(context, mDatas).apply {
            setOnItemClickCallBack(object : HighwayListAdapter.OnItemClickCallBack {
                override fun callback(position: Int) {
                    openPreViewActivity(position)
                }
            })
            setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
                override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                    openPreViewActivity(position)
                }
            })
        }
        recyclerView.adapter = adapter
    }

    private fun openPreViewActivity(position: Int) {
        if (position in 0 until mDatas.size) {
            openActivity(HighwayPreViewActivity::class.java, Bundle().apply {
                putString("roadoldid", mDatas[position].roadoldid)
                putString("shortname", mDatas[position].shortname)
                putString("poiname", mDatas[position].poiname)
            })
        }
    }

    override fun initData() {
        doRequest(WebApiService.HIGHWAY_LIST, WebApiService.highwayListParams(longitude,
                latitude, type, "", index, size), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                if (isFirstLoad) setPageLoading()
            }

            override fun onSuccess(data: String?) {
                if (isFirstLoad) isFirstLoad = false
                finishLoad()
                setPageEndLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdls = GsonUtils.fromDataToList(data, HighwayMDL::class.java)
                    updateData(mdls)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
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

    private fun updateData(mdls: MutableList<HighwayMDL>) {
        if (index == 1) mDatas.clear()
        mDatas.addAll(mdls)
        adapter.notifyDataSetChanged()
        if (mdls.size < size) {
            refreshLayout.setNoMoreData(true)
        } else {
            refreshLayout.setNoMoreData(false)
        }
        if (index == 1 && mDatas.size == 0) {
            setPageNoData()
        }
        index += 1
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
}