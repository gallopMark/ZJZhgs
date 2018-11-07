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
import com.uroad.zhgs.fragment.MainFragment
import com.uroad.zhgs.fragment.MineFragment
import com.uroad.zhgs.fragment.ShoppingFragment
import com.uroad.zhgs.helper.AppLocalHelper
import com.uroad.zhgs.model.sys.AppConfigMDL
import com.uroad.zhgs.model.sys.SysConfigMDL
import com.uroad.zhgs.service.MyTracksService
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_main.*

//app首页
class MainActivity : BaseActivity() {
    private var mainFragment: MainFragment? = null
    private var shoppingFragment: ShoppingFragment? = null
    private var mineFragment: MineFragment? = null
    private lateinit var handler: Handler
    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_main)
        initTab()
        setCurrentTab(1)
        /*已经登录才启动记录足迹的服务*/
        if (isLogin()) startService(Intent(this, MyTracksService::class.java))
        handler = Handler(Looper.getMainLooper())
    }

    private fun setCurrentTab(tab: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        hideFragments(transaction)
        when (tab) {
            1 -> {
                if (mainFragment == null) mainFragment = MainFragment().apply {
                    setOnMenuClickListener(object : MainFragment.OnMenuClickListener {
                        override fun onMenuClick() {
                            this@MainActivity.radioGroup.check(R.id.rbShop)
                        }
                    })
                    if (!this.isAdded) transaction.add(R.id.content, this)
                }
                else mainFragment?.let { transaction.show(it) }
            }
            2 -> {
                if (shoppingFragment == null) shoppingFragment = ShoppingFragment().apply { if (!this.isAdded) transaction.add(R.id.content, this) }
                else shoppingFragment?.let { transaction.show(it) }
            }
            3 -> {
                if (mineFragment == null) mineFragment = MineFragment().apply { if (!this.isAdded) transaction.add(R.id.content, this) }
                else mineFragment?.let { transaction.show(it) }
            }
        }
        transaction.commit()
    }

    private fun hideFragments(transaction: FragmentTransaction) {
        mainFragment?.let { transaction.hide(it) }
        shoppingFragment?.let { transaction.hide(it) }
        mineFragment?.let { transaction.hide(it) }
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
        radioGroup.check(R.id.rbHome)
    }

    override fun initData() {
        initAppConfig()
        getAppConfig()
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
                            break
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
                    handler.postDelayed({ getAppConfig() }, 3000)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                handler.postDelayed({ getAppConfig() }, 3000)
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

    //记录用户首次点击返回键的时间
    private var firstTime: Long = 0

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
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
