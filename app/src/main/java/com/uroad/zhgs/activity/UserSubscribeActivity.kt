package com.uroad.zhgs.activity

import android.support.v4.content.ContextCompat
import android.view.MotionEvent
import com.uroad.library.rxbus.RxBus
import com.uroad.zhgs.R
import com.uroad.zhgs.adapteRv.UserSubscribeAdapter
import com.uroad.zhgs.common.BaseRefreshRvActivity
import com.uroad.zhgs.rxbus.MessageEvent
import com.uroad.zhgs.model.SubscribeMDL
import com.uroad.zhgs.rv.OnActivityTouchListener
import com.uroad.zhgs.rv.RecyclerTouchListener
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_base_refreshrv.*

/**
 *Created by MFB on 2018/8/15.
 * 我的订阅
 */
class UserSubscribeActivity : BaseRefreshRvActivity(), RecyclerTouchListener.RecyclerTouchListenerHelper {
    private val mDatas = ArrayList<SubscribeMDL>()
    private lateinit var adapter: UserSubscribeAdapter
    private var isFirstLoad = true
    private var activityTouchListener: OnActivityTouchListener? = null

    override fun initViewData() {
        withTitle(resources.getString(R.string.usersubscribe_title))
        refreshLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.color_f7))
        refreshLayout.isEnableLoadMore = false
        val onTouchListener = RecyclerTouchListener(this, recyclerView)
        onTouchListener.setSwipeOptionViews(R.id.tvDelete).setSwipeable(R.id.ll_rowFG, R.id.tvDelete, object : RecyclerTouchListener.OnSwipeOptionsClickListener {
            override fun onSwipeOptionClicked(viewID: Int, position: Int) {
                if (viewID == R.id.tvDelete && position in 0 until mDatas.size) {
                    delete(position)
                }
            }
        })
        recyclerView.addOnItemTouchListener(onTouchListener)
        adapter = UserSubscribeAdapter(this, mDatas)
        recyclerView.adapter = adapter
    }

    override fun initData() {
        doRequest(WebApiService.USER_SUBSCRIBES, WebApiService.subscribeParams(getUserId()), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                if (isFirstLoad) setPageLoading()
            }

            override fun onSuccess(data: String?) {
                finishLoad()
                if (GsonUtils.isResultOk(data)) {
                    setPageEndLoading()
                    isFirstLoad = false
                    val mdLs = GsonUtils.fromDataToList(data, SubscribeMDL::class.java)
                    updateData(mdLs)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                finishLoad()
                if (isFirstLoad) setPageError()
                else onHttpError(e)
            }
        })
    }

    private fun updateData(mdLs: MutableList<SubscribeMDL>) {
        mDatas.clear()
        mDatas.addAll(mdLs)
        adapter.notifyDataSetChanged()
        if (mDatas.size == 0) setPageNoData()
    }

    override fun pullToRefresh() {
        initData()
    }

    override fun pullToLoadMore() {
    }

    private fun delete(position: Int) {
        mDatas[position].subscribeid?.let {
            doRequest(WebApiService.DELETE_SUBSCRIBE, WebApiService.deleteSubscribeParams(it, "0"), object : HttpRequestCallback<String>() {
                override fun onPreExecute() {
                    showLoading("删除订阅…")
                }

                override fun onSuccess(data: String?) {
                    endLoading()
                    if (GsonUtils.isResultOk(data)) {
                        RxBus.getDefault().post(MessageEvent().apply { obj = mDatas[position] })
                        mDatas.removeAt(position)
                        adapter.notifyDataSetChanged()
                        if (mDatas.size == 0) setPageNoData()
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

    override fun setOnActivityTouchListener(listener: OnActivityTouchListener) {
        this.activityTouchListener = listener
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        activityTouchListener?.getTouchCoordinates(ev)
        // 必不可少，否则所有的组件都不会有TouchEvent了
        return if (window.superDispatchTouchEvent(ev)) {
            true
        } else onTouchEvent(ev)
    }
}