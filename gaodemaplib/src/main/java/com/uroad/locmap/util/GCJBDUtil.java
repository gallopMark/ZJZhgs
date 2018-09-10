package com.uroad.locmap.util;


import com.uroad.locmap.model.LatLngMDL;

public class GCJBDUtil {
	private static double M_PI = 3.14159265358979324;
	private static double x_pi = M_PI * 3000.0 / 180.0;

	/**
	 * 高德坐标转百度坐标
	 * **/
	public static LatLngMDL GCJ2BD(LatLngMDL poi) {
		LatLngMDL bdPoi = new LatLngMDL();
		double x = poi.getLongitude(), y = poi.getLatitude();
		double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
		double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
		double lng = z * Math.cos(theta) + 0.0065;
		double lat = z * Math.sin(theta) + 0.006;
		bdPoi = new LatLngMDL(lat, lng);
		return bdPoi;
	}

	/**
	 * 
	 * 百度坐标转高德坐标
	 * **/
	public static LatLngMDL BD2GCJ(LatLngMDL poi) {
		LatLngMDL gcjPoi = null;
		double x = poi.getLongitude() - 0.0065, y = poi.getLatitude() - 0.006;
		double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
		double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
		double lng = z * Math.cos(theta);
		double lat = z * Math.sin(theta);
		gcjPoi = new LatLngMDL(lat, lng);
		return gcjPoi;
	}

//	/**
//	 * gps坐标转高德坐标
//	 * **/
//	public static LatLngMDL gps2GCJ(LatLngMDL poi) {
//		GeoPoint pos = CoordinateConvert.fromGpsToAMap(poi.getLatitude(),
//				poi.getLongitude());
//		LatLngMDL location = new LatLngMDL(pos.getLatitudeE6() * 1.E-6,
//				pos.getLongitudeE6() * 1.E-6);
//		return location;
//	}

	/**
	 * w84转经纬度(gps)
	 * 
	 * WGS84是地理坐标的一种,地理坐标本身有很多种,web墨卡托是投影坐标
	 * **/
	public static LatLngMDL w842gps(LatLngMDL poi) {
		double lat = Math
				.log(Math.tan((90 + poi.getLatitude()) * Math.PI / 360))
				/ (Math.PI / 180);
		lat = lat * 20037508.34 / 180;
		double lon = poi.getLongitude() * 20037508.34 / 180;
		return new LatLngMDL(lat, lon);

	}

	/**
	 * gps转经纬度(w84)
	 * 
	 * WGS84是地理坐标的一种,地理坐标本身有很多种,web墨卡托是投影坐标
	 * **/
	public static LatLngMDL gps2w84(LatLngMDL poi) {
		double lat = (poi.getLatitude() / 20037508.34) * 180;
		lat = 180 / Math.PI
				* (2 * Math.atan(Math.exp(lat * Math.PI / 180)) - Math.PI / 2);
		double lon = (poi.getLongitude() / 20037508.34) * 180;
		return new LatLngMDL(lat, lon);
	}
}
