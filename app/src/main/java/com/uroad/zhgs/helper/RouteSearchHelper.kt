package com.uroad.zhgs.helper

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.amap.api.col.sln3.it
import com.amap.api.services.core.LatLonPoint
import com.uroad.zhgs.utils.GsonUtils
import java.util.ArrayList

/**
 *Created by MFB on 2018/8/27.
 */
class RouteSearchHelper {
    class MyLatLonPoint {
        var longitude: Double = 0.0
        var latitude: Double = 0.0
    }

    companion object {
        private const val PREFS_ROUTE_SEARCH = "route_search"
        private const val CONTENT = "content"
        private const val SPLIT = "!"
        private const val DIVIDER = "-"
        private const val UNDER_LINE = "_"

        private fun getSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_ROUTE_SEARCH, Context.MODE_PRIVATE)
        }

        fun saveContent(context: Context, startPos: String, startPoint: LatLonPoint,
                        endPos: String, endPoint: LatLonPoint) {
            val sp = getSharedPreferences(context)
            var spHistory = sp.getString(CONTENT, "")
            val startLLP = MyLatLonPoint().apply {
                longitude = startPoint.longitude
                latitude = startPoint.latitude
            }
            val endLLP = MyLatLonPoint().apply {
                longitude = endPoint.longitude
                latitude = endPoint.latitude
            }
            val start = "$startPos$UNDER_LINE${GsonUtils.fromObjectToJson(startLLP)}"
            val end = "$endPos$UNDER_LINE${GsonUtils.fromObjectToJson(endLLP)}"
            if (TextUtils.isEmpty(spHistory)) {
                spHistory = "$start$DIVIDER$end"
            } else {
                val data = spHistory.split(SPLIT).toMutableList()
                val content1 = "$start$DIVIDER$end"
                val content2 = "$end$DIVIDER$start"
                if (data.contains(content1)) data.remove(content1)
                if (data.contains(content2)) data.remove(content2)
                //如果有相同的记录 则删除旧的，添加新的
                val sb = StringBuilder()
                for (i in 0 until data.size) {
                    sb.append(data[i])
                    if (i < data.size - 1) sb.append(SPLIT)
                }
                spHistory = sb.toString()
                if (TextUtils.isEmpty(spHistory)) spHistory = "$start$DIVIDER$end"
                else spHistory += "$SPLIT$start$DIVIDER$end"
            }
            sp.edit().putString(CONTENT, spHistory).apply()
        }

        fun clear(context: Context) {
            getSharedPreferences(context).edit().clear().apply()
        }

        fun deleteItem(context: Context, content: String) {
            val sp = getSharedPreferences(context)
            val spHistory = sp.getString(CONTENT, "")
            if (!TextUtils.isEmpty(spHistory)) {
                val data = spHistory.split(SPLIT).toMutableList()
                data.remove(content)
                val sb = StringBuilder()
                for (i in 0 until data.size) {
                    sb.append(data[i])
                    if (i < data.size - 1) sb.append(SPLIT)
                }
                sp.edit().putString(CONTENT, sb.toString()).apply()
            }
        }

        fun getHistoryList(context: Context): MutableList<String> {
            val spHistory = getSharedPreferences(context).getString(CONTENT, "")
            if (!TextUtils.isEmpty(spHistory)) {
                val data = spHistory.split(SPLIT).toMutableList()
                data.reverse()
                return data
            }
            return ArrayList()
        }

        fun content(content: String): String {
            return "${getStartPos(content)}$DIVIDER${getEndPos(content)}"
        }

        fun getStartPos(content: String): String {
            return try {
                content.split(DIVIDER)[0].split(UNDER_LINE)[0]
            } catch (e: Exception) {
                ""
            }
        }

        fun getStartPoint(content: String): LatLonPoint? {
            try {
                val json = content.split(DIVIDER)[0].split(UNDER_LINE)[1]
                val latLonPoint = GsonUtils.fromJsonToObject(json, MyLatLonPoint::class.java)
                latLonPoint?.let { return LatLonPoint(it.latitude, it.longitude) }
                return null
            } catch (e: Exception) {
                return null
            }
        }

        fun getEndPos(content: String): String {
            return try {
                content.split(DIVIDER)[1].split(UNDER_LINE)[0]
            } catch (e: Exception) {
                ""
            }
        }

        fun getEndPoint(content: String): LatLonPoint? {
            try {
                val json = content.split(DIVIDER)[1].split(UNDER_LINE)[1]
                val latLonPoint = GsonUtils.fromJsonToObject(json, MyLatLonPoint::class.java)
                latLonPoint?.let { return LatLonPoint(it.latitude, it.longitude) }
                return null
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
    }
}