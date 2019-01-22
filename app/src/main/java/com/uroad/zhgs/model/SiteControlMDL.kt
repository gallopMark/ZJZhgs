package com.uroad.zhgs.model

import com.amap.api.col.sln3.nu

/**
 * @author MFB
 * @create 2018/12/4
 * @describe 站点管制
 * 参数项	名称	备注
fx1rk	方向1入口状态	1 正常；2 关闭
fx2rk	方向2入口状态	1 正常；2 关闭
name	收费站名称
shortname	高速名称
picurl	国标图片地址
direction1	方向1名称
direction2	方向2名称
distance	距离
 */
class SiteControlMDL {
    var fx1rk: Int = 0
    var fx2rk: Int = 0
    var name: String? = null
    var shortname: String? = null
    var picurl: String? = null
    var direction1: String? = null
    var direction2: String? = null
    var distance: String? = null
    var sfzname: String? = null
    var snname: String? = null
    var pointtype: String? = null
    var reportout: String? = null
}