package com.uroad.zhgs.model

import android.text.TextUtils
import com.uroad.zhgs.R



/**
 *Created by MFB on 2018/8/15.
 * 天气数据
 * 	"temperature": "31°C",
"weather": "晴",
"city": "杭州市",
"Icon": null,
"latitude": "120.2099470",
"longitude": "30.2458530"
 */
class WeatherMDL : MutilItem {
    override fun getItemType(): Int = 4
    var temperature: String? = null
    var weather: String? = null
    var city: String? = null
    var Icon: String? = null
    var latitude: Double? = 0.0
    var longitude: Double? = 0.0
    fun latitude(): Double {
        latitude?.let { return it }
        return 0.0
    }

    fun longitude(): Double {
        longitude?.let { return it }
        return 0.0
    }

    companion object {
        fun getWeatherIco(weather: String?): Int {
            if (!TextUtils.isEmpty(weather)) {
                if (weather == "晴") return R.mipmap.weathy_01
                if (weather == "多云") return R.mipmap.weathy_02
                if (weather == "阴") return R.mipmap.weathy_03
                if (weather == "阵雨") return R.mipmap.weathy_04
                if (weather == "雷阵雨") return R.mipmap.weathy_05
                if (weather == "雷阵雨并伴有冰雹") return R.mipmap.weathy_06
                if (weather == "雨夹雪" || weather == "冻雨") return R.mipmap.weathy_07
                if (weather == "小雨") return R.mipmap.weathy_08
                if (weather == "中雨" || weather == "小雨-中雨") return R.mipmap.weathy_09
                if (weather == "大雨" || weather == "中雨-大雨") return R.mipmap.weathy_10
                if (weather == "大雨-暴雨"
                        || weather == "暴雨"
                        || weather == "大暴雨"
                        || weather == "暴雨-大暴雨"
                        || weather == "特大暴雨"
                        || weather == "大暴雨-特大暴雨")
                    return R.mipmap.weathy_11
                if (weather == "阵雪" || weather == "小雪")
                    return R.mipmap.weathy_12
                if (weather == "中雪" || weather == "小雪-中雪")
                    return R.mipmap.weathy_13
                if (weather == "大雪" || weather == "中雪-大雪")
                    return R.mipmap.weathy_14
                if (weather == "暴雪" || weather == "大雪-暴雪"
                        || weather == "")
                    return R.mipmap.weathy_15
                if (weather == "雾") return R.mipmap.weathy_22
                if (weather == "沙尘暴") return R.mipmap.weathy_16
                if (weather == "浮尘") return R.mipmap.weathy_17
                if (weather == "扬沙" || weather == "强沙尘暴") return R.mipmap.weathy_18
                if (weather == "飑") return R.mipmap.weathy_19
                if (weather == "龙卷风") return R.mipmap.weathy_20
                if (weather == "弱高吹雪") return R.mipmap.weathy_21
                if (weather == "轻霾") return R.mipmap.weathy_23
                if (weather == "霾") return R.mipmap.weathy_24
            }
            return 0
        }
    }
}