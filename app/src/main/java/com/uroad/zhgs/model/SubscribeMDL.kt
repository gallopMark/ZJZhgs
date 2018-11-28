package com.uroad.zhgs.model

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import com.amap.api.col.sln3.it
import com.uroad.zhgs.R
import com.uroad.zhgs.enumeration.EventType
import com.uroad.zhgs.enumeration.MapDataType
import com.uroad.zhgs.utils.TimeUtil
import java.text.SimpleDateFormat
import java.util.*
import android.text.format.DateFormat.getDateFormat


/**
 *Created by MFB on 2018/8/15.
 *   "eventid": "96ea8a25-6934-11e8-ac2d-fa163e051d83",
"eventtype": "1006002",
"eventtypename": "施工",
"roadtitle": "G25长深高速（杭宁）往江苏方向",
"reportout": "06-06 10:51 G25长深高速(杭宁)往江苏方向距湖州北站2公里有施工，后方缓行3公里，江苏方向余杭、德清、德清北、青山、湖州南进口关闭，请您注意避让。",
"occtime": "2018-06-06 10:51:41",
"handletime": "",
"realovertime": "2018-06-06 15:11:57",
"subscribeid": "2"
参数项	名称	备注
eventid	事件ID
eventtype	事件类型
eventtypename	事件类型名称
roadtitle	事件小标题
reportout	事件描述
occtime	开始时间
handletime	处理时间
realovertime	结束时间
 */
class SubscribeMDL {
    enum class SubType(val code: String) {
        /**
         * 1170001 突发事件 ： 1170002 计划施工 ； 1170003 管制事件 ； 1170004 拥堵
         */
        //subtype等于1170005【救援进展
        //subtype等于1170006【救援缴费
        Emergencies("1170001"),
        Planned("1170002"),
        Control("1170003"),
        TrafficJam("1170004"),
        RescueProgress("1170005"),
        RescuePay("1170006")
    }

    /**
     * 1170001 突发事件 、 1170002 计划事件、1170003 管制事件
     * 显示格式跟事件列表一致；
     */
    var subscribeid: String? = null
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
    var isuseful:Int?=0
    /*1170004 拥堵 显示格式跟拥堵详情一致
    * "eventid": "330515349170521",
		"shortname": "练杭高速",
		"eventtype": "8",
		"directiname": "往杭州",
		"pubtime": "2018-08-22 14:30:52",
		"jamspeed": "27",
		"jamdist": "0.58",
		"longtime": "138",
		"xy": "120.174202,30.495474",
		"statusname": "进行中",
		"statuscolor": "#FF9F37",
		"updatetime": "",
		"eventstatus": "拥堵",
		"subscribeid": "62",
		"roadtitle": "练杭高速往杭州",
		"content": "练杭高速往杭州拥堵",
		"latitude": "30.495474",
		"longitude": "120.174202",
		"subtype": "1170004"
    * */
    var subscribestatus: Int? = 0
    var shortname: String? = null
    var directiname: String? = null
    var pubtime: String? = null
    var jamspeed: String? = null
    var jamdist: String? = null
    var longtime: String? = null
    var xy: String? = null
    var eventstatus: String? = null

    /*1170005【救援进展】
roadname	高速名称
created	求助时间
accepttime	接受任务时间
starttime	出发时间
arrivetime	到达时间
overtime	结束时间
content	最新一条进展内容；空代表暂无
eventtype	事件类型
    */
    var rescueid: String? = null
    var roadname: String? = null
    var accepttime: String? = null
    var created: String? = null
    var starttime: String? = null
    var arrivetime: String? = null
    var overtime: String? = null
    var content: String? = null

    /*1170006【救援缴费】*/
    var dataid: String? = null
    var msg: String? = null

