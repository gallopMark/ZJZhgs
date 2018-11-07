package com.uroad.zhgs.model.sys

class AppConfigMDL {
    enum class Type constructor(val CODE: String) {
        NAV_MENU_VER("1"),
        ANDROID_VER("2"),
        DIAGRAM_VER("5"),
        SYSTEM_VER("6")
    }

    var confid: String? = null
    var title: String? = null
    var conf_name: String? = null
    var conf_ver: String? = null
    var url: String? = null
    var isforce: Int? = 0
}