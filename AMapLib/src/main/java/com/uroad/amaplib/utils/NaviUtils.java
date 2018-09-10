package com.uroad.amaplib.utils;

import com.amap.api.navi.enums.PathPlanningErrCode;


/**
 * Created by MFB on 2018/9/6.
 */
public class NaviUtils {
    /**
     * ACCESS_TOO_FREQUENT
     * 单位时间内访问过于频繁
     * static int	DISABLE_RESTRICT
     * 无法躲避限行区域。
     * static int	ERROR_BUF
     * Buf数据格式错误
     * static int	ERROR_CONNECTION
     * 网络超时或网络失败。
     * static int	ERROR_DISTANCE
     * 起点/终点/途经点的距离太长(步行距离>100km，骑行距离>1200km)
     * static int	ERROR_ENCODER
     * 算路服务端编码失败
     * static int	ERROR_ENDPOINT
     * 终点错误
     * static int	ERROR_NAVI_PARAMS
     * 调用直接导航 没有算路 参数错误，缺失有效的导航路径，无法开始导航
     * static int	ERROR_NOROADFORENDPOINT
     * 终点没有找到道路
     * static int	ERROR_NOROADFORSTARTPOINT
     * 起点没有找到道路。
     * static int	ERROR_NOROADFORWAYPOINT
     * 途径点没有找到道路
     * static int	ERROR_PREVIEW
     * 路径数据缺乏预览数据
     * static int	ERROR_PROTOCOL
     * 请求协议非法。
     * static int	ERROR_STARTPOINT
     * 起点错误
     * static int	ERROR_WAYPOINT
     * 途经点错误
     * static int	INSUFFICIENT_PRIVILEGES
     * 无权限访问此服务。
     * static int	INVALID_PARAMS
     * 请求参数非法。
     * static int	INVALID_USER_KEY
     * 用户key非法或过期（请检查key是否正确）
     * static int	INVALID_USER_SCODE
     * MD5安全码未通过验证,需要开发者判定key绑定的SHA1,package是否与sdk包里的一致.
     * static int	OUT_OF_SERVICE
     * 使用路径规划服务接口时可能出现该问题，规划点（包括起点、终点、途经点）不在中国陆地范围内
     * static int	OVER_DIRECTION_RANGE
     * 使用路径规划服务接口时可能出现该问题，路线计算失败，通常是由于道路起点和终点距离过长导致
     * static int	OVER_QUOTA
     * 请求超出配额。
     * static int	SERVICE_NOT_EXIST
     * 请求服务不存在。
     * static int	SERVICE_RESPONSE_ERROR
     * 请求服务响应错误。
     * static int	UNKNOWN_ERROR
     * 未知错误(可能是由于连接的网络无法访问外网)
     */

    public static String getError(int errorCode) {
        if(errorCode == PathPlanningErrCode.ACCESS_TOO_FREQUENT) return "单位时间内访问过于频繁";
        if(errorCode == PathPlanningErrCode.DISABLE_RESTRICT) return "无法躲避限行区域";
        if(errorCode == PathPlanningErrCode.ERROR_BUF) return "数据格式错误";
        if(errorCode == PathPlanningErrCode.ERROR_CONNECTION) return "网络超时，请检查网络设置";
        if(errorCode == PathPlanningErrCode.ERROR_DISTANCE) return "途经点的距离太长";
        if(errorCode == PathPlanningErrCode.ERROR_NOROADFORENDPOINT) return "终点没有找到道路";
        if(errorCode == PathPlanningErrCode.ERROR_NOROADFORSTARTPOINT) return "起点没有找到道路";
        if(errorCode == PathPlanningErrCode.ERROR_PREVIEW) return "路径数据缺乏预览数据";
        if(errorCode == PathPlanningErrCode.ERROR_PROTOCOL) return "请求协议非法";
        if(errorCode == PathPlanningErrCode.SERVICE_RESPONSE_ERROR) return "请求服务响应错误";
        return "未知错误";
    }
}
