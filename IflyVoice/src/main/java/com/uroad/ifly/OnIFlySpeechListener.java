package com.uroad.ifly;

import android.os.Bundle;

import com.iflytek.cloud.SpeechError;

public interface OnIFlySpeechListener {
    void onInitSuccess();

    void onInitFailure(int errorCode);

    void onSpeakBegin();

    void onBufferProgress(int percent, int beginPos, int endPos, String info);

    void onSpeakPaused();

    void onSpeakResumed();

    void onSpeakProgress(int percent, int beginPos, int endPos);

    void onCompleted(SpeechError speechError);

    void onEvent(int eventType, int arg1, int arg2, Bundle obj);

}
