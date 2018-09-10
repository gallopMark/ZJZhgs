package com.uroad.zhgs.helper

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import java.util.*

/**
 *Created by MFB on 2018/8/14.
 */
class HighwayHotlineSearchHelper {
    companion object {
        private const val PREFS_HOTLINE_SEARCH = "hotline_search"
        private const val CONTENT = "content"

        private fun getSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_HOTLINE_SEARCH, Context.MODE_PRIVATE)
        }

        fun saveContent(context: Context, content: String) {
            val sp = getSharedPreferences(context)
            var spHistory = sp.getString(CONTENT, "")
            if (TextUtils.isEmpty(spHistory)) {
                spHistory = content
            } else {
                val data = spHistory.split(",").toMutableList()
                if (data.contains(content)) data.remove(content) //如果有相同的记录 则删除旧的，添加新的
                val sb = StringBuilder()
                for (i in 0 until data.size) {
                    sb.append(data[i])
                    if (i < data.size - 1) sb.append(",")
                }
                spHistory = sb.toString()
                if (TextUtils.isEmpty(spHistory)) spHistory = content
                else spHistory += ",$content"
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
                val data = spHistory.split(",").toMutableList()
                data.remove(content)
                val sb = StringBuilder()
                for (i in 0 until data.size) {
                    sb.append(data[i])
                    if (i < data.size - 1) sb.append(",")
                }
                sp.edit().putString(CONTENT, sb.toString()).apply()
            }
        }

        fun getHistoryList(context: Context): MutableList<String> {
            val spHistory = getSharedPreferences(context).getString(CONTENT, "")
            if (!TextUtils.isEmpty(spHistory)) {
                val data = spHistory.split(",").toMutableList()
                data.reverse()   //倒序排序
                return data
            }
            return ArrayList()
        }
    }
}