package com.uroad.amaplib.driveroute;

import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.TMC;
import com.uroad.amaplib.R;
import com.uroad.amaplib.driveroute.util.AMapUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * 导航路线图层类。
 */
public class DrivingRouteOverlay extends RouteOverlay {

    private DrivePath drivePath;
    private List<LatLonPoint> throughPointList;
    private List<Marker> throughPointMarkerList = new ArrayList<>();
    private boolean throughPointMarkerVisible = true;
    private PolylineOptions mPolylineOptions;
    private PolylineOptions mPolylineOptionsColor;
    private boolean isColorfulline = true;
    private float mWidth = 25;

    public void setIsColorfulline(boolean iscolorfulline) {
        this.isColorfulline = iscolorfulline;
    }

    /**
     * 根据给定的参数，构造一个导航路线图层类对象。
     *
     * @param amap 地图对象。
     * @param path 导航路线规划方案。
     */
    public DrivingRouteOverlay(AMap amap, DrivePath path, LatLonPoint start, LatLonPoint end, List<LatLonPoint> throughPointList) {
        super(amap);
        this.drivePath = path;
        startPoint = AMapUtil.convertToLatLng(start);
        endPoint = AMapUtil.convertToLatLng(end);
        this.throughPointList = throughPointList;
        //initBitmapDescriptor();
    }

    public float getRouteWidth() {
        return mWidth;
    }

    /**
     * 设置路线宽度
     *
     * @param mWidth 路线宽度，取值范围：大于0
     */
    public void setRouteWidth(float mWidth) {
        this.mWidth = mWidth;
    }

