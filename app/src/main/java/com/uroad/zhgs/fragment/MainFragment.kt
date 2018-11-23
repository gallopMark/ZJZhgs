package com.uroad.zhgs.fragment

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.amap.api.location.AMapLocation
import com.uroad.zhgs.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_main.*
import com.amap.api.services.weather.WeatherSearch
import com.amap.api.services.weather.WeatherSearchQuery
import com.amap.api.services.weather.LocalWeatherForecastResult
import com.amap.api.services.weather.LocalWeatherLiveResult
import com.uroad.zhgs.model.*
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import android.text.TextUtils
import com.uroad.zhgs.activity.*
import com.uroad.mqtt.IMqttCallBack
import com.uroad.zhgs.R
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.dialog.*
import com.uroad.zhgs.helper.AppLocalHelper
import com.uroad.zhgs.model.mqtt.AddTeamMDL
import com.uroad.zhgs.webservice.ApiService
import kotlinx.android.synthetic.main.layout_fragment_main.*
import kotlinx.android.synthetic.main.layout_riders_message.*
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken


/**
 *Created by MFB on 2018/7/28.
 * Copyright 2018年 浙江综合交通大数据开发有限公司.
 * 说明：app首
 * 18802076493 a123456
 */
class MainFragment : BaseFragment(), WeatherSearch.OnWeatherSearchListener {
    private var isLocationSuccess = false  //是否已经点位成功
    private var weatherSearch: WeatherSearch? = null    //高德api天气搜索
    private var isDestroyView = false
    private var onMenuClickListener: OnMenuClickListener? = null
    private var longitude = CurrApplication.APP_LATLNG.longitude
    private var latitude = CurrApplication.APP_LATLNG.latitude
    private lateinit var mainSubscribeFragment: MainSubscribeFragment
    private lateinit var mainNearByFragment: MainNearByFragment
    private lateinit var mainNewsFragment: MainNewsFragment
    private val handler = Handler()

    /*数据加载失败，通过handler延迟 重新加载数据*/
    companion object {
        const val TAG_MENU = "menu"
        const val TAG_SUBSCRIBE = "subscribe"
        const val TAG_NEARBY = "nearby"
        const val TAG_NEWS = "news"
        const val DELAY_MILLIS = 3000L
        const val UPDATE_TIME = 5 * 60 * 1000L  //我的附近，定时5分钟刷新一次
    }

    private val nearByRun = Runnable { openLocation() }
    override fun setBaseLayoutResID(): Int = R.layout.fragment_main

