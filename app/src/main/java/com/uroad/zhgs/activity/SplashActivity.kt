package com.uroad.zhgs.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.imageloader_v4.listener.IImageLoaderListener
import com.uroad.library.utils.BitmapUtils
import com.uroad.library.utils.DisplayUtils
import com.uroad.rxhttp.RxHttpManager
import com.uroad.zhgs.R
import com.uroad.zhgs.adaptervp.SplashGuideAdapter
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.helper.AppLocalHelper
import com.uroad.zhgs.model.WelComeMDL
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.ApiService
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_splash.*
import java.lang.Exception
import java.lang.ref.WeakReference

/**
 *Created by MFB on 2018/8/9.
 */
class SplashActivity : BaseActivity() {
    private var delayMillis: Int = 3
    private val updateCode = 0x0001
    private lateinit var handler: MHandler
    private var isGoMain = false

    private class MHandler(activity: SplashActivity) : Handler() {
        private val weakReference: WeakReference<SplashActivity> = WeakReference(activity)
        override fun handleMessage(msg: Message) {
            val activity = weakReference.get() ?: return
            if (msg.what == activity.updateCode) {
                if (activity.delayMillis <= 0) {
                    removeMessages(activity.updateCode)
                    if (!activity.isGoMain) activity.openMain()
                } else {
                    activity.tvJump.visibility = View.VISIBLE
                    val delayText = "跳过\u2000" + activity.delayMillis + "s"
                    activity.tvJump.text = delayText
                    activity.delayMillis--
                    sendEmptyMessageDelayed(activity.updateCode, 1000)
                }
            }
        }
    }

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_splash)
        val isFirstInstall = AppLocalHelper.isFirstInstall(this)
        if (isFirstInstall) {  //如果是第一次安装，展示引导页
            onGuide()
        } else {
            onAdvert()
        }
        handler = MHandler(this)
    }

    //展示引导页
    private fun onGuide() {
        flGuide.visibility = View.VISIBLE
        val pics = arrayListOf(R.mipmap.ic_guide1_bg, R.mipmap.ic_guide2_bg, R.mipmap.ic_guide3_bg)
        val indicators = ArrayList<ImageView>()
        for (i in 0 until pics.size) {
            val imageView = ImageView(this).apply { layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { rightMargin = DisplayUtils.dip2px(this@SplashActivity, 10f) } }
            if (i == 0) imageView.setImageResource(R.mipmap.ic_indicator_selected)
            else imageView.setImageResource(R.mipmap.ic_indicator_default)
            indicators.add(imageView)
            llIndicator.addView(imageView)
        }
        val adapter = SplashGuideAdapter(this, pics)
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (position == pics.size - 1) ivGoTo.visibility = View.VISIBLE
                else ivGoTo.visibility = View.INVISIBLE
                for (i in 0 until indicators.size) {
                    //选中的页面改变小圆点为选中状态，反之为未选中
                    if(position == i) indicators[i].setImageResource(R.mipmap.ic_indicator_selected)
                    else indicators[i].setImageResource(R.mipmap.ic_indicator_default)
                }
            }
        })
        ivGoTo.setOnClickListener { openMain() }
        AppLocalHelper.setFirstInstall(this, false)
    }

    //不是第一次安装，则加载广告页
    private fun onAdvert() {
        flAdvert.visibility = View.VISIBLE
        ivPic.setImageResource(R.mipmap.ic_splash_bg)
        tvJump.setOnClickListener { openMain() }
        getWelComeJpg()
    }

    private fun getWelComeJpg() {
        doRequest(RxHttpManager.getSInstance()
                .baseUrl(ApiService.BASE_URL)
                .connectTimeout(5 * 1000)
                .readTimeout(5 * 1000)
                .createSApi(ApiService::class.java)
                .doPost(ApiService.createRequestBody(HashMap(), WebApiService.WELCOME_JPG)), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, WelComeMDL::class.java)
                    if (mdl == null) handler.postDelayed({ if (!isGoMain) openMain() }, 1000)
                    else updateData(mdl)
                } else {
                    openMain()
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                handler.postDelayed({ if (!isGoMain) openMain() }, 1000)
            }
        })
    }

    private fun updateData(comeMDL: WelComeMDL) {
        comeMDL.adtime?.let { delayMillis = it }
        ImageLoaderV4.getInstance().displayImage(this, comeMDL.jpgurl, ivPic, object : IImageLoaderListener {
            override fun onLoadingFailed(url: String?, target: ImageView?, exception: Exception?) {
            }

            override fun onLoadingComplete(url: String?, target: ImageView?) {
                handler.sendEmptyMessage(updateCode)
            }
        })
    }

    private fun openMain() {
        openActivity(MainActivity::class.java)
        isGoMain = true
        finish()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}