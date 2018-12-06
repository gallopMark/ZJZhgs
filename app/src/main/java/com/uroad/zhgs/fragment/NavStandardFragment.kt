package com.uroad.zhgs.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Build
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
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.maps.model.animation.AlphaAnimation
import com.amap.api.maps.model.animation.Animation
import com.uroad.library.utils.DisplayUtils
import com.uroad.rxhttp.RxHttpManager
import com.uroad.rxhttp.interceptor.Transformer
import com.uroad.zhgs.R
import com.uroad.zhgs.activity.LoginActivity
import com.uroad.zhgs.activity.AMapNaviSearchActivity
import com.uroad.zhgs.activity.VideoPlayerActivity
import com.uroad.zhgs.cluster.Cluster
import com.uroad.zhgs.cluster.ClusterItem
import com.uroad.zhgs.cluster.CustomClusterItem
import com.uroad.zhgs.common.BaseLocationFragment
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.dialog.*
import com.uroad.zhgs.enumeration.MapDataType
import com.uroad.zhgs.model.*
import com.uroad.zhgs.utils.AndroidBase64Utils
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.ApiService
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import com.uroad.zhgs.widget.CustomView
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_nav_standard.*

/**
 *Created by MFB on 2018/8/10.
 * 路况导航 地图模式
 */
class NavStandardFragment : BaseLocationFragment() {

    private var fromHome: Boolean = false
    private var isOpenLocation = false
    private var location: AMapLocation? = null
    private var longitude: Double = 0.toDouble()
    private var latitude: Double = 0.toDouble()
    private lateinit var aMap: AMap
    private lateinit var myLocationView: View
    private lateinit var animationDrawable: AnimationDrawable
    private var targetLatLng: LatLng? = null
    private val checkMap = ArrayMap<String, Boolean>()
    private var mClusterSize: Int = 0
    private var mClusterDistance: Double = 0.0
    private val clusterMap = ArrayMap<String, ArrayList<CustomClusterItem>>()
    private val mAddMarkers = ArrayList<Marker>()
    private val weatherMarkers = ArrayList<Marker>()
    private val statusMap = ArrayMap<String, Disposable>()

    override fun setBaseLayoutResID(): Int {
        return R.layout.fragment_nav_standard
    }

    override fun setUp(view: View, savedInstanceState: Bundle?) {
        myLocationView = layoutInflater.inflate(R.layout.mapview_mylocation2, FrameLayout(context), false)
        val ivDiffuse = myLocationView.findViewById<ImageView>(R.id.ivDiffuse)
        animationDrawable = ivDiffuse.drawable as AnimationDrawable
        arguments?.let { fromHome = it.getBoolean("fromHome", false) }
        initTopSearch()
        mapView.onCreate(savedInstanceState)
        initMapView()
        requestLocationPermissions(object : RequestLocationPermissionCallback {
            override fun doAfterGrand() {
                openLocation()
            }

            override fun doAfterDenied() {
                showDismissLocationDialog()
            }
        })
    }

    private fun initTopSearch() {
        ivBack.setOnClickListener { context.onBackPressed() }
        llSearch.setOnClickListener { openActivity(AMapNaviSearchActivity::class.java) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val params = llSearch.layoutParams as FrameLayout.LayoutParams
            val topMargin = params.topMargin + DisplayUtils.getStatusHeight(context)
            params.topMargin = topMargin
            llSearch.layoutParams = params
        }
    }

