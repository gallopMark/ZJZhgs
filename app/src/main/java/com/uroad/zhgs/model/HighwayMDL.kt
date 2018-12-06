package com.uroad.zhgs.model

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.enumeration.MapDataType

/**
 *Created by MFB on 2018/8/16.
 * 参数项	名称	备注
roadoldid	路段ID
shortname	路段名称
picurl	图标
poiname	开始站 - 结束站
eventnum	事件数
up	上行路况
down	下行路况
 */
class HighwayMDL {
    var roadoldid: String? = null
    var shortname: String? = null
    var picurl: String? = null
    var poiname: String? = null
    var eventnum: MutableList<EventNum>? = null
    var up: MutableList<State>? = null
    var down: MutableList<State>? = null

    fun getEventList(): MutableList<EventNum> {
        eventnum?.let {
            val list = ArrayList<EventNum>()
            for (item in it) {
                if (!TextUtils.isEmpty(item.eventids)) list.add(item)
            }
            return list
        }
        return ArrayList()
    }

    fun getRoadUp(): MutableList<State> {
        up?.let { return it }
        return ArrayList()
    }

    fun getRoadDown(): MutableList<State> {
        down?.let { return it }
        return ArrayList()
    }

    /**
     * eventids	事件ID 逗号分隔
    num	事件数
    eventtype	事件类型	1006001 事故 ； 1006002 施工 ； 1006003 管制 ； 1006004 kuaipai
    icon	图标
     */
    class EventNum {
        var eventids: String? = null
        var num: Int = 0
        var eventtype: String? = null
        var icon: String? = null

        fun getIcon(): Int {
            eventtype?.let {
                if (it == MapDataType.ACCIDENT.code) return R.mipmap.ic_menu_event_sg_p
                if (it == MapDataType.TRAFFIC_JAM.code) return R.mipmap.ic_menu_event_yd_p
                if (it == MapDataType.CONSTRUCTION.code) return R.mipmap.ic_menu_event_shig_p
                if (it == MapDataType.CONTROL.code) return R.mipmap.ic_menu_event_gz_p
                if (it == MapDataType.BAD_WEATHER.code) return R.mipmap.ic_menu_event_eltq_p
                if (it == MapDataType.TRAFFIC_INCIDENT.code) return R.mipmap.ic_menu_event_jtsj_p
                if (it == MapDataType.SNAPSHOT_RESPONSE.code) return R.mipmap.ic_menu_jtss_spot_p
                if (it == MapDataType.SITE_CONTROL.code) return R.mipmap.ic_menu_event_zdgz_p
                if (it == MapDataType.SERVICE_AREA.code) return R.mipmap.ic_menu_jtss_service_p
            }
            return 0
        }
    }

    /**
     * color	路况颜色
    name	站点名称
    isshow	是否显示
    pointtype	站点类型	1002001 收费站 ; 1002003 枢纽
     */
    class State {
        var color: String? = null
        var name: String? = null
        var isshow: Int = 0
        var pointtype: String? = null

        fun getColor(context: Context): Int {
            color?.let {
                return try {
                    Color.parseColor(it)
                } catch (e: Exception) {
                    return ContextCompat.getColor(context, R.color.roadline_color)
                }
            }
            return ContextCompat.getColor(context, R.color.roadline_color)
        }

        fun isHinge(): Boolean {
            pointtype?.let { return it == "1002003" }
            return false
        }
    }
}