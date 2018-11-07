package com.uroad.zhgs.model.sys

class SysConfigMDL {
    companion object {
        const val VOICE_MAX_SEC = "voicemaxsec" //最大录音时长
        const val VIDEO_MAX_SEC = "videomaxsec" //最大视频录制时长
        const val WISDOM_URL = "xzwwurl"  //小智问问url
        const val ALINE_URL= "gszburl" //高速直播url
        const val BREAK_RULES_URL = "wfcxurl" //违章查询url
    }

    var syscode: String? = null
    var sysvalue: String? = null

    fun getVoiceValue(value: String?): Int {
        value?.let {
            return try {
                it.toInt()
            } catch (e: Exception) {
                20
            }
        }
        return 20
    }

    fun getVideoValue(value: String?): Int {
        value?.let {
            return try {
                it.toInt()
            } catch (e: Exception) {
                20
            }
        }
        return 20
    }
}