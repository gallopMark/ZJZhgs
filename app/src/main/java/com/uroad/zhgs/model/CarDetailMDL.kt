package com.uroad.zhgs.model


/**
usercarid	车辆ID
carno	车牌
carcategory	车辆类别
cartype	车辆类型
isdefault	是否默认车辆	0 否 ； 1 是
total	总重量
fixedload	核定载重
carlength	车长
carwide	车宽
carheight	车高
carlength	车长
axisnum	轴数
userid	用户id
enginno	脱敏后的发动机号
realenginno	发动机号
 */
class CarDetailMDL {
    var usercarid: String? = null
    var carno: String? = null
    var enginno: String? = null
    var carcategory: String? = null
    var cartype: String? = null
    var isdefault: Int = 0
    var total: String? = null
    var fixedload: String? = null
    var carlength: String? = null
    var carwide: String? = null
    var carheight: String? = null
    var axisnum: String? = null
    var realenginno: String? = null
    var gdcartype: String? = null
    var gdaxisnum: String? = null

    fun getCarNum(): Array<String?> {
        val array = arrayOfNulls<String>(2)
        carno?.let {
            if (it.isNotEmpty()) {
                array[0] = it.substring(0, 1)
                if (it.length > 1) {
                    array[1] = it.substring(1, it.length)
                }
            }
        }
        return array
    }
}