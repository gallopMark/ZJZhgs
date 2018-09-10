package com.uroad.locmap.model;

import java.io.Serializable;

public class LocationMDL implements Serializable {

	/**
	 * 获取详细地址信息
	 */
	private String addrStr;
	/**
	 * 获取城市
	 */
	private String city;
	/**
	 * 获取城市编码
	 */
	private String cityCode;
	/**
	 * 获取所用坐标系，以locationClientOption里设定的坐标系为准(gcj02,bd09,bd09ll)
	 */
	private String coorType;
	/**
	 * 获取国家
	 */
	private String country;
	/**
	 * 获取国家编码
	 */
	private String countryCode;
	/**
	 * gps定位结果时，行进的方向，单位度
	 */
	private float direction;
	/**
	 * 获取区/县信息
	 */
	private String district;

	/**
	 * 获取纬度坐标
	 */
	private double latitude;
	/**
	 * 获取经度坐标
	 */
	private double longitude;
	/**
	 * 获取省份
	 */
	private String province;
	/**
	 * 获取街道
	 */
	private String street;
	/**
	 * 获取定位精度,默认值0.0f
	 */
	private float Radius;
	/**
	 * 获取速度
	 * **/
	private double speed;

	public String getAddrStr() {
		return addrStr;
	}

	public void setAddrStr(String addrStr) {
		this.addrStr = addrStr;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public String getCoorType() {
		return coorType;
	}

	public void setCoorType(String coorType) {
		this.coorType = coorType;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public float getDirection() {
		return direction;
	}

	public void setDirection(float direction) {
		this.direction = direction;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
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

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public float getRadius() {
		return Radius;
	}

	public void setRadius(float radius) {
		Radius = radius;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

}
