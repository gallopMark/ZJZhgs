package com.uroad.zhgs.fragment

import android.os.Bundle
import android.support.v4.util.ArrayMap
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.zhgs.R
import com.uroad.zhgs.activity.VideoPlayerActivity
import com.uroad.zhgs.adapteRv.HighwayPreViewAdapter
import com.uroad.zhgs.common.BasePageFragment
import com.uroad.zhgs.dialog.CCTVDetailPageDialog
import com.uroad.zhgs.dialog.EventDetailPageDialog
import com.uroad.zhgs.model.EventMDL
import com.uroad.zhgs.model.HighwayPreViewMDL
import com.uroad.zhgs.model.RtmpMDL
import com.uroad.zhgs.model.SnapShotMDL
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService

/**
 *Created by MFB on 2018/8/16.
 */
class HighwayPreviewFragment : BasePageFragment() {
    private lateinit var ivIcon: ImageView
    private lateinit var tvShortname: TextView
    private lateinit var tvPoiname: TextView
    private lateinit var tvChange: TextView
    private lateinit var recyclerView: RecyclerView
    private var roadoldid = ""
    private var direction = 1
    private var isFirstLoad = true
    private val mDatas = ArrayList<HighwayPreViewMDL.Traffic>()
    private lateinit var adapter: HighwayPreViewAdapter
    private val map = ArrayMap<Int, HighwayPreViewMDL>()

    override fun setBaseLayoutResID(): Int = R.layout.fragment_highway_preview

    override fun setUp(view: View) {
        arguments?.let { roadoldid = it.getString("roadoldid") }
        ivIcon = view.findViewById(R.id.ivIcon)
        tvShortname = view.findViewById(R.id.tvShortname)
        tvPoiname = view.findViewById(R.id.tvPoiname)
        tvChange = view.findViewById(R.id.tvChange)
        recyclerView = view.findViewById(R.id.recyclerView)
        tvPoiname.text = arguments?.getString("poiname")
        recyclerView.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
        adapter = HighwayPreViewAdapter(context, mDatas).apply {
            setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
                override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                    if (position in 0 until mDatas.size)
                        openLocationWebActivity(mDatas[position].detailurl, mDatas[position].name)
                }
            })
        }
        recyclerView.adapter = adapter
        adapter.setOnEventClickListener(object : HighwayPreViewAdapter.OnEventClickListener {
            override fun onEventClickListener(eventIds: String) {
                getEventDetailsById(eventIds)
            }

            override fun onCCTVClickListener(cctvIds: String) {
                getCCTVDetailsById(cctvIds)
            }
        })
        tvChange.setOnClickListener { _ ->
            val text = tvPoiname.text.split(" - ")
            if (text.size >= 2) {
                val name = "${text[1]} - ${text[0]}"
                tvPoiname.text = name
            }
            direction = if (direction == 1) {
                2
            } else {
                1
            }
            val mdl = map[direction]
            if (mdl == null) initData()
            else updateData(mdl)
        }
    }

    override fun initData() {
        doRequest(WebApiService.HIGHWAY_PREVIEW, WebApiService.roadTrafficParams(roadoldid, direction), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                if (isFirstLoad) setPageLoading()
            }

            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    setPageEndLoading()
                    val mdl = GsonUtils.fromDataBean(data, HighwayPreViewMDL::class.java)
                    if (mdl == null) showShortToast("数据解析异常")
                    else updateData(mdl)
                    map[direction] = mdl
                } else {
                    setPageError()
                }
                if (isFirstLoad) isFirstLoad = false
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                if (isFirstLoad) setPageError()
                else onHttpError(e)
            }
        })
    }

    private fun updateData(mdl: HighwayPreViewMDL) {
        tvShortname.text = mdl.shortname
        ImageLoaderV4.getInstance().displayImage(context, mdl.picurl, ivIcon)
        mDatas.clear()
        mdl.traffic?.let { mDatas.addAll(it) }
        adapter.notifyDataSetChanged()
    }

    override fun onReLoad(view: View) {
        initData()
    }

    private fun getEventDetailsById(eventIds: String) {
        doRequest(WebApiService.EVENT_DETAIL, WebApiService.eventDetailsByIdParams(eventIds, getUserId()), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdLs = GsonUtils.fromDataToList(data, EventMDL::class.java)
                    if (mdLs.size > 0) {
                        EventDetailPageDialog(context, mdLs).show()
                    } else {
                        showShortToast("暂无相关数据~")
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

    private fun getCCTVDetailsById(cctvIds: String) {
        doRequest(WebApiService.CCTV_DETAIL, WebApiService.cctvDetailParams(cctvIds), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdLs = GsonUtils.fromDataToList(data, SnapShotMDL::class.java)
                    if (mdLs.size > 0) {
                        CCTVDetailPageDialog(context, mdLs).setOnItemClickListener(object : CCTVDetailPageDialog.OnItemClickListener {
                            override fun onItemClick(position: Int, mdl: SnapShotMDL) {
                                getRoadVideo(mdl.resid, mdl.shortname)
                            }
                        }).show()
                    } else {
                        showShortToast("暂无相关数据~")
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
                        openActivity(VideoPlayerActivity::class.java, Bundle().apply {
                            putBoolean("isLive", true)
                            putString("url", it)
                            putString("title", shortName)
                        })
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
}