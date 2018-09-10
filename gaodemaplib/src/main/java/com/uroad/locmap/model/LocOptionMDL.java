package com.uroad.locmap.model;

import java.io.Serializable;

public class LocOptionMDL implements Serializable {

	/**
	 * 设置定位的坐标系(百度特有)
	 */
	private String coorType;
	/**
	 * 有三种定位Provider供用户选择，分别是: 3:LocationManagerProxy.GPS_PROVIDER，代表使用手机GPS定位；
	 * 2:LocationManagerProxy.NETWORK_PROVIDER，代表使用手机网络定位；
	 * 1:LocationProviderProxy.AMapNetwork，代表高德网络定位服务，混合定位。
	 */
	private int locationMode;
	/**
	 * 位置变化的通知时间，单位为毫秒。如果为-1，定位只定位一次
	 */
	private int scanSpan;
	/**
	 * 设置是否需要地址信息(百度特有)
	 */
	private boolean isNeedAddress;
	/**
	 * 设置是否使用gps
	 */
	private boolean openGps;
	/**
	 * 设置定位结果包含手机机头的方向(百度特有)
	 */
	private boolean needDeviceDirect;
	/**
	 * 位置变化通知距离，单位为米
	 */
	private float minDistance;

	public LocOptionMDL() {

	}

	public LocOptionMDL(String coorType, int locationMode, int scanSpan,
			boolean isNeedAddress, boolean openGps, boolean needDeviceDirect,
			float minDistance) {
		this.coorType = coorType;
		this.locationMode = locationMode;
		this.scanSpan = scanSpan;
		this.isNeedAddress = isNeedAddress;
		this.openGps = openGps;
		this.needDeviceDirect = needDeviceDirect;
		this.minDistance = minDistance;
	}

	public float getMinDistance() {
		return minDistance;
	}

	public void setMinDistance(float minDistance) {
		this.minDistance = minDistance;
	}

	public boolean isNeedDeviceDirect() {
		return needDeviceDirect;
	}

	public void setNeedDeviceDirect(boolean needDeviceDirect) {
		this.needDeviceDirect = needDeviceDirect;
	}

	public String getCoorType() {
		return coorType;
	}

	public void setCoorType(String coorType) {
		this.coorType = coorType;
	}

	public int getLocationMode() {
		return locationMode;
	}

	public void setLocationMode(int locationMode) {
		this.locationMode = locationMode;
	}

	public int getScanSpan() {
		return scanSpan;
	}

	public void setScanSpan(int scanSpan) {
		this.scanSpan = scanSpan;
	}

	public boolean isNeedAddress() {
		return isNeedAddress;
	}

	public void setNeedAddress(boolean isNeedAddress) {
		this.isNeedAddress = isNeedAddress;
	}

	public boolean isOpenGps() {
		return openGps;
	}

	public void setOpenGps(boolean openGps) {
		this.openGps = openGps;
	}

}
