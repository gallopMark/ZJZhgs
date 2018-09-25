package com.uroad.zhgs.model

import android.text.TextUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.enumeration.MapDataType
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**
 *Created by MFB on 2018/8/15.
 * 事件
 * 突发、施工、管制返回参数说明

参数项	名称	备注
subscribestatus	订阅状态	0未订阅；1已订阅
latitude	纬度
longitude	经度
eventid	事件ID
eventtype	事件类型
subtype	订阅类型	1170001 突发事件；1170002 计划施工；1170003 管制事件
eventtypename	事件名称
roadtitle	高速名称&方向
reportout	内容
occtime	开始时间
handletime	处理时间
realovertime	结束时间
planovertime	计划结束时间
statusname	状态
statuscolor	状态颜色
updatetime	更新时间	单位 min
 */
class EventMDL : MutilItem, Serializable {
    override fun getItemType(): Int = 1
    var subscribestatus: Int? = 0
    var latitude: Double? = 0.0
    var longitude: Double? = 0.0
    var eventid: String? = null
    var eventtype: String? = null
    var subtype: String? = null
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

    fun getSubType(): String {
        subtype?.let { return it }
        if (eventtype == MapDataType.ACCIDENT.code)
            return SubscribeMDL.SubType.Emergencies.code
        if (eventtype == MapDataType.CONSTRUCTION.code)
            return SubscribeMDL.SubType.Planned.code
        if (eventtype == MapDataType.CONTROL.code)
            return SubscribeMDL.SubType.Control.code
        return ""
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

    fun getRealoverTime(): String {
        if (TextUtils.isEmpty(parseDate(realovertime)))
            return "--"
        return parseDate(realovertime)
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

    fun getUpdateTime(): String {
        return parseDate(updatetime)
    }
}