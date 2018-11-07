package com.uroad.zhgs.model

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import com.amap.api.maps.model.LatLng
import com.uroad.zhgs.R
import com.uroad.zhgs.cluster.ClusterItem
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**
 *Created by MFB on 2018/8/22.
 * 拥堵返回参数说明
"subscribestatus": "0",
"subtype": "1170004",
"eventid": "330115348821271",
"shortname": "钱江通道",
"directiname": "双向",
"pubtime": "2018-08-22 04:12:47",
"jamspeed": "4",
"jamdist": "1.32",
"longtime": "2",
"xy": "120.546791,30.320795",
"statusname": "进行中",
"statuscolor": "#FF9F37",
"updatetime": "2018-08-22 04:13:47",
"eventstatus": "趋向严重",
"roadtitle": "钱江通道双向",
"content": "钱江通道双向趋向严重",
"latitude": "30.320795",
"longitude": "120.546791"
 */
class TrafficJamMDL : MutilItem, ClusterItem, Serializable {
    override fun getItemType(): Int = 2

    var subscribestatus: Int? = 0
    var eventid: String? = null
    var shortname: String? = null
    var directiname: String? = null
    var subtype: String? = null
    var pubtime: String? = null
    var jamspeed: String? = null
    var jamdist: String? = null
    var longtime: String? = null
    var xy: String? = null
    var roadtitle: String? = null
    var latitude: Double? = 0.0
    var longitude: Double? = 0.0
    var statusname: String? = null
    var statuscolor: String? = null
    var content: String? = null
    var updatetime: String? = null
    var eventstatus: String? = null
    var isuseful: Int? = 0

    var markerIcon: Int = R.mipmap.ic_marker_yd_icon
    var markerBigIco: Int = R.mipmap.ic_marker_yd_big_icon

    fun latitude(): Double {
        latitude?.let { return it }
        return 0.0
    }

    fun longitude(): Double {
        longitude?.let { return it }
        return 0.0
    }

    fun getPubTime(): String {
        return parseDate(pubtime)
    }

    fun getLongTime(size: Int, dip: Boolean): SpannableString {
        longtime?.let {
            try {
                val min = it.toInt()
                if (min < 60) {
                    val source = "${min}min"
                    return SpannableString(source).apply { setSpan(AbsoluteSizeSpan(size, dip), 0, source.indexOf("m"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }
                } else if (min > 60 && min < 60 * 24) {
                    val hour = min / 60
                    val minute = min % 60
                    val source = "${hour}h${minute}min"
                    return SpannableString(source).apply {
                        setSpan(AbsoluteSizeSpan(size, dip), 0, source.indexOf("h"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        setSpan(AbsoluteSizeSpan(size, dip), source.indexOf("h") + 1, source.indexOf("m"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                } else {
                    val day = min / 60 / 24
                    val hour = min % (60 / 24)
                    val source = "${day}d${hour}h"
                    return SpannableString(source).apply {
                        setSpan(AbsoluteSizeSpan(size, dip), 0, source.indexOf("d"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        setSpan(AbsoluteSizeSpan(size, dip), source.indexOf("d") + 1, source.indexOf("h"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            } catch (e: Exception) {
                return SpannableString("--")
            }
        }
        return SpannableString("--")
    }

    fun getSubType(): String {
        subtype?.let { return it }
        return ""
    }

    fun getStatusColor(context: Context): Int {
        statuscolor?.let {
            return try {
                Color.parseColor(it)
            } catch (e: Exception) {
                ContextCompat.getColor(context, R.color.status_normal)
            }
        }
        return ContextCompat.getColor(context, R.color.status_normal)
    }


    private fun parseDate(timeStr: String?): String {
        if (TextUtils.isEmpty(timeStr)) return ""
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return try {
            val time = format.parse(timeStr).time
            val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            val calendar = Calendar.getInstance()
            calendar.time = Date(time)
            val thisDay = calendar.get(Calendar.DAY_OF_MONTH)
            val dateFormat = if (thisDay == currentDay) {
                SimpleDateFormat("HH:mm", Locale.getDefault())
            } else {
                SimpleDateFormat("MM.dd HH:mm", Locale.getDefault())
            }
            return dateFormat.format(time)
        } catch (e: Exception) {
            ""
        }
    }

    override fun getPosition(): LatLng = LatLng(latitude(), longitude())
    override fun getMarkerSmallIcon(): Int = markerIcon
    override fun getMarkerBigIcon(): Int = markerBigIco
}