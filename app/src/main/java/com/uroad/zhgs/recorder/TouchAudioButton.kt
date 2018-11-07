package com.uroad.zhgs.recorder

import android.annotation.SuppressLint
import android.content.Context
import android.os.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import com.uroad.zhgs.common.CurrApplication


/**
 * @author MFB
 * @create 2018/10/29
 * @describe 长按录音
 */
class TouchAudioButton : Button, AudioManager.AudioStageListener {

    private val mContext: Context
    private val mAudioManager: AudioManager
    //标记是否强制终止
    private var isOverTime = false
    //设置是否允许录音,这个是是否有录音权限
    private var mHasRecordPermission = false
    //是否触发过震动
    private var isShock = false
    //是否允许短时间内再次点击录音，主要是防止故意多次连续点击。
    private var isRecording = false
    private var wantToCancel = false
    //当前录音时长
    private var mTime = 0f
    private var mMinRecordTime = 1
    //最大录音时长（单位:s def:30s)
    private var mMaxRecordTime = CurrApplication.VOICE_MAX_SEC
    private lateinit var mStateHandler: StateHandler
    private var voiceThread: VoiceThread? = null
    private var mListener: RecordListener? = null

    companion object {
        private const val MSG_AUDIO_PREPARED = 0
        private const val MSG_VOICE_CHANGE = 1
        private const val MSG_CAN_RECORD = 2
        private const val MSG_TOO_SHORT = 3
        private const val MSG_VOICE_STOP = 4 //取消录音的状态值
        private const val MSG_VOICE_FINISHED = 5
        private const val MSG_VOICE_CANCEL = 6
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
    }

    fun setMaxRecordTime(max: Int) {
        this.mMaxRecordTime = max
    }

    fun setMinRecordTime(min: Int) {
        this.mMinRecordTime = min
    }

    fun setHasRecordPermission(hasPermission: Boolean) {
        this.mHasRecordPermission = hasPermission
    }

    private fun isHasRecordPermission(): Boolean = mHasRecordPermission

    fun setOnRecordListener(mListener: RecordListener) {
        this.mListener = mListener
    }

    override fun wellPrepared() {
        mStateHandler.sendEmptyMessage(MSG_AUDIO_PREPARED)
    }

    override fun onError() {
        mListener?.onDismissPermission()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isHasRecordPermission()) {
            mListener?.onDismissPermission()
            return true
        } else {
            parent?.requestDisallowInterceptTouchEvent(true)
            val action = event.action
            val x = (event.x).toInt()
            val y = (event.y).toInt()
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    //响应DOWN事件进行录音准备。放到这里会有问题，比如用户故意连续点击多次，就会出现各种问题。
                    // 所以和录制视频处理的思路一样，我们在短时间内只允许点击一次即可。
                    if (isEnabled) {
                        isEnabled = false
                        mAudioManager.prepareAudio()
                        //这里在短时间之后再允许点击
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    // 根据x，y来判断用户是否想要取消
                    wantToCancel = wantToCancel(x, y)
                }
                MotionEvent.ACTION_UP -> {
                    // 如果按的时间太短，还没准备好或者时间录制太短，就离开了，则显示这个dialog
                    if (!isRecording) {
                        mStateHandler.sendEmptyMessage(MSG_VOICE_CANCEL)
                    } else {
                        if (wantToCancel) {
                            mStateHandler.sendEmptyMessage(MSG_VOICE_CANCEL)
                        } else {
                            if (mTime < mMinRecordTime) {
                                mStateHandler.sendEmptyMessage(MSG_TOO_SHORT)
                            } else {
                                if (isOverTime) return true//超时
                                mAudioManager.release() // release释放一个mediarecorder
                                mStateHandler.sendEmptyMessage(MSG_VOICE_FINISHED)
                            }
                        }
                    }
                    reset()// 恢复标志位
                }
            }
            return super.onTouchEvent(event)
        }
    }

    private fun reset() {
        isRecording = false
        isOverTime = false
        isShock = false
        wantToCancel = false
        isEnabled = true
        mTime = 0f
    }

    private fun wantToCancel(x: Int, y: Int): Boolean {
        // 判断是否在左边，右边，上边，下边
        return x < 0 || x > width || y < -50 || y > height + 50
    }

    private inner class StateHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_AUDIO_PREPARED -> {  // 显示应该是在audio end prepare之后回调
                    isRecording = true
                    voiceThread = VoiceThread().apply { start() }
                    removeMessages(MSG_CAN_RECORD)
                    sendEmptyMessageDelayed(MSG_CAN_RECORD, 500L)
                }
                MSG_VOICE_CHANGE -> showRemainedTime()
                MSG_CAN_RECORD -> isEnabled = true
                MSG_TOO_SHORT -> {
                    voiceThread?.stopRecord()
                    postDelayed({ mAudioManager.cancel() }, 1000)
                    mListener?.onRecordFailure(mMinRecordTime, true)
                    removeCallbacksAndMessages(null)
                }
                MSG_VOICE_STOP -> {
                    voiceThread?.stopRecord()
                    isOverTime = true//超时
                    mAudioManager.release()// release释放一个mediarecorder
                    mListener?.onFinished(mTime, mAudioManager.getCurrentFilePath())
                    reset()// 恢复标志位
                }
                MSG_VOICE_FINISHED -> {
                    voiceThread?.stopRecord()
                    mListener?.onFinished(mTime, mAudioManager.getCurrentFilePath())
                }
                MSG_VOICE_CANCEL -> {
                    voiceThread?.stopRecord()
                    mAudioManager.cancel()
                    mListener?.onRecordFailure(mMinRecordTime, false)
                    removeCallbacksAndMessages(null)
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

    // 获取音量大小的runnable
    private val runnable = Runnable {
        while (isRecording) {
            try {
                //最长mMaxRecordTimes
                if (mTime > mMaxRecordTime) {
                    mStateHandler.sendEmptyMessage(MSG_VOICE_STOP)
                    return@Runnable
                }
                Thread.sleep(100)
                mTime += 0.1f
                mStateHandler.sendEmptyMessage(MSG_VOICE_CHANGE)
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
        mListener?.onRecording(mAudioManager.getVoiceLevel(7), mTime, remainTime)
    }

    /*
        * 想设置震动大小可以通过改变pattern来设定，如果开启时间太短，震动效果可能感觉不到
        * */
    private fun doShock() {
        val vibrator = mContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(100, 400, 100, 400)   // 停止 开启 停止 开启
        vibrator.vibrate(pattern, -1)           //重复两次上面的pattern 如果只想震动一次，index设为-1
    }

    interface RecordListener {
        fun onDismissPermission()
        fun onRecording(level: Int, seconds: Float, remainTime: Int)
        fun onRecordFailure(minRecordTime: Int, tooShort: Boolean)
        fun onFinished(seconds: Float, filePath: String?)
    }

    override fun onDetachedFromWindow() {
        mAudioManager.cancel()
        mStateHandler.removeCallbacksAndMessages(null)
        super.onDetachedFromWindow()
    }
}