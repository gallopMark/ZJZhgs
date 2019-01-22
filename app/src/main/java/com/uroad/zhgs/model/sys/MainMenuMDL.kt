package com.uroad.zhgs.model.sys

class MainMenuMDL {

    class ServiceIcon {
        var icon: String? = null
    }

    companion object {
        const val LJLF = "ljlf" //路径路费
        const val FWQ = "fwq"   //服务区
        const val GSRX = "gsrx" //高速热线
        const val ZXSC = "zxsc" //在线商城
        const val CYBL = "cybl" //车友爆料
        const val WZCX = "wfcx" //违章查询
        const val GSZX = "gszx" //高速资讯
        const val CXCX = "cxcx"  //诚信查询
        const val GSZB = "gszb" //高速直播
        const val LJLF_ICON = "ljlf.png"
        const val FWQ_ICON = "fwq.png"
        const val GSRX_ICON = "gsrx.png"
        const val ZXSC_ICON = "zxsc.png"
        const val CYBL_ICON = "cybl.png"
        const val WZCX_ICON = "wfcx.png"
        const val GSZX_ICON = "gszx.png"
        const val CXCX_ICON = "cxcx.png"
        const val GSZB_ICON = "gszb.png"
    }

    var menukey: String? = null
    var menuname: String? = null
    var iconname: String? = null
    var serviceIcon: String? = null

    override fun equals(other: Any?): Boolean {
        return when (other) {
            !is MainMenuMDL -> false
            else -> this === other || menukey == other.menukey
        }
    }

    override fun hashCode(): Int {
        return 31 + (menukey?.hashCode() ?: 0)
    }
}