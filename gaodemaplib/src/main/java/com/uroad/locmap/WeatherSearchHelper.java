package com.uroad.locmap;

import android.content.Context;

import com.amap.api.services.weather.LocalDayWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.uroad.locmap.model.LocalDayWeatherForecastMDL;
import com.uroad.locmap.model.LocalWeatherForecastMDL;
import com.uroad.locmap.model.LocalWeatherLiveMDL;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 王喜航 on 2017/9/8.
 */

public class WeatherSearchHelper {

    public static final int WEATHER_TYPE_LIVE=WeatherSearchQuery.WEATHER_TYPE_LIVE;//实况天气
    public static final int WEATHER_TYPE_FORECAST=WeatherSearchQuery.WEATHER_TYPE_FORECAST;//天气预报
    WeatherSearchQuery query;
    static WeatherSearchHelper weatherSearchHelper=null;
    Context mContext;
    WeatherSearch weatherSearch;
    OnWeatherSearchResult weatherSearchResult;
    private WeatherSearchHelper(Context ct) {
        super();
        this.mContext = ct;
    }
    public static WeatherSearchHelper getInstance(Context c){
        if(weatherSearchHelper==null){
            weatherSearchHelper=new WeatherSearchHelper(c);
        }

        return weatherSearchHelper;
    }

    //检索参数为城市和天气类型，实况天气为WEATHER_TYPE_LIVE、天气预报为WEATHER_TYPE_FORECAST
    public void searchWeather(String city,int type){
        query=new WeatherSearchQuery(city,type);
        weatherSearch=new WeatherSearch(mContext);
        weatherSearch.setQuery(query);
        weatherSearch.setOnWeatherSearchListener(searchListener);
        weatherSearch.searchWeatherAsyn();
    }

    WeatherSearch.OnWeatherSearchListener searchListener=new WeatherSearch.OnWeatherSearchListener() {
        @Override
        public void onWeatherLiveSearched(LocalWeatherLiveResult localWeatherLiveResult, int i) {
            if(i==1000){
                if(localWeatherLiveResult!=null&&localWeatherLiveResult.getLiveResult()!=null){
                    LocalWeatherLive live=localWeatherLiveResult.getLiveResult();
                    LocalWeatherLiveMDL item=new LocalWeatherLiveMDL();
                    item.adcode=live.getAdCode();
                    item.city=live.getCity();
                    item.humidity=live.getHumidity();
                    item.province=live.getProvince();
                    item.reportTime=live.getReportTime();
                    item.temperature=live.getTemperature();
                    item.weather=live.getWeather();
                    item.windDirection=live.getWindDirection();
                    item.windPower=live.getWindPower();
                    weatherSearchResult.onWeatherLiveSearched(item);
                }else{
                    weatherSearchResult.onWeatherLiveSearchedFailed("暂无数据");
                }
            }else{
                weatherSearchResult.onWeatherLiveSearchedFailed("错误码:"+i);
            }
        }

        @Override
        public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {
            if(i==1000){
                if(localWeatherForecastResult!=null&&localWeatherForecastResult.getForecastResult()!=null){
                    LocalWeatherForecast forecast=localWeatherForecastResult.getForecastResult();
                    LocalWeatherForecastMDL item=new LocalWeatherForecastMDL();
                    item.city=forecast.getCity();
                    item.adcode=forecast.getAdCode();
                    item.province=forecast.getProvince();
                    item.reportTime=forecast.getReportTime();
                    List<LocalDayWeatherForecastMDL> localDays=new ArrayList<LocalDayWeatherForecastMDL>();
                    if(forecast.getWeatherForecast()!=null){
                        for (LocalDayWeatherForecast localDay:forecast.getWeatherForecast()){
                            LocalDayWeatherForecastMDL day=new LocalDayWeatherForecastMDL();
                            day.date=localDay.getDate();
                            day.dayTemp=localDay.getDayTemp();
                            day.dayWeather=localDay.getDayWeather();
                            day.dayWindDirection=localDay.getDayWindDirection();
                            day.dayWindPower=localDay.getDayWindPower();
                            day.nightDirection=localDay.getNightWindDirection();
                            day.nightTemp=localDay.getNightTemp();
                            day.nightWeather=localDay.getNightWeather();
                            day.nightWindPower=localDay.getNightWindPower();
                            day.week=localDay.getWeek();
                            localDays.add(day);
                        }
                    }
                    item.localDayWeatherForecast=localDays;
                    weatherSearchResult.onWeatherForecastSearched(item);
                }else{
                    weatherSearchResult.onWeatherForecastSearchedFailed("暂无数据");
                }
            }else{
                weatherSearchResult.onWeatherForecastSearchedFailed("错误码:"+i);
            }
        }
    };

    public void setOnWeatherSearchResult(OnWeatherSearchResult listener){
        weatherSearchResult=listener;
    }

    public interface OnWeatherSearchResult{
        void onWeatherLiveSearched(LocalWeatherLiveMDL item);
        void onWeatherLiveSearchedFailed(String errMsg);
        void onWeatherForecastSearched(LocalWeatherForecastMDL item);
        void onWeatherForecastSearchedFailed(String errMsg);
    }
}
