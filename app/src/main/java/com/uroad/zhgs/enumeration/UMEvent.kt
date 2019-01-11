package com.uroad.zhgs.enumeration

/**
 * @author MFB
 * @create 2019/1/8
 * @describe 友盟事件类型统计
 */
enum class UMEvent(var CODE:String) {
    LOGIN("zj_01"),  //登录
    REGISTER("zj_02"),  //注册
    MY_REPORT("zj_03"), //我的爆料
    MY_SUBSCRIBE("zj_04"),  //我的订阅
    RESCUE_RECORD("zj_05"), //救援记录
    MY_SHOPPING("zj_06"),   //我的商城
    MESSAGE_CENTER("zj_07"),    //消息中心
    MY_CAR("zj_08"),    //我的车辆
    PASS_RECORD("zj_09"),   //通行记录
    MY_FOOTPRINT("zj_10"),  //我的足迹
    MY_INVITATION_CODE("zj_11"),    //我的邀请码
    SETTINGS("zj_12"),  //设置
    ROAD_NAVIGATION("zj_13"),   //路况导航
    HIGHWAY_RESCUE("zj_14"),    //高速救援
    ROAD_TOLL("zj_15"), //路径路费
    SERVICE_AREA("zj_16"),  //服务区
    HIGHWAY_ALIVE("zj_17"), //高速直播
    SHOPPING_MALL("zj_18"), //商城
    RIDERS_REPORT("zj_19"), //车友爆料
    ILLEGAL_INQUIRY("zj_20"),   //违法查询
    INTEGRITY_INQUIRY("zj_21"), //诚信查询
    HIGHWAY_HOTLINE("zj_22"),   //高速热线
    NEARME_TOLL_GATE("zj_23"),  //我的附近-收费站
    NEARME_SERVICE_AREA("zj_24"), //我的附近-服务区
    NEARME_SCENIC("zj_25"), //我的附近-景点
    NEARME_MORE("zj_26"),// 我的附近-更多
    LATEST_NEWS_MORE("zj_27"), //最新资讯-更多
    XIAOZHI_ASKS("zj_28") //小智问问
}