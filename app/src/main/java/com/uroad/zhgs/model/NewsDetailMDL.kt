package com.uroad.zhgs.model

import android.text.TextUtils
import com.uroad.zhgs.utils.NumFormat
import com.uroad.zhgs.utils.TimeUtil
import java.text.SimpleDateFormat
import java.util.*


/**
 *Created by MFB on 2018/8/14.
 */
class NewsDetailMDL {
    var title: String? = null
    var intime: String? = null
    var viewcount: Int = 0
    var html: String? = null

    fun getInTime(): String {
        if (TextUtils.isEmpty(intime)) return ""
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        try {
            val time = format.parse(intime).time
            return TimeUtil.converTime(time)
        } catch (e: Exception) {
            return ""
        }
    }

    fun getViewCount(): String {
        return NumFormat.formatNum(viewcount)
    }
}