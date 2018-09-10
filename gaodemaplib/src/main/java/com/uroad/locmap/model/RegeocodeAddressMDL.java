package com.uroad.locmap.model;

import java.io.Serializable;

public class RegeocodeAddressMDL implements Serializable {

	/**
	 * 逆地理编码返回的格式化地址。如返回北京市朝阳区方恒国际中心。
	 */
	private String formatAddress;
	/**
	 * 逆地理编码返回的所在省名称、直辖市的名称 。
	 */
	private String province;
	/**
	 * 逆地理编码返回的所在城市名称。直辖市的名称参见省名称，此项为空。
	 */
	private String city;
	/**
	 * 逆地理编码返回的所在区（县）名称。
	 */
	private String district;
	/**
	 * 逆地理编码返回的乡镇名称。
	 */
	private String township;
	/**
	 * 逆地理编码返回的社区名称。
	 */
	private String neighborhood;
	/**
	 * 逆地理编码返回的建筑物名称。
	 */
	private float building;
	
	public String getFormatAddress() {
		return formatAddress;
	}
	public void setFormatAddress(String formatAddress) {
		this.formatAddress = formatAddress;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getDistrict() {
		return district;
	}
	public void setDistrict(String district) {
		this.district = district;
	}
	public String getTownship() {
		return township;
	}
	public void setTownship(String township) {
		this.township = township;
	}
	public String getNeighborhood() {
		return neighborhood;
	}
	public void setNeighborhood(String neighborhood) {
		this.neighborhood = neighborhood;
	}
	public float getBuilding() {
		return building;
	}
	public void setBuilding(float building) {
		this.building = building;
	}	
	
}
