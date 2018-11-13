package com.uroad.zhgs.activity

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.PopupWindowCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.amap.api.col.sln3.it
import com.amap.api.col.sln3.pw
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiSearch
import com.uroad.amaplib.driveroute.util.AMapUtil
import com.uroad.library.utils.DisplayUtils
import com.uroad.mqtt.IMqttCallBack
import com.uroad.mqtt.MqttService
import com.uroad.zhgs.R
import com.uroad.zhgs.R.id.mapView
import com.uroad.zhgs.adapteRv.AMapPoiAdapter
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.common.ThemeStyleActivity
import com.uroad.zhgs.dialog.MaterialDialog
import com.uroad.zhgs.model.CarTeamResultMDL
import com.uroad.zhgs.model.mqtt.TeamPlaceUpdateMDL
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.utils.InputMethodUtils
import com.uroad.zhgs.utils.PopupWindowUtils
import com.uroad.zhgs.webservice.ApiService
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_edit_riders.*
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken

/**
 * @author MFB
 * @create 2018/10/17
 * @describe 创建车队页面
 */
class RidersEditActivity : ThemeStyleActivity() {
    private lateinit var aMap: AMap
    private var isModify = false //是否是修改
    private var popupWindow: PopupWindow? = null
    private var disposable: Disposable? = null
    private var isOnTextSelected = false
    private var latLng: LatLng? = null
    private var marker: Marker? = null
    private var teamId: String? = null
    private var mqttService: MqttService? = null

    override fun themeSetUp(savedInstanceState: Bundle?) {
        setLayoutResID(R.layout.activity_edit_riders)
        mapView.onCreate(savedInstanceState)
        initMapView()
        intent.extras?.let { isModify = it.getBoolean("isModify", false) }
        if (isModify) {
            setThemeTitle(getString(R.string.modify_riders_team))
            teamId = intent.extras?.getString("teamId")
            latLng = intent.extras?.getParcelable("latLng")
            latLng?.let { updateAMap(it) }
            etContent.setText(intent.extras?.getString("teamName"))
            etContent.setSelection(etContent.text.length)
            etDestination.setText(intent.extras?.getString("destination"))
            etDestination.setSelection(etDestination.text.length)
            initMqtt()
        } else {
            setThemeTitle(getString(R.string.create_riders_team))
        }
        setThemeOption(getString(R.string.create_riders_confirm), View.OnClickListener { onConfirm() })
        initDestination()
//        applyLocation()
    }

    private fun onConfirm() {
        val latLng = this@RidersEditActivity.latLng
        when {
            TextUtils.isEmpty(etContent.text.toString().trim()) -> showShortToast(etContent.hint)
            latLng == null -> if (TextUtils.isEmpty(etDestination.text.toString())) showShortToast(etDestination.hint)
            else showShortToast("请输入正确的目的地")
            else -> {
                val teamName = etContent.text.toString()
                val toPlace = etDestination.text.toString()
                val longitude = latLng.longitude
                val latitude = latLng.latitude
                InputMethodUtils.hideSoftInput(this)
                if (!isModify)
                    createCarTeam(teamName, toPlace, longitude, latitude)
                else modifyCarTeam(teamName, toPlace, longitude, latitude)
            }
        }
    }

    /*初始化地图，定位到浙江*/
    private fun initMapView() {
        aMap = mapView.map.apply { moveCamera(CameraUpdateFactory.newLatLngZoom(CurrApplication.APP_LATLNG, this.cameraPosition.zoom)) }
    }

    /*修改车队需要创建mqtt连接*/
    private fun initMqtt() {
        mqttService = ApiService.buildMQTTService(this).apply {
            connect(object : IMqttCallBack {
                override fun messageArrived(topic: String?, message: String?, qos: Int) {
                }

                override fun connectionLost(throwable: Throwable?) {
                    endLoading()
                    throwable?.let { onHttpError(it) }
                }

                override fun deliveryComplete(deliveryToken: IMqttDeliveryToken?) {
                    endLoading()
                    modifySuccess()
                }

                override fun connectSuccess(token: IMqttToken?) {
                }

                override fun connectFailed(token: IMqttToken?, throwable: Throwable?) {
                }
            })
        }
    }

