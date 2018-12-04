package com.uroad.zhgs.common

import android.annotation.SuppressLint
import android.os.Bundle
import com.uroad.zhgs.R
import android.os.Build
import android.view.KeyEvent
import kotlinx.android.synthetic.main.activity_base_webview.*
import android.view.View
import android.webkit.*
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import com.uroad.library.utils.NetworkUtils


/**
 *Created by MFB on 2018/8/2.
 */
abstract class BaseWebViewActivity : BaseLocationActivity() {

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_base_webview)
        initSettings()
        initWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initSettings() {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        val appCachePath = applicationContext.cacheDir.absolutePath
        webView.settings.setAppCachePath(appCachePath)
        webView.settings.allowFileAccess = true
        webView.settings.setAppCacheEnabled(true)
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.loadsImagesAutomatically = true
        webView.settings.databaseEnabled = true
        webView.settings.setGeolocationEnabled(true)// 启用地理定位
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
    }

    private fun initWebView() {
        webView.webViewClient = BaseWebViewClient()
        webView.webChromeClient = BaseWebChromeClient()
    }

    open fun loadUrl(url: String) {
        if (!NetworkUtils.isConnected(this)) {
            setPageError()
        } else {
            webView.loadUrl(url)
        }
    }

    inner class BaseWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest?): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webView.loadUrl(request?.url.toString())
            } else {
                webView.loadUrl(request?.toString())
            }
            return true
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            setPageResponse()
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            setPageError()
        }

        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            handler.proceed()
        }
    }

    inner class BaseWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            if (newProgress == 100) {
                progressBar.visibility = View.GONE
                setPageResponse()
            } else {
                if (progressBar.visibility != View.VISIBLE) progressBar.visibility = View.VISIBLE
                progressBar.progress = newProgress
            }
        }

        override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
            callback?.invoke(origin, true, false)
            super.onGeolocationPermissionsShowPrompt(origin, callback)
        }
    }

    override fun onReload(view: View) {
        webView.reload()
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