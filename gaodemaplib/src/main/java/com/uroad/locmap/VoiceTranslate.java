package com.uroad.locmap;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.uroad.locmap.util.ToastUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * 讯飞语音识别
 *
 */
public class VoiceTranslate {
	private static String TAG = VoiceTranslate.class.getSimpleName();
	SpeechRecognizer mIat = null;
	Context mContext;

	// 用HashMap存储听写结果
	private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
	String msg, content;
	int ret = 0; // 函数调用返回值
	MyRecognizerListener myRecognizerListener;

	public VoiceTranslate(Context context) {
		mContext = context;
		// 1.创建 SpeechSynthesizer 对象, 第二个参数：本地合成时传 InitListener
		mIat = SpeechRecognizer.createRecognizer(mContext, mInitListener);
		// 2.参数设置
		setParam();
	}

	/**
	 * 设置参数
	 *
	 * @param context
	 * @param VAD_BOS
	 *            设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
	 * @param VAD_EOS
	 *            设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
	 * @param LANGUAGE
	 *            设置语言
	 * @param ASR_PTT
	 *            设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
	 */
	public VoiceTranslate(Context context, String VAD_BOS, String VAD_EOS,
						  String LANGUAGE, String ASR_PTT) {
		mContext = context;
		// 1.创建 SpeechSynthesizer 对象, 第二个参数：本地合成时传 InitListener
		mIat = SpeechRecognizer.createRecognizer(mContext, mInitListener);
		// 2.参数设置
		setParam(VAD_BOS, VAD_EOS, LANGUAGE, ASR_PTT);
	}

	/**
	 * 初始化监听器。
	 */
	private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			Log.d(TAG, "SpeechRecognizer init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				ToastUtil.show(mContext, "初始化失败，错误码：" + code);
			}
		}
	};

	public void setRecognizerListener(MyRecognizerListener myRecognizerListener) {
		this.myRecognizerListener = myRecognizerListener;
	}

	/**
	 * 参数设置
	 *
	 * @param
	 * @return
	 */
	private void setParam() {
		// 清空参数
		mIat.setParameter(SpeechConstant.PARAMS, null);
		// 设置听写引擎
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		// 设置返回结果格式
		mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
		// 设置语言
		mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
		// 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
		mIat.setParameter(SpeechConstant.VAD_BOS, "4000");
		// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
		mIat.setParameter(SpeechConstant.VAD_EOS, "1000");
		// 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
		mIat.setParameter(SpeechConstant.ASR_PTT, "0");
		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
				Environment.getExternalStorageDirectory() + "/msc/iat.wav");
	}

	/**
	 * 参数设置
	 *
	 * @param
	 * @return
	 */
	private void setParam(String VAD_BOS, String VAD_EOS, String LANGUAGE,
						  String ASR_PTT) {
		// 清空参数
		mIat.setParameter(SpeechConstant.PARAMS, null);
		// 设置听写引擎
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		// 设置返回结果格式
		mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
		// 设置语言
		mIat.setParameter(SpeechConstant.LANGUAGE, LANGUAGE);
		// 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
		mIat.setParameter(SpeechConstant.VAD_BOS, VAD_BOS);
		// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
		mIat.setParameter(SpeechConstant.VAD_EOS, VAD_EOS);
		// 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
		mIat.setParameter(SpeechConstant.ASR_PTT, ASR_PTT);
		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
				Environment.getExternalStorageDirectory() + "/msc/iat.wav");
	}

	public void startListening() {

		ret = mIat.startListening(mRecognizerListener);
		if (ret != ErrorCode.SUCCESS) {
			if (myRecognizerListener != null)
				myRecognizerListener.onError("听写失败,错误码：" + ret);
		} else {
		}

	}

	/**
	 * 清除内容
	 */
	public void clear() {
		content = "";
	}

	/**
	 * 听写监听器。
	 */
	private RecognizerListener mRecognizerListener = new RecognizerListener() {

		@Override
		public void onBeginOfSpeech() {
			// 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
			if (myRecognizerListener != null)
				myRecognizerListener.onBeginOfSpeech();
		}

		@Override
		public void onError(SpeechError error) {
			// Tips：
			// 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
			// 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
			if (myRecognizerListener != null) {
				myRecognizerListener.onError(error.getPlainDescription(true));
			}
		}

		@Override
		public void onEndOfSpeech() {
			// 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
			if (myRecognizerListener != null)
				myRecognizerListener.onEndOfSpeech();
		}

		@Override
		public void onResult(RecognizerResult results, boolean isLast) {
			Log.d(TAG, results.getResultString());
			printResult(results);

			if (isLast) {
				if (myRecognizerListener != null) {
					myRecognizerListener.printResult(content);
				}
				clear();
			}
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			// 若使用本地能力，会话id为null
			// if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			// String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			// Log.d(TAG, "session id =" + sid);
			// }
		}

		@Override
		public void onVolumeChanged(int arg0, byte[] arg1) {
			// TODO Auto-generated method stub

		}
	};

	private void printResult(RecognizerResult results) {
		String text = parseIatResult(results.getResultString());

		String sn = null;
		// 读取json结果中的sn字段
		try {
			JSONObject resultJson = new JSONObject(results.getResultString());
			sn = resultJson.optString("sn");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		mIatResults.put(sn, text);

		StringBuffer resultBuffer = new StringBuffer();
		for (String key : mIatResults.keySet()) {
			resultBuffer.append(mIatResults.get(key));
		}

		msg = resultBuffer.toString();
		if (content != null) {
			content = content + msg;
		} else {
			content = msg;
		}
		mIatResults.clear();
	}

	private static String parseIatResult(String json) {
		StringBuffer ret = new StringBuffer();
		try {
			JSONTokener tokener = new JSONTokener(json);
			JSONObject joResult = new JSONObject(tokener);

			JSONArray words = joResult.getJSONArray("ws");
			for (int i = 0; i < words.length(); i++) {
				// 转写结果词，默认使用第一个结果
				JSONArray items = words.getJSONObject(i).getJSONArray("cw");
				JSONObject obj = items.getJSONObject(0);
				ret.append(obj.getString("w"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret.toString();
	}

	// 退出时释放连接
	public void cancel() {
		if (mIat != null) {
			mIat.cancel();
			mIat.destroy();
		}
	}

	public interface MyRecognizerListener {
		void printResult(String content);
		void onError(String error);
		void onBeginOfSpeech();
		void onEndOfSpeech();
	}

}
