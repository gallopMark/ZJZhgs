package com.uroad.zhgs.utils

import android.annotation.SuppressLint
import android.view.Window
import android.view.WindowManager

/**
 *Created by MFB on 2018/8/10.
 */
class StatusBarUtils {
    companion object {

        @SuppressLint("PrivateApi")
        fun statusBarMIUILightMode(window: Window?, dark: Boolean): Boolean {
            var result = false
            window?.let {
                val clazz = it.javaClass
                try {
                    val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
                    val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
                    val darkModeFlag = field.getInt(layoutParams)
                    val extraFlagField = clazz.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                    if (dark) {
                        extraFlagField.invoke(window, darkModeFlag, darkModeFlag)//状态栏透明且黑色字体
                    } else {
                        extraFlagField.invoke(window, 0, darkModeFlag)//清除黑色字体
                    }
                    result = true
                } catch (e: Exception) {

                }
            }
            return result
        }

        fun statusBarFlymeLightMode(window: Window?, dark: Boolean): Boolean {
            var result = false
            window?.let {
                try {
                    val lp = window.attributes
                    val darkFlag = WindowManager.LayoutParams::class.java.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
                    val meizuFlags = WindowManager.LayoutParams::class.java.getDeclaredField("meizuFlags")
                    darkFlag.isAccessible = true
                    meizuFlags.isAccessible = true
                    val bit = darkFlag.getInt(null)
                    var value = meizuFlags.getInt(lp)
                    value = if (dark) {
                        value or bit
                    } else {
                        value and bit.inv()
                    }
                    meizuFlags.setInt(lp, value)
                    window.attributes = lp
                    result = true
                } catch (e: Exception) {

                }
            }
            return result
        }
    }
}