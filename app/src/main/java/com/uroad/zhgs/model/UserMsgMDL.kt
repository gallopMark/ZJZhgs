package com.uroad.zhgs.model

import android.text.TextUtils
import com.uroad.zhgs.utils.TimeUtil
import java.text.SimpleDateFormat
import java.util.*

/**
 *Created by MFB on 2018/8/15.
 */
class UserMsgMDL {
    var msgid: String? = null
    var msg: String? = null
    var msgtype: String? = null
    var msgtypename: String? = null
    var created: String? = null

    fun getInTime(): String {
        if (TextUtils.isEmpty(created)) return ""
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        try {
            val time = format.parse(created).time
            return TimeUtil.converTime(time)
        } catch (e: Exception) {
            return ""
        }
    }
    enum class Type(val code: String) {
        RESCUE("1140001"),
        SYSTEM("1140002")
    }
}