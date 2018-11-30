package com.uroad.zhgs.activity

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.adapteRv.MyCarAdapter
import com.uroad.zhgs.common.BaseRefreshRvActivity
import com.uroad.zhgs.model.CarMDL
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import com.uroad.zhgs.widget.CurrencyLoadView
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_base_refreshrv.*

/**
 *Created by MFB on 2018/9/2.
 */
class MyCar2Activity : BaseRefreshRvActivity() {
    private val mDatas = ArrayList<CarMDL>()
    private lateinit var adapter: MyCarAdapter
    private val requestAdd = 0x0000
    private val requestEdit = 0x0001
    private var clickIndex = -1
    private lateinit var cLoadView: CurrencyLoadView

    override fun initViewData() {
        withTitle(resources.getString(R.string.mycar_title))
        val imageView = ImageView(this).apply { setImageResource(R.mipmap.ic_add_car) }
        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.END or Gravity.BOTTOM }
        val margin = DisplayUtils.dip2px(this, 15f)
        params.setMargins(margin, margin, margin, margin)
        imageView.layoutParams = params
        baseContent.addView(imageView)
        cLoadView = CurrencyLoadView(this)
        cLoadView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.CENTER }
        baseContent.addView(cLoadView)
        imageView.setOnClickListener { openActivityForResult(BindCarActivity::class.java, requestAdd) }
        refreshLayout.isEnableLoadMore = false
        adapter = MyCarAdapter(this, mDatas)
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                if (position in 0 until mDatas.size) {
                    clickIndex = position
                    openActivityForResult(BindCarActivity::class.java, Bundle().apply {
                        putString("carId", mDatas[position].carid)
                        putString("code", mDatas[position].carcategory)
                        putBoolean("carDetails", true)
                    }, requestEdit)
                }
            }
        })
    }

    override fun initData() {
        doRequest(WebApiService.MYCAR, WebApiService.myCarParams(getUserId(), ""), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                finishLoad()
                if (GsonUtils.isResultOk(data)) {
                    val mdLs = GsonUtils.fromDataToList(data, CarMDL::class.java)
                    updateData(mdLs)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                finishLoad()
                onHttpError(e)
            }
        })
    }

    private fun updateData(mdLs: MutableList<CarMDL>) {
        mDatas.clear()
        mDatas.addAll(mdLs)
        adapter.notifyDataSetChanged()
        if (mDatas.size == 0) {
            onPageNoData()
        } else {
            cLoadView.setState(CurrencyLoadView.STATE_GONE)
            refreshLayout.visibility = View.VISIBLE
        }
    }

    private fun onPageNoData() {
        refreshLayout.visibility = View.GONE
        cLoadView.setState(CurrencyLoadView.STATE_EMPTY)
        cLoadView.setEmptyText(getString(R.string.empty_my_cars))
    }

    override fun pullToRefresh() {
        initData()
    }

    override fun pullToLoadMore() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            requestAdd -> {
                if (resultCode == RESULT_OK) {
                    cLoadView.setState(CurrencyLoadView.STATE_GONE)
                    refreshLayout.visibility = View.VISIBLE
                    pullToRefresh()
                }
            }
            requestEdit -> {
                if (resultCode == RESULT_OK) {
                    val type = data?.getStringExtra("type")
                    if (type == "alert") {
                        cLoadView.setState(CurrencyLoadView.STATE_GONE)
                        refreshLayout.visibility = View.VISIBLE
                        pullToRefresh()
                    } else {
                        mDatas.removeAt(clickIndex)
                        adapter.notifyDataSetChanged()
                        if (mDatas.size == 0) onPageNoData()
                    }
                }
            }
        }
    }
}