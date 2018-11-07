package com.uroad.zhgs.model

import com.amap.api.maps.model.LatLng
import com.uroad.zhgs.R
import com.uroad.zhgs.cluster.ClusterItem

/**
 *Created by MFB on 2018/8/23.
 * 汽修店返回参数说明
"repairid": "16",
"name": "浙江之信汽车有限公司",
"address": "下沙路1163号(近大王庙路)",
"longitude": "120.2691420",
"latitude": "30.3020310",
"distance": "4.80",
"detailurl": "http:\/\/zhgs.u-road.com\/ZJAppView\/repairDetail.html?dataid=16"
 */
class RepairShopMDL : ClusterItem, MutilItem {
    override fun getItemType(): Int = 5

    var repairid: String? = null
    var name: String? = null
    var address: String? = null
    var longitude: Double? = 0.0
    var latitude: Double? = 0.0
    var distance: String? = null
    var detailurl: String? = null

    var markerIcon: Int = R.mipmap.ic_marker_repair_icon
    var markerBigIco: Int = R.mipmap.ic_marker_repair_big_icon

    fun latitude(): Double {
        latitude?.let { return it }
        return 0.0
    }

    fun longitude(): Double {
        longitude?.let { return it }
        return 0.0
    }

    override fun getPosition(): LatLng = LatLng(latitude(), longitude())
    override fun getMarkerSmallIcon(): Int = markerIcon
    override fun getMarkerBigIcon(): Int = markerBigIco
}