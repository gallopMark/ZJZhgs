package com.uroad.zhgs.model

/**
 * @author MFB
 * @create 2018/11/24
 * @describe 我的成果
 */
class HarvestMDL {

    var personnel: MutableList<Person>? = null
    var prize: MutableList<Prize>? = null

    class Person {
        var name: String? = null
        var iconfile: String? = null
        var invitetime: String? = null
    }

    class Prize {
        var title: String? = null  //奖品名称
        var sendtime: String? = null //发送奖品时间
    }
}