    fun getIcon(): Int {
        eventtype?.let {
            if (it == MapDataType.ACCIDENT.code) return R.mipmap.ic_menu_event_sg_p
            if (it == MapDataType.TRAFFIC_JAM.code) return R.mipmap.ic_menu_event_yd_p
            if (it == MapDataType.CONSTRUCTION.code) return R.mipmap.ic_menu_event_shig_p
            if (it == MapDataType.CONTROL.code) return R.mipmap.ic_menu_event_gz_p
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
        if (TextUtils.isEmpty(parseDate(realovertime)))
            return "--"
        return parseDate(realovertime)
    }

    fun getPlanOverTime(): String {
        if (TextUtils.isEmpty(parseDate(planovertime)))
            return "--"
        return parseDate(planovertime)
    }

    fun getAcceptTime(): String {
        if (TextUtils.isEmpty(parseDate(accepttime)))
            return "--"
        return parseDate(accepttime)
    }

    fun getStartTime(): String {
        if (TextUtils.isEmpty(parseDate(starttime)))
            return "--"
        return parseDate(starttime)
    }

    fun getArriveTime(): String {
        if (TextUtils.isEmpty(parseDate(arrivetime)))
            return "--"
        return parseDate(arrivetime)
    }

    fun getOverTime(): String {
        if (TextUtils.isEmpty(parseDate(overtime)))
            return "--"
        return parseDate(overtime)
    }

    fun getCreateTime(): String {
        created?.let {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return try {
                TimeUtil.converTime(format.parse(it).time)
            } catch (e: Exception) {
                ""
            }
        }
        return ""
    }

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

    fun getUpdateTime(): String {
        return parseDate(updatetime)
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

    fun getEventMDL(): EventMDL {
        val mdl = EventMDL()
        mdl.subscribestatus = subscribestatus
        mdl.latitude = latitude
        mdl.longitude = longitude
        mdl.eventid = eventid
        mdl.eventtype = eventtype
        mdl.subtype = subtype
        mdl.eventtypename = eventtypename
        mdl.roadtitle = roadtitle
        mdl.reportout = reportout
        mdl.occtime = occtime
        mdl.handletime = handletime
        mdl.realovertime = realovertime
        mdl.planovertime = planovertime
        mdl.statusname = statusname
        mdl.statuscolor = statuscolor
        mdl.updatetime = updatetime
        mdl.isuseful = isuseful
        val markerIcon: Int
        val markerBigIco: Int
        when (mdl.getSubType()) {
            SubscribeMDL.SubType.Control.code -> {
                markerIcon = R.mipmap.ic_marker_gz_icon
                markerBigIco = R.mipmap.ic_marker_gz_big_icon
            }
            SubscribeMDL.SubType.Emergencies.code -> {
                markerIcon = R.mipmap.ic_marker_sg_icon
                markerBigIco = R.mipmap.ic_marker_sg_big_icon
            }
            else -> {
                markerIcon = R.mipmap.ic_marker_shig_icon
                markerBigIco = R.mipmap.ic_marker_shig_big_icon
            }
        }
        mdl.markerIcon = markerIcon
        mdl.markerBigIco = markerBigIco
        return mdl
    }

    fun getTrafficJamMDL(): TrafficJamMDL {
        val mdl = TrafficJamMDL()
        mdl.subscribestatus = subscribestatus
        mdl.eventid = eventid
        mdl.shortname = shortname
        mdl.directiname = directiname
        mdl.subtype = subtype
        mdl.pubtime = pubtime
        mdl.jamspeed = jamspeed
        mdl.jamdist = jamdist
        mdl.longtime = longtime
        mdl.xy = xy
        mdl.roadtitle = roadtitle
        mdl.latitude = latitude
        mdl.longitude = longitude
        mdl.statusname = statusname
        mdl.statuscolor = statuscolor
        mdl.content = content
        mdl.updatetime = content
        mdl.isuseful = isuseful
        mdl.markerIcon = R.mipmap.ic_marker_yd_icon
        mdl.markerBigIco = R.mipmap.ic_marker_yd_big_icon
        return mdl
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            !is SubscribeMDL -> false
            else -> this === other || subscribeid == other.subscribeid
        }
    }

    override fun hashCode(): Int {
        return 31 + (subscribeid?.hashCode() ?: 0)
    }

}