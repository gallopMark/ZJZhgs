package com.uroad.rxhttp.post;

import com.google.gson.Gson;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * Created by MFB on 2018/6/4.
 */
public interface PostApi {
    @FormUrlEncoded
    @POST
    Observable<String> post(@Url String url, @FieldMap Map<String, Object> params);

    @POST
    Observable<String> postWithRaw(@Url String url, @Body RequestBody body);

    class Service {
        public static RequestBody createRequestBody(Map<String, Object> params) {
            String json = new Gson().toJson(params);
            return RequestBody.create(MediaType.parse("application/json"), json);
        }
    }
}
