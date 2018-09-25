package com.uroad.zhgs.helper

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.uroad.zhgs.model.RoadTollGSMDL

/**
 * @author MFB
 * @create 2018/9/21
 * @describe 路径路费查询历史记录
 */
class RoadTollSearchHelper {
    companion object {
        private const val PREFS_SEARCH = "roadToll_search"
        private const val CONTENT = "content"
        private const val SPLIT = "↔"
        private const val DIVIDER = "-"
        private const val UNDER_LINE = "_"
        private fun getSharedPreferences(context: Context): SharedPreferences = context.getSharedPreferences(PREFS_SEARCH, Context.MODE_PRIVATE)

        fun saveContent(context: Context, startPoi: RoadTollGSMDL.Poi, endPoi: RoadTollGSMDL.Poi) {
            val sp = getSharedPreferences(context)
            var spHistory = sp.getString(CONTENT, "")
            val start = "${startPoi.name}$UNDER_LINE${startPoi.poiid}"
            val end = "${endPoi.name}$UNDER_LINE${endPoi.poiid}"
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
            return "${getStartPoi(content)}$DIVIDER${getEndPoi(content)}"
        }

        fun getStartPoi(content: String): String {
            return try {
                content.split(DIVIDER)[0].split(UNDER_LINE)[0]
            } catch (e: Exception) {
                ""
            }
        }

        fun getStartPoiId(content: String): String? {
            return try {
                content.split(DIVIDER)[0].split(UNDER_LINE)[1]
            } catch (e: Exception) {
                ""
            }
        }

        fun getEndPoi(content: String): String {
            return try {
                content.split(DIVIDER)[1].split(UNDER_LINE)[0]
            } catch (e: Exception) {
                ""
            }
        }

        fun getEndPoiId(content: String): String? {
            return try {
                content.split(DIVIDER)[1].split(UNDER_LINE)[1]
            } catch (e: Exception) {
                ""
            }
        }
    }
}