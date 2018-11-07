package com.uroad.zhgs.model

import com.amap.api.maps.model.LatLng

/**
 * @author MFB
 * @create 2018/10/22
 * @describe 我的足迹数据
 * num返回参数说明
参数项	名称	备注
province_footprint	省份数
city_footprint	城市数
total_footprint	足迹数
city返回参数说明

参数项	名称	备注
district	区名
latitude	纬度
longitude	经度
footprint_num	足迹数
 */
class FootprintMDL {

    var num: Num? = null
    var city: MutableList<City>? = null

    class Num {
        var province_footprint: Int? = 0
        var city_footprint: Int? = 0
        var total_footprint: Int? = 0
    }

    class City {
        var district: String? = null
        var latitude: Double? = null
        var longitude: Double? = null
        var footprint_num: Int? = 0
        fun getLatLng(): LatLng {
            var latitude = this.latitude
            if (latitude == null) latitude = 0.0
            var longitude = this.longitude
            if (longitude == null) longitude = 0.0
            return LatLng(latitude, longitude)
        }
    }
}