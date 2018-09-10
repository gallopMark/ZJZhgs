package com.uroad.zhgs.model

/**
 *Created by MFB on 2018/8/6.
 * 获取新闻列表上的新闻类型
 */
class NewsTabMDL {

    var type: MutableList<Type>? = null

    inner class Type {
        var dictcode: String? = null
        var dictname: String? = null
        var sontype: MutableList<SonType>? = null

        inner class SonType {

        }
    }
}