package com.uroad.zhgs.model

/**
 *Created by MFB on 2018/8/13.
carid	车辆ID
carno	车牌
carcategory	车辆类别	1000002 客车；1000003 货车
carcategoryname	车辆类别名称
cartypename	车辆类型名称
isdefault	是否默认	0 否 ； 1 是
 */
class CarMDL {
    var carid: String? = null
    var carno: String? = null
    var carcategory: String? = null
    var carcategoryname: String? = null
    var cartypename: String? = null
    var isdefault: Int = 0
}