    /**
     * 添加驾车路线添加到地图上显示。
     */
    public void addToMap() {
        initBitmapDescriptor();
        initPolylineOptions();
        try {
            if (mAMap == null) {
                return;
            }
            if (mWidth == 0 || drivePath == null) {
                return;
            }
            // List<LatLng> mLatLngsOfPath = new ArrayList<>();
            List<TMC> tmcs = new ArrayList<>();
            List<DriveStep> drivePaths = drivePath.getSteps();
            mPolylineOptions.add(startPoint);
            for (int i = 0; i < drivePaths.size(); i++) {
                DriveStep step = drivePaths.get(i);
                List<LatLonPoint> latlonPoints = step.getPolyline();
                tmcs.addAll(step.getTMCs());
                //addDrivingStationMarkers(step, convertToLatLng(latlonPoints.get(0)));
                for (LatLonPoint latlonpoint : latlonPoints) {
                    mPolylineOptions.add(convertToLatLng(latlonpoint));
                    // mLatLngsOfPath.add(convertToLatLng(latlonpoint));
                }
            }
            mPolylineOptions.add(endPoint);
            if (startMarker != null) {
                startMarker.remove();
                startMarker = null;
            }
            if (endMarker != null) {
                endMarker.remove();
                endMarker = null;
            }
            addStartAndEndMarker();
            addThroughPointMarker();
            if (isColorfulline) {
                colorWayUpdate(tmcs);
                showColorPolyline();
            } else {
                showPolyline();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化线段属性
     */
    private void initPolylineOptions() {
        mPolylineOptions = null;
        mPolylineOptions = new PolylineOptions();
        mPolylineOptions.color(getDriveColor()).width(getRouteWidth());
        mPolylineOptionsColor = null;
        mPolylineOptionsColor = new PolylineOptions();
        mPolylineOptionsColor.width(getRouteWidth()).setCustomTexture(defaultRoute);
    }

    private void showPolyline() {
        addPolyLine(mPolylineOptions);
    }

    private void showColorPolyline() {
        addPolyLine(mPolylineOptionsColor);
    }

    /**
     * 根据不同的路段拥堵情况展示不同的颜色
     */
    private void colorWayUpdate(List<TMC> tmcSection) {
        if (mAMap == null) {
            return;
        }
        if (tmcSection == null || tmcSection.size() <= 0) {
            return;
        }
        List<Integer> colorList = new ArrayList<>();
        List<BitmapDescriptor> bitmapDescriptors = new ArrayList<>();
        List<LatLng> points = new ArrayList<>();
        List<Integer> texIndexList = new ArrayList<>();
        mPolylineOptionsColor.add(startPoint);
        mPolylineOptionsColor.add(AMapUtil.convertToLatLng(tmcSection.get(0).getPolyline().get(0)));
        points.add(startPoint);
        points.add(AMapUtil.convertToLatLng(tmcSection.get(0).getPolyline().get(0)));
        colorList.add(getDriveColor());
        bitmapDescriptors.add(defaultRoute);
        BitmapDescriptor bitmapDescriptor;
        int textIndex = 0;
        texIndexList.add(textIndex);
        texIndexList.add(++textIndex);
        for (int i = 0; i < tmcSection.size(); i++) {
            TMC segmentTrafficStatus = tmcSection.get(i);
            int color = getcolor(segmentTrafficStatus.getStatus());
            bitmapDescriptor = getTrafficBitmapDescriptor(segmentTrafficStatus.getStatus());
            List<LatLonPoint> mployline = segmentTrafficStatus.getPolyline();
            for (int j = 0; j < mployline.size(); j++) {
                mPolylineOptionsColor.add(AMapUtil.convertToLatLng(mployline.get(j)));
                points.add(AMapUtil.convertToLatLng(mployline.get(j)));
                colorList.add(color);
                texIndexList.add(++textIndex);
                bitmapDescriptors.add(bitmapDescriptor);
            }
        }
        points.add(endPoint);
        colorList.add(getDriveColor());
        bitmapDescriptors.add(defaultRoute);
        texIndexList.add(++textIndex);
        mPolylineOptionsColor.addAll(points);
        mPolylineOptionsColor.setCustomTextureList(bitmapDescriptors);
//        mPolylineOptionsColor.colorValues(colorList);
//        mPolylineOptionsColor.setCustomTextureIndex(texIndexList);
//        mPolylineOptionsColor.setCustomTextureList(bitmapDescriptors);
    }

    private BitmapDescriptor defaultRoute = null;
    private BitmapDescriptor unknownTraffic = null;
    private BitmapDescriptor smoothTraffic = null;
    private BitmapDescriptor slowTraffic = null;
    private BitmapDescriptor jamTraffic = null;
    private BitmapDescriptor veryJamTraffic = null;

    private void initBitmapDescriptor() {
        defaultRoute = getDefaultRoute();
        smoothTraffic = getSmoothTraffic();
        unknownTraffic = getUnknownTraffic();
        slowTraffic = getSlowTraffic();
        jamTraffic = getJamTraffic();
        veryJamTraffic = getVeryJamTraffic();
    }

    public void setTrafficRes(int defaultRoute, int smoothTraffic, int slowTraffic
            , int jamTraffic, int veryJamTraffic, int unknownTraffic) {
        setDefaultRoute(defaultRoute);
        setSmoothTraffic(smoothTraffic);
        setSlowTraffic(slowTraffic);
        setJamTraffic(jamTraffic);
        setVeryJamTraffic(veryJamTraffic);
        setUnknownTraffic(unknownTraffic);
    }

    public void setDefaultRoute(int defaultRoute) {
        this.defaultRoute = BitmapDescriptorFactory.fromResource(defaultRoute);
    }

    public void setUnknownTraffic(int unknownTraffic) {
        this.unknownTraffic = BitmapDescriptorFactory.fromResource(unknownTraffic);
    }

    public void setSmoothTraffic(int smoothTraffic) {
        this.smoothTraffic = BitmapDescriptorFactory.fromResource(smoothTraffic);
    }

    public void setSlowTraffic(int slowTraffic) {
        this.slowTraffic = BitmapDescriptorFactory.fromResource(slowTraffic);
    }

    public void setJamTraffic(int jamTraffic) {
        this.jamTraffic = BitmapDescriptorFactory.fromResource(jamTraffic);
    }

    public void setVeryJamTraffic(int veryJamTraffic) {
        this.veryJamTraffic = BitmapDescriptorFactory.fromResource(veryJamTraffic);
    }

    public BitmapDescriptor getDefaultRoute() {
        if (defaultRoute == null)
            return BitmapDescriptorFactory.fromResource(R.drawable.amap_route_color_texture_6_arrow);
        return defaultRoute;
    }

    public BitmapDescriptor getUnknownTraffic() {
        if (unknownTraffic == null)
            return BitmapDescriptorFactory.fromResource(R.drawable.amap_route_color_texture_0_arrow);
        return unknownTraffic;
    }

    public BitmapDescriptor getSmoothTraffic() {
        if (smoothTraffic == null)
            return BitmapDescriptorFactory.fromResource(R.drawable.amap_route_color_texture_4_arrow);
        return smoothTraffic;
    }

    public BitmapDescriptor getSlowTraffic() {
        if (slowTraffic == null)
            return BitmapDescriptorFactory.fromResource(R.drawable.amap_route_color_texture_2_arrow);
        return slowTraffic;
    }

    public BitmapDescriptor getJamTraffic() {
        if (jamTraffic == null)
            return BitmapDescriptorFactory.fromResource(R.drawable.amap_route_color_texture_9_arrow);
        return jamTraffic;
    }

    public BitmapDescriptor getVeryJamTraffic() {
        return veryJamTraffic;
    }

    private BitmapDescriptor getTrafficBitmapDescriptor(String status) {
        switch (status) {
            case "畅通":
                return smoothTraffic;
            case "缓行":
                return slowTraffic;
            case "拥堵":
                return jamTraffic;
            case "严重拥堵":
                return veryJamTraffic;
            default:
                return defaultRoute;
        }
    }

    private int getcolor(String status) {
        switch (status) {
            case "畅通":
                return Color.GREEN;
            case "缓行":
                return Color.YELLOW;
            case "拥堵":
                return Color.RED;
            case "严重拥堵":
                return Color.parseColor("#990033");
            default:
                return Color.parseColor("#537edc");
        }
    }

    public LatLng convertToLatLng(LatLonPoint point) {
        return new LatLng(point.getLatitude(), point.getLongitude());
    }

    private void addDrivingStationMarkers(DriveStep driveStep, LatLng latLng) {
        addStationMarker(new MarkerOptions()
                .position(latLng)
                .title("\u65B9\u5411:" + driveStep.getAction()
                        + "\n\u9053\u8DEF:" + driveStep.getRoad())
                .snippet(driveStep.getInstruction()).visible(isNodeIconVisible())
                .anchor(0.5f, 0.5f).icon(getDriveBitmapDescriptor()));
    }

    @Override
    protected LatLngBounds getLatLngBounds() {
        LatLngBounds.Builder b = LatLngBounds.builder();
        b.include(new LatLng(startPoint.latitude, startPoint.longitude));
        b.include(new LatLng(endPoint.latitude, endPoint.longitude));
        if (this.throughPointList != null && this.throughPointList.size() > 0) {
            for (int i = 0; i < this.throughPointList.size(); i++) {
                b.include(new LatLng(
                        this.throughPointList.get(i).getLatitude(),
                        this.throughPointList.get(i).getLongitude()));
            }
        }
        return b.build();
    }

    public void setThroughPointIconVisibility(boolean visible) {
        try {
            throughPointMarkerVisible = visible;
            if (this.throughPointMarkerList != null
                    && this.throughPointMarkerList.size() > 0) {
                for (int i = 0; i < this.throughPointMarkerList.size(); i++) {
                    this.throughPointMarkerList.get(i).setVisible(visible);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void addThroughPointMarker() {
        if (this.throughPointList != null && this.throughPointList.size() > 0) {
            LatLonPoint latLonPoint;
            for (int i = 0; i < this.throughPointList.size(); i++) {
                latLonPoint = this.throughPointList.get(i);
                if (latLonPoint != null) {
                    throughPointMarkerList.add(mAMap
                            .addMarker((new MarkerOptions())
                                    .position(new LatLng(latLonPoint
                                            .getLatitude(), latLonPoint
                                            .getLongitude()))
                                    .visible(throughPointMarkerVisible)
                                    .icon(getThroughPointBitDes())
                                    .title("\u9014\u7ECF\u70B9")));
                }
            }
        }
    }

    private BitmapDescriptor getThroughPointBitDes() {
        return BitmapDescriptorFactory.fromResource(R.drawable.amap_through);
    }

    /**
     * 获取两点间距离
     */
    public static int calculateDistance(LatLng start, LatLng end) {
        double x1 = start.longitude;
        double y1 = start.latitude;
        double x2 = end.longitude;
        double y2 = end.latitude;
        return calculateDistance(x1, y1, x2, y2);
    }

    public static int calculateDistance(double x1, double y1, double x2, double y2) {
        final double NF_pi = 0.01745329251994329; // 弧度 PI/180
        x1 *= NF_pi;
        y1 *= NF_pi;
        x2 *= NF_pi;
        y2 *= NF_pi;
        double sinx1 = Math.sin(x1);
        double siny1 = Math.sin(y1);
        double cosx1 = Math.cos(x1);
        double cosy1 = Math.cos(y1);
        double sinx2 = Math.sin(x2);
        double siny2 = Math.sin(y2);
        double cosx2 = Math.cos(x2);
        double cosy2 = Math.cos(y2);
        double[] v1 = new double[3];
        v1[0] = cosy1 * cosx1 - cosy2 * cosx2;
        v1[1] = cosy1 * sinx1 - cosy2 * sinx2;
        v1[2] = siny1 - siny2;
        double dist = Math.sqrt(v1[0] * v1[0] + v1[1] * v1[1] + v1[2] * v1[2]);
        return (int) (Math.asin(dist / 2) * 12742001.5798544);
    }


    //获取指定两点之间固定距离点
    public static LatLng getPointForDis(LatLng sPt, LatLng ePt, double dis) {
        double lSegLength = calculateDistance(sPt, ePt);
        double preResult = dis / lSegLength;
        return new LatLng((ePt.latitude - sPt.latitude) * preResult + sPt.latitude, (ePt.longitude - sPt.longitude) * preResult + sPt.longitude);
    }

    /**
     * 去掉DriveLineOverlay上的线段和标记。
     */
    @Override
    public void removeFromMap() {
        try {
            super.removeFromMap();
            if (this.throughPointMarkerList != null
                    && this.throughPointMarkerList.size() > 0) {
                for (int i = 0; i < this.throughPointMarkerList.size(); i++) {
                    this.throughPointMarkerList.get(i).remove();
                }
                this.throughPointMarkerList.clear();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}