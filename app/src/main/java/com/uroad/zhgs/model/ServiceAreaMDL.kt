package com.uroad.zhgs.model

/**
 *Created by MFB on 2018/8/21.
 * 服务区
 */
class ServiceAreaMDL {
    var roadoldid: String? = null
    var shortname: String? = null
    var picurl: String? = null
    var serviceoilstatus:String?=null
    var serviceclosestatus:String?=null
    var service: MutableList<ServiceMDL>? = null

    fun getServiceList(): MutableList<ServiceMDL> {
        service?.let { return it }
        return ArrayList()
    }
}