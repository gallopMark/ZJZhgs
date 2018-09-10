package com.uroad.locmap.model;

import java.io.Serializable;
import java.util.List;


public class DrivePathMDL implements Serializable {

	/**
	 * 驾车规划方案的路段列表
	 */
	private List<DriveStepMDL> steps;
	/**
	 * 导航策略，显示为中文，如返回“速度最快”
	 */
	private String strategy;
	/**
	 * 此规划方案的距离，单位米
	 */
	private float distance;
	/**
	 * 方案的预计消耗时间，单位秒
	 */
	private float duration;
	/**
	 * 此方案中的收费道路的总长度，单位米
	 */
	private float tollDistance;
	/**
	 * 此方案中的收费道路的总费用，单位元
	 */
	private float tolls;	
	/**
	 * 起点经纬度
	 */
	private LatLngMDL start;
	/**
	 * 终点经纬度
	 */
	private LatLngMDL end;
	
	
	public LatLngMDL getStart() {
		return start;
	}
	public void setStart(LatLngMDL start) {
		this.start = start;
	}
	public LatLngMDL getEnd() {
		return end;
	}
	public void setEnd(LatLngMDL end) {
		this.end = end;
	}
	public List<DriveStepMDL> getSteps() {
		return steps;
	}
	public void setSteps(List<DriveStepMDL> steps) {
		this.steps = steps;
	}
	public String getStrategy() {
		return strategy;
	}
	public void setStrategy(String strategy) {
		this.strategy = strategy;
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
	public float getTollDistance() {
		return tollDistance;
	}
	public void setTollDistance(float tollDistance) {
		this.tollDistance = tollDistance;
	}
	public float getTolls() {
		return tolls;
	}
	public void setTolls(float tolls) {
		this.tolls = tolls;
	}
	
	
}
