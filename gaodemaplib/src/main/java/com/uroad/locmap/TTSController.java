package com.uroad.locmap;

import android.content.Context;
import android.os.Bundle;

import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechListener;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;

/**
 * 高德语音播报组件
 * 
 */
public class TTSController implements SynthesizerListener, AMapNaviListener {

	public static TTSController ttsManager;
	private Context mContext;
	// 合成对象.
	private SpeechSynthesizer mSpeechSynthesizer;

	TTSController(Context context) {
		mContext = context;
	}

	public static TTSController getInstance(Context context) {
		if (ttsManager == null) {
			ttsManager = new TTSController(context);
		}
		return ttsManager;
	}

	public void init() {
		// 1、将SpeechUser初始化方法替换成以下初始化
		SpeechUtility.createUtility(mContext,
				"appid=" + mContext.getString(R.string.xunfei_key));
		// 2、初始化合成对象.
		mSpeechSynthesizer = SpeechSynthesizer.createSynthesizer(mContext,
				new InitListener() {
					@Override
					public void onInit(int errorcode) {
						if (ErrorCode.SUCCESS == errorcode) {
							// 3、初始化成功开发者根据需求设置合成参数
							// 可以设置调用本地合成，详细设置参考语音云demo
							initSpeechSynthesizer();
						} else {
						}
					}
				});
	}

	/**
	 * 使用SpeechSynthesizer合成语音，不弹出合成Dialog.
	 * 
	 * @param
	 */
	public void playText(final String playText) {
		if (!isfinish) {
			return;
		}
		if (null == mSpeechSynthesizer) {
			// 创建合成对象.
			mSpeechSynthesizer = SpeechSynthesizer.createSynthesizer(mContext,
					new InitListener() {
						@Override
						public void onInit(int errorcode) {
							if (ErrorCode.SUCCESS == errorcode) {
								initSpeechSynthesizer();
								// 进行语音合成.
								mSpeechSynthesizer.startSpeaking(playText,
										TTSController.this);
							} else {
							}
						}
					});
		} else {
			// 进行语音合成.
			mSpeechSynthesizer.startSpeaking(playText, this);
		}

	}

	public void stopSpeaking() {
		if (mSpeechSynthesizer != null)
			mSpeechSynthesizer.stopSpeaking();
	}

	public void startSpeaking() {
		isfinish = true;
	}

	private void initSpeechSynthesizer() {
		// 设置发音人
		mSpeechSynthesizer.setParameter(SpeechConstant.VOICE_NAME,
				mContext.getString(R.string.preference_default_tts_role));
		// 设置语速
		mSpeechSynthesizer.setParameter(SpeechConstant.SPEED,
				"" + mContext.getString(R.string.preference_key_tts_speed));
		// 设置音量
		mSpeechSynthesizer.setParameter(SpeechConstant.VOLUME,
				"" + mContext.getString(R.string.preference_key_tts_volume));
		// 设置语调
		mSpeechSynthesizer.setParameter(SpeechConstant.PITCH,
				"" + mContext.getString(R.string.preference_key_tts_pitch));

	}

	/**
	 * 用户登录回调监听器.
	 */
	private SpeechListener listener = new SpeechListener() {

		@Override
		public void onBufferReceived(byte[] arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onCompleted(SpeechError arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onEvent(int arg0, Bundle arg1) {
			// TODO Auto-generated method stub

		}

	};

	boolean isfinish = true;

	public void destroy() {
		if (mSpeechSynthesizer != null) {
			mSpeechSynthesizer.stopSpeaking();
		}
	}

	@Override
	public void onArriveDestination() {
		// TODO Auto-generated method stub
		this.playText("到达目的地");
	}

	@Override
	public void onArrivedWayPoint(int arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onCalculateRouteFailure(int arg0) {
		this.playText("路径计算失败，请检查网络或输入参数");
	}

	@Override
	public void onCalculateRouteSuccess(int[] ints) {
		String calculateResult = "路径计算就绪";

		this.playText(calculateResult);
	}


	@Override
	public void onEndEmulatorNavi() {
		this.playText("导航结束");

	}

	@Override
	public void onGetNavigationText(int arg0, String arg1) {
		// TODO Auto-generated method stub
		this.playText(arg1);
	}

	@Override
	public void onInitNaviFailure() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onInitNaviSuccess() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationChange(AMapNaviLocation arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReCalculateRouteForTrafficJam() {
		// TODO Auto-generated method stub
		this.playText("前方路线拥堵，路线重新规划");
	}

	@Override
	public void onReCalculateRouteForYaw() {

		this.playText("您已偏航");
	}

	@Override
	public void onStartNavi(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTrafficStatusUpdate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGpsOpenStatus(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNaviInfoUpdated(AMapNaviInfo arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNaviInfoUpdate(NaviInfo arg0) {

		// TODO Auto-generated method stub

	}

	@Override
	public void onBufferProgress(int arg0, int arg1, int arg2, String arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCompleted(SpeechError arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSpeakBegin() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSpeakPaused() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSpeakProgress(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSpeakResumed() {
		// TODO Auto-generated method stub

	}

	@Override
	@Deprecated
	public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@Deprecated
	public void OnUpdateTrafficFacility(TrafficFacilityInfo arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hideCross() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hideLaneInfo() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyParallelRoad(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showCross(AMapNaviCross arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showLaneInfo(AMapLaneInfo[] arg0, byte[] arg1, byte[] arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateAimlessModeStatistics(AimLessModeStat arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPlayRing(int i) {

	}

	@Override
	public void onGetNavigationText(String s) {

	}

	@Override
	public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {

	}

	@Override
	public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

	}

	@Override
	public void showModeCross(AMapModelCross aMapModelCross) {

	}

	@Override
	public void hideModeCross() {

	}
}
