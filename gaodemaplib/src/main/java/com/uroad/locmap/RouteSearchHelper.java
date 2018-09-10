package com.uroad.locmap;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;
import com.amap.api.services.route.WalkRouteResult;
import com.uroad.locmap.model.DrivePathMDL;
import com.uroad.locmap.model.DriveStepMDL;
import com.uroad.locmap.model.LatLngMDL;
import com.uroad.locmap.model.PolylineOptionMDL;

public class RouteSearchHelper {

	private RouteSearchHelper(Context ct) {
		super();
		this.ct = ct;
		routeSearch = new RouteSearch(ct);
		routeSearch.setRouteSearchListener(onRouteSearchListener);
	}

	private Context ct;
	// 路径规划
	RouteSearch routeSearch;
	RouteSearch.FromAndTo fromAndTo;
	PolylineOptionMDL polylineOption;
	OnRouteSearchListener routeSearchListener = null;

	private static RouteSearchHelper routeSearchHelper;

	public static RouteSearchHelper getInstance(Context ct) {
		if (routeSearchHelper == null) {
			routeSearchHelper = new RouteSearchHelper(ct);
		}
		return routeSearchHelper;
	}

	/**
	 * 路径规划
	 * 
	 * @param polylineOptionMDL
	 * @param listener
	 */
	public void searchDriveRoute(PolylineOptionMDL polylineOptionMDL,
			OnRouteSearchListener listener) {
		if (polylineOptionMDL != null && listener != null) {
			this.routeSearchListener = listener;
			LatLonPoint startPoint = new LatLonPoint(polylineOptionMDL
					.getStart().getLatitude(), polylineOptionMDL.getStart()
					.getLongitude());
			LatLonPoint endPoint = new LatLonPoint(polylineOptionMDL.getEnd()
					.getLatitude(), polylineOptionMDL.getEnd().getLongitude());
			fromAndTo = new RouteSearch.FromAndTo(startPoint, endPoint);
			List<LatLonPoint> PassedByPoints = new ArrayList<LatLonPoint>();
			List<List<LatLonPoint>> Avoidpolygons = new ArrayList<List<LatLonPoint>>();
			String avoidRoad = "";
			if (polylineOptionMDL.getPassedByPoints() != null
					&& polylineOptionMDL.getPassedByPoints().size() > 0) {
				for (LatLngMDL latLngMDL : polylineOptionMDL
						.getPassedByPoints()) {
					LatLonPoint latLonPoint = new LatLonPoint(
							latLngMDL.getLatitude(), latLngMDL.getLongitude());
					PassedByPoints.add(latLonPoint);
				}
			}
			if (polylineOptionMDL.getAvoidpolygons() != null
					&& polylineOptionMDL.getAvoidpolygons().size() > 0) {
				for (List<LatLngMDL> latLngMDLs : polylineOptionMDL
						.getAvoidpolygons()) {
					List<LatLonPoint> Points = new ArrayList<LatLonPoint>();
					for (LatLngMDL latLngMDL : latLngMDLs) {
						LatLonPoint latLonPoint = new LatLonPoint(
								latLngMDL.getLatitude(),
								latLngMDL.getLongitude());
						Points.add(latLonPoint);
					}
					Avoidpolygons.add(Points);

				}
			}
			if (!TextUtils.isEmpty(polylineOptionMDL.getAvoidRoad())) {
				avoidRoad = polylineOptionMDL.getAvoidRoad();
			}
			DriveRouteQuery query = new DriveRouteQuery(fromAndTo,
					polylineOptionMDL.getDeiverMode(), PassedByPoints,
					Avoidpolygons, avoidRoad);// 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
			routeSearch.calculateDriveRouteAsyn(query);// 异步路径规划驾车模式查询
		}

	}

	RouteSearch.OnRouteSearchListener onRouteSearchListener = new RouteSearch.OnRouteSearchListener() {

		@Override
		public void onWalkRouteSearched(WalkRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onDriveRouteSearched(DriveRouteResult result, int rCode) {
			// TODO Auto-generated method stub
			if (routeSearchListener != null) {
//				if (rCode == 0) {

					if (result != null && result.getPaths() != null
							&& result.getPaths().size() > 0) {
						DrivePath drivePath = result.getPaths().get(0);
						List<DriveStep> driveSteps = drivePath.getSteps();
						DrivePathMDL drivePathMDL = new DrivePathMDL();
						List<DriveStepMDL> driveStepMDLs = new ArrayList<DriveStepMDL>();
						for (DriveStep driveStep : driveSteps) {
							List<LatLonPoint> lines = driveStep.getPolyline();
							List<LatLngMDL> latLngMDLs = new ArrayList<LatLngMDL>();
							for (LatLonPoint latLonPoint : lines) {
								LatLngMDL latLngMDL = new LatLngMDL(
										latLonPoint.getLatitude(),
										latLonPoint.getLongitude());
								latLngMDLs.add(latLngMDL);

							}
							DriveStepMDL driveStepMDL = new DriveStepMDL();
							driveStepMDL.setAction(driveStep.getAction());
							driveStepMDL.setDistance(driveStep.getDistance());
							driveStepMDL.setDuration(driveStep.getDuration());
							driveStepMDL.setInstruction(driveStep
									.getInstruction());
							driveStepMDL.setRoad(driveStep.getRoad());
							driveStepMDL.setPolyline(latLngMDLs);
							driveStepMDLs.add(driveStepMDL);
						}

						DriveRouteQuery driveRouteQuery = result
								.getDriveQuery();
						LatLngMDL start = new LatLngMDL(driveRouteQuery
								.getFromAndTo().getFrom().getLatitude(),
								driveRouteQuery.getFromAndTo().getFrom()
										.getLongitude());
						LatLngMDL end = new LatLngMDL(driveRouteQuery
								.getFromAndTo().getTo().getLatitude(),
								driveRouteQuery.getFromAndTo().getTo()
										.getLongitude());
						drivePathMDL.setStart(start);
						drivePathMDL.setEnd(end);
						drivePathMDL.setDistance(drivePath.getDistance());
						drivePathMDL.setDuration(drivePath.getDuration());
						drivePathMDL.setStrategy(drivePath.getStrategy());
						drivePathMDL.setTollDistance(drivePath
								.getTollDistance());
						drivePathMDL.setTolls(drivePath.getTolls());
						drivePathMDL.setSteps(driveStepMDLs);
						routeSearchListener.onDriveRouteSearched(drivePathMDL);
					} else {
						routeSearchListener.onDriveRouteSearchedFail("无法规划路径");
					}
				} else {
					routeSearchListener.onDriveRouteSearchedFail("路径规划失败");
				}
//			}
		}

		@Override
		public void onBusRouteSearched(BusRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

		}
	};

	public interface OnRouteSearchListener {
		void onDriveRouteSearched(DrivePathMDL result);

		void onDriveRouteSearchedFail(String message);
	}

	public void setOnRouteSearchListener(OnRouteSearchListener listener) {
		this.routeSearchListener = listener;
	}
}
