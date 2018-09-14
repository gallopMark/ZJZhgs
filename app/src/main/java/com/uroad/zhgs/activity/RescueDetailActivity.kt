package com.uroad.zhgs.activity

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.navi.AMapNavi
import com.amap.api.navi.enums.PathPlanningStrategy
import com.amap.api.navi.model.AMapCalcRouteResult
import com.amap.api.navi.model.AMapNaviPath
import com.amap.api.navi.model.NaviLatLng
import com.amap.api.navi.model.RouteOverlayOptions
import com.amap.api.navi.view.RouteOverLay
import com.uroad.amaplib.driveroute.util.AMapUtil
import com.uroad.amaplib.navi.simple.SimpleNavigationListener
import com.uroad.library.utils.DeviceUtils
import com.uroad.library.utils.DisplayUtils
import com.uroad.library.utils.PhoneUtils
import com.uroad.mqtt.IMqttCallBack
import com.uroad.mqtt.MqttService
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.dialog.MaterialDialog
import com.uroad.zhgs.model.MQTTMsgMDL
import com.uroad.zhgs.model.RescueDetailMDL
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.ApiService
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_rescue_detail.*
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import java.lang.ref.WeakReference

/**
 *Created by MFB on 2018/7/28.
 * 救援详情
 */
class RescueDetailActivity : BaseActivity() {
    private lateinit var aMap: AMap
    private lateinit var mAMapNavi: AMapNavi
    private var routeOverLay: RouteOverLay? = null
    private var rescueid: String = ""
    private var detail: RescueDetailMDL.Detail? = null
    private lateinit var handler: MyHandler

    companion object {
        private const val CODE_GET = 0x0000
        private const val CODE_MQTT = 0x0001
        private const val DELAY_MILLIS = 30 * 1000L
    }

    private lateinit var mqttService: MqttService

    private class MyHandler(activity: RescueDetailActivity) : Handler() {
        private val weakReference: WeakReference<RescueDetailActivity> = WeakReference(activity)
        override fun handleMessage(msg: Message) {
            val activity = weakReference.get() ?: return
            when (msg.what) {
                CODE_GET -> {
                    activity.loadDetail()
                    sendEmptyMessageDelayed(CODE_GET, DELAY_MILLIS)
                }
                CODE_MQTT -> {
                    val json = msg.obj as String
                    val mdl = GsonUtils.fromDataBean(json, MQTTMsgMDL::class.java)
                    mdl?.let {
                        activity.openActivity(RescuePayActivity::class.java, Bundle().apply { putString("rescueid", it.rescueid) })
                        activity.finish()
                    }
                }
            }
        }
    }

    override fun setUp(savedInstanceState: Bundle?) {
        withTitle(resources.getString(R.string.rescue_detail_title))
        setBaseContentLayout(R.layout.activity_rescue_detail)
        intent.extras?.getString("rescueid")?.let { rescueid = it }
        mapView.onCreate(savedInstanceState)
        initMapView()
        initNavi()
        initMQTT()
        handler = MyHandler(this)
    }

