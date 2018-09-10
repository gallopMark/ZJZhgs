package com.uroad.zhgs.common

import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.WebSettings
import com.uroad.zhgs.R
import kotlinx.android.synthetic.main.activity_html.*

/**
 *Created by MFB on 2018/8/28.
 */
abstract class BaseHtmlActivity : BaseActivity() {
    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_html)
        initWebView()
    }

    private fun initWebView() {
        webView.settings.domStorageEnabled = true
        val appCachePath = applicationContext.cacheDir.absolutePath
        webView.settings.setAppCachePath(appCachePath)
        webView.settings.allowFileAccess = true
        webView.settings.setAppCacheEnabled(false)
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.loadsImagesAutomatically = true
        webView.settings.databaseEnabled = false
        webView.settings.setGeolocationEnabled(true)// 启用地理定位
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
    }

    fun loadHtml(html: String?) {
        webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()// 返回前一个页面
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        webView.onResume()
        super.onResume()
    }

    override fun onPause() {
        webView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        webView.clearCache(true)
        webView.clearHistory()
        webView.clearFormData()
        webView.destroy()
        super.onDestroy()
    }
}