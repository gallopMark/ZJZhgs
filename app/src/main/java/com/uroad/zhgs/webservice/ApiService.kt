package com.uroad.zhgs.webservice

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.uroad.library.utils.DeviceUtils
import com.uroad.mqtt.MqttService
import com.uroad.zhgs.utils.AndroidBase64Utils
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*


/**
 *Created by MFB on 2018/7/26.
 */
interface ApiService {
    companion object {
        var isDebug = true
        // http://zhgs.u-road.com/ZJAppApi/index.php/api/
        //http://10.126.241.9
        // http://zjzhgs.u-road.com:11001  zhgs.u-road.com  http://zjgd.u-road.com/ZJAppApi/index.php/api
        const val BASE_URL = "http://zjzhgs.u-road.com:11001/ZJAppApi/index.php/api/"
        const val BASE_DEBUG_URL = "http://zhgs.u-road.com/ZJAppApi/index.php/api/"
        const val SHOPPING_URL = "https://h5.youzan.com/wscshop/feature/FzaSlSjOX1"
        //115.238.84.148 11012  47.96.92.29:9000
        const val MQTT_SERVICEURL = "tcp://115.238.84.148:11012"
        const val MQTT_USER = "zhejiang"
        const val MQTT_PASSWORD = "uroad123"
        const val MQTT_TOPIC = "Rescue/"
        const val TOPIC_CLOSE_TEAM = "ZhgsMq/CloseTeam/"  //解散车队
        const val TOPIC_ADD_TEAM = "ZhgsMq/AddTeam/"  //加入车队
        const val TOPIC_QUIT_TEAM = "ZhgsMq/QuitTeam/"  //退出车队
        const val TOPIC_SEND_MSG = "ZhgsMq/TeamSendMsg/"  //发送语音消息
        const val TOPIC_LATLNG_UPDATE = "ZhgsMq/TeamLocUpdate/" //成员位置发送变动后需要通知车队其他人员更新信息
        const val TOPIC_PLACE_UPDATE = "ZhgsMq/TeamPlaceUpdate/" //目的地更新通知车队各成员
        const val TOPIC_MSG_CALLBACK = "ZhgsMq/CallBack/" //接口响应通知操作人结果。   Java后台触发
        // const val UPLOAD_URL = "http://zhgs.u-road.com/ZJAppApi/index.php/api/Upload/uploadImg"
        fun createRequestBody(params: Map<String, String?>, method: String?): RequestBody {
            val map = HashMap<String, String?>().apply { put("fun", method) }
            val param = Gson().toJson(params)
            val jsonObject1 = JsonParser().parse(param).asJsonObject
            val jsonObject2 = JsonParser().parse(Gson().toJson(map)).asJsonObject
            jsonObject2.add("data", jsonObject1)
            val content = AndroidBase64Utils.encodeToString(jsonObject2.toString())
            return RequestBody.create(MediaType.parse("application/json"), content)
        }

        fun buildMQTTService(context: Context): MqttService = MqttService.Builder(context)
                .autoReconnect(true)
                .clientId(DeviceUtils.getUniqueId(context))
                .serverUrl(MQTT_SERVICEURL)
                .userName(MQTT_USER)
                .passWord(MQTT_PASSWORD)
                .timeOut(30)
                .keepAliveInterval(10)
                .create()
    }

    @POST("ApiIndex")
    fun doPost(@Body body: RequestBody): Observable<String>

    /**
     * 上传
     */
    @Multipart
    @POST("Upload/uploadImg")
    fun uploadFile(@Part part: MultipartBody.Part): Observable<ResponseBody>

    /**
     * 上传
     */
    @Multipart
    @POST("Upload/uploadImg")
    fun uploadFile(@Part part: MultipartBody.Part, @PartMap params: Map<String, String>): Observable<ResponseBody>
}