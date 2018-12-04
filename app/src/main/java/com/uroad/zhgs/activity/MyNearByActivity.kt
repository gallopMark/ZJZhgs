package com.uroad.zhgs.activity

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import com.amap.api.location.AMapLocation
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseLocationActivity
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.dialog.ScenicDialog
import com.uroad.zhgs.dialog.ServiceAreaDialog
import com.uroad.zhgs.dialog.TollGateDialog
import com.uroad.zhgs.enumeration.MapDataType
import com.uroad.zhgs.model.*
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import kotlinx.android.synthetic.main.activity_mynearby.*

/**
 *Created by MFB on 2018/8/23.
 */
class MyNearByActivity : BaseLocationActivity() {

    private var location: AMapLocation? = null
    private lateinit var aMap: AMap
    private var animationDrawable: AnimationDrawable? = null
    private var target: LatLng? = null
    private var type: Int = 1
    private var longitude: Double = CurrApplication.APP_LATLNG.longitude
    private var latitude: Double = CurrApplication.APP_LATLNG.latitude
    private val nameList = ArrayList<String>()
    private var visit: String = "15"
    private var isShow = false
    private var tollGateMDL: TollGateMDL? = null
    private var serviceMDL: ServiceMDL? = null
    private var scenicMDL: ScenicMDL? = null
    private val tollMarkers = ArrayList<Marker>()
    private val serviceMarkers = ArrayList<Marker>()
    private val scenicMarkers = ArrayList<Marker>()
    private var oldMarker: Marker? = null

    override fun setUp(savedInstanceState: Bundle?) {
        withTitle(resources.getString(R.string.mynearby_title))
        setBaseContentLayout(R.layout.activity_mynearby)
        intent.extras?.let { type = it.getInt("type", 1) }
        setDistance()
        mapView.onCreate(savedInstanceState)
        initMapView()
        setCheck()
        applyLocationPermission(true)
    }

