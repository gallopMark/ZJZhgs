package com.uroad.zhgs.model

class AuthMDL {
    enum class Type(var CODE: String) {
        TXJL("txjl"),  //通行记录
        CYZD("cyzd"),  //车友组队
        GSJY("gsjy"),   //高速救援
        BLFB("blfb")    //报料发布
    }

    var funcname: String? = null
    var funckey: String? = null
    var identityauthentication: Int? = 1
}