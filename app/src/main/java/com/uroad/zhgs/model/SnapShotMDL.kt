package com.uroad.zhgs.model

import android.text.TextUtils

/**
 *Created by MFB on 2018/8/22.
 * 监控快拍
"roadoldid": "6",
"latitude": "29.2186850",
"longitude": "119.9622870",
"shortname": "甬金高速",
"picurl": ""
 */
class SnapShotMDL : MutilItem {
    override fun getItemType(): Int = 3
    var cctvids: String? = null
    var roadoldid: String? = null
    var latitude: Double? = 0.0
    var longitude: Double? = 0.0
    var shortname: String? = null
    var picurl: String? = null

    var markerIcon: Int = 0
    var markerBigIco: Int = 0

    fun latitude(): Double {
        latitude?.let { return it }
        return 0.0
    }

    fun longitude(): Double {
        longitude?.let { return it }
        return 0.0
    }

    fun getPicUrls(): MutableList<String> {
        picurl?.let {
            val list = it.split(",").toMutableList()
            val data = ArrayList<String>()
            for (item in list) {
                if (!TextUtils.isEmpty(item)) {
                    data.add(item)
                }
            }
            return data
        }
        return ArrayList()
    }
}