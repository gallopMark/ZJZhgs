package com.uroad.zhgs.model


/**
 *Created by MFB on 2018/7/27.
 * roadid	路段DI
roadname	路段名称
phone	电话救助号码
mile	桩号
type	类型	0 可以救援；1 当前位置不支持救援；2 当前位置不在高速上
n_code	经纬度转换后的高速编号
注意 type不为0时，返回参数只有 type、phone、msg
 */
class LocationMDL {
    var roadid: String? = null
    var shortname: String? = null
    var phone: MutableList<Phone>? = null
    var mile: String? = null
    var type: Int? = -1
    var n_code: String? = null

    class Phone {
        var phone: String? = null
        var phonename: String? = null
    }
}