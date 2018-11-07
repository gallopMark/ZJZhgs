package com.uroad.zhgs.webservice.upload

import io.reactivex.observers.DefaultObserver

abstract class FileUploadObserver<T> : DefaultObserver<T>() {
    override fun onStart() {
        onUploadStart()
    }

    override fun onNext(t: T) {
        onUploadSuccess(t)
    }

    override fun onError(e: Throwable) {
        onUploadFail(e)
    }

    override fun onComplete() {
        onUploadComplete()
    }

    open fun onUploadStart() {}
    open fun onUploadComplete() {}

    // 上传成功的回调
    abstract fun onUploadSuccess(t: T)

    // 上传失败回调
    abstract fun onUploadFail(e: Throwable)

}
