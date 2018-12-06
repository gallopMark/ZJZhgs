package com.uroad.zhgs.recorder

import android.content.Context
import android.os.*
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import com.uroad.zhgs.common.CurrApplication

@Suppress("DEPRECATION")
/**
 * @author MFB
 * @create 2018/10/19
 * @describe 录音按钮
 */
class AudioButton : Button, AudioManager.AudioStageListener, View.OnClickListener {
    private val mContext: Context
    private val mAudioManager: AudioManager
    private var mStateHandler: StateHandler
    // 正在录音标记
    private var isRecording = false
    //当前录音时长
    private var mTime = 0f
    private var mMinRecordTime = 3
    //最大录音时长（单位:s def:30s)
    private var mMaxRecordTime = CurrApplication.VOICE_MAX_SEC
    //标记是否强制终止
    private var isOverTime = false
    //设置是否允许录音,这个是是否有录音权限
    private var mHasRecordPermission = false
    private var stamp = true  //录音按钮点击状态标记
    //是否触发过震动
    private var isShock: Boolean = false
    private var onRecordListener: OnRecordListener? = null
    private var voiceThread: VoiceThread? = null
    fun setMaxRecordTime(max: Int) {
        this.mMaxRecordTime = max
    }

    fun setMinRecordTime(min: Int) {
        this.mMinRecordTime = min
    }

    companion object {
        private const val MSG_AUDIO_PREPARED = 0x0000
        private const val MSG_VOICE_CHANGE = 0x0001
        private const val MSG_CAN_RECORD = 0x0002
        //取消录音的状态值
        private const val MSG_VOICE_STOP = 3
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context
        mAudioManager = AudioManager.from(CurrApplication.RECORDER_PATH)
        mAudioManager.setOnAudioStageListener(this)
        mStateHandler = StateHandler(Looper.getMainLooper())
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {   //如果6.0以下不需要申请录音权限
            setHasRecordPermission(true)
        } else {
            setHasRecordPermission(false)
        }
        setOnClickListener(this)
    }

    fun setHasRecordPermission(hasPermission: Boolean) {
        this.mHasRecordPermission = hasPermission
    }

    private fun isHasRecordPermission(): Boolean = mHasRecordPermission

    override fun wellPrepared() {
        mStateHandler.removeMessages(MSG_AUDIO_PREPARED)
        mStateHandler.sendEmptyMessage(MSG_AUDIO_PREPARED)
    }

    override fun onError() {
        onRecordListener?.onDismissPermission()
    }

    private inner class StateHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_AUDIO_PREPARED -> {
                    // 显示应该是在audio end prepare之后回调
                    isRecording = true
                    voiceThread = VoiceThread().apply { start() }
                }
                MSG_CAN_RECORD -> isEnabled = true
                MSG_VOICE_CHANGE -> showRemainedTime()
                MSG_VOICE_STOP -> {
                    voiceThread?.stopRecord()
                    isOverTime = true//超时
                    mAudioManager.release()// release释放一个mediarecorder
                    onRecordListener?.onFinished(mTime, mAudioManager.getCurrentFilePath())
                    reset()// 恢复标志位
                }
            }
        }
    }

    private inner class VoiceThread : Thread() {
        override fun run() {
            while (isRecording) {
                try {
                    //最长mMaxRecordTimes
                    if (mTime > mMaxRecordTime) {
                        mStateHandler.sendEmptyMessage(MSG_VOICE_STOP)
                        return
                    }
                    sleep(100)
                    mTime += 0.1f
                    mStateHandler.sendEmptyMessage(MSG_VOICE_CHANGE)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun stopRecord() {
            isRecording = false
            interrupt()
        }
    }

    private fun showRemainedTime() {
        //倒计时
        val remainTime = (mMaxRecordTime - mTime).toInt()
        if (remainTime < 5) {
            if (!isShock) {
                isShock = true
                doShock()
            }
        }
        onRecordListener?.onRecording(mAudioManager.getVoiceLevel(7), mTime, remainTime)
    }

    /*
     * 想设置震动大小可以通过改变pattern来设定，如果开启时间太短，震动效果可能感觉不到
     * */
    private fun doShock() {
        val vibrator = mContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(100, 400, 100, 400)   // 停止 开启 停止 开启
        vibrator.vibrate(pattern, -1)           //重复两次上面的pattern 如果只想震动一次，index设为-1
    }

    override fun onClick(view: View) {
        if (!isHasRecordPermission()) {
            onRecordListener?.onDismissPermission()
        } else {
            if (stamp) {
                isEnabled = false
                mAudioManager.prepareAudio()
                //这里在短时间之后再允许点击
                mStateHandler.removeMessages(MSG_CAN_RECORD)
                mStateHandler.sendEmptyMessageDelayed(MSG_CAN_RECORD, 500)
                stamp = false
            } else {
                // 如果按的时间太短，还没准备好或者时间录制太短，就离开了，则显示这个
                if (isRecording) {
                    if (mTime < mMinRecordTime) {
                        isRecording = false
                        voiceThread?.stopRecord()
                        mAudioManager.cancel()
                        onRecordListener?.onRecordFailure(mMinRecordTime, true)
                    } else {
                        if (isOverTime) return //超时
                        voiceThread?.stopRecord()
                        mAudioManager.release()// release释放一个mediarecorder
                        onRecordListener?.onFinished(mTime, mAudioManager.getCurrentFilePath())
                    }
                }
                reset()// 恢复标志位
            }
        }
    }

    /**
     * 回复标志位以及状态
     */
    private fun reset() {
        stamp = true
        isRecording = false
        mTime = 0f
        isOverTime = false
        isShock = false
        mStateHandler.removeCallbacksAndMessages(null)
    }

    fun cancel() {
        isRecording = false
        mAudioManager.cancel()
        mStateHandler.removeCallbacksAndMessages(null)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mAudioManager.cancel()
        mStateHandler.removeCallbacksAndMessages(null)
    }

    interface OnRecordListener {
        fun onDismissPermission()
        fun onRecording(level: Int, seconds: Float, remainTime: Int)
        fun onRecordFailure(minRecordTime: Int, tooShort: Boolean)
        fun onFinished(seconds: Float, filePath: String?)
    }

    fun setOnRecordListener(onRecordListener: OnRecordListener) {
        this.onRecordListener = onRecordListener
    }
}