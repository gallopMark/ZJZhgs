package com.uroad.zhgs.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.amap.api.location.AMapLocation
import com.uroad.zhgs.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_main.*
import com.amap.api.services.weather.WeatherSearch
import com.amap.api.services.weather.WeatherSearchQuery
import com.amap.api.services.weather.LocalWeatherForecastResult
import com.amap.api.services.weather.LocalWeatherLiveResult
import com.uroad.zhgs.adaptervp.UserSubscribePageAdapter
import com.uroad.zhgs.model.*
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import com.uroad.zhgs.activity.*
import com.uroad.zhgs.adapteRv.NewsAdapter
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.rxbus.MessageEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import com.uroad.library.rxbus.RxBus
import com.uroad.library.utils.VersionUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.dialog.BindCarDialog
import com.uroad.zhgs.dialog.VersionDialog
import com.uroad.zhgs.enumeration.Carcategory
import com.uroad.zhgs.service.DownloadService
import com.uroad.zhgs.utils.PackageInfoUtils
import io.reactivex.disposables.Disposable


/**
 *Created by MFB on 2018/7/28.
 * Copyright 2018年 浙江综合交通大数据开发有限公司.
 * 说明：app首
 * 18802076493 a123456
 */
class MainFragment : BaseFragment(), View.OnClickListener, WeatherSearch.OnWeatherSearchListener {
    private var isLocationSuccess = false  //是否已经点位成功
    private var weatherSearch: WeatherSearch? = null    //高德api天气搜索
    private val newsList = ArrayList<NewsMDL>()     //推荐资讯数据集合
    private lateinit var newsAdapter: NewsAdapter   //资讯列表适配器
    private val subscribeMDLs = ArrayList<SubscribeMDL>()   //我的订阅数据集（已登录状态）
    private lateinit var subscribeAdapter: UserSubscribePageAdapter
    private var disposable: Disposable? = null
    private var isDestroyView = false
    private var serviceIntent: Intent? = null
    private var onMenuClickListener: OnMenuClickListener? = null
    private var longitude = CurrApplication.APP_LATLNG.longitude
    private var latitude = CurrApplication.APP_LATLNG.latitude
    private lateinit var tollFragment: NearByTollCFragment
    private lateinit var serviceFragment: NearByServiceCFragment
    private lateinit var scenicFragment: NearByScenicCFragment
    private val handler = Handler()
    private var currentTab = 1

    /*数据加载失败，通过handler延迟 重新加载数据*/
    companion object {
        const val DELAY_MILLIS = 3000L
        const val UPDATE_TIME = 5 * 60 * 1000L  //我的附近，定时5分钟刷新一次
        var cars: MutableList<CarMDL>? = null
    }

    private val nearByRun = Runnable { openLocation() }
    override fun setBaseLayoutResID(): Int = R.layout.fragment_main

