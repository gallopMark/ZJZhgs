package com.uroad.rxhttp.post;

import com.uroad.rxhttp.callback.RequestCallback;
import com.uroad.rxhttp.exception.ApiException;
import com.uroad.rxhttp.interceptor.Transformer;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by MFB on 2018/6/4.
 */
public class PostRetrofit {
    private Retrofit.Builder mBuilder;

    private PostRetrofit() {
        mBuilder = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl("http://www.xxx.com/");
    }

    public static PostRetrofit get() {
        return new PostRetrofit();
    }

    private Retrofit retrofit() {
        return mBuilder.build();
    }

    public static Disposable post(String url, Map<String, Object> params, final RequestCallback callback) {
        if (params == null) params = new HashMap<>();
        Observable<String> observable = PostRetrofit.get()
                .retrofit()
                .create(PostApi.class)
                .post(url, params);
        return doOnSubscribe(observable, callback);
    }

    public static Disposable doPostRaw(String url, Map<String, Object> params, final RequestCallback callback) {
        Observable<String> observable = PostRetrofit.get()
                .retrofit()
                .create(PostApi.class)
                .postWithRaw(url, PostApi.Service.createRequestBody(params));
        return doOnSubscribe(observable, callback);
    }

    private static Disposable doOnSubscribe(Observable<String> observable, final RequestCallback callback) {
        return observable.compose(Transformer.<String>switchSchedulers())
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
