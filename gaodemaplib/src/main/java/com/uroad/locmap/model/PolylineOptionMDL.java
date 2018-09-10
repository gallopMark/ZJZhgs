package com.uroad.locmap.model;

import java.io.Serializable;
import java.util.List;

public class PolylineOptionMDL implements Serializable {

	/**
	 * 驾驶模式
	 */
	private int deiverMode;
	/**
	 * 起点
	 */
	private LatLngMDL start;
	/**
	 * 终点
	 */
	private LatLngMDL end;
	/**
	 * 规划路线颜色
	 */
	private int color;
	/**
	 * 规划路线宽度
	 */
	private float polylineWidth;
	
	/**
	 * 途经点（最多支持16个） 
	 */
	private List<LatLngMDL> passedByPoints;
	
	/**
	 * 避让区域（最多支持32个）
	 */
	private  List<List<LatLngMDL>> avoidpolygons;
	
	/**
	 * 查询的避让道路
	 */
	private String avoidRoad;
	
	/**
	 * 设置是否使用纹理贴图画线
	 */
	private boolean useTexture;	
	
	/**
	 * 设置线段的纹理图，图片为2的n次方
	 */
	private int customTexture;
	
	
	public PolylineOptionMDL() {

	}
	

	public PolylineOptionMDL(int deiverMode, LatLngMDL start, LatLngMDL end,
			int color, float polylineWidth) {
		super();
		this.deiverMode = deiverMode;
		this.start = start;
		this.end = end;
		this.color = color;
		this.polylineWidth = polylineWidth;
	}

	
	public boolean isUseTexture() {
		return useTexture;
	}


	public void setUseTexture(boolean useTexture) {
		this.useTexture = useTexture;
	}


	public int getCustomTexture() {
		return customTexture;
	}


	public void setCustomTexture(int customTexture) {
		this.customTexture = customTexture;
	}


	public int getDeiverMode() {
		return deiverMode;
	}

	public void setDeiverMode(int deiverMode) {
		this.deiverMode = deiverMode;
	}

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

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public float getPolylineWidth() {
		return polylineWidth;
	}

	public void setPolylineWidth(float polylineWidth) {
		this.polylineWidth = polylineWidth;
	}


	public List<LatLngMDL> getPassedByPoints() {
		return passedByPoints;
	}


	public void setPassedByPoints(List<LatLngMDL> passedByPoints) {
		this.passedByPoints = passedByPoints;
	}


	public List<List<LatLngMDL>> getAvoidpolygons() {
		return avoidpolygons;
	}


	public void setAvoidpolygons(List<List<LatLngMDL>> avoidpolygons) {
		this.avoidpolygons = avoidpolygons;
	}


	public String getAvoidRoad() {
		return avoidRoad;
	}


	public void setAvoidRoad(String avoidRoad) {
		this.avoidRoad = avoidRoad;
	}

	
	

}
