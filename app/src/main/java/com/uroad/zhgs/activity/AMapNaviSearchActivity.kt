package com.uroad.zhgs.activity

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.support.v4.content.ContextCompat
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
import com.amap.api.navi.model.AMapCalcRouteResult
import com.amap.api.navi.model.AMapNaviPath
import com.amap.api.navi.model.NaviLatLng
import com.amap.api.navi.model.RouteOverlayOptions
import com.amap.api.navi.view.RouteOverLay
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiSearch
import com.uroad.amaplib.driveroute.util.AMapUtil
import com.uroad.amaplib.navi.simple.SimpleNavigationListener
import com.uroad.amaplib.utils.NaviUtils
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.adapteRv.PoiItemAdapter
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.dialog.MaterialDialog
import com.uroad.zhgs.helper.RouteSearchHelper
import com.uroad.zhgs.model.PoiItemMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.utils.InputMethodUtils
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_maproute_search.*

/**
 *Created by MFB on 2018/8/27.
 *  路径规划和导航
 */
class AMapNaviSearchActivity : BaseActivity() {

    private inner class HistoryAdapter(private val context: Context, mDatas: MutableList<String>)
        : BaseArrayRecyclerAdapter<String>(context, mDatas) {
        override fun bindView(viewType: Int): Int = R.layout.item_search_history

        override fun onBindHoder(holder: RecyclerHolder, t: String, position: Int) {
            holder.setText(R.id.tvContent, RouteSearchHelper.content(t))
            holder.setOnClickListener(R.id.ivDelete, View.OnClickListener {
                RouteSearchHelper.deleteItem(context, t)
                mDatas.remove(t)
                notifyDataSetChanged()
                if (mDatas.size == 0) llHirstoryData.visibility = View.GONE
            })
            holder.itemView.setOnClickListener {
                etMyLocation.setText(RouteSearchHelper.getStartPos(t))
                etMyLocation.setSelection(etMyLocation.text.length)
                etEndPos.setText(RouteSearchHelper.getEndPos(t))
                etEndPos.setSelection(etEndPos.text.length)
                val startPoint = RouteSearchHelper.getStartPoint(t)
                val endPoint = RouteSearchHelper.getEndPoint(t)
                if (startPoint != null && endPoint != null) {
                    llHirstoryData.visibility = View.GONE
                    doRouteSearch(startPoint, endPoint)
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
    private var isOnItemClick1 = false
    private var isOnItemClick2 = false
    private var isFirstSetText = true
    private var startPoint: LatLonPoint? = null
    private var endPoint: LatLonPoint? = null
    private var disposable: Disposable? = null
    private var popupWindow: PopupWindow? = null
    private lateinit var mAMapNavi: AMapNavi
    private val routeOverLayList = ArrayList<RouteOverLay>()
    private val naviPaths = ArrayList<AMapNaviPath>()
    private var naviPath: AMapNaviPath? = null  //选中的路线
    private var routeIds: IntArray? = null
    private var selectRouteId: Int? = null

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_maproute_search)
        ivBack.setOnClickListener { onBackPressed() }
        initHistoryRv()
        mapView.onCreate(savedInstanceState)
        initMapView()
        initSearchView()
        llNavigation.setOnClickListener { _ ->
            naviPath?.let { path ->
                openActivity(RouteNaviActivity::class.java, Bundle().apply {
                    putParcelable("start", path.startPoint)
                    putParcelable("end", path.endPoint)
                    //  mAMapNavi.selectRouteId(selectRouteId)
                    selectRouteId?.let { putInt("selectRouteId", it) }
                    //   putInt("strategy", path.strategy)
                })
            }
        }
        handler = Handler(Looper.getMainLooper())
        applyLocationPermission(false)
    }

    private fun initHistoryRv() {
        rvHistory.layoutManager = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.VERTICAL }
        data = RouteSearchHelper.getHistoryList(this)
        historyAdapter = HistoryAdapter(this, data)
        rvHistory.adapter = historyAdapter
        if (data.size > 0) {
            llHirstoryData.visibility = View.VISIBLE
        } else {
            llHirstoryData.visibility = View.GONE
        }
        tvClear.setOnClickListener {
            RouteSearchHelper.clear(this@AMapNaviSearchActivity)
            data.clear()
            historyAdapter.notifyDataSetChanged()
            llHirstoryData.visibility = View.GONE
        }
    }

    private fun initMapView() {
        aMap = mapView.map.apply {
            //移动到浙江省
            animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(CurrApplication.APP_LATLNG, this.cameraPosition.zoom, 0f, 0f)))
            //   setOnCameraChangeListener(this@AMapNaviSearchActivity)
        }
        mAMapNavi = AMapNavi.getInstance(applicationContext)
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
                    llBottom.visibility = View.GONE
                    showShortToast("暂无路径规划")
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
                naviPath = naviPaths[position]
            }
        }
        routeIds?.let {
            if (position in 0 until it.size) {
                selectRouteId = it[position].apply { mAMapNavi.selectRouteId(this) }
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
                    if (isFirstSetText) {
                        isFirstSetText = false
                        return
                    }
                    if (isOnItemClick1) {
                        isOnItemClick1 = false
                    } else {
                        disposable?.dispose()
                        popupWindow?.dismiss()
                        doPoiSearch(content, 1)
                    }
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
                if (isOnItemClick2) {
                    isOnItemClick2 = false
                } else {
                    disposable?.dispose()
                    popupWindow?.dismiss()
                    doPoiSearch(content, 2)
                }
            }

            override fun afterTextChanged(editable: Editable) {
            }
        })
        ivChange.setOnClickListener { _ ->
            val temp = startPoint
            startPoint = endPoint
            endPoint = temp
            val tempText = etMyLocation.text
            etMyLocation.text = etEndPos.text
            etEndPos.text = tempText
            etMyLocation.setSelection(etMyLocation.text.length)
            etEndPos.setSelection(etEndPos.text.length)
            startPoint?.let { start -> endPoint?.let { doRouteSearch(start, it) } }
        }
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

    private fun doPoiSearch(keyWord: String, type: Int) {
        val query = PoiSearch.Query(keyWord, "", "")
        val poiSearch = PoiSearch(this, query)
        disposable = Flowable.fromCallable { poiSearch.searchPOI() }
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ poiResult -> poiResult?.pois?.let { showPopupWindow(it, type) } }, {})
        addDisposable(disposable)
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // 获取控件的位置，安卓系统>7.0
                val location = IntArray(2)
                llTop.getLocationOnScreen(location)
                showAtLocation(llTop, Gravity.NO_GRAVITY, 0, location[1] + llTop.height)
            } else
                showAsDropDown(llTop)
        }
        val adapter = PoiItemAdapter(this, items)
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                if (position in 0 until items.size) {
                    if (type == 1) {
                        startPoint = items[position].latLonPoint
                        isOnItemClick1 = true
                        etMyLocation.setText(items[position].title)
                        etMyLocation.setSelection(etMyLocation.text.length)
                        startPoint?.let { start -> endPoint?.let { doRouteSearch(start, it) } }
                    } else {
                        endPoint = items[position].latLonPoint
                        isOnItemClick2 = true
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
        if (llHirstoryData.visibility != View.GONE) llHirstoryData.visibility = View.GONE
        RouteSearchHelper.saveContent(this, etMyLocation.text.toString(), startPoint, etEndPos.text.toString(), endPoint)
        disposable?.dispose()
        popupWindow?.dismiss()
        val start = ArrayList<NaviLatLng>().apply { add(NaviLatLng(startPoint.latitude, startPoint.longitude)) }
        val end = ArrayList<NaviLatLng>().apply { add(NaviLatLng(endPoint.latitude, endPoint.longitude)) }
        InputMethodUtils.hideSoftInput(this)
        showLoading()
        mAMapNavi.calculateDriveRoute(start, end, null, PathPlanningStrategy.DRIVING_MULTIPLE_ROUTES_DEFAULT)
    }

    private fun updateRvPaths() {
        llBottom.visibility = View.VISIBLE
        recyclerView.layoutManager = GridLayoutManager(this, naviPaths.size)
        recyclerView.adapter = NaviPathAdapter(this, naviPaths)
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
        popupWindow?.dismiss()
        mAMapNavi.destroy()
        mapView.onDestroy()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}