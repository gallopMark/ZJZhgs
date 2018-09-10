package com.uroad.zhgs.fragment

import android.annotation.SuppressLint
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import com.amap.api.col.sln3.it
import com.uroad.library.utils.NetworkUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseFragment
import com.uroad.zhgs.webservice.ApiService
import com.uroad.zhgs.widget.CurrencyLoadView
import kotlinx.android.synthetic.main.fragment_base_webview.*
import kotlinx.android.synthetic.main.fragment_shopping.*

/**
 *Created by MFB on 2018/8/25.
 * 说明：首页商城
 */
class ShoppingFragment : BaseFragment() {
    override fun setBaseLayoutResID(): Int = R.layout.fragment_shopping
    override fun setUp(view: View, savedInstanceState: Bundle?) {
        initBack()
        initSettings()
        initWebView()
        clv.setOnRetryListener(object : CurrencyLoadView.OnRetryListener {
            override fun onRetry(view: View) {
                webView.reload()
                wvLayout.visibility = View.VISIBLE
            }
        })
    }

    private fun initBack() {
        ivBack.setOnClickListener {
            if (webView.canGoBack()) {
                webView.goBack()
                if (!webView.canGoBack()) {
                    ivBack.visibility = View.GONE
                } else {
                    if (webView.url != null && webView.url == ApiService.SHOPPING_URL) {
                        ivBack.visibility = View.GONE
                    }
                }
            } else {
                ivBack.visibility = View.GONE
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initSettings() {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.setAppCacheEnabled(true)
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.loadsImagesAutomatically = true
        webView.settings.loadWithOverviewMode = true
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
        webView.setOnKeyListener(View.OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                webView.goBack()// 返回前一个页面
                if (!webView.canGoBack()) ivBack.visibility = View.GONE
                return@OnKeyListener true
            }
            return@OnKeyListener false
        })
        webView.webChromeClient = BaseWebChromeClient()
        webView.webViewClient = BaseWebViewClient()
    }

    open inner class BaseWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest?): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.loadUrl(request?.url.toString())
            } else {
                view.loadUrl(request?.toString())
            }
            view.url?.let {
                if (it == ApiService.SHOPPING_URL) ivBack.visibility = View.GONE
                else ivBack.visibility = View.VISIBLE
            }
            return true
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            onPageError()
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

    override fun initData() {
        loadUrl(ApiService.SHOPPING_URL)
    }

    private fun loadUrl(url: String) {
        if (!NetworkUtils.isConnected(context)) {
            onPageError()
        } else {
            webView.loadUrl(url)
        }
    }

    private fun onPageError() {
        wvLayout.visibility = View.GONE
        if (!NetworkUtils.isConnected(context))
            clv.setState(CurrencyLoadView.STATE_NONETWORK)
        else
            clv.setState(CurrencyLoadView.STATE_ERROR)
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