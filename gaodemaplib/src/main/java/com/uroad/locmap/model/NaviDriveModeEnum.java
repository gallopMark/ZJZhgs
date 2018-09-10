package com.uroad.locmap.model;

public enum NaviDriveModeEnum {

	/**
	 * -默认算路模式 速度优先（常规最快） 速度优先（常规默认选项），是指不考虑任何策略的情况下，理想路况下，根据道路固有属性（静态权值）计算得到的路线
	 * **/
	DRIVING_DEFAULT(0),
	/**
	 * 费用优先（尽量避开收费道路）
	 * **/
	DRIVING_SAVE_MONEY(1),
	/**
	 * 距离优先（距离最短）
	 * **/
	DRIVING_SHORT_DISTANCE(2),
	/**
	 * 普通路优先（不走快速路，包含高速路）
	 * **/
	DRIVING_NO_EXPRESS_WAYS(3),
	/**
	 * 时间优先 规避拥堵的路线（考虑实时路况） 躲避拥堵，完全可以按照字面意思理解，即现实路况下（结合实时交通路况），计算出最优路线。
	 * 在真实路况下，一般来说，躲避拥堵所用时间会比速度优先（常规默认选项）要少
	 * **/
	DRIVING_FASTEST_TIME(4),
	/**
	 * 规避拥堵且不走收费道路
	 * **/
	DRIVING_AVOID_CONGESTION(12),
	/**
	 * 多路径算路
	 * **/
	DRIVING_MULTIPLE_ROUTES(13);

	private final int typeCode;

	private NaviDriveModeEnum(int typeCode) {
		this.typeCode = typeCode;
	}

	public int getCode() {
		return typeCode;
	}

	public static DriveModeEnum getDriveModeEnum(int code) {
		for (DriveModeEnum item : DriveModeEnum.values()) {
			if (item.getCode() == code)
				return item;
		}
		return null;
	}
}
