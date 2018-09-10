package com.uroad.amaplib.navi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Location;

import com.amap.api.maps.AMap;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.NaviSetting;
import com.amap.api.navi.enums.SoundQuality;
import com.amap.api.navi.model.AMapCalcRouteResult;
import com.amap.api.navi.model.AMapCarInfo;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviGuide;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviRouteNotifyData;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AMapTrafficStatus;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.model.RouteOverlayOptions;
import com.amap.api.navi.view.RouteOverLay;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.uroad.amaplib.R;
import com.uroad.amaplib.navi.simple.SimpleNavigationListener;

import java.util.HashMap;
import java.util.List;

/**
 * Created by MFB on 2018/9/7.
 */
public class AMapNaviManager {
    private static volatile AMapNaviManager instance;
    private AMapNavi mAMapNavi;

    private AMapNaviManager(Context context) {
        mAMapNavi = AMapNavi.getInstance(context);
    }

    public AMapNaviManager getInstance(Context context) {
        if (instance == null) {
            synchronized (AMapNaviManager.class) {
                if (instance == null) {
                    instance = new AMapNaviManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    /**
     * 计算驾车路径(不带起点的驾车路径规划。)
     *
     * @param to        终点坐标
     * @param wayPoints 途经点坐标
     * @param strategy  路径的计算策略
     */
    public void calculateDriveRoute(List<NaviLatLng> to, List<NaviLatLng> wayPoints, int strategy) {
        mAMapNavi.calculateDriveRoute(to, wayPoints, strategy);
    }

    /**
     * 计算驾车路径(包含起点)。
     *
     * @param from      起点坐标
     * @param to        终点坐标
     * @param wayPoints 途经点坐标
     * @param strategy  路径的计算策略
     */
    public void calculateDriveRoute(List<NaviLatLng> from, List<NaviLatLng> to, List<NaviLatLng> wayPoints, int strategy) {
        mAMapNavi.calculateDriveRoute(from, to, wayPoints, strategy);
    }

    /**
     * 计算驾车路径(不带起点，起点默认为当前位置)。
     *
     * @param toPoiId     终点id
     * @param wayPointIds 途径点id集合
     * @param strategy    路径的计算策略
     */
    public void calculateDriveRoute(String toPoiId, List<String> wayPointIds, int strategy) {
        mAMapNavi.calculateDriveRoute(toPoiId, wayPointIds, strategy);
    }

    /**
     * 计算驾车路径(包含起点)。
     *
     * @param fromPoiId 起点id
     * @param toPoiId   终点id
     * @param wayPoints 途径点id集合
     * @param strateg   路径的计算策略
     */
    public void calculateDriveRoute(String fromPoiId, String toPoiId, List<java.lang.String> wayPoints, int strateg) {
        mAMapNavi.calculateDriveRoute(fromPoiId, toPoiId, wayPoints, strateg);
    }

    /**
     * 计算骑行路径(不带起点，默认为当前位置)。
     *
     * @param to 终点坐标
     */
    public void calculateRideRoute(NaviLatLng to) {
        mAMapNavi.calculateRideRoute(to);
    }

    /**
     * 计算骑行路径(包含起点)。
     *
     * @param from 起点坐标
     * @param to   终点坐标
     */
    public void calculateRideRoute(NaviLatLng from, NaviLatLng to) {
        mAMapNavi.calculateRideRoute(from, to);
    }

    /**
     * 计算步行路径(不带起点，默认为当前位置)。
     *
     * @param to 终点坐标
     */
    public void calculateWalkRoute(NaviLatLng to) {
        mAMapNavi.calculateWalkRoute(to);
    }

    /**
     * 计算步行路径(包含起点)。
     *
     * @param from 起点坐标
     * @param to   终点坐标
     */
    public void calculateWalkRoute(NaviLatLng from, NaviLatLng to) {
        mAMapNavi.calculateWalkRoute(from, to);
    }

    /**
     * 获得导航引擎类型
     */
    public int getEngineType() {
        return mAMapNavi.getEngineType();
    }

    /*获取是否使用外部GPS数据*/
    public boolean getIsUseExtraGPSData() {
        return mAMapNavi.getIsUseExtraGPSData();
    }

    /*获取是否使用内部语音播报*/
    public boolean getIsUseInnerVoice() {
        return mAMapNavi.getIsUseInnerVoice();
    }

    /*获取路段概览*/
    public List<AMapNaviGuide> getNaviGuideList() {
        return mAMapNavi.getNaviGuideList();
    }

    /*获取当前规划的路线方案 获取当前计算出的路线，步行和驾车共用这一个对象。*/
    public AMapNaviPath getNaviPath() {
        return mAMapNavi.getNaviPath();
    }

    /*获取计算的多条路径*/
    public HashMap<Integer, AMapNaviPath> getNaviPaths() {
        return mAMapNavi.getNaviPaths();
    }

    /*获取导航配置类*/
    public NaviSetting getNaviSetting() {
        return mAMapNavi.getNaviSetting();
    }

    /*获取导航位置变化驱动类型*/
    public int getNaviType() {
        return mAMapNavi.getNaviType();
    }

    /*获取当前导航路线的路况信息。*/
    public List<AMapTrafficStatus> getTrafficStatuses(int startPos, int distance) {
        return mAMapNavi.getTrafficStatuses(startPos, distance);
    }

    /*获取GPS是否准备就绪*/
    public boolean isGpsReady() {
        return mAMapNavi.isGpsReady();
    }

    /*暂停导航，仅支持模拟导航*/
    public void pauseNavi() {
        mAMapNavi.pauseNavi();
    }

    /*继续导航，仅支持模拟导航*/
    public void resumeNavi() {
        mAMapNavi.resumeNavi();
    }

    /*触发一次导航播报提示【驾车有效】*/
    public boolean readNaviInfo() {
        return mAMapNavi.readNaviInfo();
    }

    /*导航过程中重新规划路线（起点为当前位置，途经点、终点位置不变）【驾车有效】*/
    public boolean reCalculateRoute(int strategy) {
        return mAMapNavi.reCalculateRoute(strategy);
    }

    /*选择路线ID*/
    public boolean selectRouteId(int id) {
        return mAMapNavi.selectRouteId(id);
    }

    /*设置建立连接超时时间，单位毫秒级，最低3000，默认10000*/
    public void setConnectionTimeout(int connectionTimeOut) {
        mAMapNavi.setConnectionTimeout(connectionTimeOut);
    }

    /*设置模拟导航的速度*/
    public void setEmulatorNaviSpeed(int speed) {
        mAMapNavi.setEmulatorNaviSpeed(speed);
    }

    /*设置车辆信息(包括车型，车牌，车高，车重等)，路径规划时会躲避车辆限行区域和路线。*/
    public void setCarInfo(AMapCarInfo carInfo) {
        mAMapNavi.setCarInfo(carInfo);
    }

    /**
     * 此方法用于设置外部GPS数据,并使用外部GPS数据进行导航
     * 使用此方法前需要先调用AMapNavi.setIsUseExtraGPSData(boolean)将开关打开.
     */
    public void setExtraGPSData(int type, Location location) {
        mAMapNavi.setExtraGPSData(type, location);
    }

    /*设置是否使用外部GPS数据.*/
    public void setIsUseExtraGPSData(boolean isUseExtraData) {
        mAMapNavi.setIsUseExtraGPSData(isUseExtraData);
    }

    /*设置服务器返回超时时间，单位毫秒级，最低3000，默认10000.*/
    public void setSoTimeout(int soTimeOut) {
        mAMapNavi.setSoTimeout(soTimeOut);
    }

    /*设置在线语音播报质量*/
    public void setSoundQuality(SoundQuality rateInhz) {
        mAMapNavi.setSoundQuality(rateInhz);
    }

    /**
     * 设置使用内部语音播报, 默认为false, 为true时，用户设置
     * AMapNaviListener.onGetNavigationText(int, java.lang.String) 方法将不再回调
     */
    public void setUseInnerVoice(boolean isUseInnerVoice) {
        mAMapNavi.setUseInnerVoice(isUseInnerVoice);
    }

    /*设置使用内部语音播报*/
    public void setUseInnerVoice(boolean isUseInnerVoice, boolean isCallBackText) {
        mAMapNavi.setUseInnerVoice(isUseInnerVoice, isCallBackText);
    }

    /**
     * 设置在巡航模式（无路线规划）的状态下，智能播报的类型
     * 用户一旦设置，在巡航模式（无路线规划）的状态下，会获得以下回调:
     * AMapNaviListener.OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[])
     * 可以用于获得道路设施（包括电子眼，转弯提示等） AMapNaviListener.updateAimlessModeCongestionInfo(AimLessModeCongestionInfo)
     * 可以用于获得周边道路拥堵信息 AMapNaviListener.updateAimlessModeStatistics(AimLessModeStat)
     * 可以用于巡航模式的统计信息，巡航开启时间，巡航移动距离
     * AMapNaviListener.onGetNavigationText(int, String) 可以用户获得语音播报
     */
    public void startAimlessMode(int aimlessMode) {
        mAMapNavi.startAimlessMode(aimlessMode);
    }

    /*启动GPS定位*/
    public void startGPS() {
        mAMapNavi.startGPS();
    }

    /*启动GPS定位, 带距离和时间参数*/
    public void startGPS(long time, int dis) {
        mAMapNavi.startGPS(time, dis);
    }

    //开始导航
    public void startNavi(int naviType) {
        mAMapNavi.startNavi(naviType);
    }

    /*停止巡航模式*/
    public void stopAimlessMode() {
        mAMapNavi.stopAimlessMode();
    }

    /*停止GPS定位*/
    public void stopGPS() {
        mAMapNavi.stopGPS();
    }

    /*停止导航，包含实时导航和模拟导航*/
    public void stopNavi() {
        mAMapNavi.stopNavi();
    }

    /*进行算路策略转换，将传入的特定规则转换成PathPlanningStrategy的枚举值。*/
    public int strategyConvert(boolean congestion, boolean avoidSpeed, boolean cost, boolean hightSpeed, boolean multipleRoute) {
        return mAMapNavi.strategyConvert(congestion, avoidSpeed, cost, hightSpeed, multipleRoute);
    }

    /**
     * 切换平行路【驾车有效】
     * 此函数只有在GPS导航开始后才能使用，用来将路径的起点切换到当前导航路径平行的其它路径上。
     */
    public void switchParallelRoad() {
        mAMapNavi.switchParallelRoad();
    }

    public void addAMapNaviListener(AMapNaviListener aMapNaviListener) {
        mAMapNavi.addAMapNaviListener(aMapNaviListener);
    }

    public void addSimpleNavigationListener(SimpleNavigationListener onNavigationListener) {
        mAMapNavi.addAMapNaviListener(onNavigationListener);
    }

    public void removeAMapNaviListener(AMapNaviListener aMapNaviListener) {
        mAMapNavi.removeAMapNaviListener(aMapNaviListener);
    }

    public static class OverlayOptionsBuilder {
        private Bitmap smoothTraffic = null;
        private Bitmap unknownTraffic = null;
        private Bitmap slowTraffic = null;
        private Bitmap jamTraffic = null;
        private Bitmap veryJamTraffic = null;
        private Bitmap arrowOnTrafficRoute = null;
        private Bitmap normalRoute = null;
        private Bitmap passRoute = null;
        private float mLineWidth;
        private int arrowColor = Color.parseColor("#4DF6CC");
        private boolean isShowCameOnRoute = true;

        public OverlayOptionsBuilder setSmoothTraffic(Bitmap smoothTraffic) {
            this.smoothTraffic = smoothTraffic;
            return this;
        }

        public OverlayOptionsBuilder setUnknownTraffic(Bitmap unknownTraffic) {
            this.unknownTraffic = unknownTraffic;
            return this;
        }

        public OverlayOptionsBuilder setSlowTraffic(Bitmap slowTraffic) {
            this.slowTraffic = slowTraffic;
            return this;
        }

        public OverlayOptionsBuilder setJamTraffic(Bitmap jamTraffic) {
            this.jamTraffic = jamTraffic;
            return this;
        }

        public OverlayOptionsBuilder setVeryJamTraffic(Bitmap veryJamTraffic) {
            this.veryJamTraffic = veryJamTraffic;
            return this;
        }

        public OverlayOptionsBuilder setArrowOnTrafficRoute(Bitmap arrowOnTrafficRoute) {
            this.arrowOnTrafficRoute = arrowOnTrafficRoute;
            return this;
        }

        public OverlayOptionsBuilder setNormalRoute(Bitmap normalRoute) {
            this.normalRoute = normalRoute;
            return this;
        }

        public OverlayOptionsBuilder setPassRoute(Bitmap passRoute) {
            this.passRoute = passRoute;
            return this;
        }

        public OverlayOptionsBuilder setLineWidth(float mLineWidth) {
            this.mLineWidth = mLineWidth;
            return this;
        }

        public OverlayOptionsBuilder setOnRouteCameShow(boolean isShowCameOnRoute) {
            this.isShowCameOnRoute = isShowCameOnRoute;
            return this;
        }

        RouteOverlayOptions create() {
            RouteOverlayOptions options = new RouteOverlayOptions();
            options.setLineWidth(mLineWidth);
            options.setNormalRoute(normalRoute);
            options.setArrowOnTrafficRoute(arrowOnTrafficRoute);
            options.setUnknownTraffic(unknownTraffic);
            options.setVeryJamTraffic(veryJamTraffic);
            options.setSlowTraffic(slowTraffic);
            options.setSmoothTraffic(smoothTraffic);
            options.setJamTraffic(jamTraffic);
            options.setPassRoute(passRoute);
            options.setArrowColor(arrowColor);
            options.setOnRouteCameShow(isShowCameOnRoute);
            return options;
        }
    }

    public RouteOverlayOptions createRouteOverlayOptions(OverlayOptionsBuilder builder) {
        return builder.create();
    }

    public static class RouteOverLayBuilder {

    }

    /*
     * 释放导航对象资源 退出时调用此接口释放导航资源，在调用此接口后不能再调用AMapNavi类里的其它接口。
     */
    public void destroy() {
        mAMapNavi.destroy();
    }

}