    override fun setUp(view: View, savedInstanceState: Bundle?) {
        btNavigation.setOnClickListener { openActivity(RoadNavigationActivity::class.java) }
        btRescue.setOnClickListener {
            if (!isLogin()) openActivity(LoginActivity::class.java)
            else checkRescue()
        }
        initMenu()
        initSubscribe()
        initNearBy()
        initNews()
        initRefresh()
        /*未申请位置权限，则申请*/
        if (!hasLocationPermissions()) applyLocationPermissions()
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

    /*菜单列表(用fragment替换)*/
    private fun initMenu() {
        /*防止系统内存不足GC导致fragment重叠问题*/
        val fragment = childFragmentManager.findFragmentByTag(TAG_MENU)
        if (fragment != null) {
            childFragmentManager.popBackStack(MainMenuFragment::class.java.name, 0)
            childFragmentManager.beginTransaction().detach(fragment).remove(fragment).commit()
        } else {
            childFragmentManager.beginTransaction().replace(R.id.flMenu, MainMenuFragment().apply {
                setOnShopClickListener(object : MainMenuFragment.OnShopClickListener {
                    override fun onShopClick() {
                        onMenuClickListener?.onMenuClick()
                    }
                })
            }, TAG_MENU).commit()
        }
    }

    /*我的订阅(用fragment替换)*/
    private fun initSubscribe() {
        val fragment = childFragmentManager.findFragmentByTag(TAG_SUBSCRIBE)
        if (fragment != null) {
            childFragmentManager.popBackStack(MainSubscribeFragment::class.java.name, 0)
            childFragmentManager.beginTransaction().detach(fragment).remove(fragment).commit()
        } else {
            mainSubscribeFragment = MainSubscribeFragment().apply {
                setOnSubscribeEvent(object : MainSubscribeFragment.OnSubscribeEvent {
                    override fun onEvent(isEmpty: Boolean) {
                        if (isEmpty) this@MainFragment.flSubscribe.visibility = View.GONE
                        else this@MainFragment.flSubscribe.visibility = View.VISIBLE
                    }
                })
            }
            childFragmentManager.beginTransaction().replace(R.id.flSubscribe, mainSubscribeFragment, TAG_SUBSCRIBE).commit()
        }
    }

    /*我的附近（用fragment替代）*/
    private fun initNearBy() {
        val fragment = childFragmentManager.findFragmentByTag(TAG_NEARBY)
        if (fragment != null) {
            childFragmentManager.popBackStack(MainNearByFragment::class.java.name, 0)
            childFragmentManager.beginTransaction().detach(fragment).remove(fragment).commit()
        } else {
            mainNearByFragment = MainNearByFragment().apply {
                setOnRequestLocationListener(object : MainNearByFragment.OnRequestLocationListener {
                    override fun onRequest() {
                        applyLocationPermissions()
                    }
                })
            }
            childFragmentManager.beginTransaction().replace(R.id.flNearBy, mainNearByFragment).commit()
        }
    }

    /*最新资讯（用fragment替换）*/
    private fun initNews() {
        val fragment = childFragmentManager.findFragmentByTag(TAG_NEWS)
        if (fragment != null) {
            childFragmentManager.popBackStack(MainNewsFragment::class.java.name, 0)
            childFragmentManager.beginTransaction().detach(fragment).remove(fragment).commit()
        } else {
            mainNewsFragment = MainNewsFragment().apply {
                setOnRequestCallback(object : MainNewsFragment.OnRequestCallback {
                    override fun callback() {
                        this@MainFragment.refreshLayout.finishRefresh()
                    }
                })
            }
            childFragmentManager.beginTransaction().replace(R.id.flNews, mainNewsFragment).commit()
        }
    }


    /*下拉刷新 重新打开定位，刷新我的附近，我的订阅，最新资讯*/
    private fun initRefresh() {
        refreshLayout.isEnableLoadMore = false
        refreshLayout.setOnRefreshListener {
            if (hasLocationPermissions()) openLocation()
            updateSubscribe()
            updateNews()
        }
    }

    private fun removeSubscribe() {
        if (mainSubscribeFragment.isAdded) {
            childFragmentManager.popBackStack(MainSubscribeFragment::class.java.name, 0)
            childFragmentManager.beginTransaction().detach(mainSubscribeFragment).remove(mainSubscribeFragment).commit()
        }
    }

    /*刷新我的订阅*/
    private fun updateSubscribe() {
        if (!mainSubscribeFragment.isAdded) {
            initSubscribe()
        } else {
            mainSubscribeFragment.initData()
        }
    }

    private fun updateNews() {
        if (mainNewsFragment.isAdded) mainNewsFragment.initData()
    }

    override fun setListener() {
        ivCustomerService.setOnClickListener { CurrApplication.WISDOM_URL?.let { url -> openWebActivity(url, context.getString(R.string.customer_service)) } }
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
        locationUpdate(longitude, latitude)
        handler.removeCallbacks(nearByRun)
        handler.postDelayed(nearByRun, UPDATE_TIME)
        closeLocation()  //定位成功，关闭定位；用户下拉刷新再次打开
    }

    private fun locationUpdate(longitude: Double, latitude: Double) {
        if (mainNearByFragment.isAdded) mainNearByFragment.locationUpdate(longitude, latitude)
    }

    override fun locationFailure() {
        //如果第一次定位成功，再次定位时失败了，则不显示定位定位失败页，避免我的附近数据无法查阅
        if (!isLocationSuccess) onLocationFailure()
        else handler.postDelayed({ nearByRun }, DELAY_MILLIS)
    }

    private fun onLocationFailure() {
        if (mainNearByFragment.isAdded) mainNearByFragment.onLocationFailure()
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
            removeSubscribe()
        } else { //返回到首页刷新我的订阅
            updateSubscribe()
            if (!AppLocalHelper.isAuth(context)) {
                AuthenticationDialog(context).onViewClickListener(object : AuthenticationDialog.OnViewClickListener {
                    override fun onViewClick(type: Int, dialog: AuthenticationDialog) {
                        when (type) {
                            1 -> openActivity(BindCarActivity::class.java)
                            2 -> openActivity(PerfectUserInfoActivity::class.java)
                        }
                        dialog.dismiss()
                    }
                }).show()
            }
        }
        updateNews() //返回到首页刷新资讯
        checkCarTeamSituation()
        clipboard()
    }

