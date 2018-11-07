package com.uroad.zhgs.model.mqtt

import com.amap.api.maps.model.LatLng
import com.uroad.zhgs.utils.GsonUtils
import org.eclipse.paho.client.mqttv3.MqttMessage

class TeamPlaceUpdateMDL {
    var teamid: String? = null //车队ID
    var teamname: String? = null // 车队名称
    var toplace: String? = null // 目的地名称
    var latitude: Double? = 0.0 //目的地纬度
    var longitude: Double? = 0.0//目的地经度
    fun getLatLng(): LatLng {
        var latitude = this.latitude
        if (latitude == null) latitude = 0.0
        var longitude = this.longitude
        if (longitude == null) longitude = 0.0
        return LatLng(latitude, longitude)
    }

    fun obtainMessage(): MqttMessage {
        return MqttMessage().apply {
            qos = 2
            isRetained = false
            payload = GsonUtils.fromObjectToJson(this@TeamPlaceUpdateMDL)?.toByteArray()
        }
    }
}