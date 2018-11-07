package com.uroad.zhgs.model

import android.content.Context
import com.amap.api.maps.model.LatLng
import com.uroad.zhgs.R
import com.uroad.zhgs.cluster.ClusterItem
import java.io.Serializable

/**
 *Created by MFB on 2018/8/21.
 * 服务区
"parkstatusname": "充足",
"picurl": "http:\/\/zhgs.u-road.com\/ZJAppApi\/newcode\/g60.png",
"poiid": "331002",
"longitude": "120.221020",
"latitude": "30.283927",
"shortname": "G60沪昆高速(沪杭)",
"name": "长安服务区",
"distance": "2.51",
"detailurl": "http:\/\/zhgs.u-road.com\/ZJAppView\/serviceDetail.html?dataid=331002",
"hasfood": "1",
"hasroom": "0",
"hastoilet": "1",
"hasspecialty": "1",
"hasshop": "1",
"haspark": "1",
"hasoil": "1",
"hasrepair": "1",
"oil1": "5.6",
"oil2": "5.6",
"oil3": "5.6",
"oil4": "5.6",
"parkstatus": "1",
"oil_arr": ["汽油92#", "汽油95#", "汽油98#", "柴油0#"],
"service_arr": ["加油站", "停车场", "餐饮", "卫生间", "商店", "维修店", "特产"]
 */
class ServiceMDL : MutilItem, ClusterItem, Serializable {
    override fun getItemType(): Int = 8
    var poiid: String? = null
    var picurl: String? = null
    var name: String? = null
    var longitude: Double? = 0.0
    var latitude: Double? = 0.0
    var shortname: String? = null
    var distance: String? = null
    var detailurl: String? = null
    var hasfood: Int? = 0
    var hasroom: Int? = 0
    var hastoilet: Int? = 0
    var hasshop: Int? = 0
    var haspark: Int? = 0
    var hasoil: Int? = 0
    var hasrepair: Int? = 0
    var hasspecialty: Int? = 0
    var oil1: String? = null
    var oil2: String? = null
    var oil3: String? = null
    var oil4: String? = null
    var parkstatus: String? = null
    var parkstatusname: String? = null
    var oil_arr: MutableList<String>? = null
    var service_arr: MutableList<String>? = null

    var markerIcon: Int = R.mipmap.ic_marker_service_icon
    var markerBigIco: Int = R.mipmap.ic_marker_service_big_icon

    fun latitude(): Double {
        latitude?.let { return it }
        return 0.0
    }

    fun longitude(): Double {
        longitude?.let { return it }
        return 0.0
    }

    fun getOilText(): String {
        oil_arr?.let {
            val sb = StringBuilder()
            for (i in 0 until it.size) {
                sb.append(it[i])
                if (i < it.size - 1) {
                    sb.append("；")
                }
            }
            return sb.toString()
        }
        return ""
    }

    fun getServiceArr(): MutableList<String> {
        service_arr?.let { return it }
        return ArrayList()
    }

    fun getFacilities(context: Context): MutableList<String> {
        val list = ArrayList<String>()
        if (hasfood == 1) list.add(context.getString(R.string.service_area_food))
        if (hasoil == 1) list.add(context.getString(R.string.service_area_oil))
        if (haspark == 1) list.add(context.getString(R.string.service_area_park))
        if (hasroom == 1) list.add(context.getString(R.string.service_area_room))
        if (hastoilet == 1) list.add(context.getString(R.string.service_area_toilet))
        if (hasrepair == 1) list.add(context.getString(R.string.service_area_repair))
        if (hasshop == 1) list.add(context.getString(R.string.service_area_shop))
        if (hasspecialty == 1) list.add(context.getString(R.string.service_area_specialty))
        return list
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            !is ServiceMDL -> false
            else -> this === other || detailurl == other.detailurl
        }
    }

    override fun hashCode(): Int {
        return 31 + (detailurl?.hashCode() ?: 0)
    }

    override fun getPosition(): LatLng = LatLng(latitude(), longitude())
    override fun getMarkerSmallIcon(): Int = markerIcon
    override fun getMarkerBigIcon(): Int = markerBigIco
}