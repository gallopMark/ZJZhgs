package com.uroad.rxhttp.interceptor;


import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.uroad.rxhttp.constant.SPKeys;
import com.uroad.rxhttp.utils.SPUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import okhttp3.Interceptor;
import okhttp3.Response;

import static java.util.Calendar.getInstance;

/**
 * 接受服务器发的cookie   并保存到本地
 */

public class ReceivedCookiesInterceptor implements Interceptor {
    private Context context;

    public ReceivedCookiesInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        //这里获取请求返回的cookie
        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            HashSet<String> cookies = new HashSet<>(originalResponse.headers("Set-Cookie"));
            SPUtils.from(context).put(SPKeys.COOKIE, cookies);
        }
        //获取服务器相应时间--用于计算倒计时的时间差
        if (!TextUtils.isEmpty(originalResponse.header("Date"))) {
            long date = dateToStamp(originalResponse.header("Date"));
            SPUtils.from(context).put(SPKeys.DATE, date);
        }
        return originalResponse;
    }


    /**
     * 将时间转换为时间戳
     *
     * @param s date
     * @return long
     */
    private long dateToStamp(String s) {
        //转换为标准时间对象
        Date date = new Date(s);
        Calendar calendar = getInstance();
        calendar.setTime(date);
        return calendar.getTimeInMillis();
    }
}
