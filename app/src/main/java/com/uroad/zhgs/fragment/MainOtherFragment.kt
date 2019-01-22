package com.uroad.zhgs.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.widget.RelativeLayout
import com.amap.api.location.AMapLocation
import com.amap.api.services.weather.LocalWeatherForecastResult
import com.amap.api.services.weather.LocalWeatherLiveResult
import com.amap.api.services.weather.WeatherSearch
import com.amap.api.services.weather.WeatherSearchQuery
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.zhgs.R
import com.uroad.zhgs.activity.InviteCourtesyActivity
import com.uroad.zhgs.activity.LoginActivity
import com.uroad.zhgs.common.BaseFragment
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.model.ActivityMDL
import com.uroad.zhgs.model.WeatherMDL
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.fragment_mainother.*

/**
 * @author MFB
 * @create 2018/11/23
 * @describe 首页天气（活动入口）等
 */
class MainOtherFragment : BaseFragment(), WeatherSearch.OnWeatherSearchListener {
    private var weatherSearch: WeatherSearch? = null    //高德api天气搜索
    private var isDestroyView = false
    private lateinit var handler: Handler
    override fun setBaseLayoutResID(): Int = R.layout.fragment_mainother

    override fun setUp(view: View, savedInstanceState: Bundle?) {
        flBaseContent.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        handler = Handler(Looper.getMainLooper())
    }

    override fun initData() {
        doRequest(WebApiService.MAIN_ACTIVITY, WebApiService.getBaseParams(), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                CurrApplication.activityMDL = null
            }

            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, ActivityMDL::class.java)
                    if (mdl == null) handler.postDelayed({ initData() }, CurrApplication.DELAY_MILLIS)
                    else updateActivity(mdl)
                } else {
                    handler.postDelayed({ initData() }, CurrApplication.DELAY_MILLIS)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                handler.postDelayed({ initData() }, CurrApplication.DELAY_MILLIS)
            }
        })
    }

    private fun updateActivity(mdl: ActivityMDL) {
        CurrApplication.activityMDL = mdl
        ivActivity.visibility = View.VISIBLE
        ImageLoaderV4.getInstance().displayImage(context, mdl.activityicon, ivActivity)
        ivActivity.setOnClickListener {
            if (TextUtils.equals(mdl.transitionstype, ActivityMDL.Type.H5.code)) {  //跳转h5
                val content = mdl.transitionscontent
                if (content == null || content.isEmpty()) return@setOnClickListener
                if (mdl.islogin == 1) {  //需要登录
                    if (isLogin()) {
                        openWebActivity(content, "")
                    } else {
                        openActivity(LoginActivity::class.java)
                    }
                } else {
                    openWebActivity(content, "")
                }
            } else if (TextUtils.equals(mdl.transitionstype, ActivityMDL.Type.NATIVE.code)) {
                if (!isLogin()) openActivity(LoginActivity::class.java)
                else openActivity(InviteCourtesyActivity::class.java, Bundle().apply { putString("activityId", mdl.activityid) })
            }
        }
    }

    fun onLocationSuccess(location: AMapLocation) {
        val city = location.city
        val mQuery = WeatherSearchQuery(city, WeatherSearchQuery.WEATHER_TYPE_LIVE)
        weatherSearch = WeatherSearch(context).apply {
            setOnWeatherSearchListener(this@MainOtherFragment)
            query = mQuery
            searchWeatherAsyn() //异步搜索
        }
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
            handler.postDelayed({ weatherSearch?.searchWeatherAsyn() }, CurrApplication.DELAY_MILLIS)
        }
    }

    override fun onWeatherForecastSearched(localWeatherForecastResult: LocalWeatherForecastResult, rCode: Int) {

    }

    override fun onDestroyView() {
        isDestroyView = true
        CurrApplication.activityMDL = null
        super.onDestroyView()
    }
}