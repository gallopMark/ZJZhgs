package com.uroad.ifly.speech;

import android.content.Context;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

public class IFlyManager {

    public static void createUtility(Context context, String appID) {
        SpeechUtility.createUtility(context, SpeechConstant.APPID + "=" + appID);
    }
}
