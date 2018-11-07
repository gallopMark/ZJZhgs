package com.uroad.zhgs.model.mqtt

import com.uroad.zhgs.utils.GsonUtils
import org.eclipse.paho.client.mqttv3.MqttMessage

class TeamSendMsgMDL {
    /**
     * userid: 发送人ID
    username:发送人姓名
    voicefile: 语音文件播放地址
    teamid: 车队ID
     */
    var userid: String? = null
    var username: String? = null
    var voicefile: String? = null
    var teamid: String? = null

    fun obtainMessage(): MqttMessage {
        return MqttMessage().apply {
            qos = 2
            isRetained = false
            payload = GsonUtils.fromObjectToJson(this@TeamSendMsgMDL)?.toByteArray()
        }
    }
}