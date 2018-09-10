package com.uroad.rxhttp.observer;

import com.uroad.rxhttp.RxHttpManager;
import com.uroad.rxhttp.base.BaseStringObserver;

import io.reactivex.disposables.Disposable;


/**
 * 自定义Observer 处理string回调
 */

public abstract class StringObserver extends BaseStringObserver {


    public StringObserver() {
    }

    /**
     * 失败回调
     *
     * @param errorMsg 错误信息
     */
    protected abstract void onError(Throwable e, String errorMsg);

    /**
     * 成功回调
     *
     * @param data 结果
     */
    protected abstract void onSuccess(String data);


    @Override
    public void doOnSubscribe(Disposable d) {
        RxHttpManager.addDisposable(d);
    }

    @Override
    public void doOnError(Throwable e, String errorMsg) {
        onError(e, errorMsg);
    }

    @Override
    public void doOnNext(String string) {
        onSuccess(string);
    }


    @Override
    public void doOnCompleted() {
    }

}