    /*初始化mapview*/
    private fun initMapView() {
        aMap = mapView.map
        aMap.apply {
            //移动到浙江省 120.153576,30.287459
            animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(CurrApplication.APP_LATLNG, this.cameraPosition.zoom, 0f, 0f)))
        }
    }

    /*初始化导航*/
    private fun initNavi() {
        mAMapNavi = AMapNavi.getInstance(applicationContext)
        mAMapNavi.addAMapNaviListener(object : SimpleNavigationListener() {
            override fun onCalculateRouteSuccess(result: AMapCalcRouteResult?) {
                mAMapNavi.naviPath?.let { addRouteOverLay(it) }
            }
        })
    }

    /*绘制路线*/
    private fun addRouteOverLay(path: AMapNaviPath) {
        routeOverLay?.let {
            it.removeFromMap()
            it.destroy()
        }
        routeOverLay = RouteOverLay(aMap, path, this).apply {
            routeOverlayOptions = RouteOverlayOptions().apply {
                normalRoute = BitmapFactory.decodeResource(resources, R.mipmap.custtexture_green)
                jamTraffic = BitmapFactory.decodeResource(resources, R.mipmap.custtexture_bad)
                veryJamTraffic = BitmapFactory.decodeResource(resources, R.mipmap.custtexture_bad)
                slowTraffic = BitmapFactory.decodeResource(resources, R.mipmap.custtexture_slow)
                smoothTraffic = BitmapFactory.decodeResource(resources, R.mipmap.custtexture_green)
                unknownTraffic = BitmapFactory.decodeResource(resources, R.mipmap.custtexture_green)
            }
            setStartPointBitmap(BitmapFactory.decodeResource(resources, R.mipmap.ic_route_start))
            setEndPointBitmap(getEndPointBitmap(path))
            val paddingTop = DisplayUtils.dip2px(this@RescueDetailActivity, 80f)
            val paddingBottom = DisplayUtils.dip2px(this@RescueDetailActivity, 200f)
            zoomToSpan(0, 0, paddingTop, paddingBottom, path)
            setLightsVisible(false)  //不显示红绿灯
            setTrafficLightsVisible(false)
            addToMap()
        }
    }

    private fun getEndPointBitmap(path: AMapNaviPath): Bitmap {
        val view = layoutInflater.inflate(R.layout.mapview_rescuecar, LinearLayout(this), false)
        val tvDistance = view.findViewById<TextView>(R.id.tvDistance)
        val tvTime = view.findViewById<TextView>(R.id.tvTime)
        val distance = "距您${AMapUtil.getFriendlyLength(path.allLength)}"
        tvDistance.text = distance
        val time = "约${AMapUtil.getFriendlyTime(path.allTime)}"
        tvTime.text = time
        return BitmapDescriptorFactory.fromView(view).bitmap
    }

    private fun initMQTT() {
        mqttService = MqttService.Builder(this)
                .autoReconnect(true)
                .clientId(DeviceUtils.getUniqueId(this))
                .serverUrl(ApiService.MQTT_SERVICEURL)
                .userName(ApiService.MQTT_USER)
                .passWord(ApiService.MQTT_PASSWORD)
                .timeOut(30)
                .keepAliveInterval(10)
                .create()
    }

    override fun initData() {
        loadDetail()
        /*间隔30秒刷新一次*/
        handler.sendEmptyMessageDelayed(CODE_GET, DELAY_MILLIS)
        mqttService.connect(object : IMqttCallBack {
            override fun messageArrived(topic: String?, message: String?, qos: Int) {
                val msg = Message().apply {
                    what = CODE_MQTT
                    obj = message
                }
                handler.sendMessage(msg)
            }

            override fun connectionLost(throwable: Throwable?) {
            }

            override fun deliveryComplete(deliveryToken: IMqttDeliveryToken?) {
            }

            override fun connectSuccess(token: IMqttToken?) {
                mqttService.subscribe("${ApiService.MQTT_TOPIC}$rescueid", 1)
            }

            override fun connectFailed(token: IMqttToken?, throwable: Throwable?) {
                throwable?.printStackTrace()
            }
        })
    }

    private fun loadDetail() {
        doRequest(WebApiService.RESCUE_DETAIL, WebApiService.rescueDetailParams(rescueid), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, RescueDetailMDL::class.java)
                    if (mdl == null) showShortToast("数据解析异常")
                    else updateData(mdl)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                endLoading()
                onHttpError(e)
            }
        })
    }

    private fun updateData(mdl: RescueDetailMDL) {
        mdl.detail?.let { detail ->
            llBottom.visibility = View.VISIBLE
            this.detail = detail
            var textRescueNum = resources.getString(R.string.rescue_detail_request_num)
            detail.rescueno?.let { textRescueNum += it }
            tvRescueNo.text = textRescueNum
            var textRescueAddress = resources.getString(R.string.rescue_detail_rescue_address)
            detail.rescue_address?.let { textRescueAddress += it }
            tvAddress.text = textRescueAddress
            tvStatusName.text = detail.rescuestatusname
            detail.phonenum?.let {
                ivCall.visibility = View.VISIBLE
                ivCall.setOnClickListener { _ -> PhoneUtils.call(this@RescueDetailActivity, it) }
            }
            if (detail.status == RescueDetailMDL.Detail.Status.FINISHED.code ||
                    detail.status == RescueDetailMDL.Detail.Status.CANCELED.code) {
                withOption("")
            } else {
                withOption(resources.getString(R.string.cancel))
                getOptionView().setOnClickListener {
                    if (TextUtils.equals(detail.status, RescueDetailMDL.Detail.Status.DOING.code))
                        onCancel(1)
                    else {
                        onCancel(2)
                    }
                }
            }
            calculateDriveRoute(NaviLatLng(detail.getLatitude(), detail.getLongitude()), NaviLatLng(detail.getUserLatitude(), detail.getUserLongitude()))
        }
    }

    //路径搜索
    private fun calculateDriveRoute(startPoint: NaviLatLng, endPoint: NaviLatLng) {
        val start = ArrayList<NaviLatLng>().apply { add(startPoint) }
        val end = ArrayList<NaviLatLng>().apply { add(endPoint) }
        mAMapNavi.calculateDriveRoute(start, end, null, PathPlanningStrategy.DRIVING_DEFAULT)
//        val routeSearch = RouteSearch(this)
//        val fromAndTo = RouteSearch.FromAndTo(startPoint, endPoint)
//        val query = RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.DRIVING_SINGLE_DEFAULT, null, null, "")
//        addDisposable(Flowable.fromCallable { routeSearch.calculateDriveRoute(query) }
//                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ result ->
//                    result?.paths?.let {
//                        if (it.size > 0) {
//                            updatePaths(result, it[0])
//                        }
//                    }
//                }, { }))
    }

