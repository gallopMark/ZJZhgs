package com.uroad.zhgs.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import com.amap.api.location.AMapLocation
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.uroad.library.utils.PhoneUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseLocationActivity
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.dialog.MaterialDialog
import kotlinx.android.synthetic.main.activity_rescue_main.*
import com.uroad.zhgs.model.LocationMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService


/**
 * Created by MFB on 2018/7/25.
 * Copyright  2018年 浙江综合交通大数据开发有限公司.
 * 说明：救援首页
 */
class RescueMainActivity : BaseLocationActivity(), AMap.OnCameraChangeListener {
    /*高德地图类*/
    private lateinit var amap: AMap
    private var isFirstMove = true
    private var currLocation: AMapLocation? = null
    private var targetPos: LatLng? = null
    private var locationMDL: LocationMDL? = null
    private lateinit var animationDrawable: AnimationDrawable
    private lateinit var handler: Handler

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_rescue_main)
        customTitle.text = getString(R.string.rescue_main_rescue)
        customToolbar.setNavigationOnClickListener { onBackPressed() }
        animationDrawable = ivDiffuse.drawable as AnimationDrawable
        mapView.onCreate(savedInstanceState)
        amap = mapView.map
        amap.apply {
            //移动到浙江省 120.153576,30.287459
            animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(CurrApplication.APP_LATLNG, this.cameraPosition.zoom, 0f, 0f)))
            setOnCameraChangeListener(this@RescueMainActivity)
        }
        initRv()
        applyLocationPermissions()
        handler = Handler(Looper.getMainLooper())
    }

    /*申请位置权限*/
    private fun applyLocationPermissions() {
        requestLocationPermissions(object : RequestLocationPermissionCallback {
            override fun doAfterGrand() {
                btRescueCall.visibility = View.GONE
                openLocation()
            }

            override fun doAfterDenied() {
                btRescueCall.visibility = View.VISIBLE
                showDialog(getString(R.string.rescue_main_without_location_title),
                        getString(R.string.rescue_main_without_location_message),
                        getString(R.string.rescue_main_rescue_call),
                        getString(R.string.gotoSettings)
                        , object : MaterialDialog.ButtonClickListener {
                    override fun onClick(v: View, dialog: AlertDialog) {
                        //电话求助
                        PhoneUtils.call(this@RescueMainActivity, getString(R.string.rescue_default_phone))
                        dialog.dismiss()
                    }
                }, object : MaterialDialog.ButtonClickListener {
                    override fun onClick(v: View, dialog: AlertDialog) {
                        //去开启  用户没有点击“不再提示”按钮
                        dialog.dismiss()
                        applyLocationPermissions()  //回调方法重新申请位置权限
                    }
                })
            }
        })
    }

    override fun onCameraChange(position: CameraPosition) {

    }

    //地图停止移动获取屏幕中心经纬度
    override fun onCameraChangeFinish(position: CameraPosition) {
        if (isFirstMove) {   //地图的第一次移动不做请求（默认定位到杭州也会回调这个方法）
            isFirstMove = false
            return
        }
        position.target?.let {
            targetPos = position.target
            request(it.longitude, it.latitude)
        }
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

    private fun initRv() {
        rvPhone.layoutManager = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.VERTICAL }
        rvPhone.isNestedScrollingEnabled = false
    }

    override fun setListener() {
        ivLocation.setOnClickListener { _ -> currLocation?.let { amap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(LatLng(it.latitude, it.longitude), amap.cameraPosition.zoom, 0f, 0f))) } }
        btTopPostage.setOnClickListener { openActivity(RescueFeeActivity::class.java) }
        btRescueCall.setOnClickListener { PhoneUtils.call(this@RescueMainActivity, getString(R.string.rescue_default_phone)) }
        tvMorePhone.setOnClickListener { openActivity(HighWayHotlineActivity::class.java) }
    }

    override fun onResume() {
        mapView.onResume()
        super.onResume()
        if (currLocation == null && hasLocationPermissions()) openLocation()
    }

    override fun afterLocation(location: AMapLocation) {
        //  amap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(LatLng(location.latitude, location.longitude), amap.cameraPosition.zoom, 0f, 0f)))
        currLocation = location
        request(location.longitude, location.latitude)
        closeLocation()
    }

    override fun onLocationFail(errorInfo: String?) {
        handler.postDelayed({ if (!isFinishing) openLocation() }, 3000)
    }

    private fun request(longitude: Double, latitude: Double) {
        doRequest(WebApiService.LOCATION, WebApiService.locationParams(longitude, latitude), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                startAnim()
                tvCurrLocation.visibility = View.GONE
                llInfo.visibility = View.INVISIBLE
                rlOutLine.visibility = View.GONE
            }

            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, LocationMDL::class.java)
                    mdl?.let { updateLocation(it) }
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {

            }

            override fun onComplete() {
                stopAnim()
            }
        })
    }

    private fun updateLocation(mdl: LocationMDL) {
        this.locationMDL = mdl
        if (mdl.type == 0) { //0 可以救援；1 当前位置不支持救援；2 当前位置不在高速上
            llInfo.visibility = View.VISIBLE
            tvCurrLocation.visibility = View.VISIBLE
            rlOutLine.visibility = View.GONE
            tvHighWayName.text = mdl.shortname
            tvPileNum.text = mdl.mile
            var text = ""
            mdl.shortname?.let { text += "$it\n" }
            mdl.mile?.let { text += it }
            val currPos = text
            tvCurrLocation.text = currPos
            ivQuestion.setOnClickListener { getHelpNews() }
            //电话求助
            btCallHelp.setOnClickListener { PhoneUtils.call(this@RescueMainActivity, getString(R.string.rescue_default_phone)) }
            //自助救援
            btRescue.setOnClickListener { _ ->
                showTipsDialog(getString(R.string.dialog_default_title),
                        "本功能预计年底开放，敬请期待", getString(R.string.i_got_it))
//                openActivityForResult(RescueRequestActivity::class.java, Bundle().apply {
//                    putString("roadid", mdl.roadid)
//                    putString("roadname", mdl.shortname)
//                    putString("mile", mdl.mile)
//                    putString("n_code", mdl.n_code)
//                    targetPos?.let {
//                        putDouble("longitude", it.longitude)
//                        putDouble("latitude", it.latitude)
//                    }
//                }, 1)
            }
        } else {
            llInfo.visibility = View.INVISIBLE
            tvCurrLocation.visibility = View.GONE
            rlOutLine.visibility = View.VISIBLE
            outLine(mdl)
        }
    }

    // 帮助信息页面链接
    private fun getHelpNews() {
        doRequest(WebApiService.HELP_NEWS, HashMap(), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, HelpMDL::class.java)
                    mdl?.newurl?.let { openWebActivity(it, resources.getString(R.string.rescue_main_help)) }
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

    //所在位置不在高速上或不在服务区内
    private fun outLine(mdl: LocationMDL) {
        val text = resources.getString(R.string.rescue_main_outline_service_tips)
        val ss = SpannableString(text).apply {
            val start = text.indexOf("请")
            val end = text.length
            val clickSpan = object : ClickableSpan() {
                override fun onClick(view: View?) {
                    //call
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.isUnderlineText = false
                }
            }
            setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(ContextCompat.getColor(this@RescueMainActivity, R.color.colorOrange)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        tvOutLineTips.text = ss
        val list = ArrayList<LocationMDL.Phone>()
        mdl.phone?.let { list.addAll(it) }
        rvPhone.adapter = PhoneAdapter(this, list)
    }

    private class HelpMDL {
        var newurl: String? = null
    }

    private inner class PhoneAdapter(context: Context, mDatas: MutableList<LocationMDL.Phone>)
        : BaseArrayRecyclerAdapter<LocationMDL.Phone>(context, mDatas) {
        override fun bindView(viewType: Int): Int = R.layout.item_rescue_phone

        override fun onBindHoder(holder: RecyclerHolder, t: LocationMDL.Phone, position: Int) {
            holder.setText(R.id.tvPhoneName, t.phonename)
            holder.setText(R.id.tvPhone, t.phone)
            holder.setOnClickListener(R.id.ivCall, View.OnClickListener { if (!TextUtils.isEmpty(t.phone)) PhoneUtils.call(this@RescueMainActivity, t.phone) })
            if (position == itemCount - 1) holder.setVisibility(R.id.vUnderLine, false)
            else holder.setVisibility(R.id.vUnderLine, true)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            openActivity(RescueSubmissionActivity::class.java, data?.extras)
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        mapView.onDestroy()
        super.onDestroy()
    }
}