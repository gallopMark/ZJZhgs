package com.uroad.rxhttp.download;


import android.text.TextUtils;

import com.uroad.rxhttp.exception.ApiException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * 为下载单独建一个retrofit
 */

public class DownloadRetrofit {

    private Retrofit.Builder mBuilder;

    private DownloadRetrofit() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        mBuilder = new Retrofit.Builder()
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl("http://www.xxx.com/");
    }

    public static DownloadRetrofit get() {
        return new DownloadRetrofit();
    }

    private Retrofit retrofit() {
        return mBuilder.build();
    }

    public static Disposable downloadFile(final String url, final String destFilePath, final String destFileName, final DownloadListener downloadListener) {
        return DownloadRetrofit.get()
                .retrofit()
                .create(DownloadApi.class)
                .downloadFile(url)
                .map(new Function<ResponseBody, String>() {
                    @Override
                    public String apply(ResponseBody responseBody) throws Exception {
                        return saveFile(responseBody, url, destFilePath, destFileName, downloadListener);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String filePath) {
                        if (downloadListener != null) {
                            downloadListener.onFinish(filePath);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        String errorMsg = ApiException.handleException(throwable).getMessage();
                        if (downloadListener != null) downloadListener.onError(throwable, errorMsg);
                    }
                }, new Action() {
                    @Override
                    public void run() {
                        if (downloadListener != null) downloadListener.onComplete();
                    }
                }, new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) {
                        if (downloadListener != null) downloadListener.onStart(disposable);
                    }
                });
    }

    private static String saveFile(ResponseBody response, String url, String destFilePath, String destFileName, final DownloadListener downloadListener) throws IOException {
        final long contentLength = response.contentLength();
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len;
        FileOutputStream fos = null;
        try {
            is = response.byteStream();
            long sum = 0;
            File dir = new File(destFilePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (destFileName == null) destFileName = getFileNameFromUrl(url);
            File file = new File(dir, destFileName);
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                sum += len;
                fos.write(buf, 0, len);
                final long finalSum = sum;
                if (downloadListener != null) {
                    downloadListener.onProgress(finalSum, contentLength, (int) ((finalSum * 1.0f / contentLength) * 100));
                }
            }
            fos.flush();
            return file.getAbsolutePath();
        } finally {
            try {
                response.close();
                if (is != null)
                    is.close();
            } catch (IOException e) {
            }
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
            }
        }
    }

    private static String getFileNameFromUrl(String url) {
        if (!TextUtils.isEmpty(url) && url.lastIndexOf("/") > 0) {
            int lastIndex = url.lastIndexOf("/") + 1;
            return url.substring(lastIndex, url.length());
        }
        return "";
    }
}
