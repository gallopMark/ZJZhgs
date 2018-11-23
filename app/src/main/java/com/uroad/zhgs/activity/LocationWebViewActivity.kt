package com.uroad.zhgs.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.amap.api.location.AMapLocation
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Poi
import com.uroad.zhgs.R.id.webView
import com.uroad.zhgs.common.BaseWebViewActivity
import com.uroad.zhgs.dialog.MaterialDialog
import com.uroad.zhgs.helper.UserPreferenceHelper.Companion.getIconFile
import com.uroad.zhgs.helper.UserPreferenceHelper.Companion.getPhone
import com.uroad.zhgs.helper.UserPreferenceHelper.Companion.getUserName
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
        fun uroadplus_login() {
            if (!isLogin()) openActivityForResult(LoginActivity::class.java, 123)
            else plusWebUserInfo()
        }

        @android.webkit.JavascriptInterface
        fun uroadplus_lnglat() {
            applyLocationPermission(false)
        }

        //h5调用app导航
        @android.webkit.JavascriptInterface
        fun uroadplus_navi(poitype: String?, poiname: String?, longitude: String, latitude: String, address: String?) {
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
        if (isLogin()) plusWebUserInfo()
        closeLocation()
    }

    private fun plusWebUserInfo() {
        onLoad("uroadplus_web_setInfo", "('${getUserId()}','${getUserName()}','${getPhone()}','${getIconFile()}')")
    }

    private fun onLoad(callBackName: String, data: String) {
        val js = "javascript:$callBackName$data"
        webView.loadUrl(js)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && resultCode == RESULT_OK) {
            plusWebUserInfo()
        }
    }

    override fun onDestroy() {
        webView.clearCache(true)
        webView.clearFormData()
        webView.clearHistory()
        super.onDestroy()
    }
}