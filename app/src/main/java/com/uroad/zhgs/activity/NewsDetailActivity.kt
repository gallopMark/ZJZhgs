package com.uroad.zhgs.activity

import android.os.Build
import android.os.Bundle
import android.webkit.WebSettings
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.model.NewsDetailMDL
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_newsdetail.*


/**
 *Created by MFB on 2018/8/14.
 * 资讯详情
 */
@Deprecated("资讯详情加载html方式")
class NewsDetailActivity : BaseActivity() {
    private var newsid: String = ""
    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_newsdetail)
        withTitle(resources.getString(R.string.news_detail_title))
        intent.extras?.getString("newsid")?.let { newsid = it }
        initWebView()
    }

    private fun initWebView() {
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

    override fun initData() {
        doRequest(WebApiService.NEWS_DETAIL, WebApiService.newsDetailsParams(newsid), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                setPageLoading()
            }

            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    setPageEndLoading()
                    val mdl = GsonUtils.fromDataBean(data, NewsDetailMDL::class.java)
                    if (mdl == null) showShortToast("数据解析异常")
                    else updateData(mdl)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                setPageError()
            }
        })
    }

    private fun updateData(mdl: NewsDetailMDL) {
        tvTitle.text = mdl.title
        tvInTime.text = mdl.getInTime()
        val textVc = resources.getString(R.string.news_viewcounnt) + mdl.getViewCount()
        tvViewCount.text = textVc
        webView.loadDataWithBaseURL(null, mdl.html, "text/html", "utf-8", null)
    }
}