    /*发送修改车队的mq完成*/
    private fun modifySuccess() {
        val dialog = MaterialDialog(this)
        dialog.setTitle(getString(R.string.dialog_default_title))
        dialog.setMessage("车队信息修改完成")
        dialog.hideDivider()
        dialog.setPositiveButton(getString(R.string.dialog_button_confirm), object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                dialog.dismiss()
            }
        })
        dialog.show()
        dialog.setOnDismissListener { finish() }
    }

    private fun initDestination() {
        etDestination.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {
                val content = s.toString().trim()
                if (!isOnTextSelected) latLng = null
                if (!TextUtils.isEmpty(content)) {
                    if (isOnTextSelected) {
                        isOnTextSelected = false
                    } else {
                        popupWindow?.dismiss()
                        disposable?.dispose()
                        doPoiSearch(content)
                    }
                }
            }

            override fun afterTextChanged(editable: Editable?) {

            }
        })
    }

    /*调用高德API进行POI搜索*/
    private fun doPoiSearch(content: String) {
        val query = PoiSearch.Query(content, "", "")
        val poiSearch = PoiSearch(this, query)
        disposable = Flowable.fromCallable { poiSearch.searchPOI() }
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ poiResult -> poiResult?.pois?.let { showPopupWindow(it) } }, {})
        addDisposable(disposable)
    }

    private fun showPopupWindow(items: ArrayList<PoiItem>) {
        if (items.size == 0) return
        val recyclerView = RecyclerView(this).apply {
            setBackgroundColor(ContextCompat.getColor(this@RidersEditActivity, R.color.white))
            layoutManager = LinearLayoutManager(this@RidersEditActivity).apply { orientation = LinearLayoutManager.VERTICAL }
        }
        recyclerView.adapter = AMapPoiAdapter(this, items).apply {
            setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
                override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                    if (position in 0 until items.size) {
                        isOnTextSelected = true
                        latLng = AMapUtil.convertToLatLng(items[position].latLonPoint)
                        etDestination.setText(items[position].title)
                        etDestination.setSelection(etDestination.text.length)
                        popupWindow?.dismiss()
                        val latLng = AMapUtil.convertToLatLng(items[position].latLonPoint)
                        updateAMap(latLng)
                    }
                }
            })
        }
        popupWindow = PopupWindow(recyclerView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            isFocusable = false
            setBackgroundDrawable(ColorDrawable())
            isOutsideTouchable = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                val location = IntArray(2)
                llTop.getLocationInWindow(location)
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) { // 7.1 版本处理
                    val screenHeight = DisplayUtils.getWindowHeight(this@RidersEditActivity)
                    height = screenHeight - location[1] - llTop.height
                }
                showAtLocation(llTop, Gravity.NO_GRAVITY, location[0], location[1] + llTop.height)
            } else
                PopupWindowCompat.showAsDropDown(this, llTop, 0, 0, Gravity.NO_GRAVITY)
        }
    }

    private fun updateAMap(latLng: LatLng) {
        marker?.let {
            it.remove()
            it.destroy()
        }
        val options = MarkerOptions()
                .anchor(0.5f, 1f)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_riders_target))
                .position(latLng)
                .visible(true)
                .infoWindowEnable(false)
        marker = aMap.addMarker(options)
        aMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(latLng, aMap.cameraPosition.zoom, 0f, 0f)))
    }

    /*发起创建车队请求*/
    private fun createCarTeam(teamName: String, toPlace: String, longitude: Double, latitude: Double) {
        doRequest(WebApiService.CREATE_CAR_TEAM, WebApiService.createCarTeamParams(teamId,
                toPlace, teamName, longitude, latitude, getUserId()), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading("正在创建…")
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, CarTeamResultMDL::class.java)
                    if (mdl == null) onJsonParseError()
                    else {
                        openActivity(RidersDetailActivity::class.java, Bundle().apply { putString("teamId", mdl.teamid) })
                        finish()
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

    /*修改车队*/
    private fun modifyCarTeam(teamName: String, toPlace: String, longitude: Double, latitude: Double) {
        val mdl = TeamPlaceUpdateMDL().apply {
            this.teamid = this@RidersEditActivity.teamId
            this.teamname = teamName
            this.toplace = toPlace
            this.longitude = longitude
            this.latitude = latitude
        }
        showLoading()
        mqttService?.publish("${ApiService.TOPIC_PLACE_UPDATE}$teamId", mdl.obtainMessage())
    }

    override fun onResume() {
        mapView.onResume()
        super.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        InputMethodUtils.hideSoftInput(this)
        popupWindow?.dismiss()
        mapView.onDestroy()
        mqttService?.disconnect()
        super.onDestroy()
    }
}