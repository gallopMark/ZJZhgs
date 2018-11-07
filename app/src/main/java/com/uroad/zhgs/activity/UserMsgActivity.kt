package com.uroad.zhgs.activity

import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.adapteRv.UserMsgAdapter
import com.uroad.zhgs.common.BaseRefreshRvActivity
import com.uroad.zhgs.model.UserMsgMDL
import com.uroad.zhgs.photopicker.widget.RecycleViewDivider
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_base_refreshrv.*

/**
 *Created by MFB on 2018/8/14.
 */
class UserMsgActivity : BaseRefreshRvActivity() {
    private val mDatas = ArrayList<UserMsgMDL>()
    private lateinit var adapter: UserMsgAdapter
    private var index: Int = 1
    private val size: Int = 10
    override fun initViewData() {
        withTitle(resources.getString(R.string.usermsg_title))
        refreshLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.color_f7))
        recyclerView.addItemDecoration(RecycleViewDivider(this,
                LinearLayoutManager.VERTICAL, DisplayUtils.dip2px(this, 10f),
                ContextCompat.getColor(this, R.color.transparent)))
        adapter = UserMsgAdapter(this, mDatas)
        recyclerView.adapter = adapter
        refreshLayout.autoRefresh()
    }

    override fun pullToRefresh() {
        index = 1
        getMsg()
    }

    override fun pullToLoadMore() {
        getMsg()
    }

    private fun getMsg() {
        doRequest(WebApiService.USER_MSG, WebApiService.userMsgParams(getUserId(), index, size), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                finishLoad()
                if (GsonUtils.isResultOk(data)) {
                    val msgMDLs = GsonUtils.fromDataToList(data, UserMsgMDL::class.java)
                    updateData(msgMDLs)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                finishLoad()
                if (index == 1) setPageError()
                else onHttpError(e)
            }
        })
    }

    private fun updateData(mdLs: MutableList<UserMsgMDL>) {
        if (index == 1) mDatas.clear()
        mDatas.addAll(mdLs)
        adapter.notifyDataSetChanged()
        if (mdLs.size < size) {
            refreshLayout.setNoMoreData(true)
        } else {
            refreshLayout.setNoMoreData(false)
        }
        if (index == 1 && mDatas.size == 0) setPageNoData()
        else index += 1
    }

    override fun onReload(view: View) {
        pullToRefresh()
    }
}