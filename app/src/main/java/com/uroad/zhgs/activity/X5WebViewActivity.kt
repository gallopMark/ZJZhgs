package com.uroad.zhgs.activity

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient
import com.tencent.smtt.export.external.interfaces.WebResourceRequest
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import kotlinx.android.synthetic.main.activity_x5webview.*

/**
 * @author MFB
 * @create 2018/11/22
 * @describe 腾讯新x5webView（主要播放高速直播视频）
 */
class X5WebViewActivity : BaseActivity() {
    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_x5webview)
        val title = intent.extras?.getString("title")
        if (TextUtils.isEmpty(title)) {
            withTitle("详情")
        } else {
            withTitle(title)
        }
        initX5WebSettings()
        initX5WebView()
    }

    private fun initX5WebSettings() {
        webView.settings.domStorageEnabled = true
        val appCachePath = applicationContext.cacheDir.absolutePath
        webView.settings.setAppCachePath(appCachePath)
        webView.settings.allowFileAccess = true
        webView.settings.useWideViewPort = true
        webView.settings.setAppCacheEnabled(true)
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.loadsImagesAutomatically = true
        webView.settings.databaseEnabled = true
        webView.settings.setGeolocationEnabled(true)// 启用地理定位
    }

    private fun initX5WebView() {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest?): Boolean {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    webView.loadUrl(request?.url.toString())
                } else {
                    webView.loadUrl(request?.toString())
                }
                return true
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                    setPageResponse()
                } else {
                    if (progressBar.visibility != View.VISIBLE) progressBar.visibility = View.VISIBLE
                    progressBar.progress = newProgress
                }
            }

            override fun onShowCustomView(p0: View?, p1: IX5WebChromeClient.CustomViewCallback?) {
                super.onShowCustomView(p0, p1)
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE//播放时横屏幕，如果需要改变横竖屏，只需该参数就行了
            }

            override fun onHideCustomView() {
                super.onHideCustomView()
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT//不播放时竖屏
            }
        }
    }

    override fun initData() {
        val url = intent.extras?.getString("url")
        url?.let { webView.loadUrl(it) }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()// 返回前一个页面
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
        webView.resumeTimers()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
        webView.pauseTimers()
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.stopLoading()
        webView.removeAllViews()
        webView.destroy()
    }
}

