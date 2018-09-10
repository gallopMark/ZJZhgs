package com.uroad.locmap.model;

import com.amap.api.navi.view.RouteOverLay;

import java.io.Serializable;

public class RouteOverLayMDL implements Serializable {

	RouteOverLay routeOverlay;

	public RouteOverLay getRouteOverlay() {
		return routeOverlay;
	}

	public void setRouteOverlay(RouteOverLay routeOverlay) {
		this.routeOverlay = routeOverlay;
	}

	/**
	 * 获取路程长度:路程m
	 * **/
	public int getAllLength() {
		return routeOverlay.getAMapNaviPath().getAllLength();
	}

	/**
	 * 获取路程时间：时间s
	 * **/
	public int getAllTime() {
		return routeOverlay.getAMapNaviPath().getAllTime();
	}
	/**
	 * 获取路程费用:费用 元
	 * **/
	public int getAllToll(){
		return routeOverlay.getAMapNaviPath().getTollCost();
	}
	
	/**
	 * 获取路线规划策略
	 * **/
	public int getStrategy(){
		return routeOverlay.getAMapNaviPath().getStrategy();
	}

	public void removeRoute(){

		routeOverlay.removeFromMap();
	}
}
