package com.uroad.zhgs.fragment

import android.os.Bundle
import android.view.View
import com.uroad.zhgs.R
import com.uroad.zhgs.adapteRv.NewsAdapter
import com.uroad.zhgs.common.BasePageRefreshRvFragment
import com.uroad.zhgs.model.NewsMDL
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService

/**
 *Created by MFB on 2018/8/7.
 * 资讯
 */
class NewsFragment : BasePageRefreshRvFragment() {

    private var dictcode: String = ""
    private val mDatas = ArrayList<NewsMDL>()
    private lateinit var adapter: NewsAdapter
    private var isFirstInitData = true
    private var size = 10
    private var index = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString("dictcode")?.let { dictcode = it }
    }

    override fun initViewData(view: View) {
        adapter = NewsAdapter(context, mDatas)
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                if (position in 0 until mDatas.size) {
                    openWebActivity(mDatas[position].detailurl, resources.getString(R.string.news_detail_title))
                }
            }
        })
    }

    override fun initData() {
        doRequest(WebApiService.NEWS_LIST, WebApiService.newsListParams(dictcode, size, index),
                object : HttpRequestCallback<String>() {
                    override fun onPreExecute() {
                        if (isFirstInitData) setPageLoading()
                    }

                    override fun onSuccess(data: String?) {
                        finishLoad()
                        if (GsonUtils.isResultOk(data)) {
                            setPageEndLoading()
                            val dataMDLs = GsonUtils.fromDataToList(data, NewsMDL::class.java)
                            updateData(dataMDLs)
                        } else {
                            if (index == 1)
                                setPageError()
                            else showShortToast(GsonUtils.getMsg(data))
                        }
                        if (isFirstInitData) isFirstInitData = false
                    }

                    override fun onFailure(e: Throwable, errorMsg: String?) {
                        finishLoad()
                        if (isFirstInitData) setPageError()
                        else onHttpError(e)
                    }
                })
    }

    private fun updateData(mdLs: MutableList<NewsMDL>) {
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

    override fun onReLoad(view: View) {
        initData()
    }
}