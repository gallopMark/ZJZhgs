package com.uroad.zhgs.fragment

import android.text.TextUtils
import android.view.View
import com.uroad.library.utils.PhoneUtils
import com.uroad.zhgs.adapteRv.HighwayHotlineAdapter
import com.uroad.zhgs.common.BasePageRefreshRvFragment
import com.uroad.zhgs.enumeration.PhoneType
import com.uroad.zhgs.model.HotLineMDL
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService

/**
 *Created by MFB on 2018/8/13.
 * //高速热线
 */
class HighwayHotlineFragment : BasePageRefreshRvFragment() {
    private var phonetype: String = PhoneType.EMERGENCY.code
    private val mDatas = ArrayList<HotLineMDL>()
    private lateinit var adapter: HighwayHotlineAdapter
    private var isFirstLoad = true

    override fun initViewData(view: View) {
        arguments?.getString("phonetype")?.let { phonetype = it }
        refreshLayout.isEnableLoadMore = false
        adapter = HighwayHotlineAdapter(context, mDatas)
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(object :BaseRecyclerAdapter.OnItemClickListener{
            override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                if (position in 0 until mDatas.size) {
                    if (!TextUtils.isEmpty(mDatas[position].phone)) {
                        PhoneUtils.call(context, mDatas[position].phone)
                    }
                }
            }
        })
    }

    override fun initData() {
        doRequest(WebApiService.HOT_LINE, WebApiService.hotLineParams(phonetype, ""), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                if (isFirstLoad) setPageLoading()
            }

            override fun onSuccess(data: String?) {
                if (isFirstLoad) isFirstLoad = false
                refreshLayout.finishRefresh()
                setPageEndLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdls = GsonUtils.fromDataToList(data, HotLineMDL::class.java)
                    updateData(mdls)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                refreshLayout.finishRefresh()
                if (isFirstLoad) setPageError()
                else {
                    onHttpError(e)
                }
            }
        })
    }

    private fun updateData(mdls: MutableList<HotLineMDL>) {
        mDatas.clear()
        mDatas.addAll(mdls)
        adapter.notifyDataSetChanged()
        if (mDatas.size == 0) {
            setPageNoData()
        }
    }

    override fun pullToRefresh() {
        initData()
    }

    override fun pullToLoadMore() {

    }

    override fun onReLoad(view: View) {
        initData()
    }
}