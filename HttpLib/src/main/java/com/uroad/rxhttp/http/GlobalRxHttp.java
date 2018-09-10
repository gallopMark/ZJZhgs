package com.uroad.rxhttp.http;

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
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

/**
 * 网络请求工具类---使用的是全局配置的变量
 */

public class GlobalRxHttp {
    private static volatile GlobalRxHttp instance;

    private GlobalRxHttp() {
    }

    public static GlobalRxHttp getInstance() {
        if (instance == null) {
            synchronized (GlobalRxHttp.class) {
                if (instance == null) {
                    instance = new GlobalRxHttp();
                }
            }
        }
        return instance;
    }

    /**
     * 设置baseUrl
     */
    public GlobalRxHttp setBaseUrl(String baseUrl) {
        getGlobalRetrofitBuilder().baseUrl(baseUrl);
        return this;
    }


    /**
     * 设置自己的client
     */
    public GlobalRxHttp setOkClient(OkHttpClient okClient) {
        getGlobalRetrofitBuilder().client(okClient);
        return this;
    }


    /**
     * 添加统一的请求头
     */
    public GlobalRxHttp setHeaders(Map<String, Object> headerMaps) {
        getGlobalOkHttpBuilder().addInterceptor(new HeaderInterceptor(headerMaps));
        return this;
    }

    /**
     * 是否开启请求日志
     */
    public GlobalRxHttp setLog(boolean isShowLog) {
        if (isShowLog) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(@NonNull String message) {
                    Log.e("RxHttpManager", message);
                }
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            getGlobalOkHttpBuilder().addInterceptor(loggingInterceptor);
        }
        return this;
    }

    /**
     * 开启缓存，缓存到默认路径
     */
    public GlobalRxHttp setCache() {
        if (RxHttpManager.getContext() != null) {
            RxCacheInterceptor rxCacheInterceptor = new RxCacheInterceptor(RxHttpManager.getContext());
            Cache cache = new Cache(new File(RxHttpManager.getContext().getCacheDir().getAbsolutePath() + "/rxHttpCacheData"), 1024 * 1024 * 100);
            getGlobalOkHttpBuilder().addInterceptor(rxCacheInterceptor)
                    .addNetworkInterceptor(rxCacheInterceptor)
                    .cache(cache);
        }
        return this;
    }

    /**
     * 设置缓存路径及缓存文件大小
     */
    public GlobalRxHttp setCache(String cachePath, long maxSize) {
        if (RxHttpManager.getContext() != null) {
            if (!TextUtils.isEmpty(cachePath) && maxSize > 0) {
                RxCacheInterceptor rxCacheInterceptor = new RxCacheInterceptor(RxHttpManager.getContext());
                Cache cache = new Cache(new File(cachePath), maxSize);
                getGlobalOkHttpBuilder()
                        .addInterceptor(rxCacheInterceptor)
                        .addNetworkInterceptor(rxCacheInterceptor)
                        .cache(cache);
            }
        }
        return this;
    }

    /*** 持久化保存cookie保存到sp文件中*/
    public GlobalRxHttp setCookie(boolean saveCookie) {
        if (saveCookie && RxHttpManager.getContext() != null) {
            getGlobalOkHttpBuilder().addInterceptor(new AddCookiesInterceptor(RxHttpManager.getContext()))
                    .addInterceptor(new ReceivedCookiesInterceptor(RxHttpManager.getContext()));
        }
        return this;
    }

    /**
     * 设置读取超时时间
     */
    public GlobalRxHttp setReadTimeout(long second) {
        getGlobalOkHttpBuilder().readTimeout(second, TimeUnit.SECONDS);
        return this;
    }

    /**
     * 设置写入超时时间
     */
    public GlobalRxHttp setWriteTimeout(long second) {
        getGlobalOkHttpBuilder().readTimeout(second, TimeUnit.SECONDS);
        return this;
    }

    /**
     * 设置连接超时时间
     */
    public GlobalRxHttp setConnectTimeout(long second) {
        getGlobalOkHttpBuilder().readTimeout(second, TimeUnit.SECONDS);
        return this;
    }

    /**
     * 信任所有证书,不安全有风险
     */
    public GlobalRxHttp setSslSocketFactory() {
        SSLUtils.SSLParams sslParams = SSLUtils.getSslSocketFactory();
        getGlobalOkHttpBuilder().sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
        return this;
    }

    /**
     * 使用预埋证书，校验服务端证书（自签名证书）
     */
    public GlobalRxHttp setSslSocketFactory(InputStream... certificates) {
        SSLUtils.SSLParams sslParams = SSLUtils.getSslSocketFactory(certificates);
        getGlobalOkHttpBuilder().sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
        return this;
    }

    /**
     * 使用bks证书和密码管理客户端证书（双向认证），使用预埋证书，校验服务端证书（自签名证书）
     */
    public GlobalRxHttp setSslSocketFactory(InputStream bksFile, String password, InputStream... certificates) {
        SSLUtils.SSLParams sslParams = SSLUtils.getSslSocketFactory(bksFile, password, certificates);
        getGlobalOkHttpBuilder().sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
        return this;
    }


    /**
     * 全局的 retrofit
     */
    public static Retrofit getGlobalRetrofit() {
        return RetrofitClient.getInstance().getRetrofit();
    }

    /**
     * 全局的 RetrofitBuilder
     */
    public Retrofit.Builder getGlobalRetrofitBuilder() {
        return RetrofitClient.getInstance().getRetrofitBuilder();
    }

    public OkHttpClient.Builder getGlobalOkHttpBuilder() {
        return HttpClient.getInstance().getBuilder();
    }

    /**
     * 使用全局变量的请求
     */
    public static <K> K createGApi(final Class<K> cls) {
        return getGlobalRetrofit().create(cls);
    }
}
