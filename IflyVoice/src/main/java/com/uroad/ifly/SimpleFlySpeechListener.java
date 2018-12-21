package com.uroad.ifly;

import android.os.Bundle;

import com.iflytek.cloud.SpeechError;

public class SimpleFlySpeechListener implements OnIFlySpeechListener {
    @Override
    public void onInitSuccess() {

    }

    @Override
    public void onInitFailure(int errorCode) {

    }

    @Override
    public void onSpeakBegin() {

    }

    @Override
    public void onBufferProgress(int percent, int beginPos, int endPos, String info) {

    }

    @Override
    public void onSpeakPaused() {

    }

    @Override
    public void onSpeakResumed() {

    }

    @Override
    public void onSpeakProgress(int percent, int beginPos, int endPos) {

    }

    @Override
    public void onCompleted(SpeechError speechError) {

    }

    @Override
    public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

    }
}
