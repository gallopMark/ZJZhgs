package com.uroad.rxhttp.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.uroad.rxhttp.RxHttpManager;

import java.util.UUID;

/**
 * 关于应用的工具类
 */

public class AppUtils {

    /**
     * 获取手机版本号
     */
    public static String getAppVersion(Context context) {
        PackageInfo pi;
        String versionNum;
        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            versionNum = pi.versionName;
        } catch (Exception e) {
            versionNum = "0";
        }
        return versionNum;
    }

    /**
     * 获取手机唯一标识码UUID
     * 记得添加相应权限
     * android.permission.READ_PHONE_STATE
     */
    public static String getUUID(Context context) {
        String uuid = (String) SPUtils.get(context, "PHONE_UUID", "");
        if (TextUtils.isEmpty(uuid)) {
            try {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                @SuppressLint("MissingPermission") String tmDevice = telephonyManager.getDeviceId();
                @SuppressLint({"HardwareIds", "MissingPermission"}) String tmSerial = telephonyManager.getSimSerialNumber();
                String androidId = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
                UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
                String uniqueId = deviceUuid.toString();
                uuid = uniqueId;
                SPUtils.put(context, "PHONE_UUID", uuid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return uuid;
    }
}
