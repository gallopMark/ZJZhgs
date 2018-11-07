package com.uroad.zhgs.service

import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.uroad.zhgs.common.BaseService
import com.uroad.zhgs.helper.AppLocalHelper
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService

/**
 * @author MFB
 * @create 2018/10/21
 * @describe 记录我的足迹后台服务
 */
class MyTracksService : BaseService(), AMapLocationListener {
    private var mLocationClient: AMapLocationClient? = null
    private lateinit var handler: Handler
    private val delayMillis = 3000L
    override fun getBinder(intent: Intent): IBinder? = null
    override fun onCreate() {
        super.onCreate()
        handler = Handler(Looper.getMainLooper())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        openLocation()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun openLocation() {
        if (mLocationClient == null) {
            mLocationClient = AMapLocationClient(this).apply {
                setLocationOption(AMapLocationClientOption().apply {
                    locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                    interval = 5 * 60 * 1000L   //5分钟定位一次
                })
                setLocationListener(this@MyTracksService)
                startLocation()
            }
        } else {
            mLocationClient?.startLocation()
        }
    }

    override fun onLocationChanged(location: AMapLocation?) {
        if (location != null && location.errorCode == AMapLocation.LOCATION_SUCCESS) {
            afterLocation(location)
        } else {
            onLocationFailure()
        }
    }

    private fun afterLocation(location: AMapLocation) {
        val adCode = location.adCode
        //如果区域编码一样，则不调足迹接口
        if (!isLogin()) return
        if (!AppLocalHelper.containsFootprint(this, getUserId(), adCode)) {
            saveTracks(location)
        }
    }

    /*区域编码发生了变化，则保存足迹*/
    private fun saveTracks(location: AMapLocation) {
        val province = location.province
        val city = location.city
        val district = location.district
        val street = location.street
        val cityCode = location.cityCode
        val adCode = location.adCode
        val address = location.address
        doRequest(WebApiService.SAVE_TRACKS, WebApiService.saveTracksParams(getUserId(),
                province, city, district, street, cityCode, adCode, address), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    AppLocalHelper.saveFootprint(this@MyTracksService, getUserId(), adCode)
                } else {
                    handler.postDelayed({ saveTracks(location) }, delayMillis)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                handler.postDelayed({ saveTracks(location) }, delayMillis)
            }
        })
    }

    private fun onLocationFailure() {
        handler.postDelayed({ openLocation() }, delayMillis)
    }

    override fun onDestroy() {
        closeLocation()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun closeLocation() {
        mLocationClient?.let {
            it.stopLocation()
            it.onDestroy()
        }
        mLocationClient = null
    }
}