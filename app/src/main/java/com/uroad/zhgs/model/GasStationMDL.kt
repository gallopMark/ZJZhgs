package com.uroad.zhgs.model

import com.amap.api.col.sln3.km
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import java.math.BigDecimal
import java.text.DecimalFormat


/**
 *Created by MFB on 2018/8/23.
 * 加油站
 * "latitude": "30.270023",
"longitude": "120.230346",
"name": "东恒石油钱江路加油站",
"address": "钱江路1751号"
 */
class GasStationMDL : MutilItem {
    override fun getItemType(): Int = 6
    var longitude: Double? = 0.0
    var latitude: Double? = 0.0
    var name: String? = null
    var address: String? = null
    var realDistance: Float = 0f

    var markerIcon: Int = 0
    var markerBigIco: Int = 0

    fun latitude(): Double {
        latitude?.let { return it }
        return 0.0
    }

    fun longitude(): Double {
        longitude?.let { return it }
        return 0.0
    }

    fun setDistance(longitude1: Double, latitude1: Double,
                    longitude2: Double, latitude2: Double) {
        realDistance = AMapUtils.calculateLineDistance(LatLng(latitude1, longitude1), LatLng(latitude2, longitude2))
    }

    // 返回单位km
    fun getDistance(): Double = BigDecimal(realDistance.toDouble() / 1000).setScale(2, BigDecimal.ROUND_HALF_UP).toDouble()

    private fun rad(d: Double): Double {
        return d * Math.PI / 180.0
    }
}