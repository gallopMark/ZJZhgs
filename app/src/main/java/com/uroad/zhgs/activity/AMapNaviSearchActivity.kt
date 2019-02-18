package com.uroad.zhgs.activity

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.support.v4.content.ContextCompat
import android.support.v4.widget.PopupWindowCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.amap.api.location.AMapLocation
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.navi.AMapNavi
import com.amap.api.navi.enums.PathPlanningStrategy
import com.amap.api.navi.model.*
import com.amap.api.navi.view.RouteOverLay
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiSearch
import com.uroad.amaplib.utils.AMapUtil
import com.uroad.amaplib.navi.simple.SimpleNavigationListener
import com.uroad.amaplib.utils.NaviUtils
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.adapteRv.PoiItemAdapter
import com.uroad.zhgs.common.BaseLocationActivity
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.dialog.WheelViewDialog
import com.uroad.zhgs.enumeration.Carcategory
import com.uroad.zhgs.helper.RouteSearchHelper
import com.uroad.zhgs.model.CarDetailMDL
import com.uroad.zhgs.model.CarMDL
import com.uroad.zhgs.model.PoiItemMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.utils.InputMethodUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_maproute_search.*
import kotlinx.android.synthetic.main.content_emptytruck.*

/**
 *Created by MFB on 2018/8/27.
 *  路径规划和导航
 */
class AMapNaviSearchActivity : BaseLocationActivity() {

    private inner class HistoryAdapter(private val context: Context, mDatas: MutableList<String>)
        : BaseArrayRecyclerAdapter<String>(context, mDatas) {
        override fun bindView(viewType: Int): Int = R.layout.item_search_history

        override fun onBindHoder(holder: RecyclerHolder, t: String, position: Int) {
            holder.setText(R.id.tvContent, RouteSearchHelper.content(t))
            holder.setOnClickListener(R.id.ivDelete, View.OnClickListener {
                RouteSearchHelper.deleteItem(context, t)
                mDatas.remove(t)
                notifyDataSetChanged()
                if (mDatas.size == 0) llHistoryData.visibility = View.GONE
            })
            holder.itemView.setOnClickListener {
                isStartSetText = true
                isEndSetText = true
                etMyLocation.setText(RouteSearchHelper.getStartPos(t))
                etMyLocation.setSelection(etMyLocation.text.length)
                etEndPos.setText(RouteSearchHelper.getEndPos(t))
                etEndPos.setSelection(etEndPos.text.length)
                startPoint = RouteSearchHelper.getStartPoint(t)
                endPoint = RouteSearchHelper.getEndPoint(t)
                if (startPoint != null && endPoint != null) {
                    llHistoryData.visibility = View.GONE
                    startPoint?.let { start -> endPoint?.let { end -> doRouteSearch(start, end) } }
                }
            }
        }
    }