    /*检查是否有车队或者邀请*/
    private fun checkCarTeamSituation() {
        if (!isLogin() || !isAuth()) {
            llRidersWindow.visibility = View.GONE
        } else {
            doRequest(WebApiService.CHECK_RIDERS, WebApiService.checkRidersParams(getUserId()), object : HttpRequestCallback<String>() {
                override fun onPreExecute() {
                    llRidersWindow.visibility = View.GONE
                }

                override fun onSuccess(data: String?) {
                    if (GsonUtils.isResultOk(data)) {
                        val mdl = GsonUtils.fromDataBean(data, RidersMsgMDL::class.java)
                        mdl?.let { updateCarTeam(it) }
                    } else {
                        handler.postDelayed({ checkCarTeamSituation() }, DELAY_MILLIS)
                    }
                }

                override fun onFailure(e: Throwable, errorMsg: String?) {
                    handler.postDelayed({ checkCarTeamSituation() }, DELAY_MILLIS)
                }
            })
        }
    }

    private fun updateCarTeam(mdl: RidersMsgMDL) {
        when (mdl.type) {  //1 已加入车队；2 收到邀请
            1 -> mdl.content?.let {
                if (it.size > 0) {
                    val content = it[0]
                    llRidersWindow.visibility = View.VISIBLE
                    tvMsgCount.visibility = View.GONE
                    tvDestination.visibility = View.VISIBLE
                    tvDestination.text = content.toplace
                    llRidersWindow.setOnClickListener { openActivity(RidersDetailActivity::class.java, Bundle().apply { putString("teamId", content.teamid) }) }
                }
            }
            2 -> mdl.content?.let { mdLs ->
                if (mdLs.size > 0) {
                    llRidersWindow.visibility = View.VISIBLE
                    tvMsgCount.visibility = View.VISIBLE
                    tvMsgCount.text = mdLs.size.toString()
                    tvDestination.visibility = View.VISIBLE
                    tvDestination.text = "你被邀请加入车队"
                    llRidersWindow.setOnClickListener {
                        if (mdLs.size == 1) {
                            RidersInTokenDialog(context).withData(mdLs[0]).setOnViewClickListener(object : RidersInTokenDialog.OnViewClickListener {
                                override fun onViewClick(type: Int, dialog: RidersInTokenDialog) {
                                    when (type) {
                                        1 -> openActivity(RidersAgreementActivity::class.java)
                                        2 -> {
                                            refuseInvitation()
                                            dialog.dismiss()
                                        }
                                        3 -> {
                                            joinCarTeam(mdLs[0].teamid)
                                            dialog.dismiss()
                                        }
                                    }
                                }
                            }).show()
                        } else {
                            RidersMultiInvitDialog(context, mdLs).onViewClickListener(object : RidersMultiInvitDialog.OnViewClickListener {
                                override fun onViewClick(type: Int, dialog: RidersMultiInvitDialog) {
                                    when (type) {
                                        1 -> {   //点击了组队协议
                                            openActivity(RidersAgreementActivity::class.java)
                                        }
                                        2 -> {  //点击不用了（即拒绝邀请）
                                            llRidersWindow.visibility = View.GONE
                                            refuseInvitation()
                                            dialog.dismiss()
                                        }
                                    }
                                }

                                override fun onItemSelected(content: RidersMsgMDL.Content, dialog: RidersMultiInvitDialog) {
                                    onJoinConfirm(content, dialog)
                                }
                            }).show()
                        }
                    }
                }
            }
            else -> {
                llRidersWindow.visibility = View.GONE
            }
        }
    }

