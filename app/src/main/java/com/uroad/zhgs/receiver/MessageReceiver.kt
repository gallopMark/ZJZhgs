package com.uroad.zhgs.receiver

import android.content.Context
import android.util.Log
import com.tencent.android.tpush.*


/**
 * @author MFB
 * @create 2018/11/13
 * @describe 信鸽推送广播接收器
 */
class MessageReceiver : XGPushBaseReceiver() {
    override fun onSetTagResult(context: Context?, errorCode: Int, tagName: String?) {
        Log.e("xgPush", "onSetTagResult")
    }

    override fun onNotifactionShowedResult(context: Context?, notifiShowedRlt: XGPushShowedResult?) {
        Log.e("xgPush", "onNotifactionShowedResult")
    }

    override fun onUnregisterResult(context: Context?, errorCode: Int) {
        Log.e("xgPush", "onUnregisterResult")
    }

    override fun onDeleteTagResult(context: Context?, errorCode: Int, tagName: String?) {
        Log.e("xgPush", "onDeleteTagResult")
    }

    override fun onRegisterResult(context: Context?, errorCode: Int, result: XGPushRegisterResult?) {
        Log.e("xgPush", "onRegisterResult")
    }

    override fun onTextMessage(context: Context?, message: XGPushTextMessage?) {
        Log.e("xgPush", "onTextMessage")
        if (context == null) return
        message?.content?.let { Log.e("xgPush", it) }
    }

    override fun onNotifactionClickedResult(context: Context?, result: XGPushClickedResult?) {
        Log.e("xgPush", "onNotifactionClickedResult")
    }
}
