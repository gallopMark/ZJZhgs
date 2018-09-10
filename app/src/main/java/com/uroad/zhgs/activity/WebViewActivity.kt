package com.uroad.zhgs.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import com.uroad.zhgs.common.BaseWebViewActivity

/**
 *Created by MFB on 2018/8/29.
 */
class WebViewActivity : BaseWebViewActivity() {
    companion object {
        const val WEB_URL = "WEB_URL"
        const val WEB_TITLE = "WEB_TITLE"
        const val WEB_DEFAULT_TITLE = "详情"
    }

    @SuppressLint("JavascriptInterface", "AddJavascriptInterface")
    override fun setUp(savedInstanceState: Bundle?) {
        super.setUp(savedInstanceState)
        val title = intent.extras?.getString(WEB_TITLE)
        if (!TextUtils.isEmpty(title)) {
            withTitle(title)
        } else {
            withTitle(WEB_DEFAULT_TITLE)
        }
        val url = intent.extras?.getString(WEB_URL)
        url?.let { loadUrl(it) }
    }
}