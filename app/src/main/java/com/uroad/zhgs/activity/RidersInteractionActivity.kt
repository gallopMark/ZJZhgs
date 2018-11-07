package com.uroad.zhgs.activity

import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.KeyEvent
import com.uroad.zhgs.R
import com.uroad.zhgs.common.ThemeStyleActivity
import com.uroad.zhgs.fragment.RidersReportFollowFragment
import com.uroad.zhgs.fragment.RidersReportFragment
import com.uroad.zhgs.fragment.RidesOrganizeFragment
import com.uroad.zhgs.model.RidersMsgMDL
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.utils.InputMethodUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_riders_interaction.*

/**
 * @author MFB
 * @create 2018/10/9
 * @describe 车友互动、我的关注、车友组队、高速直播
 */
class RidersInteractionActivity : ThemeStyleActivity() {
    private lateinit var ridersReportFragment: RidersReportFragment
    private lateinit var ridersReportFollowFragment: RidersReportFollowFragment
    private lateinit var ridersOrganizeFragment: RidesOrganizeFragment

    override fun themeSetUp(savedInstanceState: Bundle?) {
        setLayoutResIdWithOutTitle(R.layout.activity_riders_interaction)
        ivBack.setOnClickListener { onBackPressed() }
        initFragments()
        setCurrentTab(0)
        initTab()
    }

    private fun initFragments() {
        ridersReportFragment = RidersReportFragment().apply { arguments = intent.extras }
        ridersReportFollowFragment = RidersReportFollowFragment().apply { arguments = Bundle().apply { putBoolean("myFollow", true) } }
        ridersOrganizeFragment = RidesOrganizeFragment()
    }

    private fun setCurrentTab(tab: Int) {
        InputMethodUtils.hideSoftInput(this)
        if (ridersReportFragment.isMenuOpen()) ridersReportFragment.closeMenu()
        if (ridersReportFollowFragment.isMenuOpen()) ridersReportFollowFragment.closeMenu()
        initTv()
        val ts16 = resources.getDimension(R.dimen.font_16)
        val color = ContextCompat.getColor(this, R.color.white)
        val transaction = supportFragmentManager.beginTransaction()
        hideFragments(transaction)
        when (tab) {
            0 -> {
                tvTab1.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts16)
                tvTab1.setTextColor(color)
                if (!ridersReportFragment.isAdded) {
                    transaction.add(R.id.container, ridersReportFragment)
                } else {
                    transaction.show(ridersReportFragment)
                }
            }
            1 -> {
                tvTab2.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts16)
                tvTab2.setTextColor(color)
                if (!ridersReportFollowFragment.isAdded) {
                    transaction.add(R.id.container, ridersReportFollowFragment)
                } else {
                    transaction.show(ridersReportFollowFragment)
                }
            }
            2 -> {
                tvTab3.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts16)
                tvTab3.setTextColor(color)
                if (!ridersOrganizeFragment.isAdded) {
                    transaction.add(R.id.container, ridersOrganizeFragment)
                } else {
                    transaction.show(ridersOrganizeFragment)
                }
            }
        }
        transaction.commitAllowingStateLoss()
    }

    private fun hideFragments(transaction: FragmentTransaction) {
        if (ridersReportFragment.isAdded) transaction.hide(ridersReportFragment)
        if (ridersReportFollowFragment.isAdded) transaction.hide(ridersReportFollowFragment)
        if (ridersOrganizeFragment.isAdded) transaction.hide(ridersOrganizeFragment)
    }

    private fun initTv() {
        val ts14 = resources.getDimension(R.dimen.font_14)
        tvTab1.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts14)
        tvTab2.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts14)
        tvTab3.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts14)
        val color = ContextCompat.getColor(this, R.color.colorTransparent)
        tvTab1.setTextColor(color)
        tvTab2.setTextColor(color)
        tvTab3.setTextColor(color)
    }

    private fun initTab() {
        tvTab1.setOnClickListener { setCurrentTab(0) }
        tvTab2.setOnClickListener { setCurrentTab(1) }
        tvTab3.setOnClickListener { checkCarTeamSituation() }
    }


    /*是否有车队或者邀请*/
    private fun checkCarTeamSituation() {
        doRequest(WebApiService.CHECK_RIDERS, WebApiService.checkRidersParams(getUserId()), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, RidersMsgMDL::class.java)
                    withResult(mdl)
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

    private fun withResult(mdl: RidersMsgMDL?) {
        if (mdl == null) { //不存在车队
            setCurrentTab(2)
        } else {
            if (mdl.type == 1) { //已加入车队
                mdl.content?.let {
                    if (it.size > 0) {
                        val content = it[0]
                        openActivity(RidersDetailActivity::class.java, Bundle().apply { putString("teamId", content.teamid) })
                    }
                }
            } else {
                setCurrentTab(2)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (ridersReportFragment.isMenuOpen()) {
                ridersReportFragment.closeMenu()
                return true
            } else if (ridersReportFollowFragment.isMenuOpen()) {
                ridersReportFollowFragment.closeMenu()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}