package com.uroad.zhgs.enumeration

/**
 *Created by MFB on 2018/8/15.
 */
enum class VerificationCode(val code: String) {
    //	1 登录 ; 2 注册 ; 3 其它 ; 4 找回密码
    LOGIN("1"),
    REGISTER("2"),
    OTHER("3"),
    RETRIEVE("4")
}