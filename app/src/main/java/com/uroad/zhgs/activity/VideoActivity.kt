package com.uroad.zhgs.activity

import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.amap.api.col.sln3.it
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.dialog.MaterialDialog
import kotlinx.android.synthetic.main.activity_videoplayer.*
import tv.danmaku.ijk.media.player.IMediaPlayer

/**
 * @author MFB
 * @create 2018/10/23
 * @describe 本地视频播放
 */
class VideoActivity : BaseActivity() {
    override fun requestWindow() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_videoplayer)
        val url = intent.extras?.getString("url")
        videoView.setVideoPath(url)
        videoView.setOnPreparedListener { videoView.start() }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            progressView.visibility = View.VISIBLE
            videoView.setOnInfoListener { _, what, _ ->
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    progressView.visibility = View.VISIBLE
                } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END ||
                        what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    progressView.visibility = View.GONE
                }
                return@setOnInfoListener true
            }
        }
        videoView.setOnCompletionListener { finish() }
        videoView.setOnErrorListener { _, _, _ ->
            onError()
            return@setOnErrorListener true
        }
    }

    private fun onError() {
        val dialog = MaterialDialog(this)
        dialog.setTitle(getString(R.string.dialog_default_title))
        dialog.setMessage("播放出了点小问题,稍后重试")
        dialog.setPositiveButton(getString(R.string.i_got_it), null)
        dialog.hideDivider()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnDismissListener { finish() }
    }

    override fun onResume() {
        if (videoView.isPlaying)
            videoView.resume()
        super.onResume()
    }

    override fun onPause() {
        if (videoView.canPause())
            videoView.pause()
        super.onPause()
    }

    override fun onDestroy() {
        videoView.stopPlayback()
        super.onDestroy()
    }
}