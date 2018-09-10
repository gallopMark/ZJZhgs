package com.uroad.zhgs.model

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import com.amap.api.col.sln3.it
import com.uroad.zhgs.R
import com.uroad.zhgs.enumeration.MapDataType
import com.uroad.zhgs.photopicker.model.ImageItem

/**
 *Created by MFB on 2018/8/16.
 * 高速快览
 */
class HighwayPreViewMDL {
    enum class PointType(val code: String) {
        //1002001 收费站 ； 1002003 枢纽
        TOLL("1002001"),
        HINGE("1002003")
    }

    var shortname: String? = null
    var picurl: String? = null
    var traffic: MutableList<Traffic>? = null

    /**
     * "color": "#00DC00",
    "eventids": null,
    "cctvids": null,
    "stationid": "13101",
    "name": "海盐枢纽",
    "pointtype": "1002003",
    "roadids": "23,28,30",
    "poistatus": "1",
    "detailurl": "http:\/\/zhgs.u-road.com\/ZJAppView\/stationDetail.html?dataid=13101",
     */
    class Traffic {
        var color: String? = null
        var eventids: String? = null
        var cctvids: String? = null
        var stationid: String? = null
        var name: String? = null
        var pointtype: String? = null
        var poistatus: Int? = null
        var roadids: String? = null
        var roads: MutableList<Road>? = null
        var detailurl: String? = null

        fun isHinge(): Boolean {  //是否是枢纽
            pointtype?.let { return it == PointType.HINGE.code }
            return false
        }

        fun isNormal(): Boolean {
            poistatus?.let { return it == 1 }
            return false
        }

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

        fun getEventList(): MutableList<Event> {
            val data = ArrayList<Event>()
            eventids?.let {
                val arr = it.split(",")
                for (item in arr) {
                    val arr2 = item.split("/")
                    try {
                        data.add(Event().apply {
                            eventid = arr2[0]
                            eventtype = arr2[1]
                        })
                    } catch (e: Exception) {
                        continue
                    }
                }
            }
            val list = ArrayList<Event>()
            cctvids?.let { list.add(Event().apply { cctvids = it }) }
            for (item in data) {
                if (!list.contains(item)) {
                    list.add(item.apply { item.eventid?.let { item.eventIds = it } })
                } else {
                    item.eventid?.let { list[list.indexOf(item)].eventIds += ",$it" }
                }
            }
            return list
        }

        fun getRoadName(): String {
            roads?.let { roads ->
                if (roads.size > 0) {
                    roads[0].shortname?.let { return it }
                }
            }
            return ""
        }

        class Road {
            var shortname: String? = null
            var roadoldid: String? = null
        }

        class Event {
            var eventid: String? = null
            var eventtype: String? = null
            var eventIds: String = ""
            var cctvids: String = ""
            val cctvIco = R.mipmap.ic_menu_jtss_spot_p
            /* 图片的路径和创建时间相同就认为是同一张图片*/
            override fun equals(other: Any?): Boolean {
                return when (other) {
                    !is Event -> false
                    else -> this === other || getEventType() == other.getEventType()
                }
            }

            override fun hashCode(): Int {
                return 31 + (eventtype?.hashCode() ?: 0)
            }

            fun getEventType(): String {
                eventtype?.let { return it }
                return ""
            }

            fun getIcon(): Int {
                eventtype?.let {
                    if (it == MapDataType.ACCIDENT.code) return R.mipmap.ic_menu_event_sg_p
                    if (it == MapDataType.TRAFFIC_JAM.code) return R.mipmap.ic_menu_event_yd_p
                    if (it == MapDataType.CONSTRUCTION.code) return R.mipmap.ic_menu_event_shig_p
                    if (it == MapDataType.CONTROL.code) return R.mipmap.ic_menu_event_gz_p
                }
                return 0
            }
        }
    }
}