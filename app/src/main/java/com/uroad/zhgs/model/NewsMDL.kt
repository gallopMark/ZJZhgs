package com.uroad.zhgs.model

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.utils.TimeUtil
import java.text.SimpleDateFormat
import java.util.*

/**
 *Created by MFB on 2018/8/7.
 * "title": "资讯测试",
"newsid": "T20180822092350921562775926",
"jpgurl": "",
"intime": "2018-08-22 09:23:50",
"newstypename": "公告",
"remark1": "#EDECFE,#8B75FB",
"detailurl": "http:\/\/zhgs.u-road.com\/ZJAppView\/newsDetail.html?dataid=T20180822092350921562775926"
 */
class NewsMDL {
    val newsid: String? = null
    var title: String? = null
    var jpgurl: String? = null
    var intime: String? = null
    var newstypename: String? = null
    var remark1: String? = null
    var detailurl: String? = null

    fun getTime(): String {
        if (TextUtils.isEmpty(intime)) return ""
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        try {
            val time = format.parse(intime).time
            return TimeUtil.converTime(time)
        } catch (e: Exception) {
            return ""
        }
    }

    fun getBgColor(context: Context): Int {
        remark1?.let {
            val arr = it.split(",")
            if (arr.isNotEmpty()) {
                try {
                    return Color.parseColor(arr[0])
                } catch (e: Exception) {
                    return ContextCompat.getColor(context, R.color.transparent)
                }
            }
        }
        return ContextCompat.getColor(context, R.color.transparent)
    }

    fun getTextColor(context: Context): Int {
        remark1?.let {
            val arr = it.split(",")
            if (arr.isNotEmpty()) {
                try {
                    return Color.parseColor(arr[1])
                } catch (e: Exception) {
                    return ContextCompat.getColor(context, R.color.grey)
                }
            }
        }
        return ContextCompat.getColor(context, R.color.grey)
    }
}