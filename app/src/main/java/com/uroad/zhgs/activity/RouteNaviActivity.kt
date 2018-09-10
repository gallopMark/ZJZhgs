package com.uroad.zhgs.activity

import android.os.Bundle
import com.amap.api.navi.AMapNavi
import com.amap.api.navi.AMapNaviListener
import com.amap.api.navi.AMapNaviViewListener
import com.amap.api.navi.enums.NaviType
import com.amap.api.navi.model.*
import com.autonavi.tbt.TrafficFacilityInfo
import com.uroad.zhgs.common.BaseActivity
import kotlinx.android.synthetic.main.activity_route_navi.*
import com.amap.api.navi.model.NaviLatLng
import com.uroad.zhgs.R


/**
 *Created by MFB on 2018/8/30.
 */
class RouteNaviActivity : BaseActivity(), AMapNaviListener, AMapNaviViewListener {
    override fun onNaviTurnClick() {
    }

    override fun onScanViewButtonClick() {
    }

    override fun onLockMap(p0: Boolean) {
    }

    override fun onMapTypeChanged(p0: Int) {
    }

    //导航取消
    override fun onNaviCancel() {
        finish()
    }

    override fun onNaviViewLoaded() {
    }

    override fun onNaviBackClick(): Boolean = false

    override fun onNaviMapMode(p0: Int) {
    }

    override fun onNextRoadClick() {
    }

    override fun onNaviViewShowMode(p0: Int) {
    }

    override fun onNaviSetting() {
    }

    //算路终点坐标
    private var mEndLatlng = NaviLatLng(22.652, 113.966)
    //算路起点坐标
    private var mStartLatlng = NaviLatLng(22.540332, 113.939961)
    //存储算路起点的列表
    private val sList = ArrayList<NaviLatLng>()
    //存储算路终点的列表
    private val eList = ArrayList<NaviLatLng>()
    private var selectRouteId: Int? = null
    private var strategy: Int = 0

    private lateinit var mAMapNavi: AMapNavi
    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_route_navi)
        naviView.onCreate(savedInstanceState)
        naviView.setAMapNaviViewListener(this)
        intent.extras?.let {
            mStartLatlng = it.getParcelable("start")
            mEndLatlng = it.getParcelable("end")
            strategy = it.getInt("strategy")
            selectRouteId = it.getInt("selectRouteId")
        }
        mAMapNavi = AMapNavi.getInstance(applicationContext)
        mAMapNavi.addAMapNaviListener(this)
        mAMapNavi.setUseInnerVoice(true)
        mAMapNavi.setEmulatorNaviSpeed(60)
        selectRouteId?.let { mAMapNavi.selectRouteId(it) }
        sList.add(mStartLatlng)
        eList.add(mEndLatlng)
    }

    override fun onNaviInfoUpdate(p0: NaviInfo?) {
    }

    override fun onCalculateRouteSuccess(p0: IntArray?) {
    }

    override fun onCalculateRouteSuccess(p0: AMapCalcRouteResult?) {
        mAMapNavi.startNavi(NaviType.GPS)
    }

    override fun onCalculateRouteFailure(p0: Int) {
    }

    override fun onCalculateRouteFailure(p0: AMapCalcRouteResult?) {
    }

    override fun onServiceAreaUpdate(p0: Array<out AMapServiceAreaInfo>?) {
    }

    override fun onEndEmulatorNavi() {
    }

    override fun onArrivedWayPoint(p0: Int) {
    }

    override fun onArriveDestination() {
    }

    override fun onPlayRing(p0: Int) {
    }

    override fun onTrafficStatusUpdate() {
    }

    override fun onGpsOpenStatus(p0: Boolean) {
    }

    override fun updateAimlessModeCongestionInfo(p0: AimLessModeCongestionInfo?) {
    }

    override fun showCross(p0: AMapNaviCross?) {
    }

    override fun onGetNavigationText(p0: Int, p1: String?) {
    }

    override fun onGetNavigationText(p0: String?) {
    }

    override fun updateAimlessModeStatistics(p0: AimLessModeStat?) {
    }

    override fun hideCross() {
    }

    override fun onInitNaviFailure() {
    }

    override fun onInitNaviSuccess() {
        /**
         * 方法: int strategy=mAMapNavi.strategyConvert(congestion, avoidhightspeed, cost, hightspeed, multipleroute); 参数:
         *
         * @congestion 躲避拥堵
         * @avoidhightspeed 不走高速
         * @cost 避免收费
         * @hightspeed 高速优先
         * @multipleroute 多路径
         *
         *  说明: 以上参数都是boolean类型，其中multipleroute参数表示是否多条路线，
         *  如果为true则此策略会算出多条路线。
         *  注意: 不走高速与高速优先不能同时为true 高速优先与避免收费不能同时为true
         */
        var strategy = 0
        try {
            //再次强调，最后一个参数为true时代表多路径，否则代表单路径
            strategy = mAMapNavi.strategyConvert(true, false, false, false, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // 驾车算路
        mAMapNavi.calculateDriveRoute(sList, eList, null, strategy)
    }

    override fun onReCalculateRouteForTrafficJam() {
    }

    override fun updateIntervalCameraInfo(p0: AMapNaviCameraInfo?, p1: AMapNaviCameraInfo?, p2: Int) {
    }

    override fun hideLaneInfo() {
    }

    override fun onNaviInfoUpdated(p0: AMapNaviInfo?) {
    }

    override fun showModeCross(p0: AMapModelCross?) {
    }

    override fun updateCameraInfo(p0: Array<out AMapNaviCameraInfo>?) {
    }

    override fun hideModeCross() {
    }

    override fun onLocationChange(p0: AMapNaviLocation?) {
    }

    override fun onReCalculateRouteForYaw() {
    }

    override fun onStartNavi(p0: Int) {
    }

    override fun notifyParallelRoad(p0: Int) {
    }

    override fun OnUpdateTrafficFacility(p0: AMapNaviTrafficFacilityInfo?) {
    }

    override fun OnUpdateTrafficFacility(p0: Array<out AMapNaviTrafficFacilityInfo>?) {
    }

    override fun OnUpdateTrafficFacility(p0: TrafficFacilityInfo?) {
    }

    override fun onNaviRouteNotify(p0: AMapNaviRouteNotifyData?) {
    }

    override fun showLaneInfo(p0: Array<out AMapLaneInfo>?, p1: ByteArray?, p2: ByteArray?) {
    }

    override fun showLaneInfo(p0: AMapLaneInfo?) {
    }

    override fun onResume() {
        super.onResume()
        naviView.onResume()
        mAMapNavi.resumeNavi()
    }

    override fun onPause() {
        super.onPause()
        naviView.onPause()
        mAMapNavi.pauseNavi()
        //
        //        停止导航之后，会触及底层stop，然后就不会再有回调了，但是讯飞当前还是没有说完的半句话还是会说完
        //        mAMapNavi.stopNavi();
    }

    override fun onDestroy() {
        naviView.onDestroy()
        mAMapNavi.stopNavi()
        mAMapNavi.destroy()
        super.onDestroy()
    }
}