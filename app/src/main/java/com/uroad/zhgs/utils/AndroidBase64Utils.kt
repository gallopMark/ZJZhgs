package com.uroad.zhgs.utils

import android.util.Base64

/**
 *Created by MFB on 2018/7/27.
 */
object AndroidBase64Utils {
    fun encodeToString(content: String?): String {
        return try {
            if (content != null) Base64.encodeToString(content.toByteArray(), Base64.DEFAULT)
            else ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun decodeToString(content: String?): String {
        return try {
            if (content != null) String(Base64.decode(content.toByteArray(), Base64.DEFAULT))
            else ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}