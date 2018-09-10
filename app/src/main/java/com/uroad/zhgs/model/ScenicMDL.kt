package com.uroad.zhgs.model

import com.uroad.zhgs.R
import java.io.Serializable

/**
 *Created by MFB on 2018/8/23.
 * 景点
"latitude": "30.2519760",
"longitude": "120.1861810",
"name": "横河公园",
"picurls": "http:\/\/zhgs.u-road.com\/ZJAppApi\/defaulticon\/ly.jpg",
"newsid": "235",
"address": "环城东路22-13号",
"distance": "4.88",
"detailurl": "http:\/\/zhgs.u-road.com\/ZJAppView\/touristDetail.html?dataid=235"
 */
class ScenicMDL : MutilItem, Serializable {
    override fun getItemType(): Int = 7
    var name: String? = null
    var longitude: Double? = 0.0
    var latitude: Double? = 0.0
    var picurls: String? = null
    var newsid: String? = null
    var address: String? = null
    var detailurl: String? = null
    var distance: String? = null

    var markerIcon: Int = R.mipmap.ic_marker_secnic_icon
    var markerBigIco: Int = R.mipmap.ic_marker_secnic_big_icon

    fun latitude(): Double {
        latitude?.let { return it }
        return 0.0
    }

    fun longitude(): Double {
        longitude?.let { return it }
        return 0.0
    }
}