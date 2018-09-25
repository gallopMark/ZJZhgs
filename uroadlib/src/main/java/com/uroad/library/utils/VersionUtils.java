package com.uroad.library.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

/**
 * Created by MFB on 2018/8/12.
 */
public class VersionUtils {
    /**
     * 获取版本号
     *
     * @return 当前应用的版本号，默认是1.0.0
     */
    public static String currentVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            return "1.0.0";
        }
    }

    public static String getVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getApplicationContext().getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            return "1.0";
        }
    }

    /**
     * @return if version1 > version2, return 1, if equal, return 0, else return -1
     */
    public static boolean isNeedUpdate(String versionServer, String versionLocal) {
        if (TextUtils.isEmpty(versionServer) || TextUtils.isEmpty(versionLocal))
            return false;
        int index1 = 0;
        int index2 = 0;
        while (index1 < versionServer.length() && index2 < versionLocal.length()) {
            int[] number1 = getValue(versionServer, index1);
            int[] number2 = getValue(versionLocal, index2);
            if (number1[0] < number2[0]) {
                return false;
            } else if (number1[0] > number2[0]) {
                return true;
            } else {
                index1 = number1[1] + 1;
                index2 = number2[1] + 1;
            }
        }
        return (index1 != versionServer.length() ||
                index2 != versionLocal.length()) &&
                index1 < versionServer.length();
    }

    /**
     * @param index the starting point
     * @return the number between two dots, and the index of the dot
     */
    private static int[] getValue(String version, int index) {
        int[] value_index = new int[2];
        StringBuilder sb = new StringBuilder();
        while (index < version.length() && version.charAt(index) != '.') {
            sb.append(version.charAt(index));
            index++;
        }
        value_index[0] = Integer.parseInt(sb.toString());
        value_index[1] = index;
        return value_index;
    }
}
