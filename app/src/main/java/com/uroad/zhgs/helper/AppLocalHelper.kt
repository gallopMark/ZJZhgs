package com.uroad.zhgs.helper

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils

/**
 *Created by MFB on 2018/9/4.
 * 本地保存相关信息
 */
class AppLocalHelper {
    companion object {
        private const val APP_FILE = "app_prefs"
        private const val FIRST_INSTALL = "first_install"  //是否是第一次安装，用于启动页是否展示引导图
        private const val LAYER = "layer"   //路况导航，保存用户点击的图层，下次进入导航打开上次的图层
        private const val AGREE_RIDERS = "agree_riders"  //是否同意了“车友组队出行服务协议”
        private const val NAVI_VRE = "navi_ver" //首页菜单版本
        private const val NAVI_DATA = "navi_data"  //首页版本数据
        private const val SVG_VER = "svg_ver"  //简图版本号
        private const val SYS_VER = "sys_ver" //app配置版本
        private const val VOICE_MAX_SEC = "voice_max_sec"
        private const val VIDEO_MAX_SEC = "video_max_sec"
        private const val WISDOM_URL = "wisdom_url"  //小智问问url
        private const val ALIVE_URL = "alive_url"  //直播url
        private const val BREAK_RULES_URL = "break_rules_url"  //违章查询url
        private const val AUTHENTICATION = "Authentication" //是否弹过认证对话框
        private const val AUTH_VER = "auth_ver"  //功能是否需要实名认证
        private const val AUTH_TXJL = "auth_txjl" //通行记录是否要实名认证
        private const val AUTH_CYZD = "auth_cyzd"   //车友组队是否要实名认证
        private const val AUTH_GSJY = "auth_gsjy" //高速救援是否要实名认证
        private const val AUTH_BLFB = "auth_blfb" //爆料发布是否要实名认证
        private const val NAVIGATE_VER = "navigationVer" //新功能内容版本

        private fun getPrefs(context: Context): SharedPreferences = context.getSharedPreferences(APP_FILE, Context.MODE_PRIVATE)

        fun isFirstInstall(context: Context): Boolean = getPrefs(context).getBoolean(FIRST_INSTALL, true)

        fun setFirstInstall(context: Context, isFirstInstall: Boolean) = getPrefs(context).edit().putBoolean(FIRST_INSTALL, isFirstInstall).apply()

        fun saveLayer(context: Context, layer: Int) = getPrefs(context).edit().putInt(LAYER, layer).apply()

        fun getLayer(context: Context): Int = getPrefs(context).getInt(LAYER, 1)

        fun saveNavVersion(context: Context, version: String?) = getPrefs(context).edit().putString(NAVIGATE_VER, version).apply()

        fun getNavVersion(context: Context): String? = getPrefs(context).getString(NAVIGATE_VER, "")

        fun saveAgreeRiders(context: Context, isAgree: Boolean) = getPrefs(context).edit().putBoolean(AGREE_RIDERS, isAgree).apply()

        fun isAgreeRiders(context: Context) = getPrefs(context).getBoolean(AGREE_RIDERS, false)

        fun saveFootprint(context: Context, userId: String?, adCode: String?) {
            if (TextUtils.isEmpty(userId)) return
            var spHistory: String? = getPrefs(context).getString(userId, "")
            if (TextUtils.isEmpty(spHistory)) {
                spHistory = adCode
            } else {
                spHistory += ",$adCode"
            }
            getPrefs(context).edit().putString(userId, spHistory).apply()
        }

        fun containsFootprint(context: Context, userId: String?, adCode: String?): Boolean {
            val spHistory: String? = getPrefs(context).getString(userId, "")
            spHistory?.let { return it.split(",").contains(adCode) }
            return false
        }

        fun saveNaviVer(context: Context, ver: String?) = getPrefs(context).edit().putString(NAVI_VRE, ver).apply()

        fun getNaviVer(context: Context) = getPrefs(context).getString(NAVI_VRE, "1.0")
        fun saveNaviData(context: Context, data: String?) = getPrefs(context).edit().putString(NAVI_DATA, data).apply()
        fun getNaviData(context: Context) = getPrefs(context).getString(NAVI_DATA, "")

        fun saveSvgVer(context: Context, ver: String?) = getPrefs(context).edit().putString(SVG_VER, ver).apply()
        fun getSvgVer(context: Context): String? = getPrefs(context).getString(SVG_VER, "1.0")
        fun saveSysVer(context: Context, ver: String?) = getPrefs(context).edit().putString(SYS_VER, ver).apply()
        fun getSysVer(context: Context): String? = getPrefs(context).getString(SYS_VER, "1.0")
        fun saveAuthVer(context: Context, ver: String?) = getPrefs(context).edit().putString(AUTH_VER, ver).apply()
        fun getAuthVer(context: Context) = getPrefs(context).getString(AUTH_VER, "0.0")

        fun saveVoiceMax(context: Context, max: Int) = getPrefs(context).edit().putInt(VOICE_MAX_SEC, max).apply()
        fun getVoiceMax(context: Context) = getPrefs(context).getInt(VOICE_MAX_SEC, 20)
        fun saveVideoMax(context: Context, max: Int) = getPrefs(context).edit().putInt(VIDEO_MAX_SEC, max).apply()
        fun getVideoMax(context: Context) = getPrefs(context).getInt(VIDEO_MAX_SEC, 20)
        fun saveWisdomUrl(context: Context, url: String?) = getPrefs(context).edit().putString(WISDOM_URL, url).apply()
        fun getWisdomUrl(context: Context) = getPrefs(context).getString(WISDOM_URL, "")
        fun saveAliveUrl(context: Context, url: String?) = getPrefs(context).edit().putString(ALIVE_URL, url).apply()
        fun getAliveUrl(context: Context) = getPrefs(context).getString(ALIVE_URL, "")
        fun saveBreakRulesUrl(context: Context, url: String?) = getPrefs(context).edit().putString(BREAK_RULES_URL, url).apply()
        fun getBreakRulesUrl(context: Context) = getPrefs(context).getString(BREAK_RULES_URL, "")

        fun saveAuth(context: Context) = getPrefs(context).edit().putBoolean(AUTHENTICATION, true).apply()
        fun isAuth(context: Context) = getPrefs(context).getBoolean(AUTHENTICATION, false)

        fun isAuthTXJL(context: Context) = getPrefs(context).getBoolean(AUTH_TXJL, false)
        fun saveAuthTXJL(context: Context, needAuth: Boolean) = getPrefs(context).edit().putBoolean(AUTH_TXJL, needAuth).apply()
        fun isAuthCYZD(context: Context) = getPrefs(context).getBoolean(AUTH_CYZD, false)
        fun saveAuthCYZD(context: Context, needAuth: Boolean) = getPrefs(context).edit().putBoolean(AUTH_CYZD, needAuth).apply()
        fun isAuthGSJY(context: Context) = getPrefs(context).getBoolean(AUTH_GSJY, false)
        fun saveAuthGSJY(context: Context, needAuth: Boolean) = getPrefs(context).edit().putBoolean(AUTH_GSJY, needAuth).apply()
        fun isAuthBLFB(context: Context) = getPrefs(context).getBoolean(AUTH_BLFB, false)
        fun saveAuthBLFB(context: Context, needAuth: Boolean) = getPrefs(context).edit().putBoolean(AUTH_BLFB, needAuth).apply()
    }
}