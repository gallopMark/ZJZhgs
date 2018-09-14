package com.uroad.zhgs.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.amap.api.location.AMapLocation
import com.uroad.zhgs.*
import com.uroad.zhgs.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_main.*
import com.amap.api.services.weather.WeatherSearch
import com.amap.api.services.weather.WeatherSearchQuery
import com.amap.api.services.weather.LocalWeatherForecastResult
import com.amap.api.services.weather.LocalWeatherLiveResult
import com.uroad.zhgs.adapteRv.NearByScenicAdapter
import com.uroad.zhgs.adapteRv.NearByServiceAdapter
import com.uroad.zhgs.adapteRv.NearByTollAdapter
import com.uroad.zhgs.adaptervp.UserSubscribePageAdapter
import com.uroad.zhgs.enumeration.MapDataType
import com.uroad.zhgs.model.*
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import java.lang.ref.WeakReference
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import com.uroad.zhgs.activity.*
import com.uroad.zhgs.adapteRv.NewsAdapter
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.rxbus.MessageEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import com.uroad.library.rxbus.RxBus
import io.reactivex.disposables.Disposable


/**
 *Created by MFB on 2018/7/28.
 * Copyright 2018年 浙江综合交通大数据开发有限公司.
 * 说明：app首
 * 18802076493 a123456
 */
class MainFragment : BaseFragment(), View.OnClickListener, WeatherSearch.OnWeatherSearchListener {
    private var isOpenLocation = false   //是否已经打开定位
    private var weatherSearch: WeatherSearch? = null    //高德api天气搜索
    private var latitude = 0.0
    private var longitude = 0.0
    private lateinit var handler: MHandler
    private val tollList = ArrayList<TollGateMDL>()   //附近加油站数据集合
    private lateinit var tollAdapter: NearByTollAdapter     //附近加油站适配器
    private val serviceList = ArrayList<ServiceMDL>()   //附近服务区数据集合
    private lateinit var serviceAdapter: NearByServiceAdapter      //附近服务区适配器
    private val scenicList = ArrayList<ScenicMDL>()     //附近景点数据集合
    private lateinit var scenicAdapter: NearByScenicAdapter //附近景点适配器
    private val newsList = ArrayList<NewsMDL>()     //推荐资讯数据集合
    private lateinit var newsAdapter: NewsAdapter   //资讯列表适配器
    private val subscribeMDLs = ArrayList<SubscribeMDL>()   //我的订阅数据集（已登录状态）
    private lateinit var subscribeAdapter: UserSubscribePageAdapter
    private var disposable: Disposable? = null
    private var isDestroyView = false

    /*数据加载失败，通过handler延迟 重新加载数据*/
    companion object {
        const val CODE_SUBSCRIBE = 0x0001
        const val CODE_TOLL = 0x0002
        const val CODE_SERVICE = 0x0003
        const val CODE_SCENIC = 0x0004
        const val CODE_NEWS = 0x0005
        const val CODE_WEATHER = 0x0006
    }

    override fun setBaseLayoutResID(): Int = R.layout.fragment_main

