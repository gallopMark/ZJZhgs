package com.uroad.zhgs.activity

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import com.uroad.library.utils.PhoneUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.adapteRv.HighwayHotlineAdapter
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.helper.HighwayHotlineSearchHelper
import com.uroad.zhgs.model.HotLineMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.utils.InputMethodUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_highway_hotline_search.*

/**
 *Created by MFB on 2018/8/14.
 */
class HighwayHotlineSearchActivity : BaseActivity() {
    private inner class HistoryAdapter(private val context: Context, mDatas: MutableList<String>)
        : BaseArrayRecyclerAdapter<String>(context, mDatas) {
        override fun onBindHoder(holder: RecyclerHolder, t: String, position: Int) {
            holder.setText(R.id.tvContent, t)
            holder.setOnClickListener(R.id.ivDelete, View.OnClickListener {
                HighwayHotlineSearchHelper.deleteItem(context, t)
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
                onSearch(t)
            }
        }

        override fun bindView(viewType: Int): Int {
            return R.layout.item_search_history
        }
    }

    private lateinit var data: MutableList<String>
    private lateinit var historyAdapter: HistoryAdapter
    private val mDatas = ArrayList<HotLineMDL>()
    private lateinit var adapter: HighwayHotlineAdapter

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_highway_hotline_search)
        ivBack.setOnClickListener { onBackPressed() }
        initHistoryRv()
        initRv()
        initSearch()
        tvClear.setOnClickListener {
            HighwayHotlineSearchHelper.clear(this@HighwayHotlineSearchActivity)
            data.clear()
            historyAdapter.notifyDataSetChanged()
            llHirstoryData.visibility = View.GONE
            tvEmptyHis.visibility = View.VISIBLE
        }
    }

    private fun initHistoryRv() {
        rvHistory.layoutManager = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.VERTICAL }
        data = HighwayHotlineSearchHelper.getHistoryList(this)
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
        adapter = HighwayHotlineAdapter(this, mDatas)
        recyclerView.adapter = adapter
        adapter.setOnItemChildClickListener(object : BaseRecyclerAdapter.OnItemChildClickListener {
            override fun onItemChildClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                if (position in 0 until mDatas.size) {
                    if (!TextUtils.isEmpty(mDatas[position].phone)) {
                        PhoneUtils.call(this@HighwayHotlineSearchActivity, mDatas[position].phone)
                    }
                }
            }
        })
    }

    private fun initSearch() {
        ivSearch.setOnClickListener {
            val content = etContent.text.toString()
            if (TextUtils.isEmpty(content.trim())) {
                showShortToast("请输入搜索内容")
            } else {
                onSearch(content)
            }
        }
    }

    private fun onSearch(content: String) {
        InputMethodUtils.hideSoftInput(this, etContent)
        HighwayHotlineSearchHelper.saveContent(this@HighwayHotlineSearchActivity, content)
        if (llHirstory.visibility != View.GONE) llHirstory.visibility = View.GONE
        doRequest(WebApiService.HOT_LINE, WebApiService.hotLineParams("", content), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                setPageEndLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdls = GsonUtils.fromDataToList(data, HotLineMDL::class.java)
                    updateData(mdls)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                onHttpError(e)
            }
        })
    }

    private fun updateData(mdls: MutableList<HotLineMDL>) {
        mDatas.clear()
        mDatas.addAll(mdls)
        adapter.notifyDataSetChanged()
        if (mDatas.size > 0) {
            recyclerView.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
        } else {
            recyclerView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        }
    }
}