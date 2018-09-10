package com.uroad.zhgs.activity

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.uroad.library.utils.DataCleanManager
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.helper.UserPreferenceHelper
import com.uroad.zhgs.dialog.MaterialDialog
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
        if (UserPreferenceHelper.isLogin(this)) {
            btLogout.visibility = View.VISIBLE
        } else {
            btLogout.visibility = View.GONE
        }
        setCache()
        llClearCache.setOnClickListener {
            if (!TextUtils.isEmpty(tvCacheSize.text)) {
                showDialog("清除缓存", "清除缓存会导致下载的内容删除，确定清除吗?",
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

    private fun setCache() {
        val fileSize = DataCleanManager.getAppCacheSize(this)
        if (fileSize > 1024) {
            tvCacheSize.text = DataCleanManager.getTotalCacheSize(this)
        } else {
            tvCacheSize.text = ""
        }
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
                    UserPreferenceHelper.clear(this@SettingsActivity)
                    showShortToast("您已登出")
                    finish()
                }
            })
        }
    }
}