    override fun setUp(view: View, savedInstanceState: Bundle?) {
        initRfv()
        btNavigation.setOnClickListener { openActivity(RoadNavigationActivity::class.java) }
        btRescue.setOnClickListener {
            if (!isLogin()) openActivity(LoginActivity::class.java)
            else checkRescue()
        }
        initTab()
        initRv()
        /*未申请位置权限，则申请*/
        if (!hasLocationPermissions()) applyLocationPermissions()
        handler = MHandler(this)
        //注册rxBus 接收订阅取消的消息，将我的订阅列表中的相关信息移除
        disposable = RxBus.getDefault().toObservable(MessageEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { event -> onEvent(event) }
    }

    private fun applyLocationPermissions() {
        requestLocationPermissions(object : RequestLocationPermissionCallback {
            override fun doAfterGrand() {
                openLocation()
            }

            override fun doAfterDenied() {
                onLocationFailure()
            }
        })
    }

    private fun onEvent(event: MessageEvent?) {
        event?.obj.let {
            if (it is SubscribeMDL) {
                if (subscribeMDLs.contains(it)) subscribeMDLs.remove(it)
                if (subscribeMDLs.size > 0) {
                    flSubscribe.visibility = View.VISIBLE
                } else {
                    flSubscribe.visibility = View.GONE
                }
            }
        }
    }

    //点击救援先检查是否存在救援工单
    private fun checkRescue() {
        doRequest(WebApiService.CHECK_RESCUE, WebApiService.checkRescueParams(getUserId()), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, CheckRescueMDL::class.java)
                    if (mdl == null) onJsonParseError()
                    else {
                        onRescue(mdl)
                    }
                } else showShortToast(GsonUtils.getMsg(data))
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                endLoading()
                onHttpError(e)
            }
        })
    }

    //判断救援状态
    private fun onRescue(mdl: CheckRescueMDL) {
        when {
            mdl.status == 0 -> openActivity(RescueNoticeActivity::class.java)
            mdl.status == 1 -> //存在未支付救援记录
                openActivity(RescuePayActivity::class.java, Bundle().apply { putString("rescueid", mdl.rescueid) })
            mdl.status == 2 -> //存在进行中救援
                openActivity(RescueDetailActivity::class.java, Bundle().apply { putString("rescueid", mdl.rescueid) })
            else -> openActivity(RescueNoticeActivity::class.java)
        }
    }

    private fun initRfv() {
        refreshLayout.isEnableLoadMore = false
        refreshLayout.setOnRefreshListener {
            if (hasLocationPermissions()) openLocation()
            initData()
        }
    }

    private fun initTab() {
        tvNearByToll.textSize = 14f
        tvNearByToll.isSelected = true
        val listener = View.OnClickListener { v ->
            tvNearByToll.textSize = 12f
            tvNearByToll.isSelected = false
            tvNearByService.textSize = 12f
            tvNearByService.isSelected = false
            tvNearByScenic.textSize = 12f
            tvNearByScenic.isSelected = false
            flToll.visibility = View.GONE
            flService.visibility = View.GONE
            flScenic.visibility = View.GONE
            when (v.id) {
                R.id.tvNearByToll -> {
                    tvNearByToll.textSize = 14f
                    tvNearByToll.isSelected = true
                    flToll.visibility = View.VISIBLE
                }
                R.id.tvNearByService -> {
                    tvNearByService.textSize = 14f
                    tvNearByService.isSelected = true
                    flService.visibility = View.VISIBLE
                }
                R.id.tvNearByScenic -> {
                    tvNearByScenic.textSize = 14f
                    tvNearByScenic.isSelected = true
                    flScenic.visibility = View.VISIBLE
                }
            }
        }
        tvNearByToll.setOnClickListener(listener)
        tvNearByService.setOnClickListener(listener)
        tvNearByScenic.setOnClickListener(listener)
        tvNearByMore.setOnClickListener { openActivity(MyNearByActivity::class.java, Bundle().apply { putInt("type", 4) }) }
    }

    private fun initRv() {
        rvToll.isNestedScrollingEnabled = false
        rvService.isNestedScrollingEnabled = false
        rvScenic.isNestedScrollingEnabled = false
        rvInfo.isNestedScrollingEnabled = false
        rvToll.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.HORIZONTAL }
        rvService.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.HORIZONTAL }
        rvScenic.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.HORIZONTAL }
        rvInfo.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
        tollAdapter = NearByTollAdapter(context, tollList)
        rvToll.adapter = tollAdapter
        serviceAdapter = NearByServiceAdapter(context, serviceList)
        rvService.adapter = serviceAdapter
        scenicAdapter = NearByScenicAdapter(context, scenicList)
        rvScenic.adapter = scenicAdapter
        tollAdapter.setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                if (position in 0 until tollList.size) {
                    openActivity(MyNearByActivity::class.java, Bundle().apply {
                        putInt("type", 1)
                        putSerializable("mdl", tollList[position])
                    })
                }
            }
        })
        serviceAdapter.setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                if (position in 0 until serviceList.size) {
                    openActivity(MyNearByActivity::class.java, Bundle().apply {
                        putInt("type", 2)
                        putSerializable("mdl", serviceList[position])
                    })
                }
            }
        })
        scenicAdapter.setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                if (position in 0 until scenicList.size) {
                    openActivity(MyNearByActivity::class.java, Bundle().apply {
                        putInt("type", 3)
                        putSerializable("mdl", scenicList[position])
                    })
                }
            }
        })
        newsAdapter = NewsAdapter(context, newsList)
        rvInfo.adapter = newsAdapter
        newsAdapter.setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                if (position in 0 until newsList.size) openWebActivity(newsList[position].detailurl, resources.getString(R.string.news_detail_title))
            }
        })
        subscribeAdapter = UserSubscribePageAdapter(context, subscribeMDLs)
        bannerView.setAdapter(subscribeAdapter)
        subscribeAdapter.setOnPageTouchListener(object : UserSubscribePageAdapter.OnPageTouchListener {
            override fun onPageClick(position: Int, mdl: SubscribeMDL) {
                if (position in 0 until subscribeMDLs.size) {
                    val bundle = Bundle()
                    bundle.putBoolean("fromHome", true)
                    if (mdl.getSubType() == SubscribeMDL.SubType.TrafficJam.code) {
                        bundle.putSerializable("mdl", mdl.getTrafficJamMDL().apply { if (subscribestatus != 1) subscribestatus = 1 })
                        openActivity(RoadNavigationActivity::class.java, bundle)
                    } else if (mdl.getSubType() == SubscribeMDL.SubType.Control.code
                            || mdl.getSubType() == SubscribeMDL.SubType.Emergencies.code
                            || mdl.getSubType() == SubscribeMDL.SubType.Planned.code) {
                        bundle.putSerializable("mdl", mdl.getEventMDL().apply { if (subscribestatus != 1) subscribestatus = 1 })
                        openActivity(RoadNavigationActivity::class.java, bundle)
                    } else if (mdl.getSubType() == SubscribeMDL.SubType.RescuePay.code) {
                        openActivity(RescuePayActivity::class.java, Bundle().apply { putString("rescueid", mdl.dataid) })
                    } else if (mdl.getSubType() == SubscribeMDL.SubType.RescueProgress.code) {
                        openActivity(RescueDetailActivity::class.java, Bundle().apply { putString("rescueid", mdl.rescueid) })
                    }
                }
            }

            override fun onPageDown() {
                bannerView.stopAutoScroll()
            }

            override fun onPageUp() {
                bannerView.startAutoScroll()
            }
        })
    }

    override fun setListener() {
        tvLJLF.setOnClickListener(this)
        tvService.setOnClickListener(this)
        tvGSRX.setOnClickListener(this)
        tvZXSC.setOnClickListener(this)
        tvShare.setOnClickListener(this)
        tvWFCX.setOnClickListener(this)
        tvGSZX.setOnClickListener(this)
        tvMore.setOnClickListener(this)
        tvInfoMore.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvLJLF -> {   //路径路费

            }
            R.id.tvService -> openActivity(ServiceAreaActivity::class.java) //服务区
            R.id.tvGSRX -> openActivity(HighWayHotlineActivity::class.java) //高速热线
            R.id.tvZXSC -> { //在线商城

            }
            R.id.tvShare -> { //车友报料
                if (!isLogin()) openActivity(LoginActivity::class.java)
                else openActivity(UserEventListActivity::class.java)
            }
            R.id.tvWFCX -> {  //违法查询

            }
            R.id.tvGSZX -> { //高速资讯
                openActivity(NewsMainActivity::class.java)
            }
            R.id.tvMore -> {  //更多

            }
            R.id.tvInfoMore -> openActivity(NewsMainActivity::class.java) //更多资讯
        }
    }

    override fun initData() {
//        getSubscribe()
        getNewsList()
    }

    /*获取我的订阅*/
    private fun getSubscribe() {
        if (!isLogin()) return
        doRequest(WebApiService.USER_SUBSCRIBES, WebApiService.subscribeParams(getUserId()), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdLs = GsonUtils.fromDataToList(data, SubscribeMDL::class.java)
                    updateSubscribe(mdLs)
                } else {
                    handler.sendEmptyMessageDelayed(CODE_SUBSCRIBE, 3000)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                //加载失败，延迟三秒重新加载
                handler.sendEmptyMessageDelayed(CODE_SUBSCRIBE, 3000)
            }
        })
    }

    //更新定制ui
    private fun updateSubscribe(mdLs: MutableList<SubscribeMDL>) {
        subscribeMDLs.clear()
        subscribeMDLs.addAll(mdLs)
        subscribeAdapter.notifyDataSetChanged()
        if (subscribeMDLs.size > 0) {
            flSubscribe.visibility = View.VISIBLE
        } else {
            flSubscribe.visibility = View.GONE
        }
    }

    /*获取资讯列表*/
    private fun getNewsList() {
        doRequest(WebApiService.HOME_NEWS, HashMap(), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                refreshLayout.finishRefresh()
                if (GsonUtils.isResultOk(data)) {
                    val mdLs = GsonUtils.fromDataToList(data, NewsMDL::class.java)
                    updateNews(mdLs)
                } else {
                    handler.sendEmptyMessageDelayed(CODE_NEWS, 3000)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                refreshLayout.finishRefresh()
                handler.sendEmptyMessageDelayed(CODE_NEWS, 3000)
            }
        })
    }

    /*更新资讯ui*/
    private fun updateNews(mdLs: MutableList<NewsMDL>) {
        newsList.clear()
        newsList.addAll(mdLs)
        newsAdapter.notifyDataSetChanged()
    }

    override fun afterLocation(location: AMapLocation) {
        isOpenLocation = true
        val city = location.city
        val mQuery = WeatherSearchQuery(city, WeatherSearchQuery.WEATHER_TYPE_LIVE)
        weatherSearch = WeatherSearch(context).apply {
            setOnWeatherSearchListener(this@MainFragment)
            query = mQuery
            searchWeatherAsyn() //异步搜索
        }
        val latitude = location.latitude
        val longitude = location.longitude
        getNearbyData(latitude, longitude)
        closeLocation()  //定位成功，关闭定位；用户下拉刷新再次打开
    }

    override fun locationFailure() {
        onLocationFailure()
    }

    private fun onLocationFailure() {
        tvLocationFailure.visibility = View.VISIBLE
        flNearby.visibility = View.GONE
        val text = context.resources.getString(R.string.home_location_failure_tips)
        val ss = SpannableString(text)
        val start = text.indexOf("，") + 1
        val end = text.length
        val clickSpan = object : ClickableSpan() {
            override fun onClick(p0: View?) {
                if (!hasLocationPermissions()) {
                    //申请位置权限时用户点击了“禁止不再提示”按钮 则引导用户到app设置页面重新打开
                    if (!isShouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_COARSE_LOCATION) || !isShouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                        openSettings()
                    } else {  //重新申请权限
                        applyLocationPermissions()
                    }
                } else {
                    flNearby.visibility = View.VISIBLE
                    tvLocationFailure.visibility = View.GONE
                    openLocation()
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
            }
        }
        ss.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorAccent)),
                start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvLocationFailure.text = ss
        tvLocationFailure.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onWeatherLiveSearched(weatherLiveResult: LocalWeatherLiveResult?, rCode: Int) {
        if (isDestroyView) return
        if (rCode == 1000 && weatherLiveResult != null && weatherLiveResult.liveResult != null) {
            val result = weatherLiveResult.liveResult
            llWeather.visibility = View.VISIBLE
            val temperature = result.temperature + "℃"
            val weather = result.weather
            iv_weather.setImageResource(WeatherMDL.getWeatherIco(weather))
            tv_temperature.text = temperature
            tv_city.text = result.city
        } else {
            handler.sendEmptyMessageDelayed(CODE_WEATHER, 3000)
        }
    }

    override fun onWeatherForecastSearched(localWeatherForecastResult: LocalWeatherForecastResult, rCode: Int) {

    }

    //根据当前经纬度获取附近信息
    private fun getNearbyData(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
        /*测试经纬度*/
//        val latitude = CurrApplication.APP_LATLNG.latitude
//        val longitude = CurrApplication.APP_LATLNG.longitude
        getNearByToll(latitude, longitude)
        getNearByService(latitude, longitude)
        getNearByScenic(latitude, longitude)
    }

    /*获取附近加油站信息*/
    private fun getNearByToll(latitude: Double, longitude: Double) {
        doRequest(WebApiService.MAP_DATA, WebApiService.mapDataByTypeParams(MapDataType.TOLL_GATE.code,
                longitude, latitude, "", "home"), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val list = GsonUtils.fromDataToList(data, TollGateMDL::class.java)
                    tollList.addAll(list)
                    updateToll(list)
                } else {
                    handler.sendEmptyMessageDelayed(CODE_TOLL, 3000)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                handler.sendEmptyMessageDelayed(CODE_TOLL, 3000)
            }
        })
    }

    /*获取附近服务区信息*/
    private fun getNearByService(latitude: Double, longitude: Double) {
        doRequest(WebApiService.MAP_DATA, WebApiService.mapDataByTypeParams(MapDataType.SERVICE_AREA.code,
                longitude, latitude, "", "home"), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val list = GsonUtils.fromDataToList(data, ServiceMDL::class.java)
                    updateService(list)
                } else {
                    handler.sendEmptyMessageDelayed(CODE_SERVICE, 3000)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                handler.sendEmptyMessageDelayed(CODE_SERVICE, 3000)
            }
        })
    }

    /*获取附近景点信息*/
    private fun getNearByScenic(latitude: Double, longitude: Double) {
        doRequest(WebApiService.MAP_DATA, WebApiService.mapDataByTypeParams(MapDataType.SCENIC.code,
                longitude, latitude, "", "home"), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val list = GsonUtils.fromDataToList(data, ScenicMDL::class.java)
                    updateScenic(list)
                } else {
                    handler.sendEmptyMessageDelayed(CODE_SCENIC, 3000)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                handler.sendEmptyMessageDelayed(CODE_SCENIC, 3000)
            }
        })
    }

    //更新附近加油站ui
    private fun updateToll(list: MutableList<TollGateMDL>) {
        tollList.clear()
        tollList.addAll(list)
        tollAdapter.notifyDataSetChanged()
        if (tollList.size > 0) {
            rvToll.visibility = View.VISIBLE
            tvEmptyToll.visibility = View.GONE
        } else {
            rvToll.visibility = View.GONE
            tvEmptyToll.visibility = View.VISIBLE
        }
    }

    //更新附近服务区ui
    private fun updateService(list: MutableList<ServiceMDL>) {
        serviceList.clear()
        serviceList.addAll(list)
        serviceAdapter.notifyDataSetChanged()
        if (serviceList.size > 0) {
            rvService.visibility = View.VISIBLE
            tvEmptyService.visibility = View.GONE
        } else {
            rvService.visibility = View.GONE
            tvEmptyService.visibility = View.VISIBLE
        }
    }

    //更新附近景点ui
    private fun updateScenic(list: MutableList<ScenicMDL>) {
        scenicList.clear()
        scenicList.addAll(list)
        scenicAdapter.notifyDataSetChanged()
        if (scenicList.size > 0) {
            rvService.visibility = View.VISIBLE
            tvEmptyScenic.visibility = View.GONE
        } else {
            rvService.visibility = View.GONE
            tvEmptyScenic.visibility = View.VISIBLE
        }
    }

    private class MHandler(fragment: MainFragment) : Handler() {
        private val weakReference: WeakReference<MainFragment> = WeakReference(fragment)
        override fun handleMessage(msg: Message) {
            val fragment = weakReference.get() ?: return
            when (msg.what) {
                CODE_SUBSCRIBE -> fragment.getSubscribe()
                CODE_TOLL -> fragment.getNearByToll(fragment.latitude, fragment.longitude)
                CODE_SERVICE -> fragment.getNearByService(fragment.latitude, fragment.longitude)
                CODE_SCENIC -> fragment.getNearByScenic(fragment.latitude, fragment.longitude)
                CODE_NEWS -> fragment.getNewsList()
                CODE_WEATHER -> fragment.weatherSearch?.searchWeatherAsyn()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasLocationPermissions() && !isOpenLocation) {
            openLocation()
            flNearby.visibility = View.VISIBLE
            tvLocationFailure.visibility = View.GONE
        }
        if (!isLogin() && flSubscribe.visibility != View.GONE) {  //退出登录
            flSubscribe.visibility = View.GONE
        }
        getSubscribe()
    }

    override fun onDestroyView() {
        isDestroyView = true
        disposable?.dispose()
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }
}