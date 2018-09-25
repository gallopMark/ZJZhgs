package com.uroad.zhgs.model

/**
 * @author MFB
 * @create 2018/9/21
 * @describe 路径费用
 */
class RoadTollFeeMDL {
    var length: String? = null
    var fee: MutableList<Fee>? = null

    class Fee {
        var price: String? = null
        var car_icon: String? = null
        var car_title: String? = null
    }
}