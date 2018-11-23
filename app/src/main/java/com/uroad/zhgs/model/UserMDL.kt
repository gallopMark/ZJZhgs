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
    var pushid: String? = null  //推送
    var name: String? = null
    var username: String? = null
    var userpassword: String? = null
    var phone: String? = null
    var status: Int = 0
    var iconfile: String? = null
    var sex: Int = 0
    var cardno: String? = null
    var requestcode: String? = null  //邀请码
    var QRCode: String? = null  //邀请二维码
    var isfollow: Int? = 0//	是否开启可关注 0 关闭；1开启
    var isauthentication: Int? = 1
    var isLogin = false  // 用于保存用户登录信息的标记

    fun isFollow() = isfollow != 0

    fun isAuth() = isauthentication == 2
}