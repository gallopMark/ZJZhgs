package com.uroad.zhgs.activity

import android.content.res.Configuration
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
import java.lang.ref.WeakReference

/*视频播放页面*/
class VideoPlayerActivity : BaseActivity() {
    /*rtmp://live.hkstv.hk.lxdns.com/live/hks 测试链接*/
    private var isLive = false
    private var url: String? = null
    private var title: String? = null
    private var times = 10

    companion object {
        private const val CODE_MSG = 0x0001
    }

    private lateinit var handler: MHandler

    private class MHandler(activity: VideoPlayerActivity) : Handler() {
        private val weakReference = WeakReference<VideoPlayerActivity>(activity)
        override fun handleMessage(msg: Message?) {
            val activity = weakReference.get() ?: return
            if (activity.times > 0) {
                activity.times--
                sendEmptyMessageDelayed(CODE_MSG, 1000)
            } else {
                activity.finish()
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
//        cpv.postDelayed(run, 5000)
//        cpv.post(run)
    }

//    private val run = Runnable {
//        cpv.visibility = View.GONE
//        zPlayer.visibility = View.VISIBLE
//        initZPlayer()
//    }

    private fun initZPlayer() {
        val videoHeight = DisplayUtils.getWindowHeight(this) / 2
        zPlayer.layoutParams = (zPlayer.layoutParams as FrameLayout.LayoutParams).apply { height = videoHeight }
        zPlayer.setLive(isLive)
                .setTitle(title)
                .setShowCenterControl(true)
                .setNetChangeListener(false)
                .onPrepared { handler.sendEmptyMessageDelayed(CODE_MSG, 1000) }
                .setScaleType(ZPlayer.SCALETYPE_FITXY)
                .setPlayerWH(0, videoHeight)
                .play(url)
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