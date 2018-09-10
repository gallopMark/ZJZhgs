package com.uroad.rxhttp.callback;


import io.reactivex.disposables.Disposable;

/**
 * Created by MFB on 2018/6/4.
 */
public abstract class RequestCallback {
    public void onStart(Disposable disposable) {
    }

    public abstract void onResponse(String response);

    public abstract void onFailure(Throwable e, String errorMsg);

    public void onComplete() {
    }
}
