package com.uroad.zhgs.fragment

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.support.v4.util.ArrayMap
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.amap.api.location.AMapLocation
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.uroad.zhgs.R
import com.uroad.zhgs.activity.LoginActivity
import com.uroad.zhgs.activity.AMapNaviSearchActivity
import com.uroad.zhgs.activity.VideoPlayerActivity
import com.uroad.zhgs.common.BaseFragment
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.dialog.*
import com.uroad.zhgs.enumeration.MapDataType
import com.uroad.zhgs.model.*
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.fragment_nav_standard.*

/**
 *Created by MFB on 2018/8/10.
 * 路况导航 地图模式
 */
class NavStandardFragment : BaseFragment() {

    private var fromHome: Boolean = false
    private var isOpenLocation = false
    private var location: AMapLocation? = null
    private lateinit var aMap: AMap
    private lateinit var myLocationView: View
    private lateinit var animationDrawable: AnimationDrawable
    private var targetLatLng: LatLng? = null
    private var oldMarker: Marker? = null
    private val markerMap = ArrayMap<String, ArrayList<Marker>>()
    private val map = ArrayMap<Int, Boolean>()

    override fun setBaseLayoutResID(): Int {
        return R.layout.fragment_nav_standard
    }

    override fun setUp(view: View, savedInstanceState: Bundle?) {
        ivBack.setOnClickListener { context.onBackPressed() }
        myLocationView = layoutInflater.inflate(R.layout.mapview_mylocation2, FrameLayout(context), false)
        val ivDiffuse = myLocationView.findViewById<ImageView>(R.id.ivDiffuse)
        animationDrawable = ivDiffuse.drawable as AnimationDrawable
        arguments?.let { fromHome = it.getBoolean("fromHome", false) }
        mapView.onCreate(savedInstanceState)
        initMapView()
        llSearch.setOnClickListener { openActivity(AMapNaviSearchActivity::class.java) }
        requestLocationPermissions(object : RequestLocationPermissionCallback {
            override fun doAfterGrand() {
                openLocation()
            }

            override fun doAfterDenied() {
                showDismissLocationDialog()
            }
        })
    }

    private fun initMapView() {
        aMap = mapView.map.apply { moveCamera(CameraUpdateFactory.newLatLngZoom(CurrApplication.APP_LATLNG, this.cameraPosition.zoom)) }
        aMap.setOnMarkerClickListener { marker ->
            if (marker.`object` is WeatherMDL) return@setOnMarkerClickListener true
            oldMarker?.let { restoreMarker(it) }
            enlargeMarkerIcon(marker)
            return@setOnMarkerClickListener true
        }
        dealWithFromHome()
    }

    //如果用户从首页我的订阅点击进来 则显示该事件详情
    private fun dealWithFromHome() {
        val mdl = arguments?.getSerializable("mdl")
        mdl?.let { mdL ->
            if (mdL is TrafficJamMDL) {
                val markers = ArrayList<Marker>()
                val options = createOptions(LatLng(mdL.latitude(), mdL.longitude())
                        , MapDataType.TRAFFIC_JAM.name, MapDataType.TRAFFIC_JAM.name,
                        BitmapDescriptorFactory.fromResource(mdL.markerIcon))
                val marker = aMap.addMarker(options)
                markers.add(marker.apply { `object` = mdL })
                markerMap[MapDataType.TRAFFIC_JAM.name] = markers
                aMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(mdL.latitude(), mdL.longitude())))
                enlargeMarkerIcon(marker)
            } else if (mdL is EventMDL) {
                val title: String = when (mdL.getSubType()) {
                    SubscribeMDL.SubType.Control.code -> MapDataType.CONTROL.name
                    SubscribeMDL.SubType.Emergencies.code -> MapDataType.CONSTRUCTION.name
                    else -> MapDataType.CONSTRUCTION.name
                }
                val markers = ArrayList<Marker>()
                val options = createOptions(LatLng(mdL.latitude(), mdL.longitude()), title, title,
                        BitmapDescriptorFactory.fromResource(mdL.markerIcon))
                val marker = aMap.addMarker(options)
                markers.add(marker.apply { `object` = mdL })
                markerMap[title] = markers
                aMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(mdL.latitude(), mdL.longitude())))
                enlargeMarkerIcon(marker)
            }
        }
    }

    fun onLocation() {
        location?.let { aMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(LatLng(it.latitude, it.longitude), aMap.cameraPosition.zoom, 0f, 0f))) }
    }

    override fun afterLocation(location: AMapLocation) {
        isOpenLocation = true
        this.location = location
        this.targetLatLng = LatLng(location.latitude, location.longitude)
        aMap.addMarker(createOptions(LatLng(location.latitude, location.longitude), location.city, location.address, BitmapDescriptorFactory.fromView(myLocationView)))
        if (!fromHome) {
            aMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(LatLng(location.latitude, location.longitude), aMap.cameraPosition.zoom, 0f, 0f)))
            //路况导航-地图模式默认开启图层：事故、管制、施工、拥堵
