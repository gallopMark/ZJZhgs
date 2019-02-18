package com.uroad.zhgs.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.tencent.android.tpush.XGPushManager
import com.uroad.library.utils.DataCleanManager
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.helper.UserPreferenceHelper
import com.uroad.zhgs.dialog.MaterialDialog
import com.uroad.zhgs.service.MyTracksService
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_settings.*

/**
 * Created by MFB on 2018/8/13.
 * Copyright  2018年 浙江综合交通大数据开发有限公司.
 * 说明：设置页面
 */
class SettingsActivity : BaseActivity() {
    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_settings)
        withTitle(resources.getString(R.string.settings_title))
        init()
        setCache()
        llClearCache.setOnClickListener {
            showDialog("清除缓存", "确定清除缓存？",
                    "取消", "确定", object : MaterialDialog.ButtonClickListener {
                override fun onClick(v: View, dialog: AlertDialog) {
                    dialog.dismiss()
                }
            }, object : MaterialDialog.ButtonClickListener {
                override fun onClick(v: View, dialog: AlertDialog) {
                    dialog.dismiss()
                    clearCache()
                }
            })
        }
    }

    private fun setCache() {
        tvCacheSize.text = DataCleanManager.getTotalCacheSize(this)
    }

    private fun init() {
        if (!isLogin()) {
            llTopSettings.visibility = View.GONE
            llAimlessNav.visibility = View.GONE
            llChangePW.visibility = View.GONE
            btLogout.visibility = View.GONE
        } else {
            llTopSettings.visibility = View.VISIBLE
            llAimlessNav.visibility = View.VISIBLE
            llChangePW.visibility = View.VISIBLE
            btLogout.visibility = View.VISIBLE
        }
        checkBox.isChecked = UserPreferenceHelper.isFollow(this)
        cbAimlessNav.isChecked = UserPreferenceHelper.isAimlessNav(this)
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            val isFollow = if (isChecked) 1 else 0
            userSetUp(isFollow)
        }
        cbAimlessNav.setOnCheckedChangeListener { _, isChecked -> UserPreferenceHelper.saveAimlessNav(this@SettingsActivity, isChecked) }
    }

    //清除缓存
    private fun clearCache() {
        addDisposable(Flowable.fromCallable { DataCleanManager.clearAllCache(this@SettingsActivity) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    showShortToast("清除完毕")
                    setCache()
                }, {
                    endLoading()
                }, {
                    endLoading()
                }, {
                    it.request(1)
                    showLoading("正在清除…")
                }))
    }

    override fun setListener() {
        tvChangePW.setOnClickListener {
            if (!isLogin()) {
                openActivity(LoginActivity::class.java)
            } else {
                openActivity(ChangePasswordActivity::class.java)
            }
        }
        tvFeedback.setOnClickListener {
            if (!isLogin()) {
                openActivity(LoginActivity::class.java)
            } else {
                openActivity(FeedbackActivity::class.java)
            }
        }
        tvAboutUs.setOnClickListener { openActivity(AboutUsActivity::class.java) }
        btLogout.setOnClickListener {
            showDialog("温馨提示", "您确定退出登录吗？",
                    "取消", "确定", object : MaterialDialog.ButtonClickListener {
                override fun onClick(v: View, dialog: AlertDialog) {
                    dialog.dismiss()
                }
            }, object : MaterialDialog.ButtonClickListener {
                override fun onClick(v: View, dialog: AlertDialog) {
                    dialog.dismiss()
                    delAccount()
                    UserPreferenceHelper.clear(this@SettingsActivity)
                    showShortToast("您已退出登录")
                    /*退出登录，则停止记录足迹的服务*/
                    stopService(Intent(this@SettingsActivity, MyTracksService::class.java))
                    finish()
                }
            })
        }
    }

    /*解绑指定账号*/
    private fun delAccount() {
        XGPushManager.delAccount(this, UserPreferenceHelper.getPushID(this))
        XGPushManager.unregisterPush(this)
    }

    private fun userSetUp(isFollow: Int) {
        doRequest(WebApiService.USER_SETUP, WebApiService.userSetupParams(getUserId(), isFollow), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    showShortToast("设置成功")
                    UserPreferenceHelper.saveFollow(this@SettingsActivity, checkBox.isChecked)
                } else {
                    checkBox.isChecked = !checkBox.isChecked
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                checkBox.isChecked = !checkBox.isChecked
                onHttpError(e)
            }
        })
    }
}