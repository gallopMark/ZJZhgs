package com.uroad.zhgs.enumeration

enum class SiteType(var CODE: String) {
    /**
     * 1002001 收费站
    1002002 虚拟收费站
    1002003 互通立交
    1002004 枢纽同站
     */
    TOLL_GATE("1002001"),
    VIRTUAL_TOLL_GATE("1002002"),
    INTERCHANGE("1002003"),
    HUB_STATION("1002004")
}