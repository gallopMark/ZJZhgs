package com.uroad.zhgs.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import com.amap.api.location.AMapLocation
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Poi
import com.uroad.zhgs.common.BaseWebViewActivity
import kotlinx.android.synthetic.main.activity_base_webview.*


/**
 *Created by MFB on 2018/8/2.
 */
class LocationWebViewActivity : BaseWebViewActivity() {
    companion object {
        const val WEB_URL = "WEB_URL"
        const val WEB_TITLE = "WEB_TITLE"
        const val WEB_DEFAULT_TITLE = "详情"
    }

    private var url: String? = null

    @SuppressLint("JavascriptInterface", "AddJavascriptInterface")
    override fun setUp(savedInstanceState: Bundle?) {
        super.setUp(savedInstanceState)
        val title = intent.extras?.getString(WEB_TITLE)
        if (!TextUtils.isEmpty(title)) {
            withTitle(title)
        } else {
            withTitle(WEB_DEFAULT_TITLE)
        }
        url = intent.extras?.getString(WEB_URL)
        webView.settings.setAppCacheEnabled(false)
        webView.addJavascriptInterface(JavascriptInterface(), "uroadhtml")
        url?.let { loadUrl(it) }
    }

    inner class JavascriptInterface {
        //12．	主动获取经纬度
        @android.webkit.JavascriptInterface
        fun uroadplus_lnglat() {
            requestLocationPermissions(object : RequestLocationPermissionCallback {
                override fun doAfterGrand() {
                    openLocation()
                }

                override fun doAfterDenied() {
                    showDismissLocationDialog()
                }
            })
        }

        //h5调用app导航
        @android.webkit.JavascriptInterface
        fun uroadplus_navi(poitype: String?, poiname: String?,
                           longitude: String, latitude: String, address: String?) {
            try {
                val end = Poi(address, LatLng(latitude.toDouble(), longitude.toDouble()), "")
                openNaviPage(null, end)
            } catch (e: Exception) {
            }
        }
    }

    override fun afterLocation(location: AMapLocation) {
        val longitude = location.longitude
        val latitude = location.latitude
        onLoad("uroadplus_web_lnglat", "('$longitude','$latitude')")
        closeLocation()
    }

    fun onLoad(callBackName: String, data: String) {
        val js = "javascript:$callBackName$data"
        webView.loadUrl(js)
    }

    override fun onDestroy() {
        webView.clearCache(true)
        webView.clearFormData()
        webView.clearHistory()
        super.onDestroy()
    }
}