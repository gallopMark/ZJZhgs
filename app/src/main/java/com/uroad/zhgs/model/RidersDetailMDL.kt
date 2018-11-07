package com.uroad.zhgs.model

import com.amap.api.maps.model.LatLng

/**
 * @author MFB
 * @create 2018/10/20
 * @describe 车队详情
 *  team_data【车队信息】返回参数说明

参数项	名称	备注
teamname	车队名称
toplace	目的地
longitude	目的地经度
latitude	目的地维度
intoken	车队口令

teammember【车队队员】返回参数说明
参数项	名称	备注
isown	是否为队长	0 否 ； 1 是
longitude	队员经度
latitude	队员维度
username	名称
iconfile	头像
 */
class RidersDetailMDL {
    var team_data: TeamData? = null
    var teammember: MutableList<TeamMember>? = null

    class TeamData {
        var teamid: String? = null
        var teamname: String? = null
        var toplace: String? = null
        var longitude: Double? = null
        var latitude: Double? = null
        var intoken: String? = null
        var token_text: String? = null
        fun getLatLng(): LatLng {
            var latitude = this.latitude
            if (latitude == null) latitude = 0.0
            var longitude = this.longitude
            if (longitude == null) longitude = 0.0
            return LatLng(latitude, longitude)
        }
    }

    class TeamMember : MutilItem {
        override fun getItemType(): Int = 0
        var isown: Int? = 0
        var longitude: Double? = null
        var latitude: Double? = null
        var userid: String? = null
        var username: String? = null
        var iconfile: String? = null
        fun getLatLng(): LatLng? {
            var latitude = this.latitude
            if (latitude == null) latitude = 0.0
            var longitude = this.longitude
            if (longitude == null) longitude = 0.0
            if (latitude > 0 && longitude > 0)
                return LatLng(latitude, longitude)
            return null
        }

        override fun equals(other: Any?): Boolean {
            return when (other) {
                !is TeamMember -> false
                else -> this === other || userid == other.userid
            }
        }

        override fun hashCode(): Int {
            return 31 + (userid?.hashCode() ?: 0)
        }
    }
}