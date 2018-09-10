package com.uroad.zhgs.model

/**
 *Created by MFB on 2018/8/4.
 * 救援详情
 */
class RescueDetailMDL {
    var detail: Detail? = null
    var track: MutableList<Track>? = null

    /**
     * paymethod	支付状态	1080001 未支付 ；1080002 已支付
    rescuestatusname	状态名称
    status	状态	1070001 接警； 1070002 已派遣 ； 1070003 进行中 ；1070004 已结束 ；1070005 已取消
    roadname	高速名称
    rescuetype	救援类型
    place	目的地
    dispatchmembernames	救援人员
    dispatchvehicles	救援车牌
    longitude	救援车辆经度
    latitude	救援车辆纬度
    user_longitude	用户车辆经度
    user_latitude	用户车辆纬度
    helpteam	救援单位
    rescueno	救援编号
    berescuedid	berescuedid
    rescue_address	救援地址
    phonenum	救援电话
     */
    class Detail {
        enum class Status(val code: String) {
            PICKING("1070001"),
            DISPATCH("1070002"),
            DOING("1070003"),
            FINISHED("1070004"),
            CANCELED("1070005")
        }

        var paymethod: String? = null
        var rescuestatusname: String? = null
        var status: String? = null
        var roadname: String? = null
        var rescuetype: String? = null
        var place: String? = null
        var dispatchmembernames: String? = null
        var dispatchvehicles: String? = null
        var longitude: Double? = 0.0
        var latitude: Double? = 0.0
        var user_longitude: Double? = 0.0
        var user_latitude: Double? = 0.0
        var helpteam: String? = null
        var rescueno: String? = null
        var berescuedid: String? = null
        var rescue_address: String? = null
        var phonenum: String? = null
        fun getLongitude(): Double {
            longitude?.let { return it }
            return 0.0
        }

        fun getLatitude(): Double {
            latitude?.let { return it }
            return 0.0
        }

        fun getUserLongitude(): Double {
            user_longitude?.let { return it }
            return 0.0
        }

        fun getUserLatitude(): Double {
            user_latitude?.let { return it }
            return 0.0
        }
    }

    class Track {
        var status: String? = null
        var latitude: Double? = null
        var longitude: Double? = null
        var content: String? = null
        var created: String? = null
        fun getDate(): String {
            created?.let {
                return try {
                    it.split(" ")[0]
                } catch (e: Exception) {
                    ""
                }
            }
            return ""
        }

        fun getTime(): String {
            created?.let {
                return try {
                    it.split(" ")[1]
                } catch (e: Exception) {
                    ""
                }
            }
            return ""
        }
    }
}