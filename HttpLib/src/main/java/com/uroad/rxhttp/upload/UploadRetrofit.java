package com.uroad.rxhttp.upload;


import com.google.gson.JsonObject;
import com.uroad.rxhttp.exception.ApiException;
import com.uroad.rxhttp.interceptor.Transformer;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 为上传单独建一个retrofit
 */

public class UploadRetrofit {

    private Retrofit.Builder mBuilder;

    private UploadRetrofit() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        mBuilder = new Retrofit.Builder()
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://www.xxx.com/");  //设置后不会报错
    }

    public static UploadRetrofit get() {
        return new UploadRetrofit();
    }

    private Retrofit retrofit() {
        return mBuilder.build();
    }

    public static Disposable uploadFile(String url, String filePath, String fileKey, HashMap<String, String> params) {
        File file = new File(filePath);
        return uploadFile(url, file, fileKey, params, null);
    }

    public static Disposable uploadFile(String url, String filePath, String fileKey, HashMap<String, String> params, UploadListener listener) {
        File file = new File(filePath);
        return uploadFile(url, file, fileKey, params, listener);
    }

    public static Disposable uploadFile(String url, File file, String fileKey, HashMap<String, String> params, final UploadListener listener) {
        RequestBody requestBody = RequestBody.create(MediaType.parse(guessMimeType(file.getName())), file);
        RequestBodyWrapper wrapper = new RequestBodyWrapper(requestBody, listener);
        if (fileKey == null) fileKey = "filename";
        MultipartBody.Part part = MultipartBody.Part.createFormData(fileKey, file.getName(), wrapper);
        if (params == null) params = new HashMap<>();
        return doOnSubscribe(UploadRetrofit.get()
                .retrofit()
                .create(UploadFileApi.class)
                .uploadFile(url, part, params), listener);
    }

    public static Disposable uploadFileWithRaw(String url, File file, String fileKey, HashMap<String, Object> params, final UploadListener listener) {
        RequestBody requestBody = RequestBody.create(MediaType.parse(guessMimeType(file.getName())), file);
        RequestBodyWrapper wrapper = new RequestBodyWrapper(requestBody, listener);
        if (fileKey == null) fileKey = "filename";
        MultipartBody.Part part = MultipartBody.Part.createFormData(fileKey, file.getName(), wrapper);
        if (params == null) params = new HashMap<>();
        return doOnSubscribe(UploadRetrofit.get()
                .retrofit()
                .create(UploadFileApi.class)
                .uploadFileWithRaw(url, part, UploadFileApi.Service.createRequestBody(params)), listener);
    }

    private static Disposable doOnSubscribe(Observable<ResponseBody> observable, final UploadListener listener) {
        return observable.compose(Transformer.<ResponseBody>switchSchedulers())
                .subscribe(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody responseBody) {
                        if (listener != null) listener.onSuccess(responseBody);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        String errorMsg = ApiException.handleException(throwable).getMessage();
                        if (listener != null) listener.onFailure(throwable, errorMsg);
                    }
                }, new Action() {
                    @Override
                    public void run() {
                        if (listener != null) listener.onComplete();
                    }
                }, new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) {
                        if (listener != null) listener.onStart(disposable);
                    }
                });
    }

    private static String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }

    public static Disposable uploadFiles(String url, List<String> filePaths, String fileKey, HashMap<String, Object> params, final MultiUploadListener listener) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (int i = 0; i < filePaths.size(); i++) {
            File file = new File(filePaths.get(i));
            RequestBody requestBody = RequestBody.create(MediaType.parse(guessMimeType(file.getName())), file);
            MultiRequestBodyWrapper wrapper = new MultiRequestBodyWrapper(file, requestBody, listener);
            //后台接收图片流的参数名
            if (fileKey == null) fileKey = "filename";
            builder.addFormDataPart(fileKey, file.getName(), wrapper);
        }
        List<MultipartBody.Part> parts = builder.build().parts();
        if (params == null) params = new HashMap<>();
        return doOnSubscribe(UploadRetrofit
                .get()
                .retrofit()
                .create(UploadFileApi.class)
                .uploadFiles(url, parts, params), listener);
    }

    public static Disposable uploadFilesWithRaw(String url, List<String> filePaths, String fileKey, HashMap<String, Object> params, final MultiUploadListener listener) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (int i = 0; i < filePaths.size(); i++) {
            File file = new File(filePaths.get(i));
            RequestBody requestBody = RequestBody.create(MediaType.parse(guessMimeType(file.getName())), file);
            MultiRequestBodyWrapper wrapper = new MultiRequestBodyWrapper(file, requestBody, listener);
            //后台接收图片流的参数名
            if (fileKey == null) fileKey = "filename";
            builder.addFormDataPart(fileKey, file.getName(), wrapper);
        }
        List<MultipartBody.Part> parts = builder.build().parts();
        if (params == null) params = new HashMap<>();
        return doOnSubscribe(UploadRetrofit.get().retrofit()
                .create(UploadFileApi.class)
                .uploadFilesWithRaw(url, parts, UploadFileApi.Service.createRequestBody(params)), listener);
    }

    private static Disposable doOnSubscribe(Observable<ResponseBody> observable, final MultiUploadListener listener) {
        return observable.compose(Transformer.<ResponseBody>switchSchedulers())
                .subscribe(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody body) {
                        if (listener != null) listener.onSuccess(body);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        String errorMsg = ApiException.handleException(throwable).getMessage();
                        if (listener != null) listener.onFailure(throwable, errorMsg);
                    }
                }, new Action() {
                    @Override
                    public void run() {
                        if (listener != null) listener.onComplete();
                    }
                }, new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) {
                        if (listener != null) listener.onStart(disposable);
                    }
                });
    }
}
