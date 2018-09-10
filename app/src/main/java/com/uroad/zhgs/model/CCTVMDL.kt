package com.uroad.zhgs.model

/**
 *Created by MFB on 2018/8/16.
 * 监控快拍
 */
class CCTVMDL {
    var picurl: String? = null
    var resname: String? = null

    fun getLastPicUrl(): String {
        picurl?.let {
            val list = it.split(",").toMutableList()
            if (list.size > 0) return list[list.size - 1]
        }
        return ""
    }
}