package com.uroad.zhgs.enumeration

/**
 *Created by MFB on 2018/8/29.
 * 6．SVG简图交互-显示(隐藏)POI层
方法名称：
uroadplus_web_showPOILayer(poitype,isdisplay)
传入参数：
参数项	名称	备注
poitype	poi类型	事件 event
施工 plan
管制 control
快拍 cctv
服务区 servicearea
收费站 station
isdisplay	是否展示	1显示0隐藏
 */
enum class DiagramEventType(val code: String) {
    Accident("event"),
    Construction("plan"),
    Control("control"),
    TrafficJam("jam"),
    Snapshot("cctv"),
    ServiceArea("servicearea"),
    TollGate("station"),
    PileNumber("stake")
}