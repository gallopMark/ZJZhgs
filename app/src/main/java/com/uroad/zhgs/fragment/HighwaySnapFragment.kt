package com.uroad.zhgs.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.text.TextUtils
import android.view.View
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.activity.VideoPlayerActivity
import com.uroad.zhgs.adapteRv.CCTVDataAdapter
import com.uroad.zhgs.common.BasePageRefreshRvFragment
import com.uroad.zhgs.model.RtmpMDL
import com.uroad.zhgs.model.SnapShotMDL
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import com.uroad.zhgs.widget.GridSpacingItemDecoration

/**
 *Created by MFB on 2018/8/16.
 * 高速快拍预览
 */
class HighwaySnapFragment : BasePageRefreshRvFragment() {

    private var roadoldid: String = ""
    private val mDatas = ArrayList<SnapShotMDL>()
    private lateinit var adapter: CCTVDataAdapter
    private var isFirstLoad = true
    override fun initViewData(view: View) {
        arguments?.let { roadoldid = it.getString("roadoldid") }
        refreshLayout.isEnableLoadMore = false
        recyclerView.addItemDecoration(GridSpacingItemDecoration(2, DisplayUtils.dip2px(context, 10f), true))
        recyclerView.layoutManager = GridLayoutManager(context, 2).apply { orientation = GridLayoutManager.VERTICAL }
        adapter = CCTVDataAdapter(context, mDatas)
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                if (position in 0 until mDatas.size) {
//                    val photos = ArrayList<String>().apply { for (item in mDatas) if (!TextUtils.isEmpty(item.getLastPicUrl())) add(item.getLastPicUrl()) }
//                    showBigPic(position, photos)
                    getRoadVideo(mDatas[position].resid, mDatas[position].shortname)
                }
            }
        })
    }

    /*获取快拍请求流地址*/
    private fun getRoadVideo(resId: String?, shortName: String?) {
        doRequest(WebApiService.ROAD_VIDEO, WebApiService.roadVideoParams(resId), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, RtmpMDL::class.java)
                    mdl?.rtmpIp?.let {
                        openActivityForResult(VideoPlayerActivity::class.java, Bundle().apply {
                            putBoolean("isLive", true)
                            putString("url", it)
                            putString("title", shortName)
                        }, 345)
                    }
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                endLoading()
                onHttpError(e)
            }
        })
    }

    override fun initData() {
        doRequest(WebApiService.CCTV_DATA, WebApiService.cctvDataParams(roadoldid, ""), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                if (isFirstLoad) setPageLoading()
            }

            override fun onSuccess(data: String?) {
                if (isFirstLoad) isFirstLoad = false
                finishLoad()
                setPageEndLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdls = GsonUtils.fromDataToList(data, SnapShotMDL::class.java)
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

    private fun updateData(mdls: MutableList<SnapShotMDL>) {
        mDatas.clear()
        mDatas.addAll(mdls)
        adapter.notifyDataSetChanged()
        if (mDatas.size == 0)
            setPageNoData()
    }

    override fun pullToRefresh() {
        initData()
    }

    override fun pullToLoadMore() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 345 && resultCode == Activity.RESULT_OK) {
            showLongToast("播放结束")
        }
    }
}