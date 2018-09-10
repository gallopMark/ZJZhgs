package com.uroad.zhgs.common

import android.annotation.SuppressLint
import android.content.Context
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import com.uroad.library.utils.NetworkUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.R.id.progressBar
import com.uroad.zhgs.R.id.webView
import kotlinx.android.synthetic.main.fragment_base_webview.*

/**
 *Created by MFB on 2018/8/10.
 */
abstract class BaseWebViewFragment : BaseFragment() {

    override fun setBaseLayoutResID(): Int {
        return R.layout.fragment_base_webview
    }

    override fun setUp(view: View, savedInstanceState: Bundle?) {
        initSettings()
        initWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initSettings() {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.loadsImagesAutomatically = true
        webView.settings.databaseEnabled = true
        webView.settings.setGeolocationEnabled(true)// 启用地理定位
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webView.settings.allowUniversalAccessFromFileURLs = true
        }
        webView.settings.allowFileAccess = true
    }

    private fun initWebView() {
        webView.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(p0: View?, keyCode: Int, p2: KeyEvent?): Boolean {
                if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                    webView.goBack()// 返回前一个页面
                    return true
                }
                return false
            }
        })
        webView.webViewClient = BaseWebViewClient()
        webView.webChromeClient = BaseWebChromeClient()
    }

    open fun loadUrl(url: String) {
        if (!NetworkUtils.isConnected(context)) {
            setPageError()
        } else {
            webView.loadUrl(url)
        }
    }

    open inner class BaseWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest?): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webView.loadUrl(request?.url.toString())
            } else {
                webView.loadUrl(request?.toString())
            }
            return true
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            setPageEndLoading()
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            setPageError()
        }

        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            handler.proceed()
        }

    }

    open inner class BaseWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            if (newProgress == 100) {
                progressBar.visibility = View.GONE
                setPageEndLoading()
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

    override fun onReLoad(view: View) {
        webView.reload()
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

    override fun onDestroyView() {
        webView.clearCache(true)
        webView.clearFormData()
        webView.clearHistory()
        webView.stopLoading()
        webView.removeAllViews()
        webView.destroy()
        super.onDestroyView()
    }
}