package com.uroad.zhgs.model

import android.text.TextUtils

/**
 *Created by MFB on 2018/8/14.
 * "usercarid": "1",
"carno": "粤A12345",
"carcategory": "1000002",
"cartype": "1010003",
"isdefault": "1",
"total": "3",
"fixedload": "4",
"carlength": "5",
"carwide": "6",
"carheight": "7",
"axisnum": "111002",
"userid": "170376"
 */
class CarDetailMDL {
    var usercarid: String? = null
    var carno: String? = null
    var carcategory: String? = null
    var cartype: String? = null
    var isdefault: Int = 0
    var total: String? = null
    var fixedload: String? = null
    var carlength: String? = null
    var carwide: String? = null
    var carheight: String? = null
    var axisnum: String? = null

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