    private fun initMapView() {
        aMap = mapView.map.apply { moveCamera(CameraUpdateFactory.newLatLngZoom(CurrApplication.APP_LATLNG, this.cameraPosition.zoom)) }
        mClusterSize = DisplayUtils.dip2px(context, 50f)
        mClusterDistance = (aMap.scalePerPixel * mClusterSize).toDouble()
        aMap.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
            override fun onCameraChange(position: CameraPosition) {

            }

            override fun onCameraChangeFinish(position: CameraPosition) {
                mClusterDistance = (aMap.scalePerPixel * mClusterSize).toDouble()
                onReCluster()
            }
        })
        aMap.setOnMarkerClickListener { marker ->
            if (marker.`object` is Cluster) {
                val cluster = marker.`object` as Cluster
                if (cluster.getClusterItems().size >= 2) {
                    enlargeMap()
                } else {
                    cluster.getObject()?.let { enlargeMarkerIcon(cluster, marker) }
                }
                return@setOnMarkerClickListener true
            } else {
                return@setOnMarkerClickListener true
            }
        }
        dealWithFromHome()
    }

    /*地图移动时重新绘制点聚合，或者移除某一功能时*/
    private fun onReCluster() {
        val removeMarkers = ArrayList(mAddMarkers)
        val alphaAnimation = AlphaAnimation(1f, 0f)
        val myAnimationListener = MyAnimationListener(removeMarkers)
        for (marker in removeMarkers) {
            marker.setAnimation(alphaAnimation)
            marker.setAnimationListener(myAnimationListener)
            marker.startAnimation()
        }
        for ((k, _) in clusterMap) {
            calculateClusters(k)
        }
    }

    //如果用户从首页我的订阅点击进来 则显示该事件详情
    private fun dealWithFromHome() {
        val mdl = arguments?.getSerializable("mdl")
        mdl?.let { mdL ->
            if (mdL is TrafficJamMDL) {
                val jamMDL = mdl as TrafficJamMDL
                val options = MarkerOptions().anchor(0.5f, 0.5f)
                        .icon(getSmallIcon(jamMDL.markerIcon))
                        .position(jamMDL.getPosition())
                        .anchor(0.5f, 1f)
                        .visible(true)
                        .infoWindowEnable(false)
                        .draggable(false)
                val marker = aMap.addMarker(options)
                val cluster = Cluster(jamMDL.getPosition()).apply {
                    setIcon(jamMDL.markerIcon)
                    setBigIcon(jamMDL.markerBigIco)
                    setObject(jamMDL)
                }
                marker.`object` = cluster
//                clusterMap[MapDataType.TRAFFIC_JAM.code] = ArrayList<CustomClusterItem>().apply { add(CustomClusterItem(jamMDL.getPosition(), jamMDL.markerIcon, jamMDL.markerBigIco, jamMDL)) }
                aMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(mdL.latitude(), mdL.longitude())))
                enlargeMarkerIcon(cluster, marker)
            } else if (mdL is EventMDL) {
                val eventMDL = mdl as EventMDL
                val markerIcon: Int
                val markerBigIcon: Int
                when (mdL.getSubType()) {
                    SubscribeMDL.SubType.Control.code -> {
                        markerIcon = R.mipmap.ic_marker_gz_icon
                        markerBigIcon = R.mipmap.ic_marker_gz_big_icon
                    }
                    SubscribeMDL.SubType.Emergencies.code -> {
                        markerIcon = R.mipmap.ic_marker_sg_icon
                        markerBigIcon = R.mipmap.ic_marker_sg_big_icon
                    }
                    SubscribeMDL.SubType.TrafficIncident.code -> {
                        markerIcon = R.mipmap.ic_marker_jtsj_icon
                        markerBigIcon = R.mipmap.ic_marker_jtsj_big_icon
                    }
                    SubscribeMDL.SubType.BadWeather.code -> {
                        markerIcon = R.mipmap.ic_marker_eltq_icon
                        markerBigIcon = R.mipmap.ic_marker_eltq_big_icon
                    }
                    else -> {
                        markerIcon = R.mipmap.ic_marker_shig_icon
                        markerBigIcon = R.mipmap.ic_marker_shig_big_icon
                    }
                }
                eventMDL.markerIcon = markerIcon
                eventMDL.markerBigIco = markerBigIcon
                val options = MarkerOptions().anchor(0.5f, 0.5f)
                        .icon(getSmallIcon(eventMDL.markerIcon))
                        .position(eventMDL.getPosition())
                        .anchor(0.5f, 1f)
                        .visible(true)
                        .infoWindowEnable(false)
                        .draggable(false)
                val marker = aMap.addMarker(options)
                val cluster = Cluster(eventMDL.getPosition()).apply {
                    setIcon(eventMDL.markerIcon)
                    setBigIcon(eventMDL.markerBigIco)
                    setObject(eventMDL)
                }
                marker.`object` = cluster
                aMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(mdL.latitude(), mdL.longitude())))
                enlargeMarkerIcon(cluster, marker)
            }
        }
    }

    fun onLocation() {
        location?.let { aMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(LatLng(it.latitude, it.longitude), aMap.cameraPosition.zoom, 0f, 0f))) }
    }

    override fun afterLocation(location: AMapLocation) {
        isOpenLocation = true
        this.location = location
        this.longitude = location.longitude
        this.latitude = location.latitude
        this.targetLatLng = LatLng(location.latitude, location.longitude)
        aMap.addMarker(createOptions(LatLng(location.latitude, location.longitude), location.city, location.address, BitmapDescriptorFactory.fromView(myLocationView)))
        if (!fromHome) {
            aMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(LatLng(location.latitude, location.longitude), aMap.cameraPosition.zoom, 0f, 0f)))
            loadDefault()
        }
        closeLocation()
    }

    override fun locationFailure() {
        loadDefault()
    }

    //路况导航-地图模式默认开启图层：事故、管制、拥堵、恶劣天气、监控
    private fun loadDefault() {
        getMapDataByType(MapDataType.ACCIDENT.code)
        getMapDataByType(MapDataType.CONTROL.code)
        getMapDataByType(MapDataType.TRAFFIC_JAM.code)
        getMapDataByType(MapDataType.BAD_WEATHER.code)
        getMapDataByType(MapDataType.SNAPSHOT.code)
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

    fun onEvent(codeType: String, isChecked: Boolean) {
        if (TextUtils.isEmpty(codeType)) return
        checkMap[codeType] = isChecked
        if (isChecked) {
            getMapDataByType(codeType)
        } else {
            statusMap[codeType]?.dispose()
            if (codeType == MapDataType.WEATHER.code) {
                for (marker in weatherMarkers) {
                    marker.remove() //移除当前Marker
                    marker.destroy()
                }
            } else {
                clusterMap.remove(codeType)
                onReCluster()
            }
        }
    }

    private fun getMapDataByType(type: String) {
        val body = ApiService.createRequestBody(WebApiService.mapDataByTypeParams(type, longitude, latitude, "", ""), WebApiService.MAP_DATA)
        val disposable = RxHttpManager.createApi(ApiService::class.java).doPost(body).compose(Transformer.switchSchedulers())
                .subscribe({ data -> onSuccess(type, data) }, { e -> onError(e) }, {}, { startAnim() })
        statusMap[type] = disposable
    }

    /*接口数据返回*/
    private fun onSuccess(type: String, json: String?) {
        stopAnim()
        val data = AndroidBase64Utils.decodeToString(json)
        if (GsonUtils.isResultOk(data)) {
            updateData(type, data)
        } else {
            showShortToast(GsonUtils.getMsg(data))
        }
    }

    /*访问异常*/
    private fun onError(e: Throwable) {
        stopAnim()
        onHttpError(e)
    }

    //根据不同类型进行解析数据
    private fun updateData(type: String, data: String?) {
        if (type == MapDataType.ACCIDENT.code
                || type == MapDataType.CONTROL.code
                || type == MapDataType.CONSTRUCTION.code
                || type == MapDataType.BAD_WEATHER.code
                || type == MapDataType.TRAFFIC_INCIDENT.code) {   //事件类型
            onEventData(type, data)
        } else if (type == MapDataType.TRAFFIC_JAM.code) {  //拥堵类型
            onTrafficJamData(type, data)
        } else if (type == MapDataType.SNAPSHOT.code) { //快拍类型
            onSnapShotData(type, data)
        } else if (type == MapDataType.WEATHER.code) {  //天气类型
            val dataMDLs = GsonUtils.fromDataToList(data, WeatherMDL::class.java)
            insertPoint(ArrayList<MutilItem>().apply { addAll(dataMDLs) })
        } else if (type == MapDataType.REPAIR_SHOP.code) {  //维修店类型
            onRepairShopData(type, data)
        } else if (type == MapDataType.GAS_STATION.code) {  //加油站类型
            onGasStationData(type, data)
        } else if (type == MapDataType.SCENIC.code) {  //景点类型
            onScenicData(type, data)
        } else if (type == MapDataType.SERVICE_AREA.code) {  //服务区类型
            onServiceData(type, data)
        } else if (type == MapDataType.TOLL_GATE.code) { //收费站类型
            onTollGateData(type, data)
        }
    }

    //事件类型数据处理
    private fun onEventData(type: String, data: String?) {
        val dataMDLs = GsonUtils.fromDataToList(data, EventMDL::class.java)
        val isChecked = checkMap[type]
        if (dataMDLs.size == 0 && isChecked != null && isChecked) showShortToast(context.getString(R.string.NoDataAtAll))
        else {
            val markerIcon: Int
            val markerBigIco: Int
            when (type) {
                MapDataType.ACCIDENT.code -> {
                    markerIcon = R.mipmap.ic_marker_sg_icon
                    markerBigIco = R.mipmap.ic_marker_sg_big_icon
                }
                MapDataType.CONTROL.code -> {
                    markerIcon = R.mipmap.ic_marker_gz_icon
                    markerBigIco = R.mipmap.ic_marker_gz_big_icon
                }
                MapDataType.BAD_WEATHER.code -> {
                    markerIcon = R.mipmap.ic_marker_eltq_icon
                    markerBigIco = R.mipmap.ic_marker_eltq_big_icon
                }
                MapDataType.TRAFFIC_INCIDENT.code -> {
                    markerIcon = R.mipmap.ic_marker_jtsj_icon
                    markerBigIco = R.mipmap.ic_marker_jtsj_big_icon
                }
                else -> {
                    markerIcon = R.mipmap.ic_marker_shig_icon
                    markerBigIco = R.mipmap.ic_marker_shig_big_icon
                }
            }
            for (item in dataMDLs) {
                item.markerIcon = markerIcon
                item.markerBigIco = markerBigIco
            }
            cluster(type, ArrayList<ClusterItem>().apply { addAll(dataMDLs) })
        }
    }

    /*拥堵类型数据处理*/
    private fun onTrafficJamData(type: String, data: String?) {
        val dataMDLs = GsonUtils.fromDataToList(data, TrafficJamMDL::class.java)
        val isChecked = checkMap[type]
        if (dataMDLs.size == 0 && isChecked != null && isChecked) showShortToast(context.getString(R.string.NoDataAtAll))
        else cluster(type, ArrayList<ClusterItem>().apply { addAll(dataMDLs) })
    }

    /*监控快拍数据处理*/
    private fun onSnapShotData(type: String, data: String?) {
        val dataMDLs = GsonUtils.fromDataToList(data, SnapShotMDL::class.java)
        val isChecked = checkMap[type]
        if (dataMDLs.size == 0 && isChecked != null && isChecked) showShortToast(context.getString(R.string.NoDataAtAll))
        else cluster(type, ArrayList<ClusterItem>().apply { addAll(dataMDLs) })
    }

    /*附近维修店数据类型处理*/
    private fun onRepairShopData(type: String, data: String?) {
        val dataMDLs = GsonUtils.fromDataToList(data, RepairShopMDL::class.java)
        val isChecked = checkMap[type]
        if (dataMDLs.size == 0 && isChecked != null && isChecked) showShortToast(context.getString(R.string.NoDataAtAll))
        else cluster(type, ArrayList<ClusterItem>().apply { addAll(dataMDLs) })
    }

    /*附近加油站数据类型处理*/
    private fun onGasStationData(type: String, data: String?) {
        val dataMDLs = GsonUtils.fromDataToList(data, GasStationMDL::class.java)
        val isChecked = checkMap[type]
        if (dataMDLs.size == 0 && isChecked != null && isChecked) showShortToast(context.getString(R.string.NoDataAtAll))
        else cluster(type, ArrayList<ClusterItem>().apply { addAll(dataMDLs) })
    }

    /*附近景点类型数据处理*/
    private fun onScenicData(type: String, data: String?) {
        val dataMDLs = GsonUtils.fromDataToList(data, ScenicMDL::class.java)
        val isChecked = checkMap[type]
        if (dataMDLs.size == 0 && isChecked != null && isChecked) showShortToast(context.getString(R.string.NoDataAtAll))
        else cluster(type, ArrayList<ClusterItem>().apply { addAll(dataMDLs) })
    }

    /*附近服务区类型数据处理*/
    private fun onServiceData(type: String, data: String?) {
        val dataMDLs = GsonUtils.fromDataToList(data, ServiceMDL::class.java)
        val isChecked = checkMap[type]
        if (dataMDLs.size == 0 && isChecked != null && isChecked) showShortToast(context.getString(R.string.NoDataAtAll))
        else cluster(type, ArrayList<ClusterItem>().apply { addAll(dataMDLs) })
    }

    /*附近收费站类型数据处理*/
    private fun onTollGateData(type: String, data: String?) {
        val dataMDLs = GsonUtils.fromDataToList(data, TollGateMDL::class.java)
        val isChecked = checkMap[type]
        if (dataMDLs.size == 0 && isChecked != null && isChecked) showShortToast(context.getString(R.string.NoDataAtAll))
        else cluster(type, ArrayList<ClusterItem>().apply { addAll(dataMDLs) })
    }

    //进行点聚合
    private fun cluster(type: String, data: MutableList<ClusterItem>) {
        val items = ArrayList<CustomClusterItem>()
        val markerIcon: Int
        val markerBigIco: Int
        when (type) {
            MapDataType.ACCIDENT.code -> {
                markerIcon = R.mipmap.ic_marker_sg_icon
                markerBigIco = R.mipmap.ic_marker_sg_big_icon
            }
            MapDataType.CONTROL.code -> {
                markerIcon = R.mipmap.ic_marker_gz_icon
                markerBigIco = R.mipmap.ic_marker_gz_big_icon
            }
            MapDataType.CONSTRUCTION.code -> {
                markerIcon = R.mipmap.ic_marker_shig_icon
                markerBigIco = R.mipmap.ic_marker_shig_big_icon
            }
            MapDataType.TRAFFIC_JAM.code -> {
                markerIcon = R.mipmap.ic_marker_yd_icon
                markerBigIco = R.mipmap.ic_marker_yd_big_icon
            }
            MapDataType.SNAPSHOT.code -> {
                markerIcon = R.mipmap.ic_marker_snap_icon
                markerBigIco = R.mipmap.ic_marker_snap_big_icon
            }
            MapDataType.REPAIR_SHOP.code -> {
                markerIcon = R.mipmap.ic_marker_repair_icon
                markerBigIco = R.mipmap.ic_marker_repair_big_icon
            }
            MapDataType.GAS_STATION.code -> {
                markerIcon = R.mipmap.ic_marker_gas_icon
                markerBigIco = R.mipmap.ic_marker_gas_big_icon
            }
            MapDataType.SCENIC.code -> {
                markerIcon = R.mipmap.ic_marker_secnic_icon
                markerBigIco = R.mipmap.ic_marker_secnic_big_icon
            }
            MapDataType.SERVICE_AREA.code -> {
                markerIcon = R.mipmap.ic_marker_service_icon
                markerBigIco = R.mipmap.ic_marker_service_big_icon
            }
            MapDataType.BAD_WEATHER.code -> {
                markerIcon = R.mipmap.ic_marker_eltq_icon
                markerBigIco = R.mipmap.ic_marker_eltq_big_icon
            }
            MapDataType.TRAFFIC_INCIDENT.code -> {
                markerIcon = R.mipmap.ic_marker_jtsj_icon
                markerBigIco = R.mipmap.ic_marker_jtsj_big_icon
            }
            else -> {
                markerIcon = R.mipmap.ic_marker_toll_icon
                markerBigIco = R.mipmap.ic_marker_toll_big_icon
            }
        }
        for (item in data) {
            items.add(CustomClusterItem(item.getPosition(), markerIcon, markerBigIco, item))
        }
        clusterMap[type] = items
        calculateClusters(type)
    }

    private fun calculateClusters(type: String) {
        val mClusters = ArrayList<Cluster>()
        val visibleBounds = aMap.projection.visibleRegion.latLngBounds
        clusterMap[type]?.let {
            for (clusterItem in it) {
                val latLng = clusterItem.getPosition()
                if (visibleBounds.contains(latLng)) {
                    var cluster = getCluster(latLng, mClusters)
                    if (cluster != null) {
                        cluster.setIcon(clusterItem.getMarkerSmallIcon())
                        cluster.setBigIcon(clusterItem.getMarkerBigIcon())
                        cluster.setObject(clusterItem.getObject())
                        cluster.addClusterItem(clusterItem)
                    } else {
                        cluster = Cluster(latLng).apply {
                            setIcon(clusterItem.getMarkerSmallIcon())
                            setBigIcon(clusterItem.getMarkerBigIcon())
                            setObject(clusterItem.getObject())
                        }
                        mClusters.add(cluster)
                        cluster.addClusterItem(clusterItem)
                    }
                }
            }
            addClusterToMap(mClusters)
        }
    }

    /**
     * 根据一个点获取是否可以依附的聚合点，没有则返回null
     */
    private fun getCluster(latLng: LatLng, clusters: MutableList<Cluster>): Cluster? {
        for (cluster in clusters) {
            val clusterCenterPoint = cluster.getCenterLatLng()
            val distance = AMapUtils.calculateLineDistance(latLng, clusterCenterPoint).toDouble()
            if (distance < mClusterDistance && aMap.cameraPosition.zoom < 19) {
                return cluster
            }
        }
        return null
    }

    /**
     * 将聚合元素添加至地图上
     */
    private fun addClusterToMap(clusters: List<Cluster>) {
        for (cluster in clusters) {
            addSingleClusterToMap(cluster)
        }
    }

    /**
     * marker渐变动画，动画结束后将Marker删除
     */
    inner class MyAnimationListener(private val mRemoveMarkers: MutableList<Marker>) : Animation.AnimationListener {

        override fun onAnimationStart() {
        }

        override fun onAnimationEnd() {
            for (marker in mRemoveMarkers) {
                marker.remove()
            }
            mRemoveMarkers.clear()
        }
    }

    /**
     * 将单个聚合元素添加至地图显示
     */
    private fun addSingleClusterToMap(cluster: Cluster) {
        val latLng = cluster.getCenterLatLng()
        val markerOptions = MarkerOptions()
        markerOptions.anchor(0.5f, 0.5f)
                .icon(getBitmapDes(cluster, cluster.getClusterCount()))
                .position(latLng)
                .visible(true)
                .infoWindowEnable(false)
                .draggable(false)
        val marker = aMap.addMarker(markerOptions)
        marker.setObject(cluster)
        marker.startAnimation()
        cluster.setMarker(marker)
        mAddMarkers.add(marker)
    }

    private fun getBitmapDes(cluster: Cluster, size: Int): BitmapDescriptor {
        val view = LayoutInflater.from(context).inflate(R.layout.mapview_cluster, LinearLayout(context), false)
        val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)
        val cvParent = view.findViewById<CustomView>(R.id.cvParent)
        val tvCount = view.findViewById<TextView>(R.id.tvCount)
        ivIcon.setImageResource(cluster.getIcon())
        if (size > 1) {
            tvCount.text = size.toString()
            cvParent.visibility = View.VISIBLE
        } else {
            cvParent.visibility = View.GONE
        }
        return BitmapDescriptorFactory.fromView(view)
    }

    private fun insertPoint(data: MutableList<MutilItem>) {
        val markers = ArrayList<Marker>()
        for (item in data) {   //天气类型
            val mdl = item as WeatherMDL
            val option = createOptions(LatLng(mdl.latitude(), mdl.longitude()), MapDataType.WEATHER.name, mdl.city, BitmapDescriptorFactory.fromView(getWeatherView(mdl)))
            markers.add(aMap.addMarker(option).apply { `object` = mdl })
        }
        weatherMarkers.clear()
        weatherMarkers.addAll(markers)
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

    /*marker点击放大 查看详情*/
    private fun enlargeMarkerIcon(cluster: Cluster, marker: Marker) {
        when (cluster.getObject()) {
            is EventMDL -> {  //事件类型
                marker.setIcon(getBigIcon(cluster.getBigIcon()))
                val mdl = cluster.getObject() as EventMDL
                val dialog = EventDetailDialog(context, mdl)
                dialog.setOnViewClickListener(object : EventDetailDialog.OnViewClickListener {
                    override fun onViewClick(dataMDL: EventMDL, type: Int) {
                        if (!isLogin()) openActivity(LoginActivity::class.java)
                        else {
                            when (type) {
                                1 -> {  //点击了“有用”
                                    saveIsUseful(cluster, mdl, 1, dialog)
                                }
                                2 -> {  //点击了“没用”
                                    saveIsUseful(cluster, mdl, 2, dialog)
                                }
                                else -> dataMDL.eventid?.let { saveSubscribe(cluster, dataMDL, dialog) }
                            }
                        }
                    }
                })
                dialog.show()
                dialog.setOnDismissListener { marker.setIcon(getSmallIcon(cluster.getIcon())) }
            }
            is TrafficJamMDL -> {  //拥堵类型
                marker.setIcon(getBigIcon(cluster.getBigIcon()))
                val mdl = cluster.getObject() as TrafficJamMDL
                val dialog = TrafficJamDetailDialog(context, mdl)
                dialog.setOnViewClickListener(object : TrafficJamDetailDialog.OnViewClickListener {
                    override fun onViewClick(dataMDL: TrafficJamMDL, type: Int) {
                        if (!isLogin()) openActivity(LoginActivity::class.java)
                        else {
                            when (type) {
                                1 -> {  //点击了“有用”
                                    saveIsUseful(cluster, dataMDL, 1, dialog)
                                }
                                2 -> {  //点击了“没用”
                                    saveIsUseful(cluster, dataMDL, 2, dialog)
                                }
                                else -> dataMDL.eventid?.let { saveSubscribe(cluster, dataMDL, dialog) }
                            }
                            dialog.dismiss()
                        }
                    }
                })
                dialog.show()
                dialog.setOnDismissListener { marker.setIcon(getSmallIcon(cluster.getIcon())) }
            }
            is SnapShotMDL -> {  //快拍类型
                marker.setIcon(getBigIcon(cluster.getBigIcon()))
                val mdl = cluster.getObject() as SnapShotMDL
                val dialog = SnapShotDialog(context, mdl)
                dialog.setOnItemClickListener(object : SnapShotDialog.OnItemClickListener {
                    override fun onItemClick(dataMDL: SnapShotMDL) {
                        getRoadVideo(dataMDL.resid, dataMDL.shortname)
                    }
                })
                dialog.show()
                dialog.setOnDismissListener { marker.setIcon(getSmallIcon(cluster.getIcon())) }
            }
            is RepairShopMDL -> { //维修店类型
                marker.setIcon(getBigIcon(cluster.getBigIcon()))
                val mdl = cluster.getObject() as RepairShopMDL
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
                dialog.setOnDismissListener { marker.setIcon(getSmallIcon(cluster.getIcon())) }
            }
            is GasStationMDL -> { //加油站类型
                marker.setIcon(getBigIcon(cluster.getBigIcon()))
                val mdl = cluster.getObject() as GasStationMDL
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
                dialog.setOnDismissListener { marker.setIcon(getSmallIcon(cluster.getIcon())) }
            }
            is ScenicMDL -> {  //景点类型
                marker.setIcon(getBigIcon(cluster.getBigIcon()))
                val mdl = cluster.getObject() as ScenicMDL
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
                dialog.setOnDismissListener { marker.setIcon(getSmallIcon(cluster.getIcon())) }
            }
            is ServiceMDL -> { //服务区类型
                marker.setIcon(getBigIcon(cluster.getBigIcon()))
                val mdl = cluster.getObject() as ServiceMDL
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
                dialog.setOnDismissListener { marker.setIcon(getSmallIcon(cluster.getIcon())) }
            }
            is TollGateMDL -> {  //收费站类型
                marker.setIcon(getBigIcon(cluster.getBigIcon()))
                val mdl = cluster.getObject() as TollGateMDL
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
                dialog.setOnDismissListener { marker.setIcon(getSmallIcon(cluster.getIcon())) }
            }
        }
    }

    /*marker大图标*/
    private fun getBigIcon(bigIcon: Int): BitmapDescriptor {
        val view = LayoutInflater.from(context).inflate(R.layout.mapview_cluster, LinearLayout(context), false)
        val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)
        view.findViewById<CustomView>(R.id.cvParent).visibility = View.GONE
        ivIcon.setImageResource(bigIcon)
        return BitmapDescriptorFactory.fromView(view)
    }

    /*marker小图标*/
    private fun getSmallIcon(smallIcon: Int): BitmapDescriptor {
        val view = LayoutInflater.from(context).inflate(R.layout.mapview_cluster, LinearLayout(context), false)
        val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)
        view.findViewById<CustomView>(R.id.cvParent).visibility = View.GONE
        ivIcon.setImageResource(smallIcon)
        return BitmapDescriptorFactory.fromView(view)
    }

    //是否有用
    private fun saveIsUseful(cluster: Cluster, dataMDL: MutilItem, type: Int, dialog: Dialog) {
        val eventId = if (dataMDL is EventMDL) {
            dataMDL.eventid
        } else {
            (dataMDL as TrafficJamMDL).eventid
        }
        doRequest(WebApiService.SAVE_IS_USEFUL, WebApiService.isUsefulParams(eventId, getUserId(), type), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    if (dataMDL is EventMDL) {
                        dataMDL.isuseful = type
                        cluster.setObject(dataMDL)
                        (dialog as EventDetailDialog).updateMDL(dataMDL)
                    } else {
                        val mdl = dataMDL as TrafficJamMDL
                        mdl.isuseful = type
                        cluster.setObject(mdl)
                        (dialog as TrafficJamDetailDialog).updateMDL(dataMDL)
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

    /*保存订阅 事件类型或拥堵类型才可以订阅*/
    private fun saveSubscribe(cluster: Cluster, dataMDL: MutilItem, dialog: Dialog) {
        val subtype: String?
        val dataId: String?
        if (dataMDL is EventMDL) {
            subtype = dataMDL.subtype
            dataId = dataMDL.eventid
        } else {
            val mdl = dataMDL as TrafficJamMDL
            subtype = mdl.subtype
            dataId = mdl.eventid
        }
        doRequest(WebApiService.SAVE_SUBSCRIBE, WebApiService.saveSubscribeParams(getUserId(), subtype, dataId), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading("保存订阅…")
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    if (dataMDL is EventMDL) {
                        dataMDL.subscribestatus = 1
                        cluster.setObject(dataMDL)
                        (dialog as EventDetailDialog).updateSubscribe(dataMDL)
                    } else {
                        val mdl = dataMDL as TrafficJamMDL
                        mdl.subscribestatus = 1
                        cluster.setObject(mdl)
                        (dialog as TrafficJamDetailDialog).updateSubscribe(mdl)
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
                        openActivityForResult(VideoPlayerActivity::class.java, Bundle().apply {
                            putBoolean("isLive", true)
                            putString("url", it)
                            putString("title", shortName)
                        }, 345)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 345 && resultCode == Activity.RESULT_OK) {
            showLongToast("播放结束")
        }
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