//    private fun updatePaths(result: DriveRouteResult, drivePath: DrivePath) {
//        aMap.clear()
//        val myLocationView = layoutInflater.inflate(R.layout.mapview_rescuecar, LinearLayout(this), false)
//        val tvDistance = myLocationView.findViewById<TextView>(R.id.tvDistance)
//        val tvTime = myLocationView.findViewById<TextView>(R.id.tvTime)
//        val distance = "距您${AMapUtil.getFriendlyLength(drivePath.distance.toInt())}"
//        tvDistance.text = distance
//        val time = "约${AMapUtil.getFriendlyTime(drivePath.duration.toInt())}"
//        tvTime.text = time
//        val routeWidth = DisplayUtils.dip2px(this, 15f).toFloat()
//        val overlay = DrivingRouteOverlay(aMap, drivePath, result.startPos, result.targetPos, null)
//        overlay.driveColor = ContextCompat.getColor(this, R.color.route_color_selected)
//        overlay.setStartBitmapDescriptor(R.mipmap.ic_route_start)
//        overlay.setEndDescriptor(myLocationView)
//        overlay.routeWidth = routeWidth
//        overlay.setNodeIconVisibility(true)//设置节点marker是否显示
//        overlay.setTrafficRes(R.mipmap.custtexture_green, R.mipmap.custtexture_slow, R.mipmap.custtexture_slow,
//                R.mipmap.custtexture_bad, R.mipmap.custtexture_bad, R.mipmap.custtexture_green)
//        overlay.setIsColorfulline(true)//是否用颜色展示交通拥堵情况，默认true
//        overlay.removeFromMap()
//        overlay.addToMap()
//        overlay.zoomToSpan()
//    }

    private fun onCancel(type: Int) {
        val title = resources.getString(R.string.dialog_default_title)
        val message = if (type == 1) getString(R.string.rescue_detail_dialog_msg)
        else getString(R.string.rescue_detail_cancel_tips)
        showDialog(title, message, object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                dialog.dismiss()
            }

        }, object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                dialog.dismiss()
                cancel()
            }
        })
    }

    private fun cancel() {
        doRequest(WebApiService.CANCEL_RESCUE, WebApiService.cancelRescueParams(rescueid), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    showShortToast(GsonUtils.getDataAsString(data))
                    Handler().postDelayed({ if (!isFinishing) finish() }, 1500)
                    //  openActivity(RescuePayActivity::class.java)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                onHttpError(e)
            }
        })
    }

    override fun onResume() {
        mapView.onResume()
        mAMapNavi.resumeNavi()
        super.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        mAMapNavi.pauseNavi()
        super.onPause()
    }

    override fun onDestroy() {
        mqttService.disconnect()
        handler.removeCallbacksAndMessages(null)
        mAMapNavi.destroy()
        mapView.onDestroy()
        super.onDestroy()
    }
}