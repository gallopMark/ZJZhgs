package com.uroad.rxhttp.upload;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Decorates an OkHttp request body to count the number of bytes written when writing it. Can
 * decorate any request body, but is most useful for tracking the upload progress of large
 * multipart requests.
 */
public class MultiRequestBodyWrapper extends RequestBody {

    private File file;
    private RequestBody delegate;
    private MultiUploadListener mListener;
    private Handler handler;
    private BufferedSink bufferedSink;

    MultiRequestBodyWrapper(File file, RequestBody delegate, MultiUploadListener mListener) {
        this.file = file;
        this.delegate = delegate;
        this.mListener = mListener;
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public MediaType contentType() {
        return delegate.contentType();
    }

    @Override
    public long contentLength() {
        try {
            return delegate.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        if (bufferedSink == null) {
            bufferedSink = Okio.buffer(sink(sink));  //包装
        }
        //写入
        delegate.writeTo(bufferedSink);
        //必须调用flush，否则最后一部分数据可能不会被写入
        bufferedSink.flush();
    }

    private Sink sink(Sink sink) {
        return new ForwardingSink(sink) {
            //当前写入字节数
            long bytesWritten = 0L;
            //总字节长度，避免多次调用contentLength()方法
            long contentLength = 0L;

            @Override
            public void write(@NonNull Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == 0) {
                    //获得contentLength的值，后续不再调用
                    contentLength = contentLength();
                }
                //增加当前写入的字节数
                bytesWritten += byteCount;
                //回调上传接口
                if (mListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onProgress(file, bytesWritten, contentLength, (int) (bytesWritten * 100 / contentLength));
                        }
                    });
                }
            }
        };
    }
}
