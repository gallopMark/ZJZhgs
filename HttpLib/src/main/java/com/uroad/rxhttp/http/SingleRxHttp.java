package com.uroad.rxhttp.http;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.uroad.rxhttp.RxHttpManager;
import com.uroad.rxhttp.interceptor.AddCookiesInterceptor;
import com.uroad.rxhttp.interceptor.RxCacheInterceptor;
import com.uroad.rxhttp.interceptor.HeaderInterceptor;
import com.uroad.rxhttp.interceptor.ReceivedCookiesInterceptor;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * 网络请求-----可以对每个请求单独配置参数
 */

public class SingleRxHttp {
    private String baseUrl;
    private Map<String, Object> headerMaps = new HashMap<>();
    private boolean isShowLog = false;
    private boolean cache = false;
    private boolean saveCookie = true;
    private String cachePath;
    private long cacheMaxSize;
    private long readTimeout;
    private long writeTimeout;
    private long connectTimeout;
    private SSLUtils.SSLParams sslParams;
    private OkHttpClient okClient;
    private List<Converter.Factory> converterFactories = new ArrayList<>();
    private List<CallAdapter.Factory> adapterFactories = new ArrayList<>();

    private SingleRxHttp() {
    }

    /**
     * 不使用单利模式是因为单个请求的参数配置是单次有效的
     *
     * @return SingleRxHttp
     */
    public static SingleRxHttp getInstance() {
        return new SingleRxHttp();
    }

    public SingleRxHttp baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    /**
     * 局部设置Converter.Factory,默认GsonConverterFactory.create()
     */
    public SingleRxHttp addConverterFactory(Converter.Factory factory) {
        if (factory != null) {
            converterFactories.add(factory);
        }
        return this;
    }

    /**
     * 局部设置CallAdapter.Factory,默认RxJavaCallAdapterFactory.create()
     */
    public SingleRxHttp addCallAdapterFactory(CallAdapter.Factory factory) {
        if (factory != null) {
            adapterFactories.add(factory);
        }
        return this;
    }

    public SingleRxHttp addHeaders(Map<String, Object> headerMaps) {
        this.headerMaps = headerMaps;
        return this;
    }

    public SingleRxHttp log(boolean isShowLog) {
        this.isShowLog = isShowLog;
        return this;
    }

    public SingleRxHttp cache(boolean cache) {
        this.cache = cache;
        return this;
    }

    public SingleRxHttp saveCookie(boolean saveCookie) {
        this.saveCookie = saveCookie;
        return this;
    }

    public SingleRxHttp cachePath(String cachePath, long maxSize) {
        this.cachePath = cachePath;
        this.cacheMaxSize = maxSize;
        return this;
    }

    public SingleRxHttp readTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public SingleRxHttp writeTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    public SingleRxHttp connectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * 信任所有证书,不安全有风险
     */
    public SingleRxHttp sslSocketFactory() {
        sslParams = SSLUtils.getSslSocketFactory();
        return this;
    }

    /**
     * 使用预埋证书，校验服务端证书（自签名证书）
     */
    public SingleRxHttp sslSocketFactory(InputStream... certificates) {
        sslParams = SSLUtils.getSslSocketFactory(certificates);
        return this;
    }

    /**
     * 使用bks证书和密码管理客户端证书（双向认证），使用预埋证书，校验服务端证书（自签名证书）
     */
    public SingleRxHttp sslSocketFactory(InputStream bksFile, String password, InputStream... certificates) {
        sslParams = SSLUtils.getSslSocketFactory(bksFile, password, certificates);
        return this;
    }

    public SingleRxHttp client(OkHttpClient okClient) {
        this.okClient = okClient;
        return this;
    }

    /**
     * 使用自己自定义参数创建请求
     */
    public <K> K createSApi(Class<K> cls) {
        return getSingleRetrofitBuilder().build().create(cls);
    }

    /**
     * 单个RetrofitBuilder
     */
    public Retrofit.Builder getSingleRetrofitBuilder() {
        Retrofit.Builder builder = new Retrofit.Builder();
        if (converterFactories.isEmpty()) {
            //获取全局的对象重新设置
            List<Converter.Factory> listConverterFactory = RetrofitClient.getInstance().getRetrofit().converterFactories();
            for (Converter.Factory factory : listConverterFactory) {
                builder.addConverterFactory(factory);
            }
        } else {
            for (Converter.Factory converterFactory : converterFactories) {
                builder.addConverterFactory(converterFactory);
            }
        }
        if (adapterFactories.isEmpty()) {
            //获取全局的对象重新设置
            List<CallAdapter.Factory> listAdapterFactory = RetrofitClient.getInstance().getRetrofit().callAdapterFactories();
            for (CallAdapter.Factory factory : listAdapterFactory) {
                builder.addCallAdapterFactory(factory);
            }
        } else {
            for (CallAdapter.Factory adapterFactory : adapterFactories) {
                builder.addCallAdapterFactory(adapterFactory);
            }
        }
        if (TextUtils.isEmpty(baseUrl)) {
            builder.baseUrl(RetrofitClient.getInstance().getRetrofit().baseUrl());
        } else {
            builder.baseUrl(baseUrl);
        }
        builder.client(okClient == null ? getSingleOkHttpBuilder().build() : okClient);
        return builder;
    }

    /**
     * 获取单个 OkHttpClient.Builder
     */
    private OkHttpClient.Builder getSingleOkHttpBuilder() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.retryOnConnectionFailure(true);
        builder.addInterceptor(new HeaderInterceptor(headerMaps));
        if (cache && RxHttpManager.getContext() != null) {
            RxCacheInterceptor rxCacheInterceptor = new RxCacheInterceptor(RxHttpManager.getContext());
            Cache cache;
            if (!TextUtils.isEmpty(cachePath) && cacheMaxSize > 0) {
                cache = new Cache(new File(cachePath), cacheMaxSize);
            } else {
                cache = new Cache(new File(Environment.getExternalStorageDirectory().getPath() + "/rxHttpCacheData")
                        , 1024 * 1024 * 100);
            }
            builder.addInterceptor(rxCacheInterceptor)
                    .addNetworkInterceptor(rxCacheInterceptor)
                    .cache(cache);
        }
        if (isShowLog) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(@NonNull String message) {
                    Log.e("RxHttpManager", message);
                }
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }
        if (saveCookie && RxHttpManager.getContext() != null) {
            builder.addInterceptor(new AddCookiesInterceptor(RxHttpManager.getContext()))
                    .addInterceptor(new ReceivedCookiesInterceptor(RxHttpManager.getContext()));
        }
        builder.readTimeout(readTimeout > 0 ? readTimeout : 10, TimeUnit.SECONDS);
        builder.writeTimeout(writeTimeout > 0 ? writeTimeout : 10, TimeUnit.SECONDS);
        builder.connectTimeout(connectTimeout > 0 ? connectTimeout : 10, TimeUnit.SECONDS);
        if (sslParams != null) {
            builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
        }
        return builder;
    }
}
