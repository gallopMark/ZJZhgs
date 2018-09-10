package com.uroad.zhgs.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Created by MFB on 2018/6/8.
 */
class TimeUtil {
    companion object {
        private val minute = (60 * 1000).toLong()// 1分钟
        private val hour = 60 * minute// 1小时
        private val day = 24 * hour// 1天
        private val month = 31 * day// 月
        private val year = 12 * month// 年

        fun converTime(timestamp: Long): String {
            val diff = Date().time - Date(timestamp).time
            if (diff > year) {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                return format.format(Date(timestamp))
            }
            if (diff > month) {
                return (diff / month).toString() + "个月前"
            }
            if (diff > day) {
                return (diff / day).toString() + "天前"
            }
            if (diff > hour) {
                return (diff / hour).toString() + "个小时前"
            }
            return if (diff > minute) {
                (diff / minute).toString() + "分钟前"
            } else "刚刚"
        }
    }

}
