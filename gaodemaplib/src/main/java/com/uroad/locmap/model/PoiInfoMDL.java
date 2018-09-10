package com.uroad.locmap.model;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class PoiInfoMDL implements Serializable {
	@Id(autoincrement = true)
	private int id;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * poi地址信息
	 */
	private String address;
	/**
	 * poi所在城市
	 */
	private String city;
	/**
	 * 纬度坐标
	 */
	private double latitude;
	/**
	 * 经度坐标
	 */
	private double longitude;	
	/**
	 * poi名称
	 */
	private String name;
	/**
	 * poi电话信息
	 */
	private String phoneNum;
	/**
	 * poi邮编
	 */
	private String postCode;
	/**
	 * poi类型，0：普通点，1：公交站，2：公交线路，3：地铁站，4：地铁线路，5：家，6：公司，7：导航收藏,8:搜索历史
	 */
	private int type;
	/**
	 * 距离
	 * **/
	private String distance;
	
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhoneNum() {
		return phoneNum;
	}
	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}
	public String getPostCode() {
		return postCode;
	}
	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getDistance() {
		return distance;
	}
	public void setDistance(String distance) {
		this.distance = distance;
	}

}
