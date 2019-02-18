package com.uroad.zhgs.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils

/**
 * @author MFB
 * @create 2018/11/27
 * @describe
 */
class ClipboardUtils {
    companion object {

        private fun getClipboard(context: Context): ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        /*获取粘贴板文本*/
        private fun getClipboardText(context: Context): String {
            val cm = getClipboard(context)
            val data = cm.primaryClip
            if (data != null && data.itemCount > 0) {
                val item = data.getItemAt(0) ?: return ""
                return item.text?.toString() ?: return ""
            }
            return ""
        }

        /*获取车有组队口令*/
        fun getRiderToken(context: Context): String? {
            val content = getClipboardText(context)
            if (!TextUtils.isEmpty(content)
                    && content.contains("智慧高速车友组队")
                    && content.contains("¢")) {
                //从口令中截取口令
                return getToken(content)
            }
            return ""
        }


        /*检测粘贴板内容是否是“邀请注册码”口令*/
        fun isRegisterToken(content: String): Boolean {
            return !TextUtils.isEmpty(content) && content.contains("邀请注册码") && content.contains("¢")
        }

        /*注册页面，获取粘贴板邀请码口令的邀请码字段*/
        fun getRegisterToken(context: Context): String? {
            val content = getClipboardText(context)
            if (isRegisterToken(content)) {
                //从口令中截取口令
                return getToken(content)
            }
            return ""
        }

        private fun getToken(content: String): String {
            return try {
                val start = content.indexOf("¢") + 1
                val end = content.lastIndexOf("¢")
                content.substring(start, end)
            } catch (e: Exception) {
                ""
            }
        }

        fun clear(context: Context) {
            val cm = getClipboard(context)
            cm.primaryClip = ClipData.newPlainText(null, "")
        }
    }
}