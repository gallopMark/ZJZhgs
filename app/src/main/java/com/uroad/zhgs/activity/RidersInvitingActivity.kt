package com.uroad.zhgs.activity

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.uroad.zhgs.R
import com.uroad.zhgs.adapteRv.RidersInvitingAdapter
import com.uroad.zhgs.common.ThemeStyleActivity
import com.uroad.zhgs.model.MutilItem
import com.uroad.zhgs.model.RidersInvitingMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.utils.InputMethodUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_riders_inviting.*
import kotlinx.android.synthetic.main.layout_empty.*
import java.lang.StringBuilder

/**
 * @author MFB
 * @create 2018/10/18
 * @describe 车队邀请加入
 */
class RidersInvitingActivity : ThemeStyleActivity() {
    private var teamId: String? = ""
    private var keyWord: String = ""
    private val mDatas = ArrayList<MutilItem>()
    private lateinit var adapter: RidersInvitingAdapter
    private val mSelected = ArrayList<RidersInvitingMDL.Riders>()
    private lateinit var mAdapter: SelectedAdapter
    private var isOnSearch = false
    override fun themeSetUp(savedInstanceState: Bundle?) {
        setLayoutResID(R.layout.activity_riders_inviting)
        intent.extras?.let { teamId = it.getString("teamId") }
        setThemeTitle(getString(R.string.riders_inviting_title))
        setThemeOption(getString(R.string.create_riders_confirm), View.OnClickListener { onInviting() })
        initRv()
        ivSearch.setOnClickListener { onSearch() }
    }

    private fun initRv() {
        rvSelected.layoutManager = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.HORIZONTAL }
        recyclerView.layoutManager = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.VERTICAL }
        mAdapter = SelectedAdapter(this, mSelected)
        rvSelected.adapter = mAdapter
        adapter = RidersInvitingAdapter(this, mDatas).apply {
            setOnCheckChangeListener(object : RidersInvitingAdapter.OnCheckChangeListener {
                override fun onSelected(mSelected: MutableList<RidersInvitingMDL.Riders>) {
                    this@RidersInvitingActivity.mSelected.clear()
                    this@RidersInvitingActivity.mSelected.addAll(mSelected)
                    mAdapter.notifyDataSetChanged()
                }
            })
        }
        recyclerView.adapter = adapter
    }

    private fun onSearch() {
        isOnSearch = true
        keyWord = etSearch.text.toString()
        InputMethodUtils.hideSoftInput(this, etSearch)
        initData()
    }

    override fun initData() {
        doRequest(WebApiService.CAR_TEAM_LIST, WebApiService.carTeamListParams(getUserId(), keyWord), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                setThemeLoading()
            }

            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, RidersInvitingMDL::class.java)
                    if (mdl == null) onJsonParseError()
                    else {
                        setThemeEndLoading()
                        updateUI(mdl)
                    }
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                setThemePageError()
            }
        })
    }

    private fun updateUI(mdl: RidersInvitingMDL) {
        mDatas.clear()
        mdl.lastteammember?.let {
            if (it.size > 0) {
                mDatas.add(RidersInvitingMDL.RiderType().apply { text = getString(R.string.riders_last_team) })
                mDatas.addAll(it)
            }
        }
        mdl.follow?.let {
            if (it.size > 0) {
                mDatas.add(RidersInvitingMDL.RiderType().apply { text = getString(R.string.riders_myNotice) })
                mDatas.addAll(it)
            }
        }
        if (mDatas.size > 0) {
            recyclerView.visibility = View.VISIBLE
            adapter.notifyDataSetChanged()
        } else {
            if (isOnSearch) {
                onEmptyData(getString(R.string.search_empty))
                isOnSearch = false
            } else {
                onEmptyData(getString(R.string.riders_empty_list))
            }
        }
    }

    private fun onEmptyData(emptyTips: CharSequence) {
        recyclerView.visibility = View.GONE
        llEmpty.visibility = View.VISIBLE
        mEmptyTv.text = emptyTips
    }

    private class SelectedAdapter(context: Context, mDatas: MutableList<RidersInvitingMDL.Riders>) :
            BaseArrayRecyclerAdapter<RidersInvitingMDL.Riders>(context, mDatas) {
        override fun bindView(viewType: Int): Int = R.layout.item_riders_selected

        override fun onBindHoder(holder: RecyclerHolder, t: RidersInvitingMDL.Riders, position: Int) {
            holder.displayImage(R.id.ivIcon, t.iconfile, R.mipmap.ic_user_default)
        }
    }

    private fun onInviting() {
        if (mSelected.size > 0) {
            inviting()
        } else {
            showShortToast(getString(R.string.riders_inviting_unSelected))
        }
    }

    /*发送邀请好友请求*/
    private fun inviting() {
        val sb = StringBuilder()
        for (i in 0 until mSelected.size) {
            sb.append(mSelected[i].userid)
            if (i < mSelected.size - 1) {
                sb.append(",")
            }
        }
        val userIds = sb.toString()
        doRequest(WebApiService.INVITE_RIDERS, WebApiService.inviteRidersParams(teamId, userIds, getUserId()), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    showShortToast("邀请成功")
                    Handler().postDelayed({ if (!isFinishing) finish() }, 1500)
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