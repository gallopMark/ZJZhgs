package com.uroad.zhgs.enumeration

/**
 *Created by MFB on 2018/8/11.
 */
enum class MapDataType(val code: String) {
    /**
     * 1 收费站 ； 2 服务区 ； 3 景点 ； 4 加油站 ；
     * 5 维修店 ; 6 天气 ； 7快拍 ； 8拥堵 ； 1006001 事故 ； 1006002 施工 ； 1006003 管制
     */
    TOLL_GATE("1"),
    SERVICE_AREA("2"),
    SCENIC("3"),
    GAS_STATION("4"),
    REPAIR_SHOP("5"),
    WEATHER("6"),
    SNAPSHOT("7"),
    TRAFFIC_JAM("8"),
    ACCIDENT("1006001"),
    CONSTRUCTION("1006002"),
    CONTROL("1006003")
}