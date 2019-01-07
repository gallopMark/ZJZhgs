package com.uroad.ifly;

import android.content.Context;
import android.os.Bundle;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

public class IFlySynthesizer implements InitListener {
    private SpeechSynthesizer speechSynthesizer;
    private OnIFlySpeechListener onIFlySpeechListener;

    public static IFlySynthesizer create(Context context, OnIFlySpeechListener onIFlySpeechListener) {
        return new IFlySynthesizer(context, onIFlySpeechListener);
    }

    private IFlySynthesizer(Context context, OnIFlySpeechListener onIFlySpeechListener) {
        this.onIFlySpeechListener = onIFlySpeechListener;
        this.speechSynthesizer = SpeechSynthesizer.createSynthesizer(context, this);
        setParam();
    }

    private void setParam() {
        // 清空参数
        speechSynthesizer.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        speechSynthesizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置在线合成发音人
        speechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        //设置合成语速
        speechSynthesizer.setParameter(SpeechConstant.SPEED, "50");
        //设置合成音调
        speechSynthesizer.setParameter(SpeechConstant.PITCH, "50");
        //设置合成音量
        speechSynthesizer.setParameter(SpeechConstant.VOLUME, "50");
        //设置播放器音频流类型
        speechSynthesizer.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        speechSynthesizer.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        speechSynthesizer.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
    }

    public void setOnIFlySpeechListener(OnIFlySpeechListener onIFlySpeechListener) {
        this.onIFlySpeechListener = onIFlySpeechListener;
    }

    public void startSpeaking(String text) {
        speechSynthesizer.startSpeaking(text, new SynthesizerListener() {
            @Override
            public void onSpeakBegin() {
                if (onIFlySpeechListener != null) onIFlySpeechListener.onSpeakBegin();
            }

            @Override
            public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
                if (onIFlySpeechListener != null)
                    onIFlySpeechListener.onBufferProgress(percent, beginPos, endPos, info);
            }

            @Override
            public void onSpeakPaused() {
                if (onIFlySpeechListener != null) onIFlySpeechListener.onSpeakPaused();
            }

            @Override
            public void onSpeakResumed() {
                if (onIFlySpeechListener != null) onIFlySpeechListener.onSpeakResumed();
            }

            @Override
            public void onSpeakProgress(int percent, int beginPos, int endPos) {
                if (onIFlySpeechListener != null)
                    onIFlySpeechListener.onSpeakProgress(percent, beginPos, endPos);
            }

            @Override
            public void onCompleted(SpeechError speechError) {
                if (onIFlySpeechListener != null) onIFlySpeechListener.onCompleted(speechError);
            }

            @Override
            public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
                if (onIFlySpeechListener != null)
                    onIFlySpeechListener.onEvent(eventType, arg1, arg2, obj);
            }
        });
    }

    @Override
    public void onInit(int code) {
        if (code == ErrorCode.SUCCESS) {
            if (onIFlySpeechListener != null) onIFlySpeechListener.onInitSuccess();
        } else {
            if (onIFlySpeechListener != null) onIFlySpeechListener.onInitFailure(code);
        }
    }

    public void setParameter(String key, String value) {
        speechSynthesizer.setParameter(key, value);
    }

    public String getParameter(String key) {
        return speechSynthesizer.getParameter(key);
    }

    public boolean isSpeaking() {
        return speechSynthesizer.isSpeaking();
    }

    public void stopSpeaking() {
        speechSynthesizer.stopSpeaking();
    }

    public void pauseSpeaking() {
        speechSynthesizer.pauseSpeaking();
    }

    public void resumeSpeaking() {
        speechSynthesizer.resumeSpeaking();
    }

    public void destroy() {
        speechSynthesizer.destroy();
    }
}
