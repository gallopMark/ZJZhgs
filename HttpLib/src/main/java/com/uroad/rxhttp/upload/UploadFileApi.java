package com.uroad.rxhttp.upload;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Url;

/**
 * @author Allen
 * 文件上传
 */

public interface UploadFileApi {

    class Service {
        public static RequestBody createRequestBody(Map<String, Object> params) {
            String json = new Gson().toJson(params);
            return RequestBody.create(MediaType.parse("application/json"), json);
        }
    }

    /**
     * 上传
     *
     * @param url 地址
     * @return ResponseBody
     */
    @Multipart
    @POST
    Observable<ResponseBody> uploadFile(@Url String url, @Part MultipartBody.Part part, @PartMap Map<String, String> params);

    @Multipart
    @POST
    Observable<ResponseBody> uploadFileWithRaw(@Url String url, @Part MultipartBody.Part part, @Body RequestBody body);

    /**
     * 上传多个文件（带参数）
     *
     * @param url    地址
     * @param parts  文件集合
     * @param params 参数
     * @return ResponseBody
     */
    @Multipart
    @POST
    Observable<ResponseBody> uploadFiles(@Url String url, @Part List<MultipartBody.Part> parts, @PartMap HashMap<String, Object> params);

    @Multipart
    @POST
    Observable<ResponseBody> uploadFilesWithRaw(@Url String url, @Part List<MultipartBody.Part> parts, @Body RequestBody body);
}