    private lateinit var data: MutableList<String>
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var aMap: AMap
    private var currLocation: AMapLocation? = null
    private var isLocationComplete = false
    private lateinit var handler: Handler
    private var isStartSetText = false
    private var isEndSetText = false
    private var isFirstSetText = true
    private var startPoint: LatLonPoint? = null
    private var endPoint: LatLonPoint? = null
    private var startKey: String? = ""
    private var endKey: String? = ""
    private var startDisposable: Disposable? = null
    private var endDisposable: Disposable? = null
    private var popupWindow: PopupWindow? = null
    private lateinit var mAMapNavi: AMapNavi
    private val routeOverLayList = ArrayList<RouteOverLay>()
    private val naviPaths = ArrayList<AMapNaviPath>()
    private var routeIds: IntArray? = null
    private var selectRouteId: Int? = null
    private var naviType: Int = 1
    private var isGetTrucks = false
    private val trucks = ArrayList<CarMDL>()
    private var truckDetail: CarDetailMDL? = null

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_maproute_search)
        ivBack.setOnClickListener { onBackPressed() }
        initHistoryRv()
        mapView.onCreate(savedInstanceState)
        initMapView()
        initSearchView()
        initTvNavi()
        llNavigation.setOnClickListener { selectRouteId?.let { routeId -> openActivity(RouteNaviActivity::class.java, Bundle().apply { putInt("selectRouteId", routeId) }) } }
        handler = Handler(Looper.getMainLooper())
        applyLocationPermission(false)
    }

    private fun initHistoryRv() {
        rvHistory.layoutManager = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.VERTICAL }
        data = RouteSearchHelper.getHistoryList(this)
        historyAdapter = HistoryAdapter(this, data)
        rvHistory.adapter = historyAdapter
        if (data.size > 0) {
            llHistoryData.visibility = View.VISIBLE
            tvEmptyHis.visibility = View.GONE
        } else {
            llHistoryData.visibility = View.GONE
            tvEmptyHis.visibility = View.VISIBLE
        }
        tvClear.setOnClickListener {
            RouteSearchHelper.clear(this@AMapNaviSearchActivity)
            data.clear()
            historyAdapter.notifyDataSetChanged()
            llHistoryData.visibility = View.GONE
            tvEmptyHis.visibility = View.VISIBLE
        }
    }

    private fun initMapView() {
        aMap = mapView.map.apply {
            //移动到浙江省
            animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(CurrApplication.APP_LATLNG, this.cameraPosition.zoom, 0f, 0f)))
            //   setOnCameraChangeListener(this@AMapNaviSearchActivity)
        }
        mAMapNavi = AMapNavi.getInstance(this)
        mAMapNavi.setConnectionTimeout(15 * 1000)
        mAMapNavi.addAMapNaviListener(object : SimpleNavigationListener() {

            override fun onCalculateRouteSuccess(result: AMapCalcRouteResult?) {
                endLoading()
                val routeIds = result?.routeid
                naviPaths.clear()
                if (routeIds != null && routeIds.isNotEmpty()) {
                    this@AMapNaviSearchActivity.routeIds = routeIds
                    for (i in 0 until routeIds.size) {
                        val routeId = routeIds[i]
                        val path = mAMapNavi.naviPaths[routeId]
                        path?.let { naviPaths.add(path) }
                    }
                    updatePaths(0)
                    updateRvPaths()
                } else {
                    showShortToast("暂无路径规划")
                    llNavigationLayout.visibility = View.GONE
                }
            }

            override fun onCalculateRouteFailure(result: AMapCalcRouteResult?) {
                endLoading()
                result?.let { showShortToast(NaviUtils.getError(it.errorCode)) }
            }
        })
    }

    /**
     * 	setJamTraffic(Bitmap jamTraffic)
    设置交通状况拥堵下的纹理位图
    void	setLineWidth(float lineWidth)
    设置导航线路的宽度
    void	setNormalRoute(Bitmap normalRoute)
    设置路线的图标
    void	setOnRouteCameShow(boolean routeCameShow)
    设置路线上的摄像头气泡是否显示。
    void	setPassRoute(Bitmap passRoute)
    自定义走过路线纹理，默认走过路线置灰功能为关，需要在AMapNaviViewOptions.setAfterRouteAutoGray(boolean)打开，该方法才生效
    void	setRect(Rect rect)
    设置全览状态下，路线距离四周的边距
    void	setSlowTraffic(Bitmap slowTraffic)
    设置交通状况迟缓下的纹理位图
    void	setSmoothTraffic(Bitmap smoothTraffic)
    设置交通状况情况良好下的纹理位图
    void	setUnknownTraffic(Bitmap unknownTraffic)
    设置交通状况未知下的纹理位图
    void	setVeryJamTraffic(Bitmap veryJamTraffic)
    设置交通状况非常拥堵下的纹理位图
     */
    private fun updatePaths(position: Int) {
        for (item in routeOverLayList) {
            item.removeFromMap()
            item.destroy()
        }
        var zIndex = 0
        if (naviPaths.size > 0) {
            for (i in 0 until naviPaths.size) {
                if (i != position) {
                    val options = RouteOverlayOptions().apply {
                        normalRoute = BitmapFactory.decodeResource(resources, R.mipmap.custtexture_green_n)
                        jamTraffic = BitmapFactory.decodeResource(resources, R.mipmap.custtexture_bad_n)
                        veryJamTraffic = BitmapFactory.decodeResource(resources, R.mipmap.custtexture_bad_n)
                        slowTraffic = BitmapFactory.decodeResource(resources, R.mipmap.custtexture_slow_n)
                        smoothTraffic = BitmapFactory.decodeResource(resources, R.mipmap.custtexture_green_n)
                        unknownTraffic = BitmapFactory.decodeResource(resources, R.mipmap.custtexture_green_n)
                        lineWidth = DisplayUtils.dip2px(this@AMapNaviSearchActivity, 15f).toFloat()
                    }
                    zIndex++
                    addRouteOverLay(naviPaths[i], options, zIndex)
                }
            }
            if (position in 0 until naviPaths.size) {
                zIndex++
                addRouteOverLay(naviPaths[position], RouteOverlayOptions().apply {
                    normalRoute = BitmapFactory.decodeResource(resources, R.mipmap.custtexture_green)
                    jamTraffic = BitmapFactory.decodeResource(resources, R.mipmap.custtexture_bad)
                    veryJamTraffic = BitmapFactory.decodeResource(resources, R.mipmap.custtexture_bad)
                    slowTraffic = BitmapFactory.decodeResource(resources, R.mipmap.custtexture_slow)
                    smoothTraffic = BitmapFactory.decodeResource(resources, R.mipmap.custtexture_green)
                    unknownTraffic = BitmapFactory.decodeResource(resources, R.mipmap.custtexture_green)
                    lineWidth = DisplayUtils.dip2px(this@AMapNaviSearchActivity, 15f).toFloat()
                }, zIndex)
            }
        }
        routeIds?.let {
            if (position in 0 until it.size) {
                selectRouteId = it[position]
            }
        }
    }

    private fun addRouteOverLay(path: AMapNaviPath, options: RouteOverlayOptions, zIndex: Int) {
        val routeOverLay = RouteOverLay(aMap, path, this)
        routeOverLay.routeOverlayOptions = options
        routeOverLay.setStartPointBitmap(BitmapFactory.decodeResource(resources, R.mipmap.ic_route_start))
        routeOverLay.setEndPointBitmap(BitmapFactory.decodeResource(resources, R.mipmap.ic_route_target))
        routeOverLay.zoomToSpan(DisplayUtils.dip2px(this, 100f))
        routeOverLay.setLightsVisible(false)  //不显示红绿灯
        routeOverLay.setTrafficLightsVisible(false)
        routeOverLay.setArrowOnRoute(false) //隐藏道路箭头
        routeOverLay.setZindex(zIndex)
        routeOverLay.addToMap()
        routeOverLayList.add(routeOverLay)
    }

    private fun initSearchView() {
        etMyLocation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val content = s.toString()
                if (!TextUtils.isEmpty(content.trim())) {
                    when {
                        isFirstSetText -> isFirstSetText = false
                        isStartSetText -> isStartSetText = false
                        else -> {
                            startPoint = null
                            startKey = content
                            handler.removeCallbacks(startPoiRunnable)
                            handler.postDelayed(startPoiRunnable, 500)
                        }
                    }
                } else {
                    startPoint = null
                    handler.removeCallbacks(startPoiRunnable)
                }
            }

            override fun afterTextChanged(editable: Editable) {

            }
        })
        etEndPos.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {
                val content = s.toString()
                if (!TextUtils.isEmpty(content.trim())) {
                    if (isEndSetText) {
                        isEndSetText = false
                    } else {
                        endPoint = null
                        endKey = content
                        handler.removeCallbacks(endPoiRunnable)
                        handler.postDelayed(endPoiRunnable, 500)
                    }
                } else {
                    endPoint = null
                    handler.removeCallbacks(endPoiRunnable)
                }
            }

            override fun afterTextChanged(editable: Editable) {
            }
        })
        ivChange.setOnClickListener {
            isStartSetText = true
            isEndSetText = true
            val temp = startPoint
            startPoint = endPoint
            endPoint = temp
            val tempText = etMyLocation.text
            etMyLocation.text = etEndPos.text
            etEndPos.text = tempText
            etMyLocation.setSelection(etMyLocation.text.length)
            etEndPos.setSelection(etEndPos.text.length)
            startPoint?.let { start -> endPoint?.let { end -> doRouteSearch(start, end) } }
        }
    }

    private fun initTvNavi() {
        tvCoach.isSelected = true
        tvCoach.setOnClickListener {
            truckDetail = null
            tvCoach.isSelected = true
            tvTruck.isSelected = false
            rvCoach.visibility = View.VISIBLE
            flTruck.visibility = View.GONE
            llNavigationLayout.visibility = View.GONE
            naviType = 1
            startPoint?.let { start -> endPoint?.let { end -> doRouteSearch(start, end) } }
        }
        tvTruck.setOnClickListener {
            if (!isLogin()) openActivity(LoginActivity::class.java) else {
                tvCoach.isSelected = false
                tvTruck.isSelected = true
                rvCoach.visibility = View.GONE
                flTruck.visibility = View.VISIBLE
                llNavigationLayout.visibility = View.GONE
                naviType = 2
                if (!isGetTrucks) {
                    getTrucks()
                } else {
                    whenGetTrucks()
                }
            }
        }
    }

    private fun getTrucks() {
        doRequest(WebApiService.MYCAR, WebApiService.myCarParams(getUserId(), Carcategory.TRUCK.code), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
                contentEmptyTruck.visibility = View.GONE
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    isGetTrucks = true
                    val cars = GsonUtils.fromDataToList(data, CarMDL::class.java)
                    trucks.addAll(cars)
                    whenGetTrucks()
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

    private fun whenGetTrucks() {
        if (trucks.size > 0) {
            contentEmptyTruck.visibility = View.GONE
            val data = ArrayList<String?>()
            for (truck in trucks) data.add(truck.carno)
            WheelViewDialog(this).withData(data).withListener(object : WheelViewDialog.OnItemSelectListener {
                override fun onItemSelect(position: Int, text: String?, dialog: WheelViewDialog) {
                    dialog.dismiss()
                    val carMDL = trucks[position]
                    getCarDetail(carMDL)
                }
            }).show()
        } else {
            contentEmptyTruck.visibility = View.VISIBLE
            tvGotoBindCar.setOnClickListener { openActivityForResult(BindCarActivity::class.java, 1) }
        }
    }

    private fun getCarDetail(carMDL: CarMDL) {
        doRequest(WebApiService.CAR_DETAILS, WebApiService.carDetailsParams(carMDL.carid), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, CarDetailMDL::class.java)
                    if (mdl == null) showShortToast("数据解析异常")
                    else {
                        truckDetail = mdl
                        startPoint?.let { start -> endPoint?.let { end -> doRouteSearch(start, end) } }
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

    override fun afterLocation(location: AMapLocation) {
        isLocationComplete = true
        currLocation = location
        etMyLocation.setText(resources.getString(R.string.routeSearch_myLocation))
        etMyLocation.setSelection(etMyLocation.text.length)
        startPoint = LatLonPoint(location.latitude, location.longitude)
        aMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(LatLng(location.latitude, location.longitude), aMap.cameraPosition.zoom, 0f, 0f)))
        closeLocation()
    }

    private val startPoiRunnable = Runnable {
        cancelStartSearch()
        startPoiSearch(startKey)
    }

    private val endPoiRunnable = Runnable {
        cancelEndSearch()
        endPoiSearch(endKey)
    }

    private fun cancelStartSearch() {
        startDisposable?.dispose()
        popupWindow?.let {
            it.dismiss()
            popupWindow = null
        }
    }

    private fun cancelEndSearch() {
        endDisposable?.dispose()
        popupWindow?.let {
            it.dismiss()
            popupWindow = null
        }
    }

    private fun startPoiSearch(keyWord: String?) {
        startDisposable = Observable.fromCallable {
            val query = PoiSearch.Query(keyWord, "", "")
            val poiSearch = PoiSearch(this, query)
            poiSearch.searchPOI()
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ poiResult -> poiResult?.pois?.let { showPopupWindow(it, 1) } }, {})
    }

    private fun endPoiSearch(keyWord: String?) {
        endDisposable = Observable.fromCallable {
            val query = PoiSearch.Query(keyWord, "", "")
            val poiSearch = PoiSearch(this, query)
            poiSearch.searchPOI()
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ poiResult -> poiResult?.pois?.let { showPopupWindow(it, 2) } }, {})
    }

    private fun showPopupWindow(poiItems: ArrayList<PoiItem>, type: Int) {
        if (poiItems.size == 0) return
        if (type == 1) {
            if (TextUtils.isEmpty(etMyLocation.text.toString())) return
        } else {
            if (TextUtils.isEmpty(etEndPos.text.toString())) return
        }
        val items = ArrayList<PoiItemMDL>()
        for (item in poiItems) {
            items.add(PoiItemMDL().apply {
                title = item.title
                snippet = item.snippet
                latLonPoint = item.latLonPoint
                currLocation?.let { setDistance(LatLng(it.latitude, it.longitude), LatLng(item.latLonPoint.latitude, item.latLonPoint.longitude)) }
            })
        }
        val recyclerView = RecyclerView(this).apply {
            setBackgroundColor(ContextCompat.getColor(this@AMapNaviSearchActivity, R.color.white))
            layoutManager = LinearLayoutManager(this@AMapNaviSearchActivity).apply { orientation = LinearLayoutManager.VERTICAL }
        }
        popupWindow = PopupWindow(recyclerView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            isFocusable = false
            setBackgroundDrawable(ColorDrawable())
            isOutsideTouchable = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                val location = IntArray(2)
                llTop.getLocationInWindow(location)
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) { // 7.1 版本处理
                    val screenHeight = DisplayUtils.getWindowHeight(this@AMapNaviSearchActivity)
                    height = screenHeight - location[1] - llTop.height
                }
                showAtLocation(llTop, Gravity.NO_GRAVITY, location[0], location[1] + llTop.height)
            } else
                PopupWindowCompat.showAsDropDown(this, llTop, 0, 0, Gravity.NO_GRAVITY)
        }
        val adapter = PoiItemAdapter(this, items)
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                if (position in 0 until items.size) {
                    if (type == 1) {
                        startPoint = items[position].latLonPoint
                        isStartSetText = true
                        etMyLocation.setText(items[position].title)
                        etMyLocation.setSelection(etMyLocation.text.length)
                        startPoint?.let { start -> endPoint?.let { doRouteSearch(start, it) } }
                    } else {
                        endPoint = items[position].latLonPoint
                        isEndSetText = true
                        etEndPos.setText(items[position].title)
                        etEndPos.setSelection(etEndPos.text.length)
                        endPoint?.let { end -> startPoint?.let { doRouteSearch(it, end) } }
                    }
                }
                popupWindow?.dismiss()
            }
        })
    }

    //定位失败
    override fun onLocationFail(errorInfo: String?) {
        //间隔2秒再重新打开定位
        handler.postDelayed({ if (!isFinishing) openLocation() }, 2000)
    }

    //路径搜索
    private fun doRouteSearch(startPoint: LatLonPoint, endPoint: LatLonPoint) {
        this.startPoint = startPoint
        this.endPoint = endPoint
        if (flHistory.visibility != View.GONE) flHistory.visibility = View.GONE
        RouteSearchHelper.saveContent(this, etMyLocation.text.toString(), startPoint, etEndPos.text.toString(), endPoint)
        val start = ArrayList<NaviLatLng>().apply { add(NaviLatLng(startPoint.latitude, startPoint.longitude)) }
        val end = ArrayList<NaviLatLng>().apply { add(NaviLatLng(endPoint.latitude, endPoint.longitude)) }
        InputMethodUtils.hideSoftInput(this)
        showLoading()
        if (rvCoach.visibility != View.GONE) rvCoach.visibility = View.GONE
        if (flTruck.visibility != View.GONE) flTruck.visibility = View.GONE
        if (llNavigationLayout.visibility != View.GONE) llNavigationLayout.visibility = View.GONE
        if (llBottom.visibility != View.GONE) llBottom.visibility = View.GONE
        val truck = this.truckDetail
        if (truck != null && naviType == 2) {
            mAMapNavi.setCarInfo(AMapCarInfo().apply {
                carType = "1" //设置车辆类型，0小车，1货车
                carNumber = truck.carno //设置车辆的车牌号码. 如:京DFZ239,京ABZ239
                vehicleSize = truck.gdcartype // 设置货车的等级
                vehicleLoad = truck.total //设置货车的总重，单位：吨。
                vehicleWeight = truck.fixedload //设置货车的载重，单位：吨。
                vehicleLength = truck.carlength //  设置货车的最大长度，单位：米。
                vehicleWidth = truck.carwide //设置货车的最大宽度，单位：米。 如:1.8，1.5等等。
                vehicleHeight = truck.carheight //设置货车的高度，单位：米。
                vehicleAxis = truck.gdaxisnum //设置货车的轴数
                isVehicleLoadSwitch = true //设置车辆的载重是否参与算路
                isRestriction = true //设置是否躲避车辆限行。
            })
        } else {
            mAMapNavi.setCarInfo(AMapCarInfo().apply { carType = "0" })
        }
        mAMapNavi.calculateDriveRoute(start, end, null, PathPlanningStrategy.DRIVING_MULTIPLE_ROUTES_DEFAULT)
    }

    private fun updateRvPaths() {
        llBottom.visibility = View.VISIBLE
        if (naviType == 1) {
            rvCoach.visibility = View.VISIBLE
            rvCoach.layoutManager = GridLayoutManager(this, naviPaths.size)
            rvCoach.adapter = NaviPathAdapter(this, naviPaths)
        } else {
            flTruck.visibility = View.VISIBLE
            rvTruck.visibility = View.VISIBLE
            rvTruck.layoutManager = GridLayoutManager(this, naviPaths.size)
            rvTruck.adapter = NaviPathAdapter(this, naviPaths)
        }
        llNavigationLayout.visibility = View.VISIBLE
    }

    private inner class NaviPathAdapter(context: Context, mDatas: MutableList<AMapNaviPath>)
        : BaseArrayRecyclerAdapter<AMapNaviPath>(context, mDatas) {
        private var selected: Int = 0
        override fun bindView(viewType: Int): Int = R.layout.item_maproute

        override fun onBindHoder(holder: RecyclerHolder, t: AMapNaviPath, position: Int) {
            val llItem = holder.obtainView<LinearLayout>(R.id.llItem)
            holder.setText(R.id.tvStrategy, t.labels)
            holder.setText(R.id.tvTime, AMapUtil.getFriendlyTime(t.allTime))
            holder.setText(R.id.tvMile, AMapUtil.getFriendlyLength(t.allLength))
            llItem.isSelected = position == selected
            holder.itemView.setOnClickListener {
                selected = position
                notifyDataSetChanged()
                updatePaths(position)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            isGetTrucks = false
            getTrucks()
        }
    }

    override fun onResume() {
        mapView.onResume()
        mAMapNavi.resumeNavi()
        if (hasLocationPermissions() && !isLocationComplete) openLocation()
        super.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        mAMapNavi.pauseNavi()
        super.onPause()
    }

    override fun onDestroy() {
        InputMethodUtils.hideSoftInput(this)
        cancelStartSearch()
        cancelEndSearch()
        handler.removeCallbacksAndMessages(null)
        mAMapNavi.destroy()
        mapView.onDestroy()
        super.onDestroy()
    }
}