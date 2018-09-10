package com.uroad.zhgs.model

import android.text.TextUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.enumeration.MapDataType
import java.text.SimpleDateFormat
import java.util.*

/**
 *Created by MFB on 2018/8/11.
"eventid": "9ddbb3df-0270-11e8-bb6e-fa163e051d83",
"eventtype": "1006001",
"eventtypename": "事故",
"roadtitle": "G1501宁波绕城高速逆时针方向",
"reportout": "01-26 16:11 G1501宁波绕城高速丁家山往临江方向，小港站附近因施工，小港进口关闭。",
"occtime": "2018-01-26 16:11:20",
"handletime": "",
"realovertime": "2018-01-26 16:16:31",
"planovertime": "0000-00-00 00:00:00",
"statusname": "进行中",
"statuscolor": "#FF9F37",
"updatetime": "2018-01-26 16:18:06"
 */
class MapDataMDL {
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var eventid: String? = null
    var eventtype: String? = null
    var eventtypename: String? = null
    var roadtitle: String? = null
    var reportout: String? = null
    var occtime: String? = null
    var handletime: String? = null
    var realovertime: String? = null
    var planovertime: String? = null
    var statusname: String? = null
    var statuscolor: String? = null
    var updatetime: String? = null

    fun canSubscribe(): Boolean {
        eventtype?.let {
            return it == MapDataType.ACCIDENT.code || it == MapDataType.TRAFFIC_JAM.code
                    || it == MapDataType.CONSTRUCTION.code || it == MapDataType.CONTROL.code
        }
        return false
    }

    fun getIcon(): Int {
        eventtype?.let {
            if (it == MapDataType.ACCIDENT.code) return R.mipmap.ic_menu_event_sg_p
            if (it == MapDataType.TRAFFIC_JAM.code) return R.mipmap.ic_menu_event_yd_p
            if (it == MapDataType.CONSTRUCTION.code) return R.mipmap.ic_menu_event_shig_p
            if (it == MapDataType.CONTROL.code) return R.mipmap.ic_menu_event_gz_p
            if (it == MapDataType.SNAPSHOT.code) return R.mipmap.ic_menu_jtss_spot_p
        }
        return 0
    }

    fun getOccTime(): String {
        return parseDate(occtime)
    }

    fun getHandleTime(): String {
        return parseDate(handletime)
    }

    fun getRealoverTime(): String {
        return parseDate(realovertime)
    }

    private fun parseDate(timeStr: String?): String {
        if (TextUtils.isEmpty(timeStr)) return ""
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        try {
            val time = format.parse(timeStr).time
            val currYear = Calendar.getInstance().get(Calendar.YEAR)
            val year = Calendar.getInstance().apply { this.time = Date(time) }.get(Calendar.YEAR)
            val dateFormat: SimpleDateFormat
            if (year != currYear) {
                dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            } else {
                dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
            }
            return dateFormat.format(Date(time)).replace("-", ".")
        } catch (e: Exception) {
            return ""
        }
    }
}