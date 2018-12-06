package com.uroad.zhgs.activity

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.FrameLayout
import com.uroad.ijkplayer.ZPlayer
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.common.CurrApplication
import kotlinx.android.synthetic.main.activity_videoplay.*
import tv.danmaku.ijk.media.player.IMediaPlayer
import java.lang.ref.WeakReference

/*视频播放页面*/
class VideoPlayerActivity : BaseActivity() {
    /*rtmp://live.hkstv.hk.lxdns.com/live/hks 测试链接*/
    private var isLive = false
    private var url: String? = null
    private var title: String? = null
    private var times = 10
    private var isPlaying = false

    companion object {
        private const val CODE_MSG = 0x0001
    }

    private lateinit var handler: MHandler

    private class MHandler(activity: VideoPlayerActivity) : Handler() {
        private val weakReference = WeakReference<VideoPlayerActivity>(activity)
        override fun handleMessage(msg: Message) {
            val activity = weakReference.get() ?: return
            when (msg.what) {
                CODE_MSG -> {
                    if (activity.times > 0) {
                        if (activity.isPlaying) activity.times--
                        sendEmptyMessageDelayed(CODE_MSG, 1000)
                    } else {
                        activity.setResult(RESULT_OK)
                        activity.finish()
                    }
                }
            }
        }
    }

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_videoplay)
        intent.extras?.let {
            isLive = it.getBoolean("isLive", false)
            url = it.getString("url")
            title = it.getString("title", "")
            CurrApplication.rtmpIp = url
        }
        handler = MHandler(this)
        initZPlayer()
        initWebButton()
    }

    private fun initZPlayer() {
        val videoHeight = DisplayUtils.getWindowHeight(this) / 2
        zPlayer.layoutParams = (zPlayer.layoutParams as FrameLayout.LayoutParams).apply { height = videoHeight }
        zPlayer.setLive(isLive)
                .setTitle(title)
                .setNetChangeListener(false)
                .setShowCenterControl(false)
                .setShowLoading(false)
                .setShowErrorControl(false)
                .setSupportAspectRatio(false)
                .setDefaultRetryTime(2000L)
                .onPrepared { handler.sendEmptyMessageDelayed(CODE_MSG, 1000) }
                .onInfo { what, _ ->
                    when (what) {
                        IMediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                            isPlaying = false
                            cpv.visibility = View.VISIBLE
                        }
                        IMediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                            isPlaying = true
                            cpv.visibility = View.GONE
                        }
                        IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                            isPlaying = true
                            cpv.visibility = View.GONE
                        }
                    }
                }
                .onError { _, _ ->
                    cpv.visibility = View.VISIBLE
                    openVideoFromX5Web()
//                    openBrowser()
//                    zPlayer.play(url)
                }
                .setScaleType(ZPlayer.SCALETYPE_FITXY)
                .setPlayerWH(0, videoHeight)
                .play(url)
        cpv.visibility = View.VISIBLE
    }

    private fun openVideoFromX5Web() {
        openActivityForResult(X5WebViewActivity::class.java, Bundle().apply {
            putString("url", url)
            putString("title", title)
            putBoolean("isSnapShot", true)
        }, 345)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 345 && resultCode == RESULT_OK) {
            setResult(RESULT_OK)
        }
        finish()
    }

    private fun initWebButton() {
        btOpenWeb.text = "如果此视频无法播放，点击到浏览器播放"
        btOpenWeb.setOnClickListener { openBrowser() }
    }

    private fun openBrowser() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            showShortToast("无法打开此链接")
        }
    }

    override fun onResume() {
        zPlayer.onResume()
        super.onResume()
    }

    override fun onPause() {
        zPlayer.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        zPlayer.onDestroy()
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        zPlayer.onConfigurationChanged(newConfig)
    }

    override fun onBackPressed() {
        if (zPlayer.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }
}