package com.uroad.zhgs.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.uroad.zhgs.activity.BindCarActivity
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BasePageRefreshRvFragment
import com.uroad.zhgs.enumeration.Carcategory
import com.uroad.zhgs.model.CarMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService

/**
 *Created by MFB on 2018/8/13.
 */
class MyCarFragment : BasePageRefreshRvFragment() {
    private var carType: String = Carcategory.COACH.code
    private val mDatas = ArrayList<CarMDL>()
    private lateinit var adapter: MyCarAdapter
    private var isFirstLoad = true
    private var clickIndex = -1
    private val requestCode = 0x0001

    override fun initViewData(view: View) {
        arguments?.getString("cartype")?.let { carType = it }
        refreshLayout.isEnableLoadMore = false
        adapter = MyCarAdapter(context, mDatas, carType)
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                if (position in 0 until mDatas.size) {
                    clickIndex = position
                    openActivityForResult(BindCarActivity::class.java, Bundle().apply {
                        putString("carId", mDatas[position].carid)
                        putString("code", carType)
                        putBoolean("carDetails", true)
                    }, requestCode)
                }
            }
        })
    }

    override fun initData() {
        doRequest(WebApiService.MYCAR, WebApiService.myCarParams(getUserId(), carType), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                if (isFirstLoad) setPageLoading()
            }

            override fun onSuccess(data: String?) {
                finishLoad()
                setPageEndLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdLs = GsonUtils.fromDataToList(data, CarMDL::class.java)
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

    private fun updateData(mdLs: MutableList<CarMDL>) {
        mDatas.clear()
        mDatas.addAll(mdLs)
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

    private class MyCarAdapter(context: Context, mDatas: MutableList<CarMDL>, private val carType: String)
        : BaseArrayRecyclerAdapter<CarMDL>(context, mDatas) {
        override fun onBindHoder(holder: RecyclerHolder, t: CarMDL, position: Int) {
            if (carType == Carcategory.COACH.code) {
                holder.setImageResource(R.id.ivIcon, R.mipmap.ic_car_coach)
            } else {
                holder.setImageResource(R.id.ivIcon, R.mipmap.ic_car_truck)
            }
            holder.setText(R.id.tvCarNo, t.carno)
            var lb = "车辆类型："
            t.carcategoryname?.let { lb += it }
            holder.setText(R.id.tvLeiBie, lb)
            var lx = "车辆类型："
            t.cartypename?.let { lx += it }
            holder.setText(R.id.tvLeiXing, lx)
            if (t.isdefault == 1) holder.setVisibility(R.id.tvDefault, true)
            else holder.setVisibility(R.id.tvDefault, false)
        }

        override fun bindView(viewType: Int): Int {
            return R.layout.item_car
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK) {
            val type = data?.getStringExtra("type")
            type?.let {
                if (it == "alert") {
                    pullToRefresh()
                } else {
                    mDatas.removeAt(clickIndex)
                    adapter.notifyDataSetChanged()
                    if (mDatas.size == 0) setPageNoData()
                }
            }
        }
    }
}