package com.uroad.rxhttp.get;

import com.uroad.rxhttp.callback.RequestCallback;
import com.uroad.rxhttp.exception.ApiException;
import com.uroad.rxhttp.interceptor.Transformer;
import com.uroad.rxhttp.observer.StringObserver;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by MFB on 2018/6/4.
 */
public class GetRetrofit {

    private Retrofit.Builder mBuilder;

    private GetRetrofit() {
        mBuilder = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl("http://www.xxx.com/");
    }

    public static GetRetrofit get() {
        return new GetRetrofit();
    }

    private Retrofit retrofit() {
        return mBuilder.build();
    }

    public static Disposable get(String url, Map<String, Object> params, final RequestCallback callback) {
        if (params == null) params = new HashMap<>();
        return GetRetrofit.get()
                .retrofit()
                .create(GetApi.class)
                .get(url, params)
                .compose(Transformer.<String>switchSchedulers())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        if (callback != null) callback.onResponse(s);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        if (callback != null) {
                            String error = ApiException.handleException(throwable).getMessage();
                            callback.onFailure(throwable, error);
                        }
                    }
                }, new Action() {
                    @Override
                    public void run() {
                        if (callback != null) callback.onComplete();
                    }
                }, new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) {
                        if (callback != null) callback.onStart(disposable);
                    }
                });
    }
}
