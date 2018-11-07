package com.uroad.zhgs.model.mqtt

import com.uroad.zhgs.utils.GsonUtils
import org.eclipse.paho.client.mqttv3.MqttMessage

class AddTeamMDL {
    var token: String? = null
    var userid: String? = null
    var username: String? = null
    var usericon: String? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var teamid: String? = null

    fun obtainMessage(): MqttMessage {
        return MqttMessage().apply {
            qos = 2
            isRetained = false
            payload = GsonUtils.fromObjectToJson(this@AddTeamMDL)?.toByteArray()
        }
    }
}