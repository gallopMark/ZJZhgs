package com.uroad.locmap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.uroad.locmap.model.LatLngMDL;

import java.util.ArrayList;

public class NaviHelper {
	
	//ii

	private static Context ct;
	private static NaviHelper naviHelper;

	// 起点终点
	private double startLat, startLon, endLat, endLon;
	// 是否为模拟导航
	private static boolean mIsEmulatorNavi = false;

	private NaviHelper(Context ct) {
		super();
		this.ct = ct;
	}

	public static NaviHelper getInstance(Context ct) {
		if (naviHelper == null) {
			naviHelper = new NaviHelper(ct);
		}
		return naviHelper;
	}

	/**
	 * 实时导航
	 * 
	 * @param startLat
	 *            起点纬度
	 * @param startLon
	 *            起点经度
	 * @param endLat
	 *            终点纬度
	 * @param endLon
	 *            终点经度
	 * @param driveMode
	 *            驾驶模式
	 */
	public void navi(double startLat, double startLon, double endLat,
			double endLon, int driveMode) {
		this.startLat = startLat;
		this.startLon = startLon;
		this.endLat = endLat;
		this.endLon = endLon;
		mIsEmulatorNavi = false;
		goNavi(mIsEmulatorNavi);
	}

	/**
	 * 模拟导航
	 * 
	 * @param startLat
	 *            起点纬度
	 * @param startLon
	 *            起点经度
	 * @param endLat
	 *            终点纬度
	 * @param endLon
	 *            终点经度
	 * @param driveMode
	 *            驾驶模式
	 */
	public void emulatorNavi(double startLat, double startLon, double endLat,
			double endLon, int driveMode) {
		this.startLat = startLat;
		this.startLon = startLon;
		this.endLat = endLat;
		this.endLon = endLon;
		mIsEmulatorNavi = true;
		goNavi(mIsEmulatorNavi);
	}

	private void goNavi(boolean isEmulator) {
		Intent intent = new Intent(ct, NaviActivity.class);
		Bundle bundle = new Bundle();
		if (mIsEmulatorNavi) {
			bundle.putInt("type", NaviActivity.NaviType_EMULATOR);
		} else {
			bundle.putInt("type", NaviActivity.NaviType_GPS);
		}
		bundle.putDouble("startLat", startLat);
		bundle.putDouble("startLon", startLon);
		bundle.putDouble("endLat", endLat);
		bundle.putDouble("endLon", endLon);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtras(bundle);
		ct.startActivity(intent);
	}

	public void navi(ArrayList<LatLngMDL> starts, ArrayList<LatLngMDL> ends,
			int driveMode) {

	}

	public void emulatorNavi(ArrayList<LatLngMDL> starts,
			ArrayList<LatLngMDL> ends, int driveMode) {

	}
}
