package com.uroad.rxhttp.upload;


import java.io.File;

import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;

/**
 * Created by MFB on 2018/6/4.
 */
public abstract class MultiUploadListener {
    public void onStart(Disposable disposable) {
    }

    public abstract void onProgress(File file, long bytesWritten, long contentLength, int progress);

    //上传成功的回调
    public abstract void onSuccess(ResponseBody responseBody);

    public void onComplete() {
    }

    //上传失败回调
    public abstract void onFailure(Throwable e, String errorMsg);
}
