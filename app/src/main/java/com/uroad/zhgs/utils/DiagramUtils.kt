package com.uroad.zhgs.utils

import android.content.Context
import android.content.SharedPreferences
import com.uroad.zhgs.common.CurrApplication
import java.io.File

/**
 *Created by MFB on 2018/8/11.
 * 简图工具类
 */
class DiagramUtils {
    companion object {
        private const val VERSION_FILE = "ver_file"
        private const val VER = "ver"

        private fun getSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(VERSION_FILE, Context.MODE_PRIVATE)
        }

        fun saveVersionSer(context: Context, version: String?) {
            getSharedPreferences(context).edit().putString(VER, version).apply()
        }

        fun getVersionLocal(context: Context): String {
            return getSharedPreferences(context).getString(VER, "")
        }

        private fun diagramPath(): String {
            return "${CurrApplication.DIAGRAM_PATH}/SVG/index.html"
        }

        fun diagramExists(): Boolean {
            val file = File(diagramPath())
            return file.exists()
        }

        fun diagramUrl(): String {
            return "file:///${diagramPath()}"
        }

        fun deleteAllFile() {
            val file = File(CurrApplication.DIAGRAM_PATH)
            if (file.exists()) {
                val files = file.listFiles()
                for (i in 0 until files.size) {
                    files[i].delete()
                }
            }
        }
    }
}