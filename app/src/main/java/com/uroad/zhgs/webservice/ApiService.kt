package com.uroad.zhgs.webservice

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonParser
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
        var isDebug = false
        // http://zhgs.u-road.com/ZJAppApi/index.php/api/
        const val BASE_URL = "http://zhgs.u-road.com/ZJAppApi/index.php/api/"
        const val Base_DEBUG_URL = "http://10.126.241.9/ZJAppApi/index.php/api/"
        const val SHOPPING_URL = "https://h5.youzan.com/wscshop/feature/FzaSlSjOX1"
        const val MQTT_SERVICEURL = "tcp://47.96.92.29:9000"
        const val MQTT_USER = "zhejiang"
        const val MQTT_PASSWORD = "uroad123"
        const val MQTT_TOPIC = "Rescue/"
        // const val UPLOAD_URL = "http://zhgs.u-road.com/ZJAppApi/index.php/api/Upload/uploadImg"
        fun createRequestBody(params: Map<String, String?>, method: String?): RequestBody {
            val map = HashMap<String, String?>().apply { put("fun", method) }
            val param = Gson().toJson(params)
            val jsonObject1 = JsonParser().parse(param).asJsonObject
            val jsonObject2 = JsonParser().parse(Gson().toJson(map)).asJsonObject
            jsonObject2.add("data", jsonObject1)
            val content = Base64.encodeToString(jsonObject2.toString().toByteArray(), Base64.DEFAULT)
            return RequestBody.create(MediaType.parse("application/json"), content)
        }
    }

    @POST("ApiIndex")
    fun doPost(@Body body: RequestBody): Observable<String>

    @POST("SvgApi")  //SvgApi/getEventDataByType
    fun doSVGPost(@Body body: RequestBody): Observable<String>

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