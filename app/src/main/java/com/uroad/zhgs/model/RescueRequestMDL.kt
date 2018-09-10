package com.uroad.zhgs.model

/**
 *Created by MFB on 2018/8/2.
 * 救援请求
 */
class RescueRequestMDL {
    var type: MutableList<Type>? = null
    var direction: MutableList<Direction>? = null
    var rescuetype: MutableList<RescueType>? = null
    var myCar: MutableList<CarMDL>? = null

    class Type {
        var dictcode: String? = null
        var dictname: String? = null
        var sontype: MutableList<Sontype>? = null
    }

    class Sontype {
        var dictcode: String? = null
        var dictname: String? = null
    }

    inner class Direction {
        var directionid: String? = null
        var directionname: String? = null
    }

    class RescueType {
        var dictcode: String? = null
        var dictname: String? = null
        var sontype: MutableList<Sontype>? = null
    }
}