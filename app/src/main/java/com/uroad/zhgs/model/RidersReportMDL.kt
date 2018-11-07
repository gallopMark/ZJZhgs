package com.uroad.zhgs.model

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.utils.TimeUtil
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 *Created by MFB on 2018/8/7.
 */
class RidersReportMDL : MutilItem {
    var eventid: String? = null
    var eventtype: String? = null
    var color: String? = null
    var iconfile: String? = null
    var userid: String? = null
    var username: String? = null
    var eventtypename: String? = null
    var shortname: String? = null
    var remark: String? = null
    var occtime: String? = null
    var imgurls: String? = null
    var commentcount: Int = 0
    var supportcount: Int = 0
    var comment: MutableList<Comment>? = null
    var issupport: String? = null
    var isfollow: Int? = 0  //是否可以关注	0 已关注；1 未关注
    var category: Int = 1  // 1-图片，2-视频，3-语音
    var remark1s: String? = null
    var isfollowstatus: Int? = 0 //关注状态	该用户是否开启了可以关注的功能 0 关闭；1 开启
    var isVoicePlaying = false
    // 1015001 拥堵，1015002 事故，1015003 施工，1015004 遗洒，1015005 积水，1015006 管制
    fun getEventType(): String {
        eventtype?.let {
            return when (it) {
                "1015001" -> "拥堵"
                "1015002" -> "事故"
                "1015003" -> "施工"
                "1015004" -> "遗洒"
                "1015005" -> "积水"
                "1015006" -> "管制"
                else -> ""
            }
        }
        return ""
    }

    fun getColor(context: Context): Int {
        color?.let {
            return try {
                Color.parseColor(it)
            } catch (e: Exception) {
                ContextCompat.getColor(context, R.color.transparent)
            }
        }
        return ContextCompat.getColor(context, R.color.transparent)
    }

    fun hasComment(): Boolean {
        comment?.let {
            return it.size > 0
        }
        return false
    }

    fun getCommentList(): MutableList<Comment> {
        comment?.let { return it }
        return ArrayList()
    }

    fun getTime(): String {
        if (TextUtils.isEmpty(occtime)) return ""
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        try {
            val time = format.parse(occtime).time
            return TimeUtil.converTime(time)
        } catch (e: Exception) {
            return ""
        }
    }

    fun getImageUrls(): MutableList<String> {
        imgurls?.let {
            val photos = ArrayList<String>()
            val data = it.split(",").toMutableList()
            for (i in 0 until data.size) {
                if (!TextUtils.isEmpty(data[i])) {
                    photos.add(data[i])
                }
            }
            return photos
        }
        return ArrayList()
    }

    override fun getItemType(): Int = 1

    class Comment : MutilItem {
        var userid: String? = null
        var username: String? = null
        var usercomment: String? = null
        var intime: String? = null
        var eventid: String? = null
        var touserid: String? = null
        var tousername: String? = null

        override fun getItemType(): Int = 2
    }
}