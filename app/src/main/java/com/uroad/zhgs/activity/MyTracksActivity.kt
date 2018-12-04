package com.uroad.zhgs.activity

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.amap.api.location.AMapLocation
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseLocationActivity
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.model.FootprintMDL
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_mytracks.*

/*我的足迹*/
class MyTracksActivity : BaseLocationActivity() {
    private lateinit var aMap: AMap
    private lateinit var handler: Handler
    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_mytracks)
        customToolbar.setNavigationOnClickListener { onBackPressed() }
        requestWindowFullScreen()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            llToolbarLayout.layoutParams = (llToolbarLayout.layoutParams as FrameLayout.LayoutParams).apply { topMargin = DisplayUtils.getStatusHeight(this@MyTracksActivity) }
        mapView.onCreate(savedInstanceState)
        initMapView()
        applyLocationPermission(false)
        handler = Handler(Looper.getMainLooper())
    }

    private fun initMapView() {
        aMap = mapView.map.apply { moveCamera(CameraUpdateFactory.newLatLngZoom(CurrApplication.APP_LATLNG, this.cameraPosition.zoom)) }
        val mapStylePath = CurrApplication.MAP_STYLE_PATH
        aMap.isTrafficEnabled = false
        aMap.isMyLocationEnabled = false
        aMap.setCustomMapStylePath(mapStylePath)
        aMap.setMapCustomEnable(true)
        aMap.setInfoWindowAdapter(object : AMap.InfoWindowAdapter {
            override fun getInfoContents(marker: Marker?): View? = null

            override fun getInfoWindow(marker: Marker?): View {
                val view = LayoutInflater.from(this@MyTracksActivity).inflate(R.layout.mapview_tracks, LinearLayout(this@MyTracksActivity), false)
                val tvCity = view.findViewById<TextView>(R.id.tvCity)
                val tvTracks = view.findViewById<TextView>(R.id.tvTracks)
                tvCity.text = marker?.title
                tvTracks.text = marker?.snippet
                return view
            }
        })
        aMap.setOnMarkerClickListener {
            it.showInfoWindow()
            return@setOnMarkerClickListener true
        }
    }

    override fun afterLocation(location: AMapLocation) {
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), aMap.cameraPosition.zoom))
        closeLocation()
    }

    override fun initData() {
        doRequest(WebApiService.MY_FOOTPRINT, WebApiService.getMyFootprintParams(getUserId()), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, FootprintMDL::class.java) ?: return
                    updateUI(mdl)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                    handler.postDelayed({ initData() }, 3000)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                onHttpError(e)
                handler.postDelayed({ initData() }, 3000)
            }
        })
    }

    private fun updateUI(mdl: FootprintMDL) {
        var total = mdl.num?.total_footprint
        if (total == null) total = 0
        if (total == 0) showTipsDialog(getString(R.string.dialog_default_title), getString(R.string.empty_my_tracks), getString(R.string.i_got_it))
        val source1 = "$total\n足迹点"
        val span = AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.font_30), false)
        tvTrackPoints.text = SpannableString(source1).apply {
            setSpan(span, 0, source1.indexOf("足"), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(ContextCompat.getColor(this@MyTracksActivity, R.color.white_trans30)), source1.indexOf("足"), source1.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        var province = mdl.num?.province_footprint
        if (province == null) province = 0
        val source2 = "$province\n省份"
        tvProvince.text = SpannableString(source2).apply {
            setSpan(span, 0, source2.indexOf("省"), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(ContextCompat.getColor(this@MyTracksActivity, R.color.white_trans30)), source2.indexOf("省"), source2.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        var city = mdl.num?.city_footprint
        if (city == null) city = 0
        val source3 = "$city\n城市"
        tvCity.text = SpannableString(source3).apply {
            setSpan(span, 0, source3.indexOf("城"), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(ContextCompat.getColor(this@MyTracksActivity, R.color.white_trans30)), source3.indexOf("城"), source3.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        mdl.city?.let { for (item in it) aMap.addMarker(createOptions(item.getLatLng(), item.district, "${item.footprint_num}个足迹")) }
    }

    private fun createOptions(latLng: LatLng, title: String?, snippet: String?) = MarkerOptions()
            .anchor(0.5f, 0.5f)
            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_tracks_point))
            .position(latLng)
            .title(title)
            .snippet(snippet)
            .autoOverturnInfoWindow(true)
            .infoWindowEnable(true)
            .draggable(false)

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onResume() {
        mapView.onResume()
        super.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }
}