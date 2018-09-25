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
    }
}