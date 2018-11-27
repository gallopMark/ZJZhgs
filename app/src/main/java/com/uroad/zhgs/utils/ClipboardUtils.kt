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
        /*获取车有组队口令*/
        fun getClipboard(context: Context): ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        fun getRiderToken(context: Context): String? {
            val cm = getClipboard(context)
            val data = cm.primaryClip
            if (data != null && data.itemCount > 0) {
                val item = data.getItemAt(0)
                val content = item.text.toString()
                if (!TextUtils.isEmpty(content)
                        && content.contains("智慧高速车友组队")
                        && content.contains("¢")) {
                    //从口令中截取口令
                    return try {
                        val start = content.indexOf("¢") + 1
                        val end = content.lastIndexOf("¢")
                        content.substring(start, end)
                    } catch (e: Exception) {
                        ""
                    }
                }
            }
            return ""
        }

        /*注册页面，获取粘贴板邀请码口令的邀请码字段*/
        fun getRegisterToken(context: Context): String? {
            val cm = getClipboard(context)
            val data = cm.primaryClip
            if (data != null && data.itemCount > 0) {
                val item = data.getItemAt(0)
                val content = item.text.toString()
                if (!TextUtils.isEmpty(content)
                        && content.contains("邀请注册码")
                        && content.contains("¢")) {
                    //从口令中截取口令
                    return try {
                        val start = content.indexOf("¢") + 1
                        val end = content.lastIndexOf("¢")
                        content.substring(start, end)
                    } catch (e: Exception) {
                        ""
                    }
                }
            }
            return ""
        }

        fun clear(context: Context) {
            val cm = getClipboard(context)
            cm.primaryClip = ClipData.newPlainText(null, "")
        }
    }
}