    override fun setUp(view: View, savedInstanceState: Bundle?) {
        btNavigation.setOnClickListener { openActivity(RoadNavigationActivity::class.java) }
        btRescue.setOnClickListener {
            if (!isLogin()) openActivity(LoginActivity::class.java)
            else checkRescue()
        }
        initTab()
        initRv()
        /*未申请位置权限，则申请*/
        if (!hasLocationPermissions()) applyLocationPermissions()
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

    private fun initTab() {
        val ts12 = context.resources.getDimension(R.dimen.font_12)
        val ts14 = context.resources.getDimension(R.dimen.font_14)
        tvNearByToll.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts14)
        tvNearByToll.isSelected = true
        initFragments()
        setTab(1)
        val listener = View.OnClickListener { v ->
            tvNearByToll.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts12)
            tvNearByToll.isSelected = false
            tvNearByService.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts12)
            tvNearByService.isSelected = false
            tvNearByScenic.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts12)
            tvNearByScenic.isSelected = false
            when (v.id) {
                R.id.tvNearByToll -> {
                    tvNearByToll.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts14)
                    tvNearByToll.isSelected = true
                    setTab(1)
                }
                R.id.tvNearByService -> {
                    tvNearByService.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts14)
                    tvNearByService.isSelected = true
                    setTab(2)
                }
                R.id.tvNearByScenic -> {
                    tvNearByScenic.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts14)
                    tvNearByScenic.isSelected = true
                    setTab(3)
                }
            }
        }
        tvNearByToll.setOnClickListener(listener)
        tvNearByService.setOnClickListener(listener)
        tvNearByScenic.setOnClickListener(listener)
        tvNearByMore.setOnClickListener { openActivity(MyNearByActivity::class.java, Bundle().apply { putInt("type", 4) }) }
    }

    private fun initFragments() {
        tollFragment = NearByTollCFragment()
        serviceFragment = NearByServiceCFragment()
        scenicFragment = NearByScenicCFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.add(R.id.flNearby, tollFragment)
        transaction.add(R.id.flNearby, serviceFragment)
        transaction.add(R.id.flNearby, scenicFragment)
        transaction.commit()
    }

    private fun initRv() {
        rvInfo.isNestedScrollingEnabled = false
        rvInfo.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
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

    override fun onPause() {
        super.onPause()
        bannerView.stopAutoScroll()
    }

    override fun setListener() {
        tvLJLF.setOnClickListener(this)
        tvService.setOnClickListener(this)
        tvGSRX.setOnClickListener(this)
        tvZXSC.setOnClickListener(this)
        tvShare.setOnClickListener(this)
        tvWFCX.setOnClickListener(this)
        tvGSZX.setOnClickListener(this)
        tvCXCX.setOnClickListener(this)
        tvInfoMore.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvLJLF -> openActivity(RoadTollActivity::class.java)  //路径路费
            R.id.tvService -> openActivity(ServiceAreaActivity::class.java) //服务区
            R.id.tvGSRX -> openActivity(HighWayHotlineActivity::class.java) //高速热线
            R.id.tvCXCX -> { //诚信查询
                if (!isLogin()) openActivity(LoginActivity::class.java)
                else getMyCar()
//                openActivity(OnlineShoppingActivity::class.java)
            }
            R.id.tvShare -> { //车友报料
                if (!isLogin()) openActivity(LoginActivity::class.java)
                else openActivity(UserEventListActivity::class.java)
            }
            R.id.tvWFCX -> {  //违法查询
                openActivity(BreakRulesActivity::class.java)
            }
            R.id.tvGSZX -> { //高速资讯
                openActivity(NewsMainActivity::class.java)
            }
            R.id.tvZXSC -> {  //在线商城
                onMenuClickListener?.onMenuClick()
//                openActivity(MoreActivity::class.java)
            }
            R.id.tvInfoMore -> openActivity(NewsMainActivity::class.java) //更多资讯
        }
    }

    /*获取用户车辆（仅客车）*/
    private fun getMyCar() {
        doRequest(WebApiService.MYCAR, WebApiService.myCarParams(getUserId(), Carcategory.COACH.code), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdLs = GsonUtils.fromDataToList(data, CarMDL::class.java)
                    if (mdLs.size > 0) {
                        cars?.clear()
                        cars = ArrayList<CarMDL>().apply { addAll(mdLs) }
                        openActivity(CarInquiryActivity::class.java)
                    } else {
                        BindCarDialog(context).setOnConfirmClickListener(object : BindCarDialog.OnConfirmClickListener {
                            override fun onConfirm(dialog: BindCarDialog) {
                                dialog.dismiss()
                                openActivity(BindCarActivity::class.java)
                            }
                        }).show()
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

    override fun initData() {
        getVersionByType()
    }

    /*获取app版本号*/
    private fun getVersionByType() {
        doRequest(WebApiService.APP_VERSION, WebApiService.appVersionParams(PackageInfoUtils.getVersionName(context)), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, VersionMDL::class.java)
                    mdl?.let { versionTips(it) }
                } else {
                    handler.postDelayed({ getVersionByType() }, DELAY_MILLIS)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                handler.postDelayed({ getVersionByType() }, DELAY_MILLIS)
            }
        })
    }

    /*版本检测是否更新*/
    private fun versionTips(mdl: VersionMDL) {
        if (VersionUtils.isNeedUpdate(mdl.conf_ver, PackageInfoUtils.getVersionName(context))) {
            VersionDialog(context, mdl).setOnConfirmClickListener(object : VersionDialog.OnConfirmClickListener {
                override fun onConfirm(mdl: VersionMDL, dialog: VersionDialog) {
                    dialog.dismiss()
                    if (TextUtils.isEmpty(mdl.url)) showShortToast(context.getString(R.string.version_update_error))
                    else {
                        serviceIntent = Intent(context, DownloadService::class.java).apply {
                            putExtra("downloadUrl", mdl.url)
                            if (mdl.isforce == 1) {
                                putExtra("isForce", true)
                            } else {
                                putExtra("isForce", false)
                            }
                            context.startService(this)
                        }
                    }
                }
            }).show()
        }
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
                    handler.postDelayed({ getSubscribe() }, DELAY_MILLIS)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                //加载失败，延迟三秒重新加载
                handler.postDelayed({ getSubscribe() }, DELAY_MILLIS)
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
                if (GsonUtils.isResultOk(data)) {
                    val mdLs = GsonUtils.fromDataToList(data, NewsMDL::class.java)
                    updateNews(mdLs)
                } else {
                    handler.postDelayed({ getNewsList() }, DELAY_MILLIS)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                handler.postDelayed({ getNewsList() }, DELAY_MILLIS)
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
        isLocationSuccess = true
        val city = location.city
        val mQuery = WeatherSearchQuery(city, WeatherSearchQuery.WEATHER_TYPE_LIVE)
        weatherSearch = WeatherSearch(context).apply {
            setOnWeatherSearchListener(this@MainFragment)
            query = mQuery
            searchWeatherAsyn() //异步搜索
        }
        longitude = location.longitude
        latitude = location.latitude
        tvLocationFailure.visibility = View.GONE
        flNearby.visibility = View.VISIBLE
        locationUpdate(longitude, latitude)
        handler.postDelayed(nearByRun, UPDATE_TIME)
        closeLocation()  //定位成功，关闭定位；用户下拉刷新再次打开
    }


    /*我的附近tab 1->附近收费站 2->附近服务区 3->附近景点*/
    private fun setTab(tab: Int) {
        currentTab = tab
        val transaction = childFragmentManager.beginTransaction()
        transaction.hide(tollFragment)
        transaction.hide(serviceFragment)
        transaction.hide(scenicFragment)
        when (tab) {
            1 -> transaction.show(tollFragment)
            2 -> transaction.show(serviceFragment)
            3 -> transaction.show(scenicFragment)
        }
        transaction.commit()
    }

    private fun locationUpdate(longitude: Double, latitude: Double) {
        if (tollFragment.isAdded) tollFragment.onLocationUpdate(longitude, latitude)
        if (serviceFragment.isAdded) serviceFragment.onLocationUpdate(longitude, latitude)
        if (scenicFragment.isAdded) scenicFragment.onLocationUpdate(longitude, latitude)
    }

    override fun locationFailure() {
        //如果第一次定位成功，再次定位时失败了，则不显示定位定位失败页，避免我的附近数据无法查阅
        if (!isLocationSuccess) onLocationFailure()
        else handler.postDelayed({ nearByRun }, DELAY_MILLIS)
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
            handler.postDelayed({ weatherSearch?.searchWeatherAsyn() }, DELAY_MILLIS)
        }
    }

    override fun onWeatherForecastSearched(localWeatherForecastResult: LocalWeatherForecastResult, rCode: Int) {

    }

    override fun onResume() {
        super.onResume()
        if (hasLocationPermissions() && !isLocationSuccess) openLocation()
        if (!isLogin()) { //退出登录
            flSubscribe.visibility = View.GONE
            subscribeMDLs.clear()
            subscribeAdapter.notifyDataSetChanged()
        } else { //返回到首页刷新我的订阅
            getSubscribe()
            bannerView.startAutoScroll()
        }
        getNewsList()  //返回到首页刷新资讯
    }

    override fun onStop() {
        super.onStop()
        if (isLogin()) bannerView.stopAutoScroll()
    }

    override fun onDestroyView() {
        isDestroyView = true
        cars?.let {
            it.clear()
            cars = null
        }
        disposable?.dispose()
        handler.removeCallbacksAndMessages(null)
        serviceIntent?.let { context.stopService(it) }
        super.onDestroyView()
    }

    interface OnMenuClickListener {
        fun onMenuClick()
    }

    fun setOnMenuClickListener(onMenuClickListener: OnMenuClickListener) {
        this.onMenuClickListener = onMenuClickListener
    }
}