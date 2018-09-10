package com.uroad.zhgs.activity

import android.os.Bundle
import android.support.v4.util.ArrayMap
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.fragment.ServiceAreaFragment
import com.uroad.zhgs.fragment.ServiceFragment
import com.uroad.zhgs.model.ServiceMDL
import com.uroad.zhgs.utils.InputMethodUtils
import kotlinx.android.synthetic.main.activity_service_area.*

/**
 *Created by MFB on 2018/8/21.
 */
class ServiceAreaActivity : BaseActivity() {
    private lateinit var aMap: AMap
    private var keyword = ""
    private val arrayMap = ArrayMap<Int, ArrayList<Marker>>()

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_service_area)
        ivBack.setOnClickListener { onBackPressed() }
        mapView.onCreate(savedInstanceState)
        initMapView()
        initSearch()
        setTab(1)
    }

    private fun initMapView() {
        aMap = mapView.map
        moveCamera()
//        aMap.setOnMarkerClickListener { marker ->
//            oldMarker?.let { restoreMarker(it) }
//            enlargeMarkerIcon(marker)
//            return@setOnMarkerClickListener true
//        }
    }

    private fun moveCamera() {
        aMap.apply {
            //移动到浙江省 120.226989,30.283935 30.3, 120.2
            animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(CurrApplication.APP_LATLNG, this.cameraPosition.zoom, 0f, 0f)))
            maxZoomLevel = 10f
        }
    }

    private fun initSearch() {
        etContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                if (editable.toString().isEmpty()) {
                    setTab(1)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
        etContent.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val content = etContent.text.toString()
                if (TextUtils.isEmpty(content)) {
                    showShortToast("请输入关键字")
                } else {
                    InputMethodUtils.hideSoftInput(this@ServiceAreaActivity, etContent)
                    keyword = content
                    setTab(2)
                }
                return@OnEditorActionListener true
            }
            return@OnEditorActionListener false
        })
    }

    private fun setTab(tab: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        if (tab == 1) {
            val fragment = ServiceAreaFragment()
            fragment.setOnItemOpenCloseListener(object : ServiceAreaFragment.OnItemOpenCloseListener {
                override fun onItemOpenClose(position: Int, serviceList: MutableList<ServiceMDL>, isOpen: Boolean) {
                    if (isOpen) {
                        addMarkers(position, serviceList)
                    } else {
                        removeMarkers(position)
                        moveCamera()
                    }
                }
            })
            transaction.replace(R.id.container, fragment)
        } else {
            for ((k, _) in arrayMap) removeMarkers(k)
            val fragment = ServiceFragment().apply { arguments = Bundle().apply { putString("keyword", keyword) } }
            fragment.setOnDataSetChangedListener(object : ServiceFragment.OnDataSetChangedListener {
                override fun dataSetChanged(mdls: MutableList<ServiceMDL>) {
                    addMarkers(0, mdls)
                }
            })
            transaction.replace(R.id.container, fragment)
        }
        transaction.commit()
    }

    private fun addMarkers(position: Int, serviceList: MutableList<ServiceMDL>) {
        val markers = ArrayList<Marker>()
        for (i in 0 until serviceList.size) {
            serviceList[i].markerBigIco = R.mipmap.ic_marker_service_big_icon
            val option = MarkerOptions().anchor(0.5f, 1f)
                    .position(LatLng(serviceList[i].latitude(), serviceList[i].longitude()))
                    .title(serviceList[i].name)
                    .visible(true)
                    .autoOverturnInfoWindow(true)
                    .infoWindowEnable(true)
                    .draggable(false)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_service_icon))
            markers.add(aMap.addMarker(option).apply { `object` = serviceList[i] })
            if (i == 0) aMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(LatLng(serviceList[i].latitude(),
                    serviceList[i].longitude()), aMap.cameraPosition.zoom, 0f, 0f)))
        }
        arrayMap[position] = markers
    }

    private fun removeMarkers(position: Int) {
        arrayMap[position]?.let {
            for (marker in it) {
                marker.remove() //移除当前Marker
            }
            aMap.reloadMap() //刷新地图
        }
    }

    //还原上次点击的marker
//    private fun restoreMarker(marker: Marker) {
//        marker.setIcon(BitmapDescriptorFactory.fromResource((marker.`object` as ServiceMDL).markerIcon))
//    }

    //放大点击的marker
//    private fun enlargeMarkerIcon(marker: Marker) {
//        val mdl = marker.`object` as ServiceMDL
//        marker.setIcon(BitmapDescriptorFactory.fromResource(mdl.markerBigIco))
//        val dialog = ServiceAreaDialog(this, mdl)
//        dialog.setOnButtonClickListener(object : ServiceAreaDialog.OnButtonClickListener {
//            override fun onDetail(dataMDL: ServiceMDL) {
//                openLocationWebActivity(dataMDL.detailurl, dataMDL.name)
//                dialog.dismiss()
//            }
//
//            override fun onNavigation(dataMDL: ServiceMDL) {
//                var poiName = ""
//                dataMDL.name?.let { poiName = it }
//                val end = Poi(poiName, LatLng(dataMDL.latitude(), dataMDL.longitude()), "")
//                openNaviPage(null, end)
//                dialog.dismiss()
//            }
//        })
//        dialog.show()
//        dialog.setOnDismissListener { restoreMarker(marker) }
//    }

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