//            aMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(CurrApplication.APP_LATLNG, aMap.cameraPosition.zoom, 0f, 0f)))
            getMapDataByType(MapDataType.ACCIDENT.code)
            getMapDataByType(MapDataType.CONTROL.code)
//            getMapDataByType(MapDataType.CONSTRUCTION.code)  //默认关闭施工
            getMapDataByType(MapDataType.TRAFFIC_JAM.code)
            getMapDataByType(MapDataType.SNAPSHOT.code)
        }
        closeLocation()
    }

    //启动帧动画
    private fun startAnim() {
        animationDrawable.start()
    }

    //选择当前动画的第一帧，然后停止
    private fun stopAnim() {
        animationDrawable.selectDrawable(0) //选择当前动画的第一帧，然后停止
        animationDrawable.stop()
    }

    //放大地图
    fun enlargeMap() {
        aMap.cameraPosition.apply {
            var mapZoom = zoom
            val mapTarget = target
            scaleMap(mapTarget, ++mapZoom)
        }
    }

    fun narrowMap() {
        aMap.cameraPosition.apply {
            var mapZoom = zoom
            val mapTarget = target
            scaleMap(mapTarget, --mapZoom)
        }
    }

    //地图放大缩小
    private fun scaleMap(nowLocation: LatLng, scaleValue: Float) {
        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(nowLocation, scaleValue))
    }

    fun onEvent(type: Int, isChecked: Boolean) {
        map[type] = isChecked
        when (type) {
            1 -> {   //点击事故菜单
                if (isChecked) {
                    getMapDataByType(MapDataType.ACCIDENT.code)
                } else {
                    clearMarkers(MapDataType.ACCIDENT.name, markerMap[MapDataType.ACCIDENT.name])
                }
            }
            2 -> {  //点击管制菜单
                if (isChecked) {
                    getMapDataByType(MapDataType.CONTROL.code)
                } else {
                    clearMarkers(MapDataType.CONTROL.name, markerMap[MapDataType.CONTROL.name])
                }
            }
            3 -> {  //点击施工菜单
                if (isChecked) {
                    getMapDataByType(MapDataType.CONSTRUCTION.code)
                } else {
                    clearMarkers(MapDataType.CONSTRUCTION.name, markerMap[MapDataType.CONSTRUCTION.name])
                }
            }
            4 -> {  //点击拥堵菜单
                if (isChecked) {
                    getMapDataByType(MapDataType.TRAFFIC_JAM.code)
                } else {
                    clearMarkers(MapDataType.TRAFFIC_JAM.name, markerMap[MapDataType.TRAFFIC_JAM.name])
                }
            }
            5 -> {    //点击监控菜单
                if (isChecked) {
                    getMapDataByType(MapDataType.SNAPSHOT.code)
                } else {
                    clearMarkers(MapDataType.SNAPSHOT.name, markerMap[MapDataType.SNAPSHOT.name])
                }
            }
            6 -> {      //点击天气菜单
                if (isChecked) {
                    getMapDataByType(MapDataType.WEATHER.code)
                } else {
                    clearMarkers(MapDataType.WEATHER.name, markerMap[MapDataType.WEATHER.name])
                }
            }
            7 -> {   //点击维修店
                if (isChecked) {
                    getMapDataByType(MapDataType.REPAIR_SHOP.code)
                } else {
                    clearMarkers(MapDataType.REPAIR_SHOP.name, markerMap[MapDataType.REPAIR_SHOP.name])
                }
            }
            8 -> {  //加油站
                if (isChecked) {
                    getMapDataByType(MapDataType.GAS_STATION.code)
                } else {
                    clearMarkers(MapDataType.GAS_STATION.name, markerMap[MapDataType.GAS_STATION.name])
                }
            }
            9 -> {  //景点
                if (isChecked) {
                    getMapDataByType(MapDataType.SCENIC.code)
                } else {
                    clearMarkers(MapDataType.SCENIC.name, markerMap[MapDataType.SCENIC.name])
                }
            }
            10 -> {  //服务区
                if (isChecked) {
                    getMapDataByType(MapDataType.SERVICE_AREA.code)
                } else {
                    clearMarkers(MapDataType.SERVICE_AREA.name, markerMap[MapDataType.SERVICE_AREA.name])
                }
            }
            11 -> { //收费站
                if (isChecked) {
                    getMapDataByType(MapDataType.TOLL_GATE.code)
                } else {
                    clearMarkers(MapDataType.TOLL_GATE.name, markerMap[MapDataType.TOLL_GATE.name])
                }
            }
        }
    }

    private fun getMapDataByType(type: String) {
        targetLatLng?.let {
            doRequest(WebApiService.MAP_DATA, WebApiService.mapDataByTypeParams(type,
                    it.longitude, it.latitude, "", ""),
                    object : HttpRequestCallback<String>() {
                        override fun onPreExecute() {
                            startAnim()
                        }

                        override fun onSuccess(data: String?) {
                            stopAnim()
                            if (GsonUtils.isResultOk(data)) {
                                updateData(type, data)
                            } else {
                                showShortToast(GsonUtils.getMsg(data))
                            }
                        }

                        override fun onFailure(e: Throwable, errorMsg: String?) {
                            stopAnim()
                            onHttpError(e)
                        }
                    })
        }
    }

    //根据不同类型进行解析数据
    private fun updateData(type: String, data: String?) {
        if (type == MapDataType.ACCIDENT.code || type == MapDataType.CONTROL.code
                || type == MapDataType.CONSTRUCTION.code) {   //事件类型
            val dataMDLs = GsonUtils.fromDataToList(data, EventMDL::class.java)
            insertPoint(type, ArrayList<MutilItem>().apply { addAll(dataMDLs) }, 1)
        } else if (type == MapDataType.TRAFFIC_JAM.code) {  //拥堵类型
            val dataMDLs = GsonUtils.fromDataToList(data, TrafficJamMDL::class.java)
            insertPoint(type, ArrayList<MutilItem>().apply { addAll(dataMDLs) }, 2)
        } else if (type == MapDataType.SNAPSHOT.code) { //快拍类型
            val dataMDLs = GsonUtils.fromDataToList(data, SnapShotMDL::class.java)
            insertPoint(type, ArrayList<MutilItem>().apply { addAll(dataMDLs) }, 3)
        } else if (type == MapDataType.WEATHER.code) {  //天气类型
            val dataMDLs = GsonUtils.fromDataToList(data, WeatherMDL::class.java)
            insertPoint(type, ArrayList<MutilItem>().apply { addAll(dataMDLs) }, 4)
        } else if (type == MapDataType.REPAIR_SHOP.code) {  //维修店类型
            val dataMDLs = GsonUtils.fromDataToList(data, RepairShopMDL::class.java)
            insertPoint(type, ArrayList<MutilItem>().apply { addAll(dataMDLs) }, 5)
        } else if (type == MapDataType.GAS_STATION.code) {  //加油站类型
            val dataMDLs = GsonUtils.fromDataToList(data, GasStationMDL::class.java)
            insertPoint(type, ArrayList<MutilItem>().apply { addAll(dataMDLs) }, 6)
        } else if (type == MapDataType.SCENIC.code) {  //景点类型
            val dataMDLs = GsonUtils.fromDataToList(data, ScenicMDL::class.java)
            insertPoint(type, ArrayList<MutilItem>().apply { addAll(dataMDLs) }, 7)
        } else if (type == MapDataType.SERVICE_AREA.code) {  //服务区类型
            val dataMDLs = GsonUtils.fromDataToList(data, ServiceMDL::class.java)
            insertPoint(type, ArrayList<MutilItem>().apply { addAll(dataMDLs) }, 8)
        } else if (type == MapDataType.TOLL_GATE.code) { //收费站类型
            val dataMDLs = GsonUtils.fromDataToList(data, TollGateMDL::class.java)
            insertPoint(type, ArrayList<MutilItem>().apply { addAll(dataMDLs) }, 9)
        }
    }

    private fun insertPoint(type: String, data: MutableList<MutilItem>, dataType: Int) {
        var markerIcon = 0
        var markerBigIco = 0
        val title: String
        when (type) {
            MapDataType.ACCIDENT.code -> {
                markerIcon = R.mipmap.ic_marker_sg_icon
                markerBigIco = R.mipmap.ic_marker_sg_big_icon
                title = MapDataType.ACCIDENT.name
            }
            MapDataType.CONTROL.code -> {
                markerIcon = R.mipmap.ic_marker_gz_icon
                markerBigIco = R.mipmap.ic_marker_gz_big_icon
                title = MapDataType.CONTROL.name
            }
            MapDataType.CONSTRUCTION.code -> {
                markerIcon = R.mipmap.ic_marker_shig_icon
                markerBigIco = R.mipmap.ic_marker_shig_big_icon
                title = MapDataType.CONSTRUCTION.name
            }
            MapDataType.TRAFFIC_JAM.code -> {
                markerIcon = R.mipmap.ic_marker_yd_icon
                markerBigIco = R.mipmap.ic_marker_yd_big_icon
                title = MapDataType.TRAFFIC_JAM.name
            }
            MapDataType.WEATHER.code -> {
                title = MapDataType.WEATHER.name
            }
            MapDataType.SNAPSHOT.code -> {
                title = MapDataType.SNAPSHOT.name
                markerIcon = R.mipmap.ic_marker_snap_icon
                markerBigIco = R.mipmap.ic_marker_snap_big_icon
            }
            MapDataType.REPAIR_SHOP.code -> {
                title = MapDataType.REPAIR_SHOP.name
                markerIcon = R.mipmap.ic_marker_repair_icon
                markerBigIco = R.mipmap.ic_marker_repair_big_icon
            }
            MapDataType.GAS_STATION.code -> {
                title = MapDataType.GAS_STATION.name
                markerIcon = R.mipmap.ic_marker_gas_icon
                markerBigIco = R.mipmap.ic_marker_gas_big_icon
            }
            MapDataType.SCENIC.code -> {
                title = MapDataType.SCENIC.name
                markerIcon = R.mipmap.ic_marker_secnic_icon
                markerBigIco = R.mipmap.ic_marker_secnic_big_icon
            }
            MapDataType.SERVICE_AREA.code -> {
                title = MapDataType.SERVICE_AREA.name
                markerIcon = R.mipmap.ic_marker_service_icon
                markerBigIco = R.mipmap.ic_marker_service_big_icon
            }
            else -> {
                title = MapDataType.TOLL_GATE.name
                markerIcon = R.mipmap.ic_marker_toll_icon
                markerBigIco = R.mipmap.ic_marker_toll_big_icon
            }
        }
        //  markerMap[title]?.let { clearMarkers(title, it) }
        val markers = ArrayList<Marker>()
        when (dataType) {
            1 -> {
                for (item in data) {  //事件类型（事故、施工、管制）
                    val mdl = item as EventMDL
                    mdl.markerIcon = markerIcon
                    mdl.markerBigIco = markerBigIco
                    val option = createOptions(LatLng(mdl.latitude(), mdl.longitude()), title, title, BitmapDescriptorFactory.fromResource(markerIcon))
                    markers.add(aMap.addMarker(option).apply { `object` = mdl })
                }
            }
            2 -> {
                for (item in data) {  //拥堵类型
                    val mdl = item as TrafficJamMDL
                    mdl.markerIcon = markerIcon
                    mdl.markerBigIco = markerBigIco
                    val option = createOptions(LatLng(mdl.latitude(), mdl.longitude()), title, title, BitmapDescriptorFactory.fromResource(markerIcon))
                    markers.add(aMap.addMarker(option).apply { `object` = mdl })
                }
            }
            3 -> {
                for (item in data) {   //快拍类型
                    val mdl = item as SnapShotMDL
                    mdl.markerIcon = markerIcon
                    mdl.markerBigIco = markerBigIco
                    val option = createOptions(LatLng(mdl.latitude(), mdl.longitude()), title, title, BitmapDescriptorFactory.fromResource(markerIcon))
                    markers.add(aMap.addMarker(option).apply { `object` = mdl })
                }
            }
            4 -> {
                for (item in data) {   //天气类型
                    val mdl = item as WeatherMDL
                    val option = createOptions(LatLng(mdl.latitude(), mdl.longitude()), title, mdl.city, BitmapDescriptorFactory.fromView(getWeatherView(mdl)))
                    markers.add(aMap.addMarker(option).apply { `object` = mdl })
                }
            }
            5 -> {   //维修店类型
                for (item in data) {
                    val mdl = item as RepairShopMDL
                    mdl.markerIcon = markerIcon
                    mdl.markerBigIco = markerBigIco
                    val option = createOptions(LatLng(mdl.latitude(), mdl.longitude()), title, title, BitmapDescriptorFactory.fromResource(markerIcon))
                    markers.add(aMap.addMarker(option).apply { `object` = mdl })
                }
            }
            6 -> { // 加油站类型
                for (item in data) {
                    val mdl = item as GasStationMDL
                    mdl.markerIcon = markerIcon
                    mdl.markerBigIco = markerBigIco
                    val option = createOptions(LatLng(mdl.latitude(), mdl.longitude()), title, title, BitmapDescriptorFactory.fromResource(markerIcon))
                    markers.add(aMap.addMarker(option).apply { `object` = mdl })
                }
            }
            7 -> { //景点类型
                for (item in data) {
                    val mdl = item as ScenicMDL
                    mdl.markerIcon = markerIcon
                    mdl.markerBigIco = markerBigIco
                    val option = createOptions(LatLng(mdl.latitude(), mdl.longitude()), title, title, BitmapDescriptorFactory.fromResource(markerIcon))
                    markers.add(aMap.addMarker(option).apply { `object` = mdl })
                }
            }
            8 -> { //服务区类型
                for (item in data) {
                    val mdl = item as ServiceMDL
                    mdl.markerIcon = markerIcon
                    mdl.markerBigIco = markerBigIco
                    val option = createOptions(LatLng(mdl.latitude(), mdl.longitude()), title, title, BitmapDescriptorFactory.fromResource(markerIcon))
                    markers.add(aMap.addMarker(option).apply { `object` = mdl })
                }
            }
            9 -> { //收费站类型
                for (item in data) {
                    val mdl = item as TollGateMDL
                    mdl.markerIcon = markerIcon
                    mdl.markerBigIco = markerBigIco
                    val option = createOptions(LatLng(mdl.latitude(), mdl.longitude()), title, title, BitmapDescriptorFactory.fromResource(markerIcon))
                    markers.add(aMap.addMarker(option).apply { `object` = mdl })
                }
            }
        }
        markerMap[title] = markers
    }

    private fun createOptions(latLng: LatLng, title: String?, snippet: String?, bitmap: BitmapDescriptor): MarkerOptions {
        return MarkerOptions().anchor(0.5f, 1f)
                .position(latLng)
                .title(title)
                .snippet(snippet)
                .visible(true)
                .infoWindowEnable(false)
                .draggable(false)
                .icon(bitmap)
    }

    private fun getWeatherView(mdl: WeatherMDL): View {
        val view = LayoutInflater.from(context).inflate(R.layout.mapview_weather_window, LinearLayout(context), false)
        val tvCity = view.findViewById<TextView>(R.id.tvCity)
        val tvWeather = view.findViewById<TextView>(R.id.tvWeather)
        val ivPic = view.findViewById<ImageView>(R.id.ivPic)
        tvCity.text = mdl.city
        tvWeather.text = mdl.temperature
        ivPic.setImageResource(WeatherMDL.getWeatherIco(mdl.weather))
        return view
    }

    //删除指定Marker
    private fun clearMarkers(id: String, markers: ArrayList<Marker>?) {
        markers?.let {
            for (marker in it) {
                if (TextUtils.equals(marker.title, id)) {
                    marker.remove() //移除当前Marker
                }
            }
        }
    }

    //还原上次点击的marker
    private fun restoreMarker(marker: Marker) {
        when {
            marker.`object` is EventMDL -> marker.setIcon(BitmapDescriptorFactory.fromResource((marker.`object` as EventMDL).markerIcon))
            marker.`object` is TrafficJamMDL -> marker.setIcon(BitmapDescriptorFactory.fromResource((marker.`object` as TrafficJamMDL).markerIcon))
            marker.`object` is SnapShotMDL -> marker.setIcon(BitmapDescriptorFactory.fromResource((marker.`object` as SnapShotMDL).markerIcon))
            marker.`object` is RepairShopMDL -> marker.setIcon(BitmapDescriptorFactory.fromResource((marker.`object` as RepairShopMDL).markerIcon))
            marker.`object` is GasStationMDL -> marker.setIcon(BitmapDescriptorFactory.fromResource((marker.`object` as GasStationMDL).markerIcon))
            marker.`object` is ScenicMDL -> marker.setIcon(BitmapDescriptorFactory.fromResource((marker.`object` as ScenicMDL).markerIcon))
            marker.`object` is ServiceMDL -> marker.setIcon(BitmapDescriptorFactory.fromResource((marker.`object` as ServiceMDL).markerIcon))
            marker.`object` is TollGateMDL -> marker.setIcon(BitmapDescriptorFactory.fromResource((marker.`object` as TollGateMDL).markerIcon))
        }
    }

    //marker点击放大图标
    private fun enlargeMarkerIcon(marker: Marker) {
        when {
            marker.`object` is EventMDL -> {
                val mdl = marker.`object` as EventMDL
                marker.setIcon(BitmapDescriptorFactory.fromResource(mdl.markerBigIco))
                val dialog = EventDetailDialog(context, mdl)
                dialog.setOnSubscribeListener(object : EventDetailDialog.OnSubscribeListener {
                    override fun onSubscribe(dataMDL: EventMDL) {
                        if (!isLogin()) openActivity(LoginActivity::class.java)
                        else dataMDL.eventid?.let { saveSubscribe(dataMDL.getSubType(), it, marker, dataMDL) }
                        dialog.dismiss()
                    }
                })
                dialog.show()
                dialog.setOnDismissListener { restoreMarker(marker) }
            }
            marker.`object` is TrafficJamMDL -> {
                val mdl = marker.`object` as TrafficJamMDL
                marker.setIcon(BitmapDescriptorFactory.fromResource(mdl.markerBigIco))
                val dialog = TrafficJamDetailDialog(context, mdl)
                dialog.setOnSubscribeListener(object : TrafficJamDetailDialog.OnSubscribeListener {
                    override fun onSubscribe(dataMDL: TrafficJamMDL) {
                        if (!isLogin()) openActivity(LoginActivity::class.java)
                        else dataMDL.eventid?.let { saveSubscribe(dataMDL.getSubType(), it, marker, dataMDL) }
                        dialog.dismiss()
                    }
                })
                dialog.show()
                dialog.setOnDismissListener { restoreMarker(marker) }
            }
            marker.`object` is SnapShotMDL -> {
                val mdl = marker.`object` as SnapShotMDL
                marker.setIcon(BitmapDescriptorFactory.fromResource(mdl.markerBigIco))
                val dialog = SnapShotDialog(context, mdl)
                dialog.setOnItemClickListener(object : SnapShotDialog.OnItemClickListener {
                    override fun onItemClick(dataMDL: SnapShotMDL) {
                        getRoadVideo(dataMDL.resid, dataMDL.shortname)
//                        showBigPic(position, photos)
                    }
                })
                dialog.show()
                dialog.setOnDismissListener { restoreMarker(marker) }
            }
            marker.`object` is RepairShopMDL -> {
                val mdl = marker.`object` as RepairShopMDL
                marker.setIcon(BitmapDescriptorFactory.fromResource(mdl.markerBigIco))
                val dialog = RepairShopDialog(context, mdl)
                dialog.setOnButtonClickListener(object : RepairShopDialog.OnButtonClickListener {
                    override fun onNavigation(dataMDL: RepairShopMDL) {
                        var poiName = ""
                        dataMDL.name?.let { poiName = it }
                        val end = Poi(poiName, LatLng(dataMDL.latitude(), dataMDL.longitude()), "")
                        openNaviPage(null, end)
                        dialog.dismiss()
                    }
                })
                dialog.show()
                dialog.setOnDismissListener { restoreMarker(marker) }
            }
            marker.`object` is GasStationMDL -> {
                val mdl = marker.`object` as GasStationMDL
                marker.setIcon(BitmapDescriptorFactory.fromResource(mdl.markerBigIco))
                val dialog = GasStationDialog(context, mdl)
                targetLatLng?.let { mdl.setDistance(it.longitude, it.latitude, mdl.longitude(), mdl.latitude()) }
                dialog.setOnNavigationListener(object : GasStationDialog.OnNavigationListener {
                    override fun onNavigation(dataMDL: GasStationMDL) {
                        var poiName = ""
                        dataMDL.name?.let { poiName = it }
                        val end = Poi(poiName, LatLng(dataMDL.latitude(), dataMDL.longitude()), "")
                        openNaviPage(null, end)
                        dialog.dismiss()
                    }
                })
                dialog.show()
                dialog.setOnDismissListener { restoreMarker(marker) }
            }
            marker.`object` is ScenicMDL -> {
                val mdl = marker.`object` as ScenicMDL
                marker.setIcon(BitmapDescriptorFactory.fromResource(mdl.markerBigIco))
                val dialog = ScenicDialog(context, mdl)
                dialog.setOnButtonClickListener(object : ScenicDialog.OnButtonClickListener {
                    override fun onDetail(dataMDL: ScenicMDL) {
                        openLocationWebActivity(dataMDL.detailurl, dataMDL.name)
                        dialog.dismiss()
                    }

                    override fun onNavigation(dataMDL: ScenicMDL) {
                        var poiName = ""
                        dataMDL.name?.let { poiName = it }
                        val end = Poi(poiName, LatLng(dataMDL.latitude(), dataMDL.longitude()), "")
                        openNaviPage(null, end)
                        dialog.dismiss()
                    }
                })
                dialog.show()
                dialog.setOnDismissListener { restoreMarker(marker) }
            }
            marker.`object` is ServiceMDL -> {
                val mdl = marker.`object` as ServiceMDL
                marker.setIcon(BitmapDescriptorFactory.fromResource(mdl.markerBigIco))
                val dialog = ServiceAreaDialog(context, mdl)
                dialog.setOnButtonClickListener(object : ServiceAreaDialog.OnButtonClickListener {
                    override fun onDetail(dataMDL: ServiceMDL) {
                        openLocationWebActivity(dataMDL.detailurl, dataMDL.name)
                        dialog.dismiss()
                    }

                    override fun onNavigation(dataMDL: ServiceMDL) {
                        var poiName = ""
                        dataMDL.name?.let { poiName = it }
                        val end = Poi(poiName, LatLng(dataMDL.latitude(), dataMDL.longitude()), "")
                        openNaviPage(null, end)
                        dialog.dismiss()
                    }
                })
                dialog.show()
                dialog.setOnDismissListener { restoreMarker(marker) }
            }
            marker.`object` is TollGateMDL -> {
                val mdl = marker.`object` as TollGateMDL
                marker.setIcon(BitmapDescriptorFactory.fromResource(mdl.markerBigIco))
                val dialog = TollGateDialog(context, mdl)
                dialog.setOnButtonClickListener(object : TollGateDialog.OnButtonClickListener {
                    override fun onDetail(dataMDL: TollGateMDL) {
                        openLocationWebActivity(dataMDL.detailurl, dataMDL.name)
                        dialog.dismiss()
                    }

                    override fun onNavigation(dataMDL: TollGateMDL) {
                        var poiName = ""
                        dataMDL.name?.let { poiName = it }
                        val end = Poi(poiName, LatLng(dataMDL.latitude(), dataMDL.longitude()), "")
                        openNaviPage(null, end)
                        dialog.dismiss()
                    }
                })
                dialog.show()
                dialog.setOnDismissListener { restoreMarker(marker) }
            }
        }
        oldMarker = marker
    }

    private fun saveSubscribe(subtype: String, dataid: String, marker: Marker, dataMDL: MutilItem) {
        doRequest(WebApiService.SAVE_SUBSCRIBE, WebApiService.saveSubscribeParams(getUserId(), subtype, dataid), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading("保存订阅…")
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    if (dataMDL is EventMDL) {
                        dataMDL.subscribestatus = 1
                        marker.`object` = dataMDL
                    } else {
                        val mdl = dataMDL as TrafficJamMDL
                        mdl.subscribestatus = 1
                        marker.`object` = mdl
                    }
                    showShortToast("订阅成功")
                } else showShortToast(GsonUtils.getMsg(data))
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                endLoading()
                onHttpError(e)
            }
        })
    }

    /*获取快拍请求流地址*/
    private fun getRoadVideo(resId: String?, shortName: String?) {
        doRequest(WebApiService.ROAD_VIDEO, WebApiService.roadVideoParams(resId), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, RtmpMDL::class.java)
                    mdl?.rtmpIp?.let {
                        openActivity(VideoPlayerActivity::class.java, Bundle().apply {
                            putBoolean("isLive", true)
                            putString("url", it)
                            putString("title", shortName)
                        })
                    }
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

    override fun onResume() {
        mapView.onResume()
        super.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        mapView.onDestroy()
        super.onDestroyView()
    }

}