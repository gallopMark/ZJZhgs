package com.uroad.zhgs.model.mqtt

import com.uroad.zhgs.utils.GsonUtils
import org.eclipse.paho.client.mqttv3.MqttMessage

class CloseTeamMDL {
    var teamid: String? = null
    var username: String? = null
    fun obtainMessage(): MqttMessage {
        return MqttMessage().apply {
            qos = 2
            isRetained = false
            payload = GsonUtils.fromObjectToJson(this@CloseTeamMDL)?.toByteArray()
        }
    }
}