    private fun initMapView() {
        aMap = mapView.map
        aMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(CurrApplication.APP_LATLNG, aMap.cameraPosition.zoom, 0f, 0f)))
        aMap.setOnMarkerClickListener { marker ->
            oldMarker?.let { restoreMarker(it) }
            enlargeMarkerIcon(marker)
            return@setOnMarkerClickListener true
        }
        dealWithFromHome()
    }

    private fun dealWithFromHome() {
        val mdl = intent.extras?.getSerializable("mdl")
        mdl?.let {
            when (it) {
                is TollGateMDL -> {
                    tollGateMDL = it
                    val option = createOptions(LatLng(it.latitude(), it.longitude()),
                            MapDataType.TOLL_GATE.name, MapDataType.TOLL_GATE.name,
                            BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_toll_icon))
                    val marker = aMap.addMarker(option).apply { `object` = mdl }
                    tollMarkers.add(marker)
                    aMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(LatLng(it.latitude(), it.longitude()), aMap.cameraPosition.zoom, 0f, 0f)))
                    enlargeMarkerIcon(marker)
                }
                is ServiceMDL -> {
                    serviceMDL = it
                    val option = createOptions(LatLng(it.latitude(), it.longitude()),
                            MapDataType.SERVICE_AREA.name, MapDataType.SERVICE_AREA.name,
                            BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_service_icon))
                    val marker = aMap.addMarker(option).apply { `object` = mdl }
                    serviceMarkers.add(marker)
                    aMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(LatLng(it.latitude(), it.longitude()), aMap.cameraPosition.zoom, 0f, 0f)))
                    enlargeMarkerIcon(marker)
                }
                is ScenicMDL -> {
                    scenicMDL = it
                    val option = createOptions(LatLng(it.latitude(), it.longitude()),
                            MapDataType.SCENIC.name, MapDataType.SCENIC.name,
                            BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_secnic_icon))
                    val marker = aMap.addMarker(option).apply { `object` = mdl }
                    scenicMarkers.add(marker)
                    aMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(LatLng(it.latitude(), it.longitude()), aMap.cameraPosition.zoom, 0f, 0f)))
                    enlargeMarkerIcon(marker)
                }
            }
        }
    }

    private fun setCheck() {
        val onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { cb, isChecked ->
            when (cb.id) {
                R.id.cbToll -> {
                    if (isChecked) {
                        if (!nameList.contains(resources.getString(R.string.mynearby_tab_toll))) {
                            nameList.add(resources.getString(R.string.mynearby_tab_toll))
                        }
                    } else {
                        nameList.remove(resources.getString(R.string.mynearby_tab_toll))
                    }
                }
                R.id.cbService -> {
                    if (isChecked) {
                        if (!nameList.contains(resources.getString(R.string.mynearby_tab_service))) {
                            nameList.add(resources.getString(R.string.mynearby_tab_service))
                        }
                    } else {
                        nameList.remove(resources.getString(R.string.mynearby_tab_service))
                    }
                }
                R.id.cbScenic -> {
                    if (isChecked) {
                        if (!nameList.contains(resources.getString(R.string.mynearby_tab_scenic))) {
                            nameList.add(resources.getString(R.string.mynearby_tab_scenic))
                        }
                    } else {
                        nameList.remove(resources.getString(R.string.mynearby_tab_scenic))
                    }
                }
            }
            val sb = StringBuilder()
            for (i in 0 until nameList.size) {
                sb.append(nameList[i])
                if (i < nameList.size - 1) sb.append("，")
            }
            tvName.text = sb.toString()
        }
        cbToll.setOnCheckedChangeListener(onCheckedChangeListener)
        cbService.setOnCheckedChangeListener(onCheckedChangeListener)
        cbScenic.setOnCheckedChangeListener(onCheckedChangeListener)
        when (type) {
            1 -> cbToll.isChecked = true
            2 -> cbService.isChecked = true
            3 -> cbScenic.isChecked = true
            else -> {
                cbToll.isChecked = true
                cbService.isChecked = true
                cbScenic.isChecked = true
            }
        }
    }

    //启动帧动画
    private fun startAnim() {
        animationDrawable?.start()
    }

    //选择当前动画的第一帧，然后停止
    private fun stopAnim() {
        animationDrawable?.selectDrawable(0) //选择当前动画的第一帧，然后停止
        animationDrawable?.stop()
    }

    override fun afterLocation(location: AMapLocation) {
        this.location = location
        this.longitude = location.longitude
        this.latitude = location.latitude
        this.target = LatLng(location.latitude, location.longitude)
        val myLocationView = layoutInflater.inflate(R.layout.mapview_mylocation2, LinearLayout(this), false)
        val ivDiffuse = myLocationView.findViewById<ImageView>(R.id.ivDiffuse)
        animationDrawable = ivDiffuse.drawable as AnimationDrawable
        aMap.addMarker(createOptions(LatLng(location.latitude, location.longitude), location.city, location.address, BitmapDescriptorFactory.fromView(myLocationView)))
        if (type == 4) aMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(LatLng(location.latitude, location.longitude), aMap.cameraPosition.zoom, 0f, 0f)))
        getCheckMapData()
        closeLocation()
    }

    private fun getCheckMapData() {
        if (cbToll.isChecked) getMapDataByType(MapDataType.TOLL_GATE.code)
        if (cbService.isChecked) getMapDataByType(MapDataType.SERVICE_AREA.code)
        if (cbScenic.isChecked) getMapDataByType(MapDataType.SCENIC.code)
    }

    //定位失败
    override fun onLocationFail(errorInfo: String?) {
        //间隔2秒再重新打开定位
        Handler().postDelayed({ if (!isFinishing) openLocation() }, 2000)
    }

    override fun setListener() {
        llTop.setOnClickListener {
            isShow = !isShow
            llOption.visibility = if (isShow) View.VISIBLE else View.GONE
            if (isShow) ivArrow.setImageResource(R.drawable.ic_expand_less_24dp)
            else ivArrow.setImageResource(R.drawable.ic_expand_more_24dp)
        }
        flOutSide.setOnTouchListener { _, _ ->
            hideOptions()
            return@setOnTouchListener true
        }
        seekBar.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams) {
                setDistance()
            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
            }
        }
        tvReset.setOnClickListener { seekBar.setProgress(4f) }
        tvConfirm.setOnClickListener {
            hideOptions()
            clearMarkers()
            getCheckMapData()
        }
    }

    private fun setDistance() {
        val progress = seekBar.progress
        val distance: String
        if (progress > 1) {
            visit = "${(progress - 1) * 5}"
            distance = "${(progress - 1) * 5}km"
        } else {
            visit = "1"
            distance = "1km"
        }
        tvDistance.text = distance
        tvDistance2.text = distance
    }

    private fun hideOptions() {
        llOption.visibility = View.GONE
        ivArrow.setImageResource(R.drawable.ic_expand_more_24dp)
        isShow = false
    }

    private fun getMapDataByType(type: String) {
        doRequest(WebApiService.MAP_DATA, WebApiService.mapDataByTypeParams(type, longitude, latitude, visit, ""), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                startAnim()
            }

            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    updateData(type, data)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
                stopAnim()
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                onHttpError(e)
                stopAnim()
            }
        })
    }

    //根据类型解析数据
    private fun updateData(type: String, data: String?) {
        when (type) {
            MapDataType.TOLL_GATE.code -> {
                val list = GsonUtils.fromDataToList(data, TollGateMDL::class.java)
                updateToll(list)
            }
            MapDataType.SERVICE_AREA.code -> {
                val list = GsonUtils.fromDataToList(data, ServiceMDL::class.java)
                updateService(list)
            }
            MapDataType.SCENIC.code -> {
                val list = GsonUtils.fromDataToList(data, ScenicMDL::class.java)
                updateScenic(list)
            }
        }
    }

    private fun clearMarkers() {
        for (marker in tollMarkers) marker.remove()
        for (marker in serviceMarkers) marker.remove()
        for (marker in scenicMarkers) marker.remove()
        tollGateMDL = null
        serviceMDL = null
        scenicMDL = null
    }

    //收费站类型数据
    private fun updateToll(list: MutableList<TollGateMDL>) {
        val markers = ArrayList<Marker>()
        val detailUrl = tollGateMDL?.detailurl
        for (item in list) {
            if(item.detailurl == detailUrl) continue
            val option = createOptions(LatLng(item.latitude(), item.longitude()),
                    MapDataType.TOLL_GATE.name, MapDataType.TOLL_GATE.name,
                    BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_toll_icon))
            markers.add(aMap.addMarker(option).apply { `object` = item })
        }
        tollMarkers.addAll(markers)
    }

    //服务区类型数据
    private fun updateService(list: MutableList<ServiceMDL>) {
        val markers = ArrayList<Marker>()
        val detailUrl = serviceMDL?.detailurl
        for (item in list) {
            if(item.detailurl == detailUrl) continue
            val option = createOptions(LatLng(item.latitude(), item.longitude()),
                    MapDataType.SERVICE_AREA.name, MapDataType.SERVICE_AREA.name,
                    BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_service_icon))
            markers.add(aMap.addMarker(option).apply { `object` = item })
        }
        serviceMarkers.addAll(markers)
    }

    //景点类型数据
    private fun updateScenic(list: MutableList<ScenicMDL>) {
        val markers = ArrayList<Marker>()
        val detailUrl = scenicMDL?.detailurl
        for (item in list) {
            if(item.detailurl == detailUrl) continue
            val option = createOptions(LatLng(item.latitude(), item.longitude()),
                    MapDataType.SCENIC.name, MapDataType.SCENIC.name,
                    BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_secnic_icon))
            markers.add(aMap.addMarker(option).apply { `object` = item })
        }
        scenicMarkers.addAll(markers)
    }

    private fun createOptions(latLng: LatLng, title: String?, snippet: String?, bitmap: BitmapDescriptor?): MarkerOptions? {
        return MarkerOptions().anchor(0.5f, 1f)
                .position(latLng)
                .title(title)
                .snippet(snippet)
                .visible(true)
                .infoWindowEnable(false)
                .draggable(false)
                .icon(bitmap)
    }

    //还原上次点击的marker
    private fun restoreMarker(marker: Marker) {
        when {
            marker.`object` is TollGateMDL -> marker.setIcon(BitmapDescriptorFactory.fromResource((marker.`object` as TollGateMDL).markerIcon))
            marker.`object` is ServiceMDL -> marker.setIcon(BitmapDescriptorFactory.fromResource((marker.`object` as ServiceMDL).markerIcon))
            marker.`object` is ScenicMDL -> marker.setIcon(BitmapDescriptorFactory.fromResource((marker.`object` as ScenicMDL).markerIcon))
        }
    }

    //放大点击的marker
    private fun enlargeMarkerIcon(marker: Marker) {
        when {
            marker.`object` is ScenicMDL -> {
                val mdl = marker.`object` as ScenicMDL
                marker.setIcon(BitmapDescriptorFactory.fromResource(mdl.markerBigIco))
                val dialog = ScenicDialog(this@MyNearByActivity, mdl)
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
                val dialog = ServiceAreaDialog(this@MyNearByActivity, mdl)
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
                val dialog = TollGateDialog(this@MyNearByActivity, mdl)
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

    override fun onResume() {
        mapView.onResume()
        if (hasLocationPermissions() && location == null) openLocation()
        super.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && llOption.visibility != View.GONE) {
            hideOptions()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }
}