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
 * @author MFB
 * @create 2018/10/26
 * @describe 车友组队协议书
 */
class RidersAgreementActivity : BaseHtmlActivity() {
    override fun setUp(savedInstanceState: Bundle?) {
        super.setUp(savedInstanceState)
        withTitle(resources.getString(R.string.team_agreement))
    }

    override fun initData() {
        doRequest(WebApiService.NEWS_BY_TYPE, WebApiService.newsByTypeParams(NewsType.RIDERS_AGREEMENT.code), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                setPageLoading()
            }

            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    setPageEndLoading()
                    val mdl = GsonUtils.fromDataBean(data, HtmlMDL::class.java)
                    if (mdl == null) onJsonParseError()
                    else loadHtml(mdl.html)
                } else {
                    setPageError()
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                setPageError()
            }
        })
    }
}