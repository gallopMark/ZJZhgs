package com.uroad.zhgs.model

import android.content.Context
import com.uroad.zhgs.R
import java.io.Serializable

/**
 *Created by MFB on 2018/8/21.
 * 服务区
"picurl	图标
poiid	站点ID
latitude	纬度
longitude	经度
shortname	高速名称
name	站点名
detailurl	详情链接
distance	距离	KM
hasfood	是否有餐饮	0 否 ； 1 是
hasroom	是否有住宿	0 否 ； 1 是
hastoilet	是否有厕所	0 否 ； 1 是
hasshop	是否有商店	0 否 ； 1 是
haspark	是否有停车	0 否 ； 1 是
hasoil	是否有油站	0 否 ； 1 是
hasrepair	是否有维修	0 否 ； 1 是
oil1	汽油92价格	对。空或者 0 就是没了
oil2	汽油95价格	空或者 0 就是没了
oil3	汽油98价格	空或者 0 就是没了
oil4	柴油0价格	空或者 0 就是没了
parkstatus	停车场状态	1-充足，2-紧张
 */
class ServiceMDL : MutilItem, Serializable {
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
}