package com.uroad.rxhttp.get;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * Created by MFB on 2018/6/4.
 */
public interface GetApi {

    @GET
    Observable<String> get(@Url String url, @QueryMap Map<String, Object> params);
}
