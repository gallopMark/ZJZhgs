package com.uroad.rxhttp.observer;


import com.uroad.rxhttp.RxHttpManager;
import com.uroad.rxhttp.base.BaseObserver;

import io.reactivex.disposables.Disposable;

/**
 * 通用的Observer
 * 用户可以根据自己需求自定义自己的类继承BaseObserver<T>即可
 */

public abstract class CommonObserver<T> extends BaseObserver<T> {


    public CommonObserver() {
    }

    /**
     * 失败回调
     */
    protected abstract void onError(String errorMsg);

    /**
     * 成功回调
     */
    protected abstract void onSuccess(T t);


    @Override
    public void doOnSubscribe(Disposable d) {
        RxHttpManager.addDisposable(d);
    }

    @Override
    public void doOnError(String errorMsg) {
        onError(errorMsg);
    }

    @Override
    public void doOnNext(T t) {
        onSuccess(t);
    }

    @Override
    public void doOnCompleted() {
    }
}
