package com.uroad.zhgs.model

import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import com.uroad.zhgs.R
import com.uroad.zhgs.cluster.ClusterItem
import java.math.BigDecimal


/**
 *Created by MFB on 2018/8/23.
 * 加油站
 * "latitude": "30.270023",
"longitude": "120.230346",
"name": "东恒石油钱江路加油站",
"address": "钱江路1751号"
 */
class GasStationMDL : ClusterItem, MutilItem {
    override fun getItemType(): Int = 6
    var longitude: Double? = 0.0
    var latitude: Double? = 0.0
    var name: String? = null
    var address: String? = null
    var realDistance: Float = 0f

    var markerIcon: Int = R.mipmap.ic_marker_gas_icon
    var markerBigIco: Int = R.mipmap.ic_marker_gas_big_icon

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

    override fun getPosition(): LatLng = LatLng(latitude(), longitude())
    override fun getMarkerSmallIcon(): Int = markerIcon

    override fun getMarkerBigIcon(): Int = markerBigIco
}