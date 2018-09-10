package com.uroad.rxhttp.download;

import io.reactivex.disposables.Disposable;

/**
 * Created by MFB on 2018/6/4.
 */
public abstract class DownloadListener {
    /*UIThread*/
    public void onStart(Disposable disposable) {
    }

    public void onProgress(long bytesRead, long contentLength, float progress) {
    }

    public abstract void onFinish(String filePath);

    public void onComplete() {
    }

    public abstract void onError(Throwable e, String errorMsg);
}
