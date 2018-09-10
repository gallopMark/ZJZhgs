package com.uroad.zhgs.enumeration

/**
 *Created by MFB on 2018/8/8.
 */
enum class EventType(val code: String) {
    //1015001 拥堵，1015002 事故，1015003 施工，1015004 遗洒，1015005 积水，1015006 管制
    TRAFFIC_JAM("1015001"),
    ACCIDENT("1015002"),
    CONSTRUCTION("1015003"),
    SPILT("1015004"),
    SEEPER("1015005"),
    CONTROL("1015006")
}