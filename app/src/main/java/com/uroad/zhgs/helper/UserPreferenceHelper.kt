package com.uroad.zhgs.helper

import android.content.Context
import android.content.SharedPreferences
import com.uroad.zhgs.model.UserMDL

/**
 *Created by MFB on 2018/8/6.
 */
class UserPreferenceHelper {
    companion object {
        private const val PREFS_USER = "Prefs_user"
        const val USER_ID = "userid"
        const val REAL_NAME = "realname"
        const val CARD_NO = "cardno"
        const val PHONE = "phone"
        const val USER_NAME = "username"
        const val USER_PASSWORD = "userpassword"
        const val STATUS = "status"
        const val ICON_FILE = "iconfile"
        const val SEX = "sex"
        const val LOGIN_STAYUS = "login_status"
        const val RESCUE_NOTICE = "rescue_notice"

        private fun from(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE)
        }

        fun save(context: Context, userMDL: UserMDL) {
            from(context).edit().apply {
                putString(USER_ID, userMDL.userid)
                putString(REAL_NAME, userMDL.name)
                putString(CARD_NO, userMDL.cardno)
                putString(PHONE, userMDL.phone)
                putString(USER_NAME, userMDL.username)
                putString(USER_PASSWORD, userMDL.userpassword)
                putInt(STATUS, userMDL.status)
                putString(ICON_FILE, userMDL.iconfile)
                putInt(SEX, userMDL.sex)
                putBoolean(LOGIN_STAYUS, userMDL.isLogin)
            }.apply()
        }

        fun getUserId(context: Context): String {
            return from(context).getString(USER_ID, "")
        }

        fun saveRealName(context: Context, realName: String) {
            from(context).edit().putString(REAL_NAME, realName).apply()
        }

        fun getRealName(context: Context): String {
            return from(context).getString(REAL_NAME, "")
        }

        fun saveCardNo(context: Context, cardNo: String) {
            from(context).edit().putString(CARD_NO, cardNo).apply()
        }

        fun getCardNo(context: Context): String {
            return from(context).getString(CARD_NO, "")
        }

        fun savePhone(context: Context, phone: String) {
            from(context).edit().putString(PHONE, phone).apply()
        }

        fun getPhone(context: Context): String {
            return from(context).getString(PHONE, "")
        }

        fun saveUserName(context: Context, userName: String) {
            from(context).edit().putString(USER_NAME, userName).apply()
        }

        fun getUserName(context: Context): String {
            return from(context).getString(USER_NAME, "")
        }

        fun getUserPassword(context: Context): String {
            return from(context).getString(USER_PASSWORD, "")
        }

        fun getStatus(context: Context): Int {
            return from(context).getInt(STATUS, 0)
        }

        fun saveIconFile(context: Context, iconFile: String) {
            from(context).edit().putString(ICON_FILE, iconFile).apply()
        }

        fun getIconFile(context: Context): String {
            return from(context).getString(ICON_FILE, "")
        }

        fun saveSex(context: Context, sex: Int) {
            from(context).edit().putInt(SEX, sex).apply()
        }

        fun getSex(context: Context): Int {
            return from(context).getInt(SEX, 0)
        }

        fun logOut(context: Context) {
            from(context).edit().putBoolean(LOGIN_STAYUS, false).apply()
        }

        fun isLogin(context: Context): Boolean {
            return from(context).getBoolean(LOGIN_STAYUS, false)
        }

        fun saveRescueNotice(context: Context, isRescueNotice: Boolean) {
            from(context).edit().putBoolean(RESCUE_NOTICE, isRescueNotice).apply()
        }

        fun isRescueNotice(context: Context): Boolean {
            return from(context).getBoolean(RESCUE_NOTICE, false)
        }

        fun clear(context: Context) {
            from(context).edit().clear().apply()
        }
    }
}