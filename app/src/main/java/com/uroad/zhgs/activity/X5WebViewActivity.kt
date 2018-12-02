package com.uroad.zhgs.activity

import android.os.*
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import com.tencent.smtt.export.external.interfaces.ClientCertRequest
import com.tencent.smtt.export.external.interfaces.WebResourceRequest
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import kotlinx.android.synthetic.main.activity_x5webview.*
import java.lang.ref.WeakReference

/**
 * @author MFB
 * @create 2018/11/22
 * @describe 腾讯新x5webView（主要播放高速直播视频）
 */
class X5WebViewActivity : BaseActivity() {
    private var isSnapShot = false
    private var handler: MHandler? = null
    private var times = 10

    companion object {
        private const val CODE_MSG = 0x0001
    }

    private class MHandler(activity: X5WebViewActivity) : Handler() {
        private val weakReference = WeakReference<X5WebViewActivity>(activity)
        override fun handleMessage(msg: Message) {
            val activity = weakReference.get() ?: return
            when (msg.what) {
                CODE_MSG -> {
                    if (activity.times > 0) {
                        activity.times--
                        sendEmptyMessageDelayed(CODE_MSG, 1000)
                    } else {
                        activity.finish()
                    }
                }
            }
        }
    }

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_x5webview)
        val title = intent.extras?.getString("title")
        intent.extras?.let { isSnapShot = it.getBoolean("isSnapShot", false) }
        if (TextUtils.isEmpty(title)) {
            withTitle("详情")
        } else {
            withTitle(title)
        }
        if (isSnapShot) handler = MHandler(this)
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
        webView.settings.mediaPlaybackRequiresUserGesture = false
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

            override fun onPageFinished(p0: WebView?, p1: String?) {
                super.onPageFinished(p0, p1)
                if (isSnapShot) {
                    handler?.removeMessages(CODE_MSG)
                    handler?.sendEmptyMessageDelayed(CODE_MSG, 5000L)
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
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
        handler?.removeCallbacksAndMessages(null)
        webView.stopLoading()
        webView.removeAllViews()
        webView.destroy()
    }
}

