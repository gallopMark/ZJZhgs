package com.uroad.zhgs.activity

import android.os.Bundle
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseHtmlActivity
import com.uroad.zhgs.enumeration.NewsType
import com.uroad.zhgs.model.HtmlMDL
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService

/**
 *Created by MFB on 2018/9/2.
 */
class RescueBookActivity : BaseHtmlActivity() {
    override fun setUp(savedInstanceState: Bundle?) {
        super.setUp(savedInstanceState)
        withTitle(resources.getString(R.string.rescue_request_book))
    }

    //获取救援告知书
    override fun initData() {
        doRequest(WebApiService.NEWS_BY_TYPE, WebApiService.newsByTypeParams(NewsType.RESCUE_BOOK.code), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, HtmlMDL::class.java)
                    if (mdl == null) onJsonParseError()
                    else loadHtml(mdl.html)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                endLoading()
                onHttpError(e)
            }
        })
    }
}