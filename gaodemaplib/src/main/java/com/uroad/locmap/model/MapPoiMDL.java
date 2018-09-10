package com.uroad.locmap.model;

import java.io.Serializable;

public class MapPoiMDL implements Serializable {

	/**
	 *  获取该兴趣点的名称
	 */
	private String name;
	/**
	 * 纬度坐标
	 */
	private double latitude;
	/**
	 * 经度坐标
	 */
	private double longitude;
	/**
	 * 获取该兴趣点的地理坐标
	 */
	private LatLngMDL latLngMDL;

	private String eventtype;
	private String eventid;
	private String title;
	private String reportout;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	public LatLngMDL getLatLngMDL() {
		return latLngMDL;
	}
	public void setLatLngMDL(LatLngMDL latLngMDL) {
		this.latLngMDL = latLngMDL;
	}
	public String getEventtype() {
		return eventtype;
	}
	public void setEventtype(String eventtype) {
		this.eventtype = eventtype;
	}
	public String getEventid() {
		return eventid;
	}
	public void setEventid(String eventid) {
		this.eventid = eventid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getReportout() {
		return reportout;
	}

	public void setReportout(String reportout) {
		this.reportout = reportout;
	}
}
