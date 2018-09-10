package com.uroad.locmap.model;

import java.io.Serializable;

public class LatLngMDL implements Serializable {

	/**
	 * 纬度坐标
	 */
	private double latitude;
	/**
	 * 经度坐标
	 */
	private double longitude;

	public LatLngMDL() {

	}

	public LatLngMDL(double latitude,double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

}
