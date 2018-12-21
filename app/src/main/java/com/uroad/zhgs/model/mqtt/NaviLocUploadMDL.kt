package com.uroad.zhgs.model.mqtt

import com.uroad.zhgs.utils.GsonUtils
import org.eclipse.paho.client.mqttv3.MqttMessage

class NaviLocUploadMDL {
    var longitude: Double? = 0.0 //经度
    var latitude: Double? = 0.0 //纬度
    var reqtime: Long = 0 //请求时间，时间戳
    fun obtainMessage(): MqttMessage {
        return MqttMessage().apply {
            qos = 2
            isRetained = false
            payload = GsonUtils.fromObjectToJson(this@NaviLocUploadMDL)?.toByteArray()
        }
    }
}