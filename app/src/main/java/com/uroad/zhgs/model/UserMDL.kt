package com.uroad.zhgs.model

/**
 *Created by MFB on 2018/8/6.
 * userid	用户ID
username	用户昵称
phone	手机号
iconfile	头像
name	姓名
cardno	身份证
 */
class UserMDL {
    var userid: String? = null
    var name: String? = null
    var username: String? = null
    var userpassword: String? = null
    var phone: String? = null
    var status: Int = 0
    var iconfile: String? = null
    var sex: Int = 0
    var cardno: String? = null
    var isLogin = false  // 用于保存用户登录信息的标记
}