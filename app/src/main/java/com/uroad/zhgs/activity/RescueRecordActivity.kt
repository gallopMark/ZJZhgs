package com.uroad.zhgs.activity

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.adapteRv.RescueRecordAdapter
import com.uroad.zhgs.common.BaseRefreshRvActivity
import com.uroad.zhgs.model.RescueItemMDL
import com.uroad.zhgs.photopicker.widget.RecycleViewDivider
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_base_refreshrv.*

/**
 *Created by MFB on 2018/8/4.
 * 救援记录
 */
class RescueRecordActivity : BaseRefreshRvActivity() {
    private val mDatas = ArrayList<RescueItemMDL>()
    private lateinit var adapter: RescueRecordAdapter
    private var isFirstInitData = true

    override fun initViewData() {
        withTitle(resources.getString(R.string.rescue_record_title))
        refreshLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.color_f7))
        recyclerView.addItemDecoration(RecycleViewDivider(this
                , LinearLayoutManager.VERTICAL
                , DisplayUtils.dip2px(this, 10f)
                , ContextCompat.getColor(this, R.color.color_f7)))
        adapter = RescueRecordAdapter(this, mDatas)
        recyclerView.adapter = adapter
        refreshLayout.isEnableLoadMore = false
        adapter.setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                if (position in 0 until mDatas.size) {
                    openActivity(RescueDetailActivity::class.java, Bundle().apply {
                        putString("rescueid", mDatas[position].rescueid)
                        putString("rescueno", mDatas[position].rescueno)
                    })
                }
            }
        })
        adapter.setOnItemChildClickListener(object : BaseRecyclerAdapter.OnItemChildClickListener {
            override fun onItemChildClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                if (position in 0 until mDatas.size) {
                    when (view.id) {
                        R.id.tvEvaluate -> {
                            openActivity(RescueEvaluateActivity::class.java, Bundle().apply { putString("rescueid", mDatas[position].rescueid) })
                        }
                        R.id.tvInvoice -> {
                            openActivity(InvoiceTitleActivity::class.java, Bundle().apply { putString("rescueid", mDatas[position].rescueid) })
                        }
                        R.id.tvPay -> {
                            openActivity(RescuePayActivity::class.java, Bundle().apply { putString("rescueid", mDatas[position].rescueid) })
                        }
                    }
                }
            }
        })
    }

    override fun initData() {
        doRequest(WebApiService.RESCUE_RECORD,
                WebApiService.rescueRecordParams(getUserId()),
                object : HttpRequestCallback<String>() {
                    override fun onPreExecute() {
                        if (isFirstInitData) setPageLoading()
                    }

                    override fun onSuccess(data: String?) {
                        setPageEndLoading()
                        finishLoad()
                        if (GsonUtils.isResultOk(data)) {
                            val mdLs = GsonUtils.fromDataToList(data, RescueItemMDL::class.java)
                            updateData(mdLs)
                        } else {
                            showShortToast(GsonUtils.getMsg(data))
                        }
                        if (isFirstInitData) isFirstInitData = false
                    }

                    override fun onFailure(e: Throwable, errorMsg: String?) {
                        setPageError()
                    }
                })
    }

    private fun updateData(list: MutableList<RescueItemMDL>) {
        mDatas.clear()
        mDatas.addAll(list)
        adapter.notifyDataSetChanged()
        if (mDatas.size == 0) setPageNoData()
    }

    override fun pullToRefresh() {
        initData()
    }

    override fun pullToLoadMore() {

    }

    override fun onReload(view: View) {
        initData()
    }
}