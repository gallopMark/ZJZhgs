package com.uroad.zhgs.model

/**
 * @author MFB
 * @create 2018/11/23
 * @describe 首页活动
 * activityid	活动ID
activityicon	活动图标
islogin	该活动是否需要登陆才能参与	0 否；1 是
transitionstype	跳转类型	1018001 Native；1018002 H5
transitionscontent	跳转内容	该字段的内容根据跳转类型字段变化，
如transitionstype为1018002时，该字段存放的就是跳转的URL链接

activityimg	活动图片	多张“,”逗号隔开
title	活动标题
subtitle	活动副标题
activityrule	活动规则
sharetitle	分享标题
sharedesc	分享描述
shareicon	分享图标URL
shareurl	分享跳转地址
islogin	活动是否需要登陆	0 否；1 是
totalnum	成功邀请用户数
 */
class ActivityMDL {
    enum class Type(val code: String) {
        NATIVE("1018001"),
        H5("1018002")
    }

    var activityid: String? = null
    var activityicon: String? = null
    var islogin: Int? = 0
    var transitionstype: String? = null
    var transitionscontent: String? = null

    var activityimg: String? = null
    var title: String? = null
    var subtitle: String? = null
    var activityrule: String? = null
    var sharetitle: String? = null
    var sharedesc: String? = null
    var shareicon: String? = null
    var shareurl: String? = null
    var totalnum: String? = null
}