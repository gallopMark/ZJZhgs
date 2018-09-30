package com.uroad.zhgs.activity

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import com.uroad.zhgs.R
import com.uroad.zhgs.adapteRv.HighwayListAdapter
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.helper.HighwaySearchHelper
import com.uroad.zhgs.model.HighwayMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.utils.InputMethodUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_highway_search.*

/**
 *Created by MFB on 2018/8/14.
 * 高速搜索
 */
class HighwaySearchActivity : BaseActivity() {
    private inner class HistoryAdapter(private val context: Context, mDatas: MutableList<String>)
        : BaseArrayRecyclerAdapter<String>(context, mDatas) {
        override fun onBindHoder(holder: RecyclerHolder, t: String, position: Int) {
            holder.setText(R.id.tvContent, t)
            holder.setOnClickListener(R.id.ivDelete, View.OnClickListener {
                HighwaySearchHelper.deleteItem(context, t)
                mDatas.remove(t)
                notifyDataSetChanged()
                if (mDatas.size == 0) {
                    llHirstoryData.visibility = View.GONE
                    tvEmptyHis.visibility = View.VISIBLE
                }
            })
            holder.itemView.setOnClickListener {
                etContent.setText(t)
                etContent.setSelection(etContent.text.length)
                keyword = etContent.text.toString()
                search()
            }
        }

        override fun bindView(viewType: Int): Int {
            return R.layout.item_search_history
        }
    }

    private lateinit var data: MutableList<String>
    private lateinit var historyAdapter: HistoryAdapter
    private val mDatas = ArrayList<HighwayMDL>()
    private lateinit var adapter: HighwayListAdapter
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    private var index = 1
    private val size = 10
    private var keyword = ""
    private var isOnClick = false

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_highway_search)
        intent.extras?.let {
            longitude = it.getDouble("longitude")
            latitude = it.getDouble("latitude")
        }
        ivBack.setOnClickListener { onBackPressed() }
        initHistoryRv()
        initRv()
        initSearch()
        tvClear.setOnClickListener {
            HighwaySearchHelper.clear(this@HighwaySearchActivity)
            data.clear()
            historyAdapter.notifyDataSetChanged()
            llHirstoryData.visibility = View.GONE
            tvEmptyHis.visibility = View.VISIBLE
        }
        refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(layout: RefreshLayout?) {
                if (TextUtils.isEmpty(keyword)) refreshLayout.finishRefresh()
                else {
                    index = 1
                    isOnClick = false
                    search()
                }
            }

            override fun onLoadMore(layout: RefreshLayout?) {
                isOnClick = false
                search()
            }
        })
    }

    private fun initHistoryRv() {
        rvHistory.layoutManager = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.VERTICAL }
        data = HighwaySearchHelper.getHistoryList(this)
        historyAdapter = HistoryAdapter(this, data)
        rvHistory.adapter = historyAdapter
        if (data.size > 0) {
            llHirstoryData.visibility = View.VISIBLE
            tvEmptyHis.visibility = View.GONE
        } else {
            llHirstoryData.visibility = View.GONE
            tvEmptyHis.visibility = View.VISIBLE
        }
    }

    private fun initRv() {
        recyclerView.layoutManager = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.VERTICAL }
        adapter = HighwayListAdapter(this, mDatas).apply {
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

    private fun initSearch() {
        ivSearch.setOnClickListener {
            keyword = etContent.text.toString()
            if (TextUtils.isEmpty(keyword.trim())) {
                showShortToast("请输入搜索内容")
            } else {
                index = 1
                isOnClick = true
                search()
            }
        }
    }

    private fun search() {
        InputMethodUtils.hideSoftInput(this, etContent)
        HighwaySearchHelper.saveContent(this@HighwaySearchActivity, keyword)
        if (llHirstory.visibility != View.GONE) llHirstory.visibility = View.GONE
        doRequest(WebApiService.HIGHWAY_LIST, WebApiService.highwayListParams(longitude,
                latitude, 2, keyword, index, size), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                if (isOnClick) showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                finishLoad()
                if (GsonUtils.isResultOk(data)) {
                    val mdls = GsonUtils.fromDataToList(data, HighwayMDL::class.java)
                    updateData(mdls)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                endLoading()
                finishLoad()
                onHttpError(e)
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
        if (mDatas.size > 0) {
            refreshLayout.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
        } else {
            refreshLayout.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        }
        index += 1
    }

    private fun finishLoad() {
        if (refreshLayout.isRefreshing) refreshLayout.finishRefresh()
        if (refreshLayout.isLoading) refreshLayout.finishLoadMore()
    }
}