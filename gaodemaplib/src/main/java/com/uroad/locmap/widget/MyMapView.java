package com.uroad.locmap.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.VisibleRegion;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearch.OnDistrictSearchListener;
import com.amap.api.services.district.DistrictSearchQuery;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;
import com.amap.api.services.route.WalkRouteResult;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.uroad.locmap.LocationHelper;
import com.uroad.locmap.R;
import com.uroad.locmap.RouteNaviActivity;
import com.uroad.locmap.model.CameraPositionMDL;
import com.uroad.locmap.model.DrivePathMDL;
import com.uroad.locmap.model.DriveStepMDL;
import com.uroad.locmap.model.LatLngMDL;
import com.uroad.locmap.model.LocationMDL;
import com.uroad.locmap.model.MapPoiMDL;
import com.uroad.locmap.model.MarkerClusterMDL;
import com.uroad.locmap.model.MarkerMDL;
import com.uroad.locmap.model.PolylineOptionMDL;
import com.uroad.locmap.model.RouteOverLayMDL;
import com.uroad.locmap.util.DataUtil;
import com.uroad.locmap.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyMapView extends LinearLayout implements LocationSource,
        AMapLocationListener {

    private Context mContext;
    private MapView mapView = null;
    private AMap aMap;
    private Map<String, List<Marker>> markers;
    AMapLocation aMapLocation = null;

    private List<Marker> allMarkers;
    private List<MarkerMDL> allMarkermdls;
    private List<Marker> markersInView;

    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);

    /**
     * 地图缩放大小
     */
    private float zoomSize = 18;
    /**
     * 1:LOCATION_TYPE_LOCATE：只在第一次定位移动到地图中心点。(默认设置)
     * 2:LOCATION_TYPE_MAP_FOLLOW：定位、移动到地图中心点并跟随。
     * 3:LOCATION_TYPE_MAP_ROTATE：定位、移动到地图中心点，跟踪并根据面向方向旋转地图。
     */
    private int mCurrentMode = DataUtil.LOCATION_TYPE_LOCATE;

    /**
     * 是否开启自动定位，默认false
     */
    private boolean autoLocation = false;

    // Marker聚合
    private int height;// 屏幕高度(px)
    private int width;// 屏幕宽度(px)
    Handler timeHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    // 更新markers
                    resetMarks();
                    break;
            }
        }
    };

    // 地图监听器
    OnMapLongClickListener mapLongClickListener = null;
    OnMapTouchListener mapTouchListener = null;
    OnMarkerClickListener markerClickListener = null;
    OnInfoWindowAdapter onInfoWindowAdapter = null;
    OnMapClickListener mapClickListener = null;
    OnRouteSearchListener routeSearchListener = null;
    OnCameraChangeListener cameraChangeListener = null;
    // 路线规划监听
    MyMapNaviListener myMapNaviListener = null;

    // 路径规划
    RouteSearch routeSearch;
    RouteSearch.FromAndTo fromAndTo;
    PolylineOptionMDL polylineOption;
    Polyline driveline = null;
    boolean autoDriveline = false;
    List<Polyline> drivelines = new ArrayList<Polyline>();

    // 定位图标
    Marker locationmarker;
    public LocationMDL locationMDL;
    // 导航
    private AMapNavi aMapNavi;
    // 多路线规划
    private HashMap<Integer, RouteOverLay> routeOverlays = new HashMap<Integer, RouteOverLay>();
    private RouteOverLay mRouteOverLay;
    int[] routeOverlayIds;

    public boolean isAutoLocation() {
        return autoLocation;
    }

    public void setAutoLocation(boolean autoLocation) {
        this.autoLocation = autoLocation;
        if (autoLocation) {
            setUpMap();
        }
    }

    public AMapLocation getaMapLocation() {
        return aMapLocation;
    }

    public void setaMapLocation(AMapLocation aMapLocation) {
        this.aMapLocation = aMapLocation;
    }

    boolean addLocationIcon = false;

    /**
     * 不开启系统定位图层，使用自定义定位
     *
     * @param addLocation
     */
    public void useCustomLocation(boolean addLocation) {
        this.addLocationIcon = addLocation;
        LocationHelper.getInstance(mContext).setLocationListener(
                new LocationHelper.Locationlistener() {
                    @Override
                    public void locationComplete(LocationMDL location) {
                        if (locationmarker != null
                                && locationmarker.isVisible()) {
                            locationmarker.remove();
                        }
                        locationMDL = location;
                        addLocationIcon(new LatLngMDL(location.getLatitude(),
                                location.getLongitude()));
                        if (!addLocationIcon) {
                            LocationHelper.getInstance(mContext)
                                    .closeLocation();
                        }
                    }

                    @Override
                    public void locationFail(String msg) {

                    }
                }
        );
        LocationHelper.getInstance(mContext).openLocation();
    }

    /**
     * 从地图上删除所有的Marker，Overlay，Polyline 等覆盖物
     */
    public final void clear() {
        if (aMap != null) {
            aMap.clear();
        }
    }

    /**
     * 获取视图中心点坐标
     **/
    public LatLngMDL getCenterPoint() {
        LatLng mTarget = aMap.getCameraPosition().target;
        return new LatLngMDL(mTarget.latitude, mTarget.longitude);
    }

    /**
     * @return 返回每个像素代表多少米
     **/
    public float getScalePix() {
        return aMap.getScalePerPixel();
    }

    /**
     * 设置是否可以通过手势缩放地图
     *
     * @param flag
     */
    public void setZoomGesturesEnabled(boolean flag) {
        if (aMap != null) {
            aMap.getUiSettings().setZoomGesturesEnabled(flag); // 设置是否可以通过手势缩放地图
        }
    }

    /**
     * 设置是否可以通过手势平移（滑动）地图
     *
     * @param flag
     */
    public void setScrollGesturesEnabled(boolean flag) {
        if (aMap != null) {
            aMap.getUiSettings().setScrollGesturesEnabled(flag); // 设置是否可以通过手势缩放地图
        }
    }

    /**
     * 设置是否可以通过手势旋转地图
     *
     * @param flag
     */
    public void setRotateGesturesEnabled(boolean flag) {
        if (aMap != null) {
            aMap.getUiSettings().setRotateGesturesEnabled(flag); // 设置是否可以通过手势缩放地图
        }
    }

    /**
     * 设置是否可以通过手势倾斜地图
     *
     * @param flag
     */
    public void setTiltGesturesEnabled(boolean flag) {
        if (aMap != null) {
            aMap.getUiSettings().setTiltGesturesEnabled(flag); // 设置是否可以通过手势缩放地图
        }
    }

    /**
     * 获取当前地图缩放比
     **/
    public float getZoomSize() {
        return zoomSize = aMap.getCameraPosition().zoom;
    }

    public void setZoomSize(float zoomSize) {
        // this.zoomSize = zoomSize;
        // if (aMapLocation != null) {
        // LatLng ll = new LatLng(aMapLocation.getLatitude(),
        // aMapLocation.getLongitude());
        // aMap.animateCamera(CameraUpdateFactory
        // .newCameraPosition(new CameraPosition(ll, zoomSize, 0, 0)),
        // null);
        // }

        if (zoomSize < aMap.getMaxZoomLevel()
                && zoomSize > aMap.getMinZoomLevel()) {
            this.zoomSize = zoomSize;
            aMap.animateCamera(CameraUpdateFactory.zoomTo(zoomSize));
        }

    }

    public int getmCurrentMode() {
        return mCurrentMode;
    }

    public void setmCurrentMode(int mCurrentMode) {
        this.mCurrentMode = mCurrentMode;
        // aMap.setMyLocationType(mCurrentMode);
    }

    public MyMapView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public MyMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        mContext = context;
        init();
    }

    private void init() {
        final View view = LayoutInflater.from(mContext).inflate(
                R.layout.layout_mapview, this, true);
        mapView = (MapView) view.findViewById(R.id.mapView);
        aMap = mapView.getMap();
        aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
        aMap.getUiSettings().setZoomControlsEnabled(false); // 设置缩放控制键不可见
        aMap.getUiSettings().setTiltGesturesEnabled(false);// 禁用倾斜手势
        aMap.getUiSettings().setRotateGesturesEnabled(false);// 禁用旋转手势


        mRouteOverLay = new RouteOverLay(aMap, null, mContext);

        markers = new HashMap<String, List<Marker>>();
        allMarkers = new ArrayList<Marker>();
        allMarkermdls = new ArrayList<MarkerMDL>();
        markersInView = new ArrayList<Marker>();

        routeSearch = new RouteSearch(mContext);

        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay()
                .getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;

        initMapViewListener();

    }

    /**
     * 拿到地图边界四个点的经纬度
     *
     * @return
     */
    public List<LatLngMDL> getWindowLatLng() {
        VisibleRegion visibleRegion = aMap.getProjection().getVisibleRegion();
        LatLng topleft = visibleRegion.farLeft; // 左上
        LatLng bottomright = visibleRegion.nearRight; // 右下
        ArrayList<LatLngMDL> list = new ArrayList<LatLngMDL>();
        list.add(new LatLngMDL(topleft.latitude, topleft.longitude));
        list.add(new LatLngMDL(bottomright.latitude, bottomright.longitude));
        return list;
    }

    /**
     * 获取视野内的marker 根据聚合算法合成自定义的marker 显示视野内的marker
     */
    private void resetMarks() {

        // 开始
        Projection projection = aMap.getProjection();
        Point p = null;
        markersInView.clear();
        // 获取在当前视野内的marker;提高效率
        for (Marker marker : allMarkers) {
            p = projection.toScreenLocation(marker.getPosition());
            if (p.x < 0 || p.y < 0 || p.x > width || p.y > height) {
                // 不添加到计算的列表中
            } else {
                markersInView.add(marker);
            }
        }
        // 自定义的聚合类MyMarkerCluster
        ArrayList<MarkerClusterMDL> clustersMarker = new ArrayList<MarkerClusterMDL>();
        for (Marker marker : markersInView) {
            if (clustersMarker.size() == 0) {
                clustersMarker.add(new MarkerClusterMDL((Activity) mContext,
                        marker, projection, 60));// 100根据自己需求调整
            } else {
                boolean isIn = false;
                for (MarkerClusterMDL cluster : clustersMarker) {
                    if (cluster.getBounds().contains(marker.getPosition())) {
                        cluster.addMarker(marker);
                        isIn = true;
                        break;
                    }
                }
                if (!isIn) {
                    clustersMarker.add(new MarkerClusterMDL(
                            (Activity) mContext, marker, projection, 60));
                }
            }
        }
        // 设置聚合点的位置和icon
        for (MarkerClusterMDL mmc : clustersMarker) {
            mmc.setpositionAndIcon();
        }
        aMap.clear();
        // 重新添加
        for (MarkerClusterMDL cluster : clustersMarker) {
            aMap.addMarker(cluster.getOptions());
        }
    }

    /**
     * 设置缩放控制键可见或不可见
     *
     * @param flag
     */
    public void setZoomControlsEnabled(boolean flag) {
        if (aMap != null) {
            aMap.getUiSettings().setZoomControlsEnabled(flag); // 设置缩放控制键可见或不可见
        }
    }

    /**
     * 选中路线
     **/
    public void setSelectRoute(int routeid) {
        aMapNavi.selectRouteId(routeid);
    }

    AMapNaviListener naviListener = new AMapNaviListener() {

        @Override
        public void updateAimlessModeStatistics(AimLessModeStat arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateAimlessModeCongestionInfo(
                AimLessModeCongestionInfo arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void showLaneInfo(AMapLaneInfo[] arg0, byte[] arg1, byte[] arg2) {
            // TODO Auto-generated method stub

        }

        @Override
        public void showCross(AMapNaviCross arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTrafficStatusUpdate() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStartNavi(int arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onReCalculateRouteForYaw() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onReCalculateRouteForTrafficJam() {
            // TODO Auto-generated method stub

        }

        @Override
        @Deprecated
        public void onNaviInfoUpdated(AMapNaviInfo arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onNaviInfoUpdate(NaviInfo arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLocationChange(AMapNaviLocation arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onInitNaviSuccess() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onInitNaviFailure() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onGpsOpenStatus(boolean arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onGetNavigationText(int arg0, String arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onEndEmulatorNavi() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onCalculateRouteSuccess(int[] routeIds) {
            routeOverlayIds = routeIds;
            HashMap<Integer, RouteOverLayMDL> routes = new HashMap<Integer, RouteOverLayMDL>();
            for (int i = 0; i < routeIds.length; i++) {
                // 你可以通过对应的路径ID获得一条道路路径AMapNaviPath
                AMapNaviPath path = (aMapNavi.getNaviPaths()).get(routeIds[i]);
                // 你可以通过这个AMapNaviPath生成一个RouteOverLay用于加在地图上
                RouteOverLay routeOverLay = new RouteOverLay(aMap, path,
                        mContext);
                routeOverLay.setTrafficLine(true);
                routeOverLay.addToMap();

                routeOverlays.put(routeIds[i], routeOverLay);
                RouteOverLayMDL item = new RouteOverLayMDL();
                item.setRouteOverlay(routeOverLay);
                routes.put(routeIds[i], item);
            }

            routeOverlays.get(routeIds[0]).zoomToSpan();
            if (myMapNaviListener != null)
                myMapNaviListener.onCalculateMultipleRoutesSuccess(routes,
                        routeIds);
        }

        @Override
        public void onCalculateRouteFailure(int arg0) {
            // TODO Auto-generated method stub
            myMapNaviListener.onCalculateRouteFailure(arg0);
        }

        @Override
        public void onArrivedWayPoint(int arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onArriveDestination() {
            // TODO Auto-generated method stub

        }

        @Override
        public void notifyParallelRoad(int arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void hideLaneInfo() {
            // TODO Auto-generated method stub

        }

        @Override
        public void hideCross() {
            // TODO Auto-generated method stub

        }

        @Override
        public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        @Deprecated
        public void OnUpdateTrafficFacility(TrafficFacilityInfo arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        @Deprecated
        public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onGetNavigationText(String s) {

        }

        @Override
        public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {

        }

        @Override
        public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

        }

        @Override
        public void showModeCross(AMapModelCross aMapModelCross) {

        }

        @Override
        public void hideModeCross() {

        }

        @Override
        public void onPlayRing(int i) {

        }
    };

    RouteSearch.OnRouteSearchListener onRouteSearchListener = new RouteSearch.OnRouteSearchListener() {

        @Override
        public void onWalkRouteSearched(WalkRouteResult arg0, int arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onDriveRouteSearched(DriveRouteResult result, int rCode) {
            // TODO Auto-generated method stub
            if (!autoDriveline && routeSearchListener != null) {
                // if (rCode == 0) {

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
                        driveStepMDL.setInstruction(driveStep.getInstruction());
                        driveStepMDL.setRoad(driveStep.getRoad());
                        driveStepMDL.setPolyline(latLngMDLs);
                        driveStepMDLs.add(driveStepMDL);
                    }

                    drivePathMDL.setDistance(drivePath.getDistance());
                    drivePathMDL.setDuration(drivePath.getDuration());
                    drivePathMDL.setStrategy(drivePath.getStrategy());
                    drivePathMDL.setTollDistance(drivePath.getTollDistance());
                    drivePathMDL.setTolls(drivePath.getTolls());
                    drivePathMDL.setSteps(driveStepMDLs);
                    routeSearchListener.onDriveRouteSearched(drivePathMDL);
                } else {
                    routeSearchListener.onDriveRouteSearchedFail("无法规划路径");
                }
                // } else {
                // routeSearchListener.onDriveRouteSearchedFail("路径规划失败");
                // }
            } else {
                // if (rCode == 0) {

                if (result != null && result.getPaths() != null
                        && result.getPaths().size() > 0) {
                    PolylineOptions polylineOptions = new PolylineOptions();
                    polylineOptions.width(polylineOption.getPolylineWidth());
                    polylineOptions.color(polylineOption.getColor());
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
                            polylineOptions
                                    .add(new LatLng(latLonPoint.getLatitude(),
                                            latLonPoint.getLongitude()));
                        }
                        DriveStepMDL driveStepMDL = new DriveStepMDL();
                        driveStepMDL.setAction(driveStep.getAction());
                        driveStepMDL.setDistance(driveStep.getDistance());
                        driveStepMDL.setDuration(driveStep.getDuration());
                        driveStepMDL.setInstruction(driveStep.getInstruction());
                        driveStepMDL.setRoad(driveStep.getRoad());
                        driveStepMDL.setPolyline(latLngMDLs);
                        driveStepMDLs.add(driveStepMDL);
                    }
                    if (driveline != null) {
                        driveline.remove();
                    }
                    driveline = aMap.addPolyline(polylineOptions);
                    drivePathMDL.setDistance(drivePath.getDistance());
                    drivePathMDL.setDuration(drivePath.getDuration());
                    drivePathMDL.setStrategy(drivePath.getStrategy());
                    drivePathMDL.setTollDistance(drivePath.getTollDistance());
                    drivePathMDL.setTolls(drivePath.getTolls());
                    drivePathMDL.setSteps(driveStepMDLs);
                    routeSearchListener.onDriveRouteSearched(drivePathMDL);
                } else {
                    routeSearchListener.onDriveRouteSearchedFail("无法规划路径");
                }
                // } else {
                // routeSearchListener.onDriveRouteSearchedFail("路径规划失败");
                // }
            }
        }

        @Override
        public void onBusRouteSearched(BusRouteResult arg0, int arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

        }
    };

    /**
     * 路径规划(自动在地图上画出规划的路线)
     *
     * @param polylineOptionMDL
     * @param listener
     */
    public void searchDriveRoute(PolylineOptionMDL polylineOptionMDL,
                                 OnRouteSearchListener listener) {
        if (polylineOptionMDL != null && listener != null) {
            autoDriveline = true;
            this.polylineOption = polylineOptionMDL;
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
                PassedByPoints = new ArrayList<LatLonPoint>();
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

    /**
     * 路径规划（手动在地图上画出规划路线）
     *
     * @param start
     * @param end
     * @param deiverMode
     * @param passedByPoints 途经点（最多支持16个）
     * @param avoidpolygons  避让区域（最多支持32个）
     * @param avoidRoad      查询的避让道路
     * @param listener
     */
    public void searchDriveRoute(LatLngMDL start, LatLngMDL end,
                                 int deiverMode, List<LatLngMDL> passedByPoints,
                                 List<List<LatLngMDL>> avoidpolygons, String avoidRoad,
                                 OnRouteSearchListener listener) {
        if (start != null && end != null && listener != null) {
            autoDriveline = false;
            this.routeSearchListener = listener;
            LatLonPoint startPoint = new LatLonPoint(start.getLatitude(),
                    start.getLongitude());
            LatLonPoint endPoint = new LatLonPoint(end.getLatitude(),
                    end.getLongitude());
            fromAndTo = new RouteSearch.FromAndTo(startPoint, endPoint);

            List<LatLonPoint> PassedByPoints = null;
            List<List<LatLonPoint>> Avoidpolygons = null;
            String AvoidRoad = "";
            if (passedByPoints != null && passedByPoints.size() > 0) {
                PassedByPoints = new ArrayList<LatLonPoint>();
                for (LatLngMDL latLngMDL : passedByPoints) {
                    LatLonPoint latLonPoint = new LatLonPoint(
                            latLngMDL.getLatitude(), latLngMDL.getLongitude());
                    PassedByPoints.add(latLonPoint);
                }
            }
            if (avoidpolygons != null && avoidpolygons.size() > 0) {
                Avoidpolygons = new ArrayList<List<LatLonPoint>>();
                for (List<LatLngMDL> latLngMDLs : avoidpolygons) {
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
            if (!TextUtils.isEmpty(avoidRoad)) {
                AvoidRoad = avoidRoad;
            }
            DriveRouteQuery query = new DriveRouteQuery(fromAndTo, deiverMode,
                    PassedByPoints, Avoidpolygons, AvoidRoad);// 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
            routeSearch.calculateDriveRouteAsyn(query);// 异步路径规划驾车模式查询
        }

    }

    /**
     * 路径规划（手动在地图上画出规划路线）
     *
     * @param start
     * @param end
     * @param deiverMode
     * @param listener
     */
    public void searchDriveRoute(LatLngMDL start, LatLngMDL end,
                                 int deiverMode, OnRouteSearchListener listener) {
        if (start != null && end != null && listener != null) {
            autoDriveline = false;
            this.routeSearchListener = listener;
            LatLonPoint startPoint = new LatLonPoint(start.getLatitude(),
                    start.getLongitude());
            LatLonPoint endPoint = new LatLonPoint(end.getLatitude(),
                    end.getLongitude());
            fromAndTo = new RouteSearch.FromAndTo(startPoint, endPoint);
            DriveRouteQuery query = new DriveRouteQuery(fromAndTo, deiverMode,
                    null, null, "");// 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
            routeSearch.calculateDriveRouteAsyn(query);// 异步路径规划驾车模式查询
        }

    }

    /**
     * 获取算路策略 方法: int strategy=mAMapNavi.strategyConvert(congestion,
     * avoidhightspeed, cost, hightspeed, multipleroute); 参数:
     *
     * @congestion 躲避拥堵
     * @avoidhightspeed 不走高速
     * @cost 避免收费
     * @hightspeed 高速优先
     * @multipleroute 多路径
     * <p>
     * 说明:
     * 以上参数都是boolean类型，其中multipleroute参数表示是否多条路线，如果为true则此策略会算出多条路线
     * 。 注意: 不走高速与高速优先不能同时为true 高速优先与避免收费不能同时为true
     */
    public int getRouteStrategy(boolean congestion, boolean avoidhightspeed,
                                boolean cost, boolean hightspeed, boolean multipleroute) {
        int strategy = 0;
        try {
            strategy = aMapNavi.strategyConvert(congestion, avoidhightspeed,
                    cost, hightspeed, multipleroute);
        } catch (Exception e) {
        }

        return strategy;
    }

    /**
     * 多路径规划（自动生成红黄绿路径）
     *
     * @param strategy :使用getRouteStrategy方法进行算路
     **/
    public void searchMultiRoutes(List<LatLngMDL> start, List<LatLngMDL> end,
                                  int strategy) {
        List<NaviLatLng> startList = new ArrayList<NaviLatLng>();
        List<NaviLatLng> endList = new ArrayList<NaviLatLng>();
        for (LatLngMDL item : start) {
            NaviLatLng poi = new NaviLatLng(item.getLatitude(),
                    item.getLongitude());
            startList.add(poi);
        }

        for (LatLngMDL item : end) {
            NaviLatLng poi = new NaviLatLng(item.getLatitude(),
                    item.getLongitude());
            endList.add(poi);
        }

        aMapNavi.calculateDriveRoute(startList, endList, null, strategy);
    }

    /**
     * 多路径规划（自动生成红黄绿路径）
     *
     * @param strategy  :使用getRouteStrategy方法进行算路
     * @param mWayPoint :途经点
     **/
    public void searchMultiRoutes(List<LatLngMDL> start, List<LatLngMDL> end,
                                  List<LatLngMDL> mWayPoint, int strategy) {
        List<NaviLatLng> startList = new ArrayList<NaviLatLng>();
        List<NaviLatLng> endList = new ArrayList<NaviLatLng>();
        List<NaviLatLng> mWayPointList = new ArrayList<NaviLatLng>();
        for (LatLngMDL item : start) {
            NaviLatLng poi = new NaviLatLng(item.getLatitude(),
                    item.getLongitude());
            startList.add(poi);
        }

        for (LatLngMDL item : end) {
            NaviLatLng poi = new NaviLatLng(item.getLatitude(),
                    item.getLongitude());
            endList.add(poi);
        }

        for (LatLngMDL item : mWayPoint) {
            NaviLatLng poi = new NaviLatLng(item.getLatitude(),
                    item.getLongitude());
            mWayPointList.add(poi);
        }

        aMapNavi.calculateDriveRoute(startList, endList, mWayPointList,
                strategy);


    }

    public void researchMultiRoutes(int strategy) {
        aMapNavi.reCalculateRoute(strategy);
    }

    /**
     * @param isGPS :true 表示实时导航，false表示模拟导航
     **/
    public void goNavi(boolean isGPS) {
        Intent intent = new Intent(mContext, RouteNaviActivity.class);
        intent.putExtra("gps", isGPS);
        mContext.startActivity(intent);
    }

    /**
     * 在地图上画出规划的路径(画路径之前会清除地图上的路径)
     *
     * @param width
     * @param color
     * @param list  路径经纬度
     */
    public void drawDriveRoute(float width, int color, List<LatLngMDL> list) {
        try {
            if (!autoDriveline && list != null && list.size() > 0) {
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.width(width);
                polylineOptions.color(color);
                for (LatLngMDL latLngMDL : list) {
                    if (latLngMDL != null) {
                        polylineOptions.add(new LatLng(latLngMDL.getLatitude(),
                                latLngMDL.getLongitude()));
                    }

                }
                if (driveline != null) {
                    driveline.remove();
                }
                driveline = aMap.addPolyline(polylineOptions);
            }
        } catch (Exception e) {
            // TODO: handle exception
        } finally {

        }

    }

    /**
     * 在地图上画出规划的路径(画路径之前会清除地图上的路径)
     *
     * @param mdl  设置
     * @param list 路径经纬度
     */
    public void drawDriveRoute(PolylineOptionMDL mdl, List<LatLngMDL> list) {
        try {
            if (!autoDriveline && list != null && list.size() > 0) {
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.width(mdl.getPolylineWidth());
                polylineOptions.color(mdl.getColor());
                polylineOptions.visible(true);
                polylineOptions.setUseTexture(mdl.isUseTexture());
                polylineOptions.setCustomTexture(BitmapDescriptorFactory
                        .fromResource(mdl.getCustomTexture()));
                for (LatLngMDL latLngMDL : list) {
                    if (latLngMDL != null) {
                        polylineOptions.add(new LatLng(latLngMDL.getLatitude(),
                                latLngMDL.getLongitude()));
                    }

                }
                if (driveline != null) {
                    driveline.remove();
                }
                driveline = aMap.addPolyline(polylineOptions);
            }
        } catch (Exception e) {
            // TODO: handle exception
        } finally {

        }

    }

    /**
     * 在地图上画出规划的路径(画路径之前不会清除地图上的路径，需要手动清除)
     *
     * @param width
     * @param color
     * @param list  路径经纬度
     */
    public void drawDriveRouteNoRemove(float width, int color,
                                       List<LatLngMDL> list) {
        try {
            if (!autoDriveline && list != null && list.size() > 0) {
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.width(width);
                polylineOptions.color(color);
                for (LatLngMDL latLngMDL : list) {
                    if (latLngMDL != null) {
                        polylineOptions.add(new LatLng(latLngMDL.getLatitude(),
                                latLngMDL.getLongitude()));
                    }

                }
                Polyline driveline = aMap.addPolyline(polylineOptions);
                drivelines.add(driveline);
            }
        } catch (Exception e) {
            // TODO: handle exception
        } finally {

        }

    }

    /**
     * 在地图上画出规划的路径(画路径之前不会清除地图上的路径，需要手动清除)
     *
     * @param mdl  设置
     * @param list 路径经纬度
     */
    public void drawDriveRouteNoRemove(PolylineOptionMDL mdl,
                                       List<LatLngMDL> list) {
        try {
            if (!autoDriveline && list != null && list.size() > 0) {
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.width(mdl.getPolylineWidth());
                polylineOptions.color(mdl.getColor());
                polylineOptions.visible(true);
                polylineOptions.setUseTexture(mdl.isUseTexture());
                polylineOptions.setCustomTexture(BitmapDescriptorFactory
                        .fromResource(mdl.getCustomTexture()));
                for (LatLngMDL latLngMDL : list) {
                    if (latLngMDL != null) {
                        polylineOptions.add(new LatLng(latLngMDL.getLatitude(),
                                latLngMDL.getLongitude()));
                    }

                }
                Polyline driveline = aMap.addPolyline(polylineOptions);
                drivelines.add(driveline);
            }
        } catch (Exception e) {
            // TODO: handle exception
        } finally {

        }

    }

    /**
     * 清除地图上所有的路径规划
     */
    public void removeDriveRoute() {
        if (driveline != null) {
            driveline.remove();
        }
        if (drivelines.size() > 0) {
            for (Polyline driveline : drivelines) {
                driveline.remove();
            }
            drivelines.clear();
        }
    }

    private void setUpMap() {
        if (autoLocation) {
//			MyLocationStyle baseLocationOverlay;
//			baseLocationOverlay = new MyLocationStyle();
//			baseLocationOverlay.myLocationIcon(BitmapDescriptorFactory
//					.fromResource(R.drawable.location_marker));// 设置小蓝点的图标
//			baseLocationOverlay.strokeColor(Color.TRANSPARENT);// 设置圆形的边框颜色
//			baseLocationOverlay.radiusFillColor(Color.TRANSPARENT);// 设置圆形的填充颜色
//			aMap.setMyLocationStyle(baseLocationOverlay);
//			aMap.setLocationSource(this);// 设置定位监听
//
//			aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
//			// 设置定位的类型为定位模式：定位（AMap.LOCATION_TYPE_LOCATE）、跟随（AMap.LOCATION_TYPE_MAP_FOLLOW）
//			// 地图根据面向方向旋转（AMap.LOCATION_TYPE_MAP_ROTATE）三种模式
//			// aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_ROTATE);

            aMap.setLocationSource(this);// 设置定位监听
            aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
            aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
            // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
            aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
            etupLocationStyle();
        }
    }

    private void etupLocationStyle() {
        // 自定义系统定位蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        // 自定义定位蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.
                fromResource(R.drawable.location_marker));
        // 自定义精度范围的圆形边框颜色
        myLocationStyle.strokeColor(STROKE_COLOR);
        //自定义精度范围的圆形边框宽度
        myLocationStyle.strokeWidth(5);
        // 设置圆形的填充颜色
        myLocationStyle.radiusFillColor(FILL_COLOR);
        // 将自定义的 myLocationStyle 对象添加到地图上
        aMap.setMyLocationStyle(myLocationStyle);
    }

    /**
     * 初始化地图监听器（当设置了监听器时，必须调用） 改方法弃用
     */
    public void initListener() {
    }

    private void initMapViewListener() {

        aMap.setOnMapLongClickListener(new AMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng arg0) {
                // TODO Auto-generated method stub
                if (mapLongClickListener != null) {
                    mapLongClickListener.onMapLongClick(arg0.latitude,
                            arg0.longitude);
                }
            }
        });

        aMap.setOnMapTouchListener(new AMap.OnMapTouchListener() {

            @Override
            public void onTouch(MotionEvent arg0) {
                if (mapTouchListener != null) {
                    mapTouchListener.onTouch(arg0);
                }

            }
        });

        aMap.setInfoWindowAdapter(new InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                if (onInfoWindowAdapter != null) {
                    int index = allMarkers.indexOf(marker);
                    MarkerMDL markerMDL = allMarkermdls.get(index);
                    return onInfoWindowAdapter.getInfoWindow(markerMDL);
                } else {
                    View view = new View(mContext);
                    view.setVisibility(View.INVISIBLE);
                    return view;
                }
            }

            @Override
            public View getInfoContents(Marker arg0) {
                return null;
            }
        });

        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                if (markerClickListener != null) {
                    int index = allMarkers.indexOf(marker);
                    MarkerMDL markerMDL = allMarkermdls.get(index);

                    markerClickListener.onMarkerClick(markerMDL, index, "");
                }
                return false;
            }

        });

        aMap.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng arg0) {
                // TODO Auto-generated method stub
                if (mapClickListener != null) {
                    LatLngMDL latLngMDL = new LatLngMDL();
                    latLngMDL.setLatitude(arg0.latitude);
                    latLngMDL.setLongitude(arg0.longitude);
                    mapClickListener.onMapClick(latLngMDL);
                    mapClickListener.onMapPoiClick(new MapPoiMDL());
                }
                if (onclickMarker != null && onclickMarkerMDL != null) {
                    if (onclickMarker.isInfoWindowShown()) {
                        onclickMarker.hideInfoWindow();
                    }
                }
            }
        });

        aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (cameraChangeListener != null) {
                    CameraPositionMDL cameraPositionMDL = new CameraPositionMDL();
                    cameraPositionMDL.setBearing(cameraPosition.bearing);
                    cameraPositionMDL.setTarget(new LatLngMDL(
                            cameraPosition.target.latitude,
                            cameraPosition.target.longitude));
                    cameraPositionMDL.setTilt(cameraPosition.tilt);
                    cameraPositionMDL.setZoom(cameraPosition.zoom);
                    cameraChangeListener.onCameraChange(cameraPositionMDL);
                }
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                if (cameraChangeListener != null) {
                    CameraPositionMDL cameraPositionMDL = new CameraPositionMDL();
                    cameraPositionMDL.setBearing(cameraPosition.bearing);
                    cameraPositionMDL.setTarget(new LatLngMDL(
                            cameraPosition.target.latitude,
                            cameraPosition.target.longitude));
                    cameraPositionMDL.setTilt(cameraPosition.tilt);
                    cameraPositionMDL.setZoom(cameraPosition.zoom);
                    cameraChangeListener
                            .onCameraChangeFinish(cameraPositionMDL);
                }
            }
        });

        routeSearch.setRouteSearchListener(onRouteSearchListener);


    }

    public static float Convert2Float(String val) {
        try {
            return Float.valueOf(val);
        } catch (Exception e) {
            // TODO: handle exception
            return 0;
        }
    }

    /**
     * 方法必须重写
     */
    public void onSaveInstanceState(Bundle outState) {
        mapView.onSaveInstanceState(outState);
    }

    public void onCreate(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        aMapNavi = AMapNavi.getInstance(mContext.getApplicationContext());// 新版导航，增加了多路径规划
        aMapNavi.addAMapNaviListener(naviListener);// 路径导航
    }

    public void onPause() {
        mapView.onPause();
        deactivate();
    }

    public void onResume() {
        mapView.onResume();
        if (mRouteOverLay != null) {
            mRouteOverLay.removeFromMap();
        }
        if (routeOverlayIds != null && routeOverlayIds.length > 0) {
            for (int i = 0; i < routeOverlayIds.length; i++) {
                routeOverlays.get(routeOverlayIds[i]).removeFromMap();
            }
        }
    }

    public void onDestroy() {
        mapView.onDestroy();
        aMapNavi.destroy();
        if (null != mlocationClient) {
            mlocationClient.onDestroy();
        }
    }

    public interface OnMapLongClickListener {
        void onMapLongClick(double latitude, double longitude);
    }

    public void setOnMapLongClickListener(OnMapLongClickListener listener) {
        this.mapLongClickListener = listener;
    }

    public interface OnMapTouchListener {
        void onTouch(MotionEvent event);
    }

    public void setOnMapTouchListener(OnMapTouchListener listener) {
        this.mapTouchListener = listener;
    }

    public interface OnMarkerClickListener {
        View onMarkerClick(MarkerMDL markerMDL, int index, String type);
    }

    public void setOnMarkerClickListener(OnMarkerClickListener listener) {
        this.markerClickListener = listener;
    }

    public interface OnMapClickListener {
        boolean onMapPoiClick(MapPoiMDL mapPoiMDL);

        void onMapClick(LatLngMDL latLngMDL);
    }

    public interface OnInfoWindowAdapter {
        View getInfoWindow(MarkerMDL markerMDL);

        View getInfoContents(MarkerMDL markerMDL);
    }

    public void setOnMapClickListener(OnMapClickListener listener) {
        this.mapClickListener = listener;
    }

    public void setOnInfoWindowAdapter(OnInfoWindowAdapter adapter) {
        this.onInfoWindowAdapter = adapter;
    }

    public interface OnRouteSearchListener {
        void onDriveRouteSearched(DrivePathMDL result);

        void onDriveRouteSearchedFail(String message);
    }

    public void setOnRouteSearchListener(OnRouteSearchListener listener) {
        this.routeSearchListener = listener;
    }

    public interface OnCameraChangeListener {
        void onCameraChangeFinish(CameraPositionMDL cameraPositionMDL);

        void onCameraChange(CameraPositionMDL cameraPositionMDL);
    }

    public void setOnCameraChangeListener(OnCameraChangeListener listener) {
        this.cameraChangeListener = listener;
    }

    public interface MyMapNaviListener {
        void onCalculateRouteSuccess(RouteOverLayMDL route);

        void onCalculateMultipleRoutesSuccess(
                HashMap<Integer, RouteOverLayMDL> routes, int[] routeIds);

        void onCalculateRouteFailure(int arg0);
    }


    public void setMyMapNaviListener(MyMapNaviListener listener) {
        myMapNaviListener = listener;
    }

    /**
     * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
     *
     * @param input
     * @return boolean
     */
    public static boolean isEmpty(String input) {
        if (input == null || "".equals(input))
            return true;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    /**
     * 选择1:标准地图/2:卫星地图/3:夜景地图事件的响应
     *
     * @param layerName
     */
    public void setLayer(String layerName) {
        if (layerName.equals("1")) {
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 标准地图模式
        } else if (layerName.equals("2")) {
            aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// 卫星地图模式
        } else if (layerName.equals("3")) {
            aMap.setMapType(AMap.MAP_TYPE_NIGHT);// 夜景地图模式
        }
    }

    public void remove(MarkerMDL markerMDL) {
        int index = allMarkermdls.indexOf(markerMDL);
        if(index>0){
            allMarkermdls.remove(index);
            allMarkers.get(index).remove();
            allMarkers.remove(index);
        }

    }

    /**
     * 在地图上增加兴趣点
     *
     * @param latitude
     * @param longitude
     * @param view
     * @return
     */
    public MarkerMDL addOverlay(double latitude, double longitude, View view) {

        LatLng ll = new LatLng(latitude, longitude);
        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(ll);
        markerOption.title("").snippet("");
        markerOption.perspective(true);
        markerOption.draggable(true);
        markerOption.icon(BitmapDescriptorFactory.fromView(view));
        Marker marker = aMap.addMarker(markerOption);
        allMarkers.add(marker);

        MarkerMDL markerMDL = new MarkerMDL();
        markerMDL.setMarker(marker);
        markerMDL.setLatitude(latitude);
        markerMDL.setLongitude(longitude);
        markerMDL.setVisible(marker.isVisible());
        allMarkermdls.add(markerMDL);
        markerMDL.setMarker(marker);
        return markerMDL;
    }

    /**
     * 在地图上增加兴趣点
     *
     * @param latitude
     * @param longitude
     * @param icon
     * @return
     */
    public MarkerMDL addOverlay(double latitude, double longitude, int icon) {

        LatLng ll = new LatLng(latitude, longitude);
        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(ll);
        markerOption.title("").snippet("");
        markerOption.perspective(true);
        markerOption.draggable(true);
        markerOption.icon(BitmapDescriptorFactory.fromResource(icon));
        Marker marker = aMap.addMarker(markerOption);
        allMarkers.add(marker);

        MarkerMDL markerMDL = new MarkerMDL();
        markerMDL.setMarker(marker);
        markerMDL.setLatitude(latitude);
        markerMDL.setLongitude(longitude);
        markerMDL.setVisible(marker.isVisible());
        markerMDL.setIcon(icon);
        allMarkermdls.add(markerMDL);
        return markerMDL;
    }

    /**
     * 在地图上增加兴趣点
     *
     * @param markerMDL
     * @return
     */
    public MarkerMDL addOverlay(MarkerMDL markerMDL) {
        if (markerMDL != null) {
            LatLng ll = new LatLng(markerMDL.getLatitude(),
                    markerMDL.getLongitude());
            MarkerOptions markerOption = new MarkerOptions();
            markerOption.position(ll);
            markerOption.title(markerMDL.getTitle()).snippet(
                    markerMDL.getSnippet());
            markerOption.perspective(true);
            markerOption.draggable(true);
            markerOption.icon(BitmapDescriptorFactory.fromResource(markerMDL
                    .getIcon()));
            markerOption.setInfoWindowOffset(markerMDL.getOffsetX(),
                    markerMDL.getOffsetY());
            Marker marker = aMap.addMarker(markerOption);
            allMarkers.add(marker);
            markerMDL.setMarker(marker);
            markerMDL.setVisible(marker.isVisible());
            allMarkermdls.add(markerMDL);
        }

        return markerMDL;
    }

    /**
     * 在地图上增加兴趣点
     *
     * @param type       Marker的类型
     * @param MarkerMDLs
     */
    public void addOverlays(String type, List<MarkerMDL> MarkerMDLs) {
        if (markers != null && markers.size() > 0) {
            List<Marker> list = new ArrayList<Marker>();
            for (MarkerMDL markerMDL : MarkerMDLs) {
                if (!isEmpty(markerMDL.getLatitude() + "")
                        && !isEmpty(markerMDL.getLongitude() + "")
                        && !isEmpty(markerMDL.getIcon() + "")) {
                    LatLng ll = new LatLng(markerMDL.getLatitude(),
                            markerMDL.getLongitude());
                    MarkerOptions markerOption = new MarkerOptions();
                    markerOption.position(ll);
                    markerOption.title(markerMDL.getTitle()).snippet(
                            markerMDL.getSnippet());
                    markerOption.perspective(true);
                    markerOption.draggable(true);
                    markerOption.icon(BitmapDescriptorFactory
                            .fromResource(markerMDL.getIcon()));
                    markerOption.setInfoWindowOffset(markerMDL.getOffsetX(),
                            markerMDL.getOffsetY());
                    Marker marker = aMap.addMarker(markerOption);
                    list.add(marker);
                }
            }
            markers.put(type, list);
        }
    }

    /**
     * 设置兴趣点显示或隐藏
     *
     * @param type    Marker的类型
     * @param visible 设置兴趣点显示或隐藏
     */
    public void setVisible(String type, boolean visible) {
        if (markers != null && markers.size() > 0) {
            try {
                List<Marker> list = markers.get(type);
                if (list != null && list.size() > 0) {
                    for (Marker marker : list) {
                        marker.setVisible(visible);
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

        }
    }

    /**
     * 设置兴趣点显示或隐藏
     *
     * @param markerMDL
     * @param visible
     */
    public void setVisible(MarkerMDL markerMDL, boolean visible) {
        /*
         * if (allMarkers != null && allMarkers.size() > 0) { try { for (Marker
		 * marker : allMarkers) { if (markerMDL.getLatitude() > 0 &&
		 * markerMDL.getLongitude() > 0 && markerMDL.getLatitude() ==
		 * marker.getPosition().latitude && markerMDL.getLongitude() ==
		 * marker.getPosition().longitude) { marker.setVisible(visible); break;
		 * } }
		 * 
		 * } catch (Exception e) { }
		 * 
		 * }
		 */

        if (markerMDL != null) {
            try {
                int index = allMarkermdls.indexOf(markerMDL);
                Marker marker = allMarkers.get(index);
                marker.setVisible(visible);
            } catch (Exception e) {
                // TODO: handle exception
            }

        }

    }

    MarkerMDL onclickMarkerMDL;
    Marker onclickMarker;
    int onclickIcon = 0;

    /**
     * 设置兴趣点图标
     *
     * @param markerMDL
     * @param icon
     */
    public void setIcon(MarkerMDL markerMDL, int icon) {
        if (markerMDL != null) {
            // try {
            // if (onclickMarkerMDL != null
            // && markerMDL.getLatitude() == onclickMarkerMDL
            // .getLatitude()
            // && markerMDL.getLongitude() == onclickMarkerMDL
            // .getLongitude() && onclickIcon == icon) {
            // return;
            // }
            // onclickMarkerMDL = markerMDL;
            // onclickIcon = icon;
            // int index = allMarkermdls.indexOf(markerMDL);
            // Marker marker = allMarkers.get(index);
            // marker.setIcon(BitmapDescriptorFactory.fromResource(icon));
            // } catch (Exception e) {
            // // TODO: handle exception
            // }

            int index = allMarkermdls.indexOf(markerMDL);
            Marker marker = allMarkers.get(index);
            if (onclickMarker != null && onclickMarkerMDL != null) {
                onclickMarker.setIcon(BitmapDescriptorFactory
                        .fromResource(onclickMarkerMDL.getIcon()));
            }
            marker.setIcon(BitmapDescriptorFactory.fromResource(icon));
            onclickMarker = marker;
            onclickMarkerMDL = markerMDL;
        }
    }

    /**
     * 根据屏幕像素点固定Marker的位置
     *
     * @param markerMDL
     * @param x
     * @param y
     */
    public void setPositionByPixels(MarkerMDL markerMDL, int x, int y) {
        if (markerMDL != null) {
            try {
                int index = allMarkermdls.indexOf(markerMDL);
                Marker marker = allMarkers.get(index);
                marker.setPositionByPixels(x, y);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    /**
     * 设置显示在屏幕中的地图地理范围
     *
     * @param LatLngMDLs
     */
    public void includeLatLngBounds(List<LatLngMDL> LatLngMDLs) {
        if (LatLngMDLs != null && LatLngMDLs.size() > 0) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLngMDL latLngMDL : LatLngMDLs) {
                LatLng latLng = new LatLng(latLngMDL.getLatitude(),
                        latLngMDL.getLongitude());
                builder.include(latLng);
            }
            LatLngBounds bounds = builder.build();
            // 移动地图，所有marker自适应显示。LatLngBounds与地图边缘10像素的填充区域
            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
        }
    }

    /**
     * 设置显示在屏幕中的地图地理范围
     *
     * @param LatLngMDLs
     * @param bound      地图边缘的距离
     */
    public void includeLatLngBounds(List<LatLngMDL> LatLngMDLs, int bound) {
        if (LatLngMDLs != null && LatLngMDLs.size() > 0) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLngMDL latLngMDL : LatLngMDLs) {
                LatLng latLng = new LatLng(latLngMDL.getLatitude(),
                        latLngMDL.getLongitude());
                builder.include(latLng);
            }
            LatLngBounds bounds = builder.build();
            // 移动地图，所有marker自适应显示。LatLngBounds与地图边缘10像素的填充区域
            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, bound));
        }
    }

    /**
     * 放大地图,默认放大0.5
     */
    public void zoomUp() {
        // if (aMapLocation != null
        // && aMap.getCameraPosition().zoom < aMap.getMaxZoomLevel() - 0.5) {
        // float zoom = Convert2Float((aMap.getCameraPosition().zoom + 0.5)
        // + "");
        // LatLng ll = new LatLng(aMapLocation.getLatitude(),
        // aMapLocation.getLongitude());
        // aMap.animateCamera(CameraUpdateFactory
        // .newCameraPosition(new CameraPosition(ll, zoom, 0, 0)),
        // null);
        // }
        if (aMap.getCameraPosition().zoom < aMap.getMaxZoomLevel() - 0.5) {
            float zoom = Convert2Float((aMap.getCameraPosition().zoom + 0.5)
                    + "");
            aMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
        }
    }

    /**
     * 放大地图
     *
     * @param size 放大尺寸
     */
    public void zoomUp(float size) {
        // if (aMapLocation != null
        // && aMap.getCameraPosition().zoom < aMap.getMaxZoomLevel()
        // - size) {
        // float zoom = Convert2Float((aMap.getCameraPosition().zoom + size)
        // + "");
        // LatLng ll = new LatLng(aMapLocation.getLatitude(),
        // aMapLocation.getLongitude());
        // aMap.animateCamera(CameraUpdateFactory
        // .newCameraPosition(new CameraPosition(ll, zoom, 0, 0)),
        // null);
        // }
        if (aMap.getCameraPosition().zoom < aMap.getMaxZoomLevel() - size) {
            float zoom = Convert2Float((aMap.getCameraPosition().zoom + size)
                    + "");
            aMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
        }
    }

    /**
     * 缩小地图
     */
    public void zoomDown() {
        // if (aMapLocation != null
        // && aMap.getCameraPosition().zoom > aMap.getMinZoomLevel() + 0.5) {
        // float zoom = Convert2Float((aMap.getCameraPosition().zoom - 0.5)
        // + "");
        // LatLng ll = new LatLng(aMapLocation.getLatitude(),
        // aMapLocation.getLongitude());
        // aMap.animateCamera(CameraUpdateFactory
        // .newCameraPosition(new CameraPosition(ll, zoom, 0, 0)),
        // null);
        // }
        if (aMap.getCameraPosition().zoom > aMap.getMinZoomLevel() + 0.5) {
            float zoom = Convert2Float((aMap.getCameraPosition().zoom - 0.5)
                    + "");
            aMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
        }
    }

    /**
     * 缩小地图
     *
     * @param size 缩小尺寸
     */
    public void zoomDown(float size) {
        // if (aMapLocation != null
        // && aMap.getCameraPosition().zoom > aMap.getMinZoomLevel()
        // + size) {
        // float zoom = Convert2Float((aMap.getCameraPosition().zoom - size)
        // + "");
        // LatLng ll = new LatLng(aMapLocation.getLatitude(),
        // aMapLocation.getLongitude());
        // aMap.animateCamera(CameraUpdateFactory
        // .newCameraPosition(new CameraPosition(ll, zoom, 0, 0)),
        // null);
        // }
        if (aMap.getCameraPosition().zoom > aMap.getMinZoomLevel() + size) {
            float zoom = Convert2Float((aMap.getCameraPosition().zoom - size)
                    + "");
            aMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
        }
    }


    public void scrollBy(float xPiexl, float yPiexl, AMap.CancelableCallback callback) {
        try {
            if (aMap == null)
                return;
            aMap.animateCamera(CameraUpdateFactory.scrollBy(xPiexl, yPiexl), callback);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    /**
     * 移动到地图上某一点
     *
     * @param latitude
     * @param longitude
     */
    public void animateMapStatus(double latitude, double longitude) {
        try {
            if (aMap == null)
                return;
            LatLng ll = new LatLng(latitude, longitude);
            aMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(new CameraPosition(ll, aMap
                            .getCameraPosition().zoom, 0, 0)), null);

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    /**
     * 移动到地图上某一点
     *
     * @param latitude
     * @param longitude
     */
    public void animateMapStatus(double latitude, double longitude, AMap.CancelableCallback callback) {
        try {
            if (aMap == null)
                return;
            LatLng ll = new LatLng(latitude, longitude);
            aMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(new CameraPosition(ll, aMap
                            .getCameraPosition().zoom, 0, 0)), callback);

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    /**
     * 移动到地图上某一点
     *
     * @param latitude
     * @param longitude
     * @param zoom      地图的缩放级别
     */
    public void animateMapStatus(double latitude, double longitude, float zoom) {
        try {
            if (aMap == null)
                return;
            LatLng ll = new LatLng(latitude, longitude);
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, zoom));
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    /**
     * 移动到地图上某一点并改变倾斜度和指向的方向
     *
     * @param latitude
     * @param longitude
     * @param tilt      目标可视区域的倾斜度，以角度为单位。
     * @param bearing   可视区域指向的方向，以角度为单位，从正北向顺时针方向计算，从0 度到360 度。
     */
    public void animateMapStatus(double latitude, double longitude, float tilt,
                                 float bearing) {
        try {
            if (aMap == null)
                return;
            LatLng ll = new LatLng(latitude, longitude);
            aMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(new CameraPosition(ll, aMap
                            .getCameraPosition().zoom, tilt, bearing)), null);

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    /**
     * 移动到地图上某一点并改变倾斜度和指向的方向
     *
     * @param latitude
     * @param longitude
     * @param tilt      目标可视区域的倾斜度，以角度为单位。
     * @param bearing   可视区域指向的方向，以角度为单位，从正北向顺时针方向计算，从0 度到360 度。
     * @param zoom
     */
    public void animateMapStatus(double latitude, double longitude, float tilt,
                                 float bearing, float zoom) {
        try {
            if (aMap == null)
                return;
            LatLng ll = new LatLng(latitude, longitude);
            aMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(new CameraPosition(ll, zoom, tilt,
                            bearing)), null);

        } catch (Exception e) {
        }
    }

    public void invalidate() {
        mapView.invalidate();
    }

    /**
     * 增加定位图标
     */
    public void addLocationIcon(LatLngMDL latLngMDL) {
        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(new LatLng(latLngMDL.getLatitude(), latLngMDL
                .getLongitude()));
        markerOption.title("").snippet("");
        markerOption.icon(BitmapDescriptorFactory
                .fromResource(R.drawable.location_marker));
        if (locationmarker != null) {
            locationmarker.remove();
            locationmarker = null;
        }
        locationmarker = aMap.addMarker(markerOption);
    }

    /**
     * 增加定位图标(自定义)
     */
    public void addLocationIcon(LatLngMDL latLngMDL, int id) {
        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(new LatLng(latLngMDL.getLatitude(), latLngMDL
                .getLongitude()));
        markerOption.title("").snippet("");
        markerOption.icon(BitmapDescriptorFactory.fromResource(id));
        if (locationmarker != null) {
            locationmarker.remove();
            locationmarker = null;
        }
        locationmarker = aMap.addMarker(markerOption);
    }

    /**
     * 定位到当前位置
     *
     * @param
     */
    public void onLocation(LatLngMDL latLngMDL) {
        try {
            if (latLngMDL == null || aMap == null)
                return;
            LatLng ll = new LatLng(latLngMDL.getLatitude(),
                    latLngMDL.getLongitude());
            aMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(new CameraPosition(ll, aMap
                            .getCameraPosition().zoom, 0, 0)), null);

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    /**
     * 定位到当前位置
     *
     * @param
     */
    public void onLocation(LocationMDL locationMDL) {
        try {
            if (locationMDL == null || aMap == null)
                return;
            LatLng ll = new LatLng(locationMDL.getLatitude(),
                    locationMDL.getLongitude());
            aMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(new CameraPosition(ll, aMap
                            .getCameraPosition().zoom, 0, 0)), null);

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    /**
     * 获取屏幕某一点的经纬度
     *
     * @param point
     */
    public LatLngMDL fromScreenLocation(Point point) {
        LatLng latLng = aMap.getProjection().fromScreenLocation(point);
        LatLngMDL latLngMDL = null;
        if (latLng != null) {
            latLngMDL = new LatLngMDL(latLng.latitude, latLng.longitude);
        }
        return latLngMDL;
    }

    /**
     * 设置实时交通图
     *
     * @param flag
     */
    public void setTrafficEnabled(boolean flag) {
        aMap.setTrafficEnabled(flag);
    }

    /**
     * 设置百度城市热力图(百度地图特有)
     *
     * @param flag
     */
    public void setBaiduHeatMapEnabled(boolean flag) {

    }

//	@Override
//	public void onLocationChanged(Location location) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void onProviderDisabled(String provider) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void onProviderEnabled(String provider) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void onStatusChanged(String provider, int status, Bundle extras) {
//		// TODO Auto-generated method stub
//
//	}

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (autoLocation) {
            if (mListener != null && amapLocation != null) {
//				if (amapLocation.getAMapException().getErrorCode() == 0) {
//					this.aMapLocation = amapLocation;
//					mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
//				}

                if (amapLocation != null
                        && amapLocation.getErrorCode() == 0) {
                    mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(18));
                } else {
                    String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                    ToastUtil.show(mContext, errText);
//					mLocationErrText.setVisibility(View.VISIBLE);
//					mLocationErrText.setText(errText);
                }
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        if (autoLocation) {
            mListener = listener;
//			if (mAMapLocationManager == null) {
//				mAMapLocationManager = LocationManagerProxy
//						.getInstance(mContext);
//				// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
//				// 注意设置合适的定位时间的间隔，并且在合适时间调用removeUpdates()方法来取消定位请求
//				// 在定位结束后，在合适的生命周期调用destroy()方法
//				// 其中如果间隔时间为-1，则定位只定一次
//				mAMapLocationManager.requestLocationData(
//						LocationProviderProxy.AMapNetwork, 5 * 1000, 10, this);
//			}

            if (mlocationClient == null) {
                mlocationClient = new AMapLocationClient(mContext);
                mLocationOption = new AMapLocationClientOption();
                //设置定位监听
                mlocationClient.setLocationListener(this);
                //设置为高精度定位模式
                mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
                //设置定位参数
                mlocationClient.setLocationOption(mLocationOption);
                // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
                // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
                // 在定位结束后，在合适的生命周期调用onDestroy()方法
                // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
                mlocationClient.startLocation();
            }
        }

    }

    @Override
    public void deactivate() {
        mListener = null;
//		if (mAMapLocationManager != null) {
//			mAMapLocationManager.removeUpdates(this);
//			mAMapLocationManager.destroy();
//		}
//		mAMapLocationManager = null;

        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    /**
     * 设置行政区域边界
     *
     * @param province
     * 省份
     * @param width
     * 画线宽度
     * @param color
     * 画线颜色
     */
    float districtWithBoundaryWidth;
    int districtWithBoundaryColor;

    public void setDistrictWithBoundary(String province, float width, int color) {
        this.districtWithBoundaryColor = color;
        this.districtWithBoundaryWidth = width;
        DistrictSearch search = new DistrictSearch(mContext);
        DistrictSearchQuery query = new DistrictSearchQuery();
        query.setKeywords(province);
        query.setShowBoundary(true);
        search.setQuery(query);
        search.setOnDistrictSearchListener(districtSearchListener);
        search.searchDistrictAnsy();
    }

    OnDistrictSearchListener districtSearchListener = new OnDistrictSearchListener() {

        @Override
        public void onDistrictSearched(DistrictResult districtResult) {
            if (districtResult == null || districtResult.getDistrict() == null) {
                return;
            }
            final DistrictItem item = districtResult.getDistrict().get(0);

            if (item == null) {
                return;
            }

            new Thread() {
                public void run() {

                    String[] polyStr = item.districtBoundary();
                    if (polyStr == null || polyStr.length == 0) {
                        return;
                    }
                    for (String str : polyStr) {
                        String[] lat = str.split(";");
                        PolylineOptions polylineOption = new PolylineOptions();
                        boolean isFirst = true;
                        LatLng firstLatLng = null;
                        for (String latstr : lat) {
                            String[] lats = latstr.split(",");
                            if (isFirst) {
                                isFirst = false;
                                firstLatLng = new LatLng(
                                        Double.parseDouble(lats[1]),
                                        Double.parseDouble(lats[0]));
                            }
                            polylineOption.add(new LatLng(Double
                                    .parseDouble(lats[1]), Double
                                    .parseDouble(lats[0])));
                        }
                        if (firstLatLng != null) {
                            polylineOption.add(firstLatLng);
                        }

                        polylineOption.width(districtWithBoundaryWidth).color(
                                districtWithBoundaryColor);
                        aMap.addPolyline(polylineOption);
                    }
                }
            }.start();

        }
    };

    public void getMapScreenShot(final onScreenShot screenShot) {
        aMap.getMapScreenShot(new AMap.OnMapScreenShotListener() {
            @Override
            public void onMapScreenShot(Bitmap bitmap) {

            }

            @Override
            public void onMapScreenShot(Bitmap bitmap, int i) {
                screenShot.onMapScreenShot(bitmap, i);
            }
        });
    }

    public interface onScreenShot {
        void onMapScreenShot(Bitmap bitmap, int status);
    }

}
