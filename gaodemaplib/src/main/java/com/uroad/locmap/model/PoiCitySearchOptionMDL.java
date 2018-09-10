package com.uroad.locmap.model;

import java.io.Serializable;
import java.util.List;

public class PoiCitySearchOptionMDL implements Serializable {
	/**
	 * 分页编号，默认从0开始
	 */
	private int pageNum;
	/**
	 * 设置每页容量，默认为每页10条
	 */
	private int pageCapacity;
	/**
	 * 搜索关键字
	 */
	private String keyword;
	/**
	 * 检索城市
	 */
	private String city;

	public PoiCitySearchOptionMDL() {

	}

	public PoiCitySearchOptionMDL(int pageNum, int pageCapacity,
			String keyword, String city) {
		this.pageNum = pageNum;
		this.pageCapacity = pageCapacity;
		this.keyword = keyword;
		this.city = city;
	}

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public int getPageCapacity() {
		return pageCapacity;
	}

	public void setPageCapacity(int pageCapacity) {
		this.pageCapacity = pageCapacity;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}
}
