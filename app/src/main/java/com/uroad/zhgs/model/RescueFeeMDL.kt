package com.uroad.zhgs.model

/**
 *Created by MFB on 2018/8/2.
 * 救援资费
 */
class RescueFeeMDL {
    var type: MutableList<Type>? = null
    var worktype: MutableList<WorkType>? = null
    var chargebasis: String? = null

    inner class Type {
        var dictcode: String? = null
        var dictname: String? = null
        var sontype: MutableList<SonType>? = null

        inner class SonType {
            var dictcode: String? = null
            var dictname: String? = null
        }
    }

    inner class WorkType {
        var dictcode: String? = null
        var dictname: String? = null
        var remark: String? = null

        fun getPic0(): String {
            remark?.let {
                if (it.split(",").isNotEmpty())
                    return it.split(",")[0]
            }
            return ""
        }

        fun getPic1(): String {
            remark?.let {
                if (it.split(",").size > 1)
                    return it.split(",")[1]
            }
            return ""
        }
    }
}