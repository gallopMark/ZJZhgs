package com.uroad.zhgs.model

import com.amap.api.maps.model.LatLng

/**
 * @author MFB
 * @create 2018/10/18
 * @describe 车队消息
 * toplace	目的地
teamid	车队ID
longitude	维度
latitude	经度
intoken	车队口令	这个划重点，后续涉及到加入车队的功能都需要这个参数
type	类型	1 已加入车队；2 收到邀请
 */
class RidersMsgMDL {

    var type: Int? = 0
    var content: MutableList<Content>? = null

    class Content {
        var teamid: String? = null
        var toplace: String? = null
        var longitude: Double? = null
        var latitude: Double? = null
        var intoken: String? = null
        var username: String? = null

        fun getLatLng(): LatLng {
            var latitude = this.latitude
            if (latitude == null) latitude = 0.0
            var longitude = this.longitude
            if (longitude == null) longitude = 0.0
            return LatLng(latitude, longitude)
        }
    }
}