package com.uroad.zhgs.activity

import android.content.res.Configuration
import android.os.Bundle
import android.widget.FrameLayout
import com.uroad.ijkplayer.ZPlayer
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import kotlinx.android.synthetic.main.activity_videoplay.*
import kotlinx.android.synthetic.main.activity_videoplay.view.*

/*视频播放页面*/
class VideoPlayerActivity : BaseActivity() {
    /*rtmp://live.hkstv.hk.lxdns.com/live/hks 测试链接*/
    private var isLive = false
    private var url: String? = null

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_videoplay)
        var title = ""
        intent.extras?.let {
            isLive = it.getBoolean("isLive", false)
            url = it.getString("url")
            title = it.getString("title", "")
        }
        val videoHeight = DisplayUtils.getWindowHeight(this) / 2
        zPlayer.layoutParams = (zPlayer.layoutParams as FrameLayout.LayoutParams).apply { height = videoHeight }
        zPlayer.setLive(isLive)
                .setShowCenterControl(true)
                .setScaleType(ZPlayer.SCALETYPE_FITXY)
                .setPlayerWH(0, videoHeight)
        //设置竖屏的时候屏幕的高度，如果不设置会切换后按照16:9的高度重置
        zPlayer.setTitle(title).play(url)
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