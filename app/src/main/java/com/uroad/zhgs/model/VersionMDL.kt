package com.uroad.zhgs.model

/**
 * @author MFB
 * @create 2018/9/20
 * @describe app版本数据源
 * conf_ver	版本号
content	更新内容
url	链接
isforce	是否强制更新 1 是；0否
 */
class VersionMDL {
    var conf_ver: String? = null
    var content: String? = null
    var url: String? = null
    var isforce: Int? = 0
}