package com.uroad.locmap.model;

import java.io.Serializable;
import java.util.List;


public class DriveStepMDL implements Serializable {

	/**
	 * 驾车路段的坐标点集合
	 */
	private List<LatLngMDL> polyline;
	/**
	 * 驾车路段的导航主要操作
	 */
	private String action;
	/**
	 * 驾车路段的行驶指示
	 */
	private String instruction;
	/**
	 * 驾车路段的道路名称
	 */
	private String road;
	/**
	 * 驾车路段的距离，单位米
	 */
	private float distance;
	/**
	 * 驾车路段的预计时间，单位秒
	 */
	private float duration;
	public List<LatLngMDL> getPolyline() {
		return polyline;
	}
	public void setPolyline(List<LatLngMDL> polyline) {
		this.polyline = polyline;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getInstruction() {
		return instruction;
	}
	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}
	public String getRoad() {
		return road;
	}
	public void setRoad(String road) {
		this.road = road;
	}
	public float getDistance() {
		return distance;
	}
	public void setDistance(float distance) {
		this.distance = distance;
	}
	public float getDuration() {
		return duration;
	}
	public void setDuration(float duration) {
		this.duration = duration;
	}
	
	
}
