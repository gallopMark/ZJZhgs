package com.uroad.locmap.model;

import java.util.List;

/**
 * Created by 王喜航 on 2017/9/9.
 */
//未来几天天气预报
public class LocalWeatherForecastMDL {
    public String province;
    public String city;
    public String adcode;
    public String reportTime;
    public List<LocalDayWeatherForecastMDL> localDayWeatherForecast;
}