    /*拒绝邀请*/
    private fun refuseInvitation() {
        doRequest(WebApiService.REFUSE_INVITE, WebApiService.refuseInviteParams(getUserId()), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (!GsonUtils.isResultOk(data)) handler.postDelayed({ refuseInvitation() }, DELAY_MILLIS)
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                handler.postDelayed({ refuseInvitation() }, DELAY_MILLIS)
            }
        })
    }

    private fun onJoinConfirm(content: RidersMsgMDL.Content, invitDialog: RidersMultiInvitDialog) {
        val dialog = MaterialDialog(context)
        dialog.setTitle(context.resources.getString(R.string.dialog_default_title))
        val message = if (!TextUtils.isEmpty(content.username)) {
            "确定加入${content.username}的组队吗？"
        } else {
            "确定加入此车队吗？"
        }
        dialog.setMessage(message)
        dialog.setNegativeButton(context.resources.getString(R.string.dialog_button_cancel), object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                dialog.dismiss()
            }
        })
        dialog.setPositiveButton(context.resources.getString(R.string.dialog_button_confirm), object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                joinCarTeam(content.teamid)
                invitDialog.dismiss()
                dialog.dismiss()
            }
        })
        dialog.show()
    }

    /*加入车队*/
    private fun joinCarTeam(teamId: String?) {
        val mqttService = ApiService.buildMQTTService(context)
        mqttService.connect(object : IMqttCallBack {
            override fun messageArrived(topic: String?, message: String?, qos: Int) {
            }

            override fun connectionLost(throwable: Throwable?) {
            }

            override fun deliveryComplete(deliveryToken: IMqttDeliveryToken?) {
                openActivity(RidersDetailActivity::class.java, Bundle().apply { putString("teamId", teamId) })
                mqttService.disconnect()
            }

            override fun connectSuccess(token: IMqttToken?) {
                val mdl = AddTeamMDL().apply {
                    this.userid = getUserId()
                    this.username = getUserName()
                    this.usericon = getIconFile()
                    this.teamid = teamId
                    this.longitude = this@MainFragment.longitude
                    this.latitude = this@MainFragment.latitude
                }
                mqttService.publish("${ApiService.TOPIC_ADD_TEAM}$teamId", mdl.obtainMessage())
            }

            override fun connectFailed(token: IMqttToken?, throwable: Throwable?) {
            }
        })
    }

    /*获取系统粘贴板内容，判断是否存在车队口令*/
    private fun clipboard() {
        if (!isLogin()) return
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val data = cm.primaryClip
        if (data != null && data.itemCount > 0) {
            val item = data.getItemAt(0)
            val content = item.text.toString()
            if (!TextUtils.isEmpty(content)
                    && content.contains("智慧高速车友组队")
                    && content.contains("¢")) {
                //从口令中截取口令
                val start = content.indexOf("¢") + 1
                val end = content.lastIndexOf("¢")
                val inToken = content.substring(start, end)
                getCarTeamData(inToken)
                cm.primaryClip = ClipData.newPlainText(null, "")
            }
        }
    }

    private fun getCarTeamData(inToken: String?) {
        doRequest(WebApiService.CAR_TEAM_DETAIL, WebApiService.getCarTeamDataParams2(inToken), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, RidersDetailMDL::class.java)
                    mdl?.teammember?.let { members ->
                        var isMySelf = false
                        for (member in members) {
                            if (TextUtils.equals(member.userid, getUserId())) {
                                isMySelf = true
                                break
                            }
                        }
                        if (!isMySelf) {
                            mdl.team_data?.let {
                                RidersInTokenDialog(context).withData(it).setOnViewClickListener(object : RidersInTokenDialog.OnViewClickListener {
                                    override fun onViewClick(type: Int, dialog: RidersInTokenDialog) {
                                        when (type) {
                                            1 -> openActivity(RidersAgreementActivity::class.java)
                                            2 -> {
                                                refuseInvitation()
                                                dialog.dismiss()
                                            }
                                            3 -> {
                                                joinCarTeam(mdl.team_data?.teamid)
                                                dialog.dismiss()
                                            }
                                        }
                                    }
                                }).show()
                            }
                        }
                    }
                } else {
                    handler.postDelayed({ getCarTeamData(inToken) }, DELAY_MILLIS)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                handler.postDelayed({ getCarTeamData(inToken) }, DELAY_MILLIS)
            }
        })
    }

    override fun onDestroyView() {
        isDestroyView = true
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    interface OnMenuClickListener {
        fun onMenuClick()
    }

    fun setOnMenuClickListener(onMenuClickListener: OnMenuClickListener) {
        this.onMenuClickListener = onMenuClickListener
    }
}