package com.uroad.locmap.model;

import java.io.Serializable;
import java.util.List;


public class PoiResultMDL implements Serializable {

	/**
	 * 获取Poi检索结果
	 */
	private List<PoiInfoMDL> allPoi;
	/**
	 * 获取单页容量,单页容量可以通过检索参数指定
	 */
	private int currentPageCapacity;
	/**
	 * 获取当前分页编号
	 */
	private int currentPageNum;
	/**
	 * 获取总分页数
	 */
	private int totalPageNum;	
	/**
	 * 获取POI总数
	 */
	private int totalPoiNum;
	/**
	 * 是否搜索到数据
	 */
	private boolean hasData;
	
	
	public boolean isHasData() {
		return hasData;
	}
	public void setHasData(boolean hasData) {
		this.hasData = hasData;
	}
	public List<PoiInfoMDL> getAllPoi() {
		return allPoi;
	}
	public void setAllPoi(List<PoiInfoMDL> allPoi) {
		this.allPoi = allPoi;
	}
	public int getCurrentPageCapacity() {
		return currentPageCapacity;
	}
	public void setCurrentPageCapacity(int currentPageCapacity) {
		this.currentPageCapacity = currentPageCapacity;
	}
	public int getCurrentPageNum() {
		return currentPageNum;
	}
	public void setCurrentPageNum(int currentPageNum) {
		this.currentPageNum = currentPageNum;
	}
	public int getTotalPageNum() {
		return totalPageNum;
	}
	public void setTotalPageNum(int totalPageNum) {
		this.totalPageNum = totalPageNum;
	}
	public int getTotalPoiNum() {
		return totalPoiNum;
	}
	public void setTotalPoiNum(int totalPoiNum) {
		this.totalPoiNum = totalPoiNum;
	}	
}
