package com.uroad.zhgs.activity

import android.app.AlertDialog
import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import com.uroad.zhgs.R
import com.uroad.zhgs.common.ThemeStyleActivity
import com.uroad.zhgs.dialog.MaterialDialog
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

    companion object {
        private const val TAG_REPORT = "report"
        private const val TAG_FOLLOW = "follow"
        private const val TAG_ORGANIZE = "organize"
    }

    override fun themeSetUp(savedInstanceState: Bundle?) {
        setLayoutResIdWithOutTitle(R.layout.activity_riders_interaction)
        ivBack.setOnClickListener { onBackPressed() }
        setCurrentTab(0)
        initTab()
    }

    private fun setCurrentTab(tab: Int) {
        InputMethodUtils.hideSoftInput(this)
        resetTagFragment()
        initTv()
        val ts16 = resources.getDimension(R.dimen.font_16)
        val color = ContextCompat.getColor(this, R.color.white)
        val transaction = supportFragmentManager.beginTransaction()
        hideFragments(transaction)
        when (tab) {
            0 -> {
                tvTab1.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts16)
                tvTab1.setTextColor(color)
                val ridersReportFragment = supportFragmentManager.findFragmentByTag(TAG_REPORT)
                if (ridersReportFragment == null) {
                    transaction.add(R.id.container, RidersReportFragment().apply { arguments = intent.extras }, TAG_REPORT)
                } else {
                    transaction.show(ridersReportFragment)
                }
            }
            1 -> {
                tvTab2.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts16)
                tvTab2.setTextColor(color)
                val ridersReportFollowFragment = supportFragmentManager.findFragmentByTag(TAG_FOLLOW)
                if (ridersReportFollowFragment == null) {
                    transaction.add(R.id.container, RidersReportFollowFragment().apply { arguments = Bundle().apply { putBoolean("myFollow", true) } }, TAG_FOLLOW)
                } else {
                    transaction.show(ridersReportFollowFragment)
                }
            }
            2 -> {
                tvTab3.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts16)
                tvTab3.setTextColor(color)
                val ridersOrganizeFragment = supportFragmentManager.findFragmentByTag(TAG_ORGANIZE)
                if (ridersOrganizeFragment == null) {
                    transaction.add(R.id.container, RidesOrganizeFragment(), TAG_ORGANIZE)
                } else {
                    transaction.show(ridersOrganizeFragment)
                }
            }
        }
        transaction.commitAllowingStateLoss()
    }

    private fun resetTagFragment() {
        val ridersReportFragment = supportFragmentManager.findFragmentByTag(TAG_REPORT)
        if (ridersReportFragment != null && ridersReportFragment is RidersReportFragment) {
            if (ridersReportFragment.isMenuOpen()) ridersReportFragment.closeMenu()
        }
        val ridersReportFollowFragment = supportFragmentManager.findFragmentByTag(TAG_FOLLOW)
        if (ridersReportFollowFragment != null && ridersReportFollowFragment is RidersReportFollowFragment) {
            if (ridersReportFollowFragment.isMenuOpen()) ridersReportFollowFragment.closeMenu()
        }
    }

    private fun hideFragments(transaction: FragmentTransaction) {
        for (fragment in supportFragmentManager.fragments) {
            transaction.hide(fragment)
        }
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
        if (!isAuth()) {
            val dialog = MaterialDialog(this)
            dialog.setTitle(getString(R.string.dialog_default_title))
            dialog.setMessage("您未通过实名认证，无法使用车有组队功能")
            dialog.hideDivider()
            dialog.setPositiveButton(getString(R.string.i_got_it), object : MaterialDialog.ButtonClickListener {
                override fun onClick(v: View, dialog: AlertDialog) {
                    dialog.dismiss()
                }
            })
            dialog.show()
        } else {
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
            val ridersReportFragment = supportFragmentManager.findFragmentByTag(TAG_REPORT)
            if (ridersReportFragment != null && ridersReportFragment is RidersReportFragment &&
                    ridersReportFragment.isMenuOpen()) {
                ridersReportFragment.closeMenu()
                return true
            }
            val ridersReportFollowFragment = supportFragmentManager.findFragmentByTag(TAG_FOLLOW)
            if (ridersReportFollowFragment != null && ridersReportFollowFragment is RidersReportFollowFragment &&
                    ridersReportFollowFragment.isMenuOpen()) {
                ridersReportFollowFragment.closeMenu()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}