package com.uroad.locmap.model;

import java.io.Serializable;

public class CameraPositionMDL implements Serializable {

	/**
	 * 可视区域指向的方向，以角度为单位，正北方向到地图方向逆时针旋转的角度，范围从0度到360度
	 */
	private float bearing;
	/**
	 * 目标位置的屏幕中心点经纬度坐标
	 */
	private LatLngMDL target;
	/**
	 * 目标可视区域的倾斜度，以角度为单位
	 */
	private float tilt;
	/**
	 * 目标可视区域的缩放级别
	 */
	private float zoom;
	public float getBearing() {
		return bearing;
	}
	public void setBearing(float bearing) {
		this.bearing = bearing;
	}
	public LatLngMDL getTarget() {
		return target;
	}
	public void setTarget(LatLngMDL target) {
		this.target = target;
	}
	public float getTilt() {
		return tilt;
	}
	public void setTilt(float tilt) {
		this.tilt = tilt;
	}
	public float getZoom() {
		return zoom;
	}
	public void setZoom(float zoom) {
		this.zoom = zoom;
	}
	
	
}
