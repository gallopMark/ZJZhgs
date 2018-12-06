package com.uroad.zhgs.model

/**
 * @author MFB
 * @create 2018/9/19
 * @describe
 */
class RoadTollGSMDL {
    var roadoldid: String? = null
    var shortname: String? = null
    var pois: MutableList<Poi>? = null

    class Poi {
        var poiid: String? = null
        var name: String? = null
        override fun equals(other: Any?): Boolean {
            return when (other) {
                !is Poi -> false
                else -> this === other || poiid == other.poiid
            }
        }

        override fun hashCode(): Int {
            return 31 + (poiid?.hashCode() ?: 0)
        }
    }
}