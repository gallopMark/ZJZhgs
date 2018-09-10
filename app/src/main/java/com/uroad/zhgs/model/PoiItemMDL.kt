package com.uroad.zhgs.model

import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import java.math.BigDecimal

/**
 *Created by MFB on 2018/8/27.
 */
class PoiItemMDL {
    var item: PoiItem? = null
    var adCode: String? = null
    var adName: String? = null
    var provinceName: String? = null
    var cityName: String? = null
    var tel: String? = null
    var poiId: String? = null
    var title: String? = null
    var snippet: String? = null
    var cityCode: String? = null
    var latLonPoint: LatLonPoint? = null
    var distance: Float = 0f
    //根据用户的起点和终点经纬度计算两点间距离，单位米。
    fun setDistance(start: LatLng, end: LatLng) {
        distance = AMapUtils.calculateLineDistance(start, end)
    }

    fun distance(): Double = BigDecimal(distance.toDouble() / 1000)
            .setScale(2, BigDecimal.ROUND_HALF_UP).toDouble()
}