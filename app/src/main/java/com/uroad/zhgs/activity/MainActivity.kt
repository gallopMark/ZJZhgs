package com.uroad.zhgs.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.FragmentTransaction
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import com.uroad.library.utils.VersionUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.dialog.VersionDialog
import com.uroad.zhgs.fragment.MainFragment
import com.uroad.zhgs.fragment.MineFragment
import com.uroad.zhgs.fragment.PraiseFragment
import com.uroad.zhgs.helper.AppLocalHelper
import com.uroad.zhgs.model.YouZanMDL
import com.uroad.zhgs.model.sys.AppConfigMDL
import com.uroad.zhgs.model.sys.SysConfigMDL
import com.uroad.zhgs.service.DownloadService
import com.uroad.zhgs.service.MyTracksService
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.utils.PackageInfoUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_main.*

//app首页
class MainActivity : BaseActivity() {
    private lateinit var handler: Handler

    companion object {
        private const val TAG_MAIN = "main"
        private const val TAG_PRAISE = "praise"
        private const val TAG_MINE = "mine"
    }

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_main)
        initTab()
        /*已经登录才启动记录足迹的服务*/
        if (isLogin()) startService(Intent(this, MyTracksService::class.java))
        handler = Handler(Looper.getMainLooper())
    }

    private fun setCurrentTab(tab: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        hideFragments(transaction)
        when (tab) {
            1 -> {
                val mainFragment = supportFragmentManager.findFragmentByTag(TAG_MAIN)
                if (mainFragment == null) {
                    transaction.add(R.id.content, MainFragment().apply {
                        setOnMenuClickListener(object : MainFragment.OnMenuClickListener {
                            override fun onMenuClick() {
                                this@MainActivity.radioGroup.check(R.id.rbShop)
                            }
                        })
                    }, TAG_MAIN)
                } else {
                    transaction.show(mainFragment)
                }
            }
            2 -> {
                val praiseFragment = supportFragmentManager.findFragmentByTag(TAG_PRAISE)
                if (praiseFragment == null) {
                    transaction.add(R.id.content, PraiseFragment(), TAG_PRAISE)
                } else {
                    transaction.show(praiseFragment)
                }
            }
            3 -> {
                val mineFragment = supportFragmentManager.findFragmentByTag(TAG_MINE)
                if (mineFragment == null) {
                    transaction.add(R.id.content, MineFragment(), TAG_MINE)
                } else {
                    transaction.show(mineFragment)
                }
            }
        }
        transaction.commit()
    }

    private fun hideFragments(transaction: FragmentTransaction) {
        for (fragment in supportFragmentManager.fragments) {
            transaction.hide(fragment)
        }
    }

    //tab切换
    private fun initTab() {
        radioGroup.setOnCheckedChangeListener { _, checkId ->
            vTab1.visibility = View.INVISIBLE
            vTab2.visibility = View.INVISIBLE
            vTab3.visibility = View.INVISIBLE
            when (checkId) {
                R.id.rbHome -> {
                    vTab1.visibility = View.VISIBLE
                    setCurrentTab(1)
                }
                R.id.rbShop -> {
                    vTab2.visibility = View.VISIBLE
                    setCurrentTab(2)
                }
                R.id.rbMine -> {
                    vTab3.visibility = View.VISIBLE
                    setCurrentTab(3)
                }
            }
        }
        setCurrentTab(1)
    }

    override fun initData() {
        initAppConfig()
        getAppConfig()
        initTokenYZ()
    }

    /*获取本地配置数据*/
    private fun initAppConfig() {
        CurrApplication.VOICE_MAX_SEC = AppLocalHelper.getVoiceMax(this)
        CurrApplication.VIDEO_MAX_SEC = AppLocalHelper.getVideoMax(this)
        CurrApplication.WISDOM_URL = AppLocalHelper.getWisdomUrl(this)
        CurrApplication.ALIVE_URL = AppLocalHelper.getAliveUrl(this)
        CurrApplication.BREAK_RULES_URL = AppLocalHelper.getBreakRulesUrl(this)
    }

    //APP配置版本
    private fun getAppConfig() {
        doRequest(WebApiService.APP_CONFIG, WebApiService.getBaseParams(), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdLs = GsonUtils.fromDataToList(data, AppConfigMDL::class.java)
                    for (item in mdLs) {
                        if (TextUtils.equals(item.confid, AppConfigMDL.Type.SYSTEM_VER.CODE)) {
                            checkAppVer(item)
                        } else if (TextUtils.equals(item.confid, AppConfigMDL.Type.ANDROID_VER.CODE)) {
                            versionTips(item)
                        }
                    }
                } else {
                    handler.postDelayed({ getAppConfig() }, CurrApplication.DELAY_MILLIS)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                handler.postDelayed({ getAppConfig() }, CurrApplication.DELAY_MILLIS)
            }
        })
    }

    private fun checkAppVer(configMDL: AppConfigMDL) {
        val currVer = AppLocalHelper.getSysVer(this)
        if (VersionUtils.isNeedUpdate(configMDL.conf_ver, currVer)) {
            getSysConfig(configMDL)
        }
    }

    private fun getSysConfig(configMDL: AppConfigMDL) {
        doRequest(WebApiService.SYS_CONFIG, WebApiService.getBaseParams(), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdLs = GsonUtils.fromDataToList(data, SysConfigMDL::class.java)
                    AppLocalHelper.saveSysVer(this@MainActivity, configMDL.conf_ver)
                    configure(mdLs)
                } else {
                    handler.postDelayed({ getAppConfig() }, CurrApplication.DELAY_MILLIS)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                handler.postDelayed({ getAppConfig() }, CurrApplication.DELAY_MILLIS)
            }
        })
    }

    /*配置数据*/
    private fun configure(mdLs: MutableList<SysConfigMDL>) {
        for (item in mdLs) {
            val sysCode = item.syscode?.toLowerCase()
            when {
                TextUtils.equals(sysCode, SysConfigMDL.VOICE_MAX_SEC) -> {
                    CurrApplication.VOICE_MAX_SEC = item.getVoiceValue(item.sysvalue)
                    AppLocalHelper.saveVoiceMax(this, CurrApplication.VOICE_MAX_SEC)
                }
                TextUtils.equals(sysCode, SysConfigMDL.VIDEO_MAX_SEC) -> {
                    CurrApplication.VIDEO_MAX_SEC = item.getVideoValue(item.sysvalue)
                    AppLocalHelper.saveVideoMax(this, CurrApplication.VIDEO_MAX_SEC)
                }
                TextUtils.equals(sysCode, SysConfigMDL.WISDOM_URL) -> {
                    CurrApplication.WISDOM_URL = item.sysvalue
                    AppLocalHelper.saveWisdomUrl(this, item.sysvalue)
                }
                TextUtils.equals(sysCode, SysConfigMDL.ALINE_URL) -> {
                    CurrApplication.ALIVE_URL = item.sysvalue
                    AppLocalHelper.saveAliveUrl(this, item.sysvalue)
                }
                TextUtils.equals(sysCode, SysConfigMDL.BREAK_RULES_URL) -> {
                    CurrApplication.BREAK_RULES_URL = item.sysvalue
                    AppLocalHelper.saveBreakRulesUrl(this, item.sysvalue)
                }
            }
        }
    }

    /*版本检测是否更新*/
    private fun versionTips(mdl: AppConfigMDL) {
        if (VersionUtils.isNeedUpdate(mdl.conf_ver, PackageInfoUtils.getVersionName(this))) {
            VersionDialog(this, mdl).setOnConfirmClickListener(object : VersionDialog.OnConfirmClickListener {
                override fun onConfirm(mdl: AppConfigMDL, dialog: VersionDialog) {
                    dialog.dismiss()
                    if (TextUtils.isEmpty(mdl.url)) showShortToast(getString(R.string.version_update_error))
                    else {
                        startService(Intent(this@MainActivity, DownloadService::class.java).apply {
                            putExtra("downloadUrl", mdl.url)
                            if (mdl.isforce == 1) {
                                putExtra("isForce", true)
                            } else {
                                putExtra("isForce", false)
                            }
                        })
                    }
                }
            }).show()
        }
    }

    /*初始化有赞*/
    private fun initTokenYZ() {
        doRequest(WebApiService.PRAISE_INIT, WebApiService.getBaseParams(), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, YouZanMDL::class.java)
                    if (mdl == null) handler.postDelayed({ initTokenYZ() }, 3000)
                    else {
                        CurrApplication.PRAISE_URL = mdl.shop_url
                        CurrApplication.PRAISE_USER_URL = mdl.personal_center_url
                    }
                } else {
                    handler.postDelayed({ initTokenYZ() }, 3000)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                handler.postDelayed({ initTokenYZ() }, 3000)
            }
        })
    }

    //记录用户首次点击返回键的时间
    private var firstTime: Long = 0

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val praiseFragment = supportFragmentManager.findFragmentByTag(TAG_PRAISE)
            if (praiseFragment != null && praiseFragment is PraiseFragment) {
                if (praiseFragment.isAdded && praiseFragment.isVisible && praiseFragment.canGoBack()) {
                    praiseFragment.onKeyEvent()
                    return true
                }
            }
            val secondTime = System.currentTimeMillis()
            if (secondTime - firstTime > 2000) {
                showShortToast("再按一次退出${getString(R.string.app_name)}")
                firstTime = secondTime
                return true
            } else {
                finish()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
