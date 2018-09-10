package com.uroad.locmap.model;

public enum DriveModeEnum {	
	/**
	 * 时间优先躲避拥堵 
	 */
	DrivingAvoidCongestion(4), 
	/**
	 * 速度优先
	 */
	DrivingDefault(0),
	/**
	 * 同时使用速度优先_费用优先_距离优先三个策略计算路径 
	 */
	DrivingMultiStrategy(5),
	/**
	 * 不走快速路
	 */
	DrivingNoExpressways(3), 
	/**
	 * 不走高速且躲避收费和拥堵
	 */
	DrivingNoHighAvoidCongestionSaveMoney(9),  
	/**
	 * 不走高速
	 */
	DrivingNoHighWay(6), 
	/**
	 * 不走高速且避免收费
	 */
	DrivingNoHighWaySaveMoney(7),  
	/**
	 * 费用优先_不走收费路的最快道路
	 */
	DrivingSaveMoney(1), 
	/**
	 * 避免收费与拥堵
	 */
	DrivingSaveMoneyAvoidCongestion(8),  
	/**
	 * 距离优先
	 */
	DrivingShortDistance(2);
	
	private final int typeCode;

	private DriveModeEnum(int typeCode) {
		this.typeCode = typeCode;
	}

	public int getCode() {
		return typeCode;
	}

	public static DriveModeEnum getDriveModeEnum(int code) {
		for (DriveModeEnum item : DriveModeEnum.values()) {
			if (item.getCode()== code)
				return item;
		}
		return null;
	}
}
