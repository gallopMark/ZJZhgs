package com.uroad.locmap.model;

import java.io.Serializable;
import java.util.List;

public class RegeocodeResultMDL implements Serializable {

	/**
	 * 逆地理编码返回的格式化地址
	 */
	private String formatAddress;
	/**
	 * 逆地理编码返回的所在区（县）名称
	 */
	private String district;	

	/**
	 * 逆地理编码返回的建筑物名称
	 */
	private String building;
	
	/**
	 * 逆地理编码返回的POI(兴趣点)列表
	 */
	private List<PoiInfoMDL> pois;

	
	public List<PoiInfoMDL> getPois() {
		return pois;
	}

	public void setPois(List<PoiInfoMDL> pois) {
		this.pois = pois;
	}

	public String getFormatAddress() {
		return formatAddress;
	}

	public void setFormatAddress(String formatAddress) {
		this.formatAddress = formatAddress;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getBuilding() {
		return building;
	}

	public void setBuilding(String building) {
		this.building = building;
	}
	
	
}
