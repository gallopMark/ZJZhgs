package com.uroad.library.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by MFB on 2018/8/13.
 */
public class PhoneUtils {
    /**
     * 调用拨号界面
     *
     * @param phone 电话号码
     */
    public static void call(Context context, String phone) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}