package com.uroad.zhgs.activity

import android.os.Bundle
import com.amap.api.navi.AMapNavi
import com.amap.api.navi.AMapNaviViewListener
import com.amap.api.navi.enums.NaviType
import com.uroad.zhgs.common.BaseActivity
import kotlinx.android.synthetic.main.activity_route_navi.*
import com.uroad.zhgs.R


/**
 *Created by MFB on 2018/8/30.
 */
class RouteNaviActivity : BaseActivity(), AMapNaviViewListener {
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

    //    //算路终点坐标
//    private var mEndLatlng: NaviLatLng? = NaviLatLng(22.652, 113.966)
//    //算路起点坐标
//    private var mStartLatlng: NaviLatLng? = NaviLatLng(22.540332, 113.939961)
//    //存储算路起点的列表
//    private val sList = ArrayList<NaviLatLng>()
//    //存储算路终点的列表
//    private val eList = ArrayList<NaviLatLng>()
    private var selectRouteId: Int? = null
    //    private var strategy: Int = 0

    private lateinit var mAMapNavi: AMapNavi
    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_route_navi)
        naviView.onCreate(savedInstanceState)
        naviView.setAMapNaviViewListener(this)
        intent.extras?.let {
            //            mStartLatlng = it.getParcelable("start")
//            mEndLatlng = it.getParcelable("end")
//            strategy = it.getInt("strategy")
            selectRouteId = it.getInt("selectRouteId")
        }
        mAMapNavi = AMapNavi.getInstance(this)
        mAMapNavi.setUseInnerVoice(true, true)
        mAMapNavi.setEmulatorNaviSpeed(60)
        selectRouteId?.let {
            mAMapNavi.selectRouteId(it)
            mAMapNavi.startNavi(NaviType.GPS)
        }
//        mStartLatlng?.let {  sList.add(it) }
//        mEndLatlng?.let { eList.add(it) }
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
//        mAMapNavi.destroy()
        super.onDestroy()
    }
}