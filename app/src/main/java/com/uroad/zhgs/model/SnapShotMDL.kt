package com.uroad.zhgs.model

import android.text.TextUtils
import com.amap.api.maps.model.LatLng
import com.uroad.zhgs.R
import com.uroad.zhgs.cluster.ClusterItem

/**
 *Created by MFB on 2018/8/22.
 * 监控快拍
"roadoldid": "21",
"latitude": "30.5291070",
"longitude": "120.5430270",
"shortname": "沪昆高速(沪杭)",
"picurl": "http:\/\/zhgs.u-road.com\/screamshot\/95053ba453154f29befb27551bff438c20180925182403.png,http:\/\/zhgs.u-road.com\/screamshot\/95053ba453154f29befb27551bff438c20180925182904.png,http:\/\/zhgs.u-road.com\/screamshot\/95053ba453154f29befb27551bff438c20180925183404.png",
"resname": "K135+701杭向",
"resid": "95053ba453154f29befb27551bff438c"
 */
class SnapShotMDL : MutilItem, ClusterItem {
    override fun getItemType(): Int = 3
    var cctvids: String? = null
    var roadoldid: String? = null
    var latitude: Double? = 0.0
    var longitude: Double? = 0.0
    var shortname: String? = null
    var picurl: String? = null
    var resname: String? = null
    var resid: String? = null

    var markerIcon: Int = R.mipmap.ic_marker_snap_icon
    var markerBigIco: Int = R.mipmap.ic_marker_snap_big_icon

    fun latitude(): Double {
        latitude?.let { return it }
        return 0.0
    }

    fun longitude(): Double {
        longitude?.let { return it }
        return 0.0
    }

    fun getPicUrls(): MutableList<String> {
        picurl?.let {
            val list = it.split(",").toMutableList()
            val data = ArrayList<String>()
            for (item in list) {
                if (!TextUtils.isEmpty(item)) {
                    data.add(item)
                }
            }
            return data
        }
        return ArrayList()
    }

    fun getLastPicUrl(): String {
        picurl?.let {
            val list = it.split(",").toMutableList()
            if (list.size > 0) return list[list.size - 1]
        }
        return ""
    }

    override fun getPosition(): LatLng = LatLng(latitude(), longitude())
    override fun getMarkerSmallIcon(): Int = markerIcon

    override fun getMarkerBigIcon(): Int = markerBigIco
}