package com.uroad.locmap;

import android.content.Context;
import android.os.Bundle;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.uroad.locmap.util.ToastUtil;

/**
 * 讯飞语音播报组件
 * 
 */
public class VoiceRead implements SynthesizerListener {

	int textIndex;
	SpeechSynthesizer mTts = null;
	Context mContext;
	ReadInterface  readListener;

	public VoiceRead(Context context) {
		mContext = context;
		// 1.创建 SpeechSynthesizer 对象, 第二个参数：本地合成时传 InitListener
		mTts = SpeechSynthesizer.createSynthesizer(mContext, null);
		// 2.合成参数设置，详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
		mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");// 设置发音人
		mTts.setParameter(SpeechConstant.SPEED, "50");// 设置语速
		mTts.setParameter(SpeechConstant.VOLUME, "80");// 设置音量，范围 0~100
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); // 设置云端
	}

	/**
	 * 传入播放参数
	 * @param context
	 * @param voiceName 发音人
	 * @param speed	语速
	 * @param volume 音量，范围 0~100
	 * @param engineType 
	 */
	public VoiceRead(Context context, String voiceName, String speed,
			String volume, String engineType) {
		mContext = context;
		// 1.创建 SpeechSynthesizer 对象, 第二个参数：本地合成时传 InitListener
		mTts = SpeechSynthesizer.createSynthesizer(mContext, null);
		// 2.合成参数设置，详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
		mTts.setParameter(SpeechConstant.VOICE_NAME, voiceName);// 设置发音人
		mTts.setParameter(SpeechConstant.SPEED, speed);// 设置语速
		mTts.setParameter(SpeechConstant.VOLUME, volume);// 设置音量，范围 0~100
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, engineType); // 设置云端
	}
	
	/**
	 * 开始播报
	 * @param content
	 */
	public void playText(String content) {
		int code = mTts.startSpeaking(content, this);
		if (code != ErrorCode.SUCCESS) {
			if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
				// 未安装则跳转到提示安装页面
				// mInstaller.install();
			} else {
				ToastUtil.show(mContext, "语音合成失败,错误码: " + code);
			}
		}
	}

	public void stop(){
		if (mTts != null) {
			mTts.stopSpeaking();
		}
	}

	public void setReadListener(ReadInterface listener){
		this.readListener=listener;
	}

	@Override
	public void onBufferProgress(int arg0, int arg1, int arg2, String arg3) {
		// TODO Auto-generated method stub
		readListener.onBufferProgress(arg0,arg1,arg2,arg3);
	}

	@Override
	public void onCompleted(SpeechError arg0) {
		// TODO Auto-generated method stub
		if (arg0==null){
			readListener.onCompleted(0);
		}else {
		readListener.onCompleted(arg0.getErrorCode());
	}
	}

	@Override
	public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
		// TODO Auto-generated method stub
		readListener.onEvent(arg0,arg1,arg2,arg3);
	}

	@Override
	public void onSpeakBegin() {
		// TODO Auto-generated method stub
		readListener.onSpeakBegin();
	}

	@Override
	public void onSpeakPaused() {
		// TODO Auto-generated method stub
		readListener.onSpeakPaused();
	}

	@Override
	public void onSpeakProgress(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		readListener.onSpeakProgress(arg0,arg1,arg2);
	}

	@Override
	public void onSpeakResumed() {
		// TODO Auto-generated method stub
		readListener.onSpeakResumed();
	}

	public void cancel() {
		if (mTts != null) {
			mTts.stopSpeaking();
			mTts.destroy();
		}
	}

	public interface ReadInterface{
		void onBufferProgress(int arg0, int arg1, int arg2, String arg3);
		void onCompleted(int arg0);
		void onEvent(int arg0, int arg1, int arg2, Bundle arg3);
		void onSpeakBegin();
		void onSpeakPaused();
		void onSpeakProgress(int arg0, int arg1, int arg2);
		void onSpeakResumed();
	}

}
