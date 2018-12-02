package com.uroad.zhgs.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebView
import com.uroad.library.utils.DeviceUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.model.YouZanMDL
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import com.youzan.androidsdk.YouzanSDK
import com.youzan.androidsdk.YouzanToken
import com.youzan.androidsdk.event.AbsAuthEvent
import com.youzan.androidsdk.event.AbsChooserEvent
import kotlinx.android.synthetic.main.activity_youzan.*

/**
 * @author MFB
 * @create 2018/11/21
 * @describe 有赞用户中心
 */
class YouZanUserActivity : BaseActivity() {

    private lateinit var handler: Handler
    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_youzan)
        withTitle(getString(R.string.mine_my_shopping))
        initBrowser()
        handler = Handler(Looper.getMainLooper())
    }

    private fun initBrowser() {
        browser.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                } else {
                    if (progressBar.visibility != View.VISIBLE) progressBar.visibility = View.VISIBLE
                    progressBar.progress = newProgress
                }
            }
        }
    }

    override fun initData() {
        if (TextUtils.isEmpty(CurrApplication.PRAISE_USER_URL)) {
            initTokenYZ()
        } else {
            loadUrl(CurrApplication.PRAISE_USER_URL)
        }
    }

    private fun initTokenYZ() {
        doRequest(WebApiService.PRAISE_INIT, WebApiService.getBaseParams(), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, YouZanMDL::class.java)
                    if (mdl == null) {
                        handler.postDelayed({ initTokenYZ() }, 3000)
                    } else {
                        if (!TextUtils.isEmpty(mdl.personal_center_url)) {
                            CurrApplication.PRAISE_USER_URL = mdl.personal_center_url
                            loadUrl(CurrApplication.PRAISE_USER_URL)
                        }
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

    private fun loadUrl(url: String?) {
        browser.loadUrl(url)
        browser.subscribe(mAbsAuthEvent)
        browser.subscribe(mAbsChooserEvent)
    }

    //认证事件(AbsAuthEvent)
    private val mAbsAuthEvent = object : AbsAuthEvent() {
        override fun call(context: Context, needLogin: Boolean) {
            if (isLogin()) {
                initLogin()
            } else if (needLogin) {
                openActivityForResult(LoginActivity::class.java, 123)
            }
        }
    }

    private val mAbsChooserEvent = object : AbsChooserEvent() {
        override fun call(context: Context?, intent: Intent, requestCode: Int) {
            startActivityForResult(intent, requestCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            initLogin()
        } else {
            if (resultCode == Activity.RESULT_OK) browser.receiveFile(requestCode, data)
        }
    }

    private fun initLogin() {
        val deviceID = DeviceUtils.getAndroidID(this)
        val extra = DeviceUtils.getFingerprint()
        doRequest(WebApiService.PRAISE_LOGIN, WebApiService.praiseLoginParams(getUserUUID(), deviceID, extra), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, YouZanMDL::class.java)
                    if (mdl == null) handler.postDelayed({ initLogin() }, 3000)
                    else syncYouSDK(mdl)
                } else {
                    handler.postDelayed({ initLogin() }, 3000)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                handler.postDelayed({ initLogin() }, 3000)
            }
        })
    }

    private fun syncYouSDK(mdl: YouZanMDL) {
        val token = YouzanToken().apply {
            this.accessToken = mdl.access_token
            this.cookieKey = mdl.cookie_key
            this.cookieValue = mdl.cookie_value
        }
        YouzanSDK.sync(applicationContext, token)
        browser.sync(token)
    }

    override fun onBackPressed() {
        if (!browser.pageGoBack())
            super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        browser.onResume()
    }

    override fun onPause() {
        super.onPause()
        browser.onPause()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        browser.destroy()
        super.onDestroy()
    }
}