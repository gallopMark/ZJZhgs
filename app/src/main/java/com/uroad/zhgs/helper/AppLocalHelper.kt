package com.uroad.zhgs.helper

import android.content.Context
import android.content.SharedPreferences

/**
 *Created by MFB on 2018/9/4.
 * 本地保存相关信息
 */
class AppLocalHelper {
    companion object {
        private const val APP_FILE = "app_prefs"
        private const val FIRST_INSTALL = "first_install"  //是否是第一次安装，用于启动页是否展示引导图
        private const val LAYER = "layer"   //路况导航，保存用户点击的图层，下次进入导航打开上次的图层

        private fun getPrefs(context: Context): SharedPreferences {
            return context.getSharedPreferences(APP_FILE, Context.MODE_PRIVATE)
        }

        fun isFirstInstall(context: Context): Boolean {
            return getPrefs(context).getBoolean(FIRST_INSTALL, true)
        }

        fun setFirstInstall(context: Context, isFirstInstall: Boolean) {
            getPrefs(context).edit().putBoolean(FIRST_INSTALL, isFirstInstall).apply()
        }

        fun saveLayer(context: Context, layer: Int) {
            getPrefs(context).edit().putInt(LAYER, layer).apply()
        }

        fun getLayer(context: Context): Int {
            return getPrefs(context).getInt(LAYER, 1)
        }

    }
}