package com.uroad.zhgs.recorder

import android.media.MediaRecorder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * @author MFB
 * @create 2018/10/19
 * @describe 录音工具类
 */
class AudioManager private constructor(private var mDirPath: String?) {
    private var mRecorder: MediaRecorder? = null
    //录音文件保存路径
    private var mCurrentPath: String? = null
    //是否真备好开始录音
    private var isPrepared: Boolean = false
    private var mListener: AudioStageListener? = null

    companion object {
        fun from(mDirPath: String): AudioManager = AudioManager(mDirPath)
    }

    fun setOnAudioStageListener(listener: AudioStageListener) {
        mListener = listener
    }

    // 准备方法
    fun prepareAudio() {
        try {
            // 一开始应该是false的
            isPrepared = false
            //创建所属文件夹
            val dir = File(mDirPath)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val fileName = generalFileName()
            val file = File(dir, fileName)
            //获取文件
            mCurrentPath = file.absolutePath
            mRecorder = MediaRecorder().apply {
                // 设置输出文件
                setOutputFile(file.absolutePath)
                // 设置meidaRecorder的音频源是麦克风
                setAudioSource(MediaRecorder.AudioSource.MIC)
                // 设置文件音频的输出格式为amr
                setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
                // 设置音频的编码格式为amr。这里采用AAC主要为了适配IOS，保证在IOS上可以正常播放。
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                // 严格遵守google官方api给出的mediaRecorder的状态流程图
                prepare()
                start()
            }
            // 准备结束
            isPrepared = true
            // 已经准备好了，可以录制了
            mListener?.wellPrepared()
        } catch (e: Exception) {
            mListener?.onError()
        }
    }

    /**
     * 随机生成文件的名称
     */
    private fun generalFileName(): String {
        val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        return "${sdf.format(System.currentTimeMillis())}.mp3"
    }

    // 获得声音的level
    fun getVoiceLevel(maxLevel: Int): Int {
        // mRecorder.getMaxAmplitude()这个是音频的振幅范围，值域是1-32767
        val recorder = mRecorder
        if (isPrepared && recorder != null) {
            try {
                // 取证+1，否则去不到7
                return maxLevel * recorder.maxAmplitude / 32768 + 1
            } catch (e: Exception) {
            }
        }
        return 1
    }

    // 释放资源
    fun release() {
        /*这里处理一些特定情况下的异常*/
        //下面三个参数必须加，不加的话会奔溃，在mediarecorder.stop();
        //报错为：RuntimeException:stop failed
        stop()
        mRecorder?.release()
        mRecorder = null
    }

    private fun stop() {
        mRecorder?.let {
            try {
                it.setOnErrorListener(null)
                it.setOnInfoListener(null)
                it.setPreviewDisplay(null)
                it.stop()
            } catch (e: Exception) {
            }
        }
    }

    // 取消,因为prepare时产生了一个文件，所以cancel方法应该要删除这个文件，
    // 这是与release的方法的区别
    fun cancel() {
        release()
        if (mCurrentPath != null) {
            val file = File(mCurrentPath)
            file.delete()
            mCurrentPath = null
        }
    }

    fun getCurrentFilePath(): String? {
        return mCurrentPath
    }

    interface AudioStageListener {
        fun wellPrepared()
        fun onError()
    }
}