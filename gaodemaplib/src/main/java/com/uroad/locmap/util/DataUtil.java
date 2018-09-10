/**
 * 
 */
package com.uroad.locmap.util;


public class DataUtil {

	/**
	 * 只在第一次定位移动到地图中心点。(默认设置)
	 */
	public static final int LOCATION_TYPE_LOCATE = 1;
	/**
	 * 定位、移动到地图中心点并跟随
	 */
	public static final int LOCATION_TYPE_MAP_FOLLOW = 2;
	/**
	 * 定位、移动到地图中心点，跟踪并根据面向方向旋转地图
	 */
	public static final int LOCATION_TYPE_MAP_ROTATE = 3;
	
	
	/**
	 * 代表高德网络定位服务，混合定位。(默认设置)
	 */
	public static final int AMapNetwork = 1;
	/**
	 * 代表使用手机网络定位
	 */
	public static final int NETWORK_PROVIDER = 2;
	/**
	 * 代表使用手机GPS定位
	 */
	public static final int GPS_PROVIDER = 3;
	
	/**
	 * 输入参数坐标为高德类型
	 */
	public static final String AMAP = "autonavi";
	/**
	 * 输入参数坐标为GPS类型
	 */
	public static final String GPS = "gps";
}
