package com.uroad.zhgs.common

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.multidex.MultiDex
import android.text.TextUtils
import android.util.Log
import com.amap.api.maps.model.LatLng
import com.liulishuo.filedownloader.model.FileDownloadStatus.paused
import com.tencent.bugly.crashreport.CrashReport
import com.uroad.library.utils.VersionUtils
import com.uroad.library.utils.ZipUtils
import com.uroad.rxhttp.RxHttpManager
import com.uroad.rxhttp.RxHttpManager.addDisposable
import com.uroad.rxhttp.download.DownloadListener
import com.uroad.rxhttp.interceptor.Transformer
import com.uroad.zhgs.R
import com.uroad.zhgs.activity.VideoPlayerActivity
import com.uroad.zhgs.model.DiagramMDL
import com.uroad.zhgs.utils.AndroidBase64Utils
import com.uroad.zhgs.utils.DiagramUtils
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.ApiService
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import java.io.File
import java.nio.file.Files.exists

/**
 *Created by MFB on 2018/7/26.
 */
class CurrApplication : BaseApplication() {

    companion object {
        val APP_LATLNG = LatLng(30.3, 120.2)   //杭州经纬度
        lateinit var DIAGRAM_PATH: String
        lateinit var COMPRESSOR_PATH: String
        var rtmpIp: String? = null
    }

    private val handler = Handler()
    override fun onCreate() {
        super.onCreate()
        downloadDragram()
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    /*下载简图*/
    private fun downloadDragram() {
        doRequest(WebApiService.DIAGRAM, WebApiService.getBaseParams(), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, DiagramMDL::class.java)
                    mdl?.let { updateData(mdl) }
                } else {
                    handler.postDelayed({ downloadDragram() }, 3000)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                handler.postDelayed({ downloadDragram() }, 3000)
            }
        })
    }

    private fun updateData(mdl: DiagramMDL) {
        val verLocal = DiagramUtils.getVersionLocal(this)
        if (VersionUtils.isNeedUpdate(mdl.ver, verLocal)) {  //判断服务器版本是否大于本地保存的版本号
            DiagramUtils.deleteAllFile()   //先删除文件夹下所有文件
            doDownload(mdl.url)
        } else {
            if (!DiagramUtils.diagramExists()) {
                doDownload(mdl.url)
            }
        }
        DiagramUtils.saveVersionSer(this, mdl.ver)  //保存服务器版本号到本地
    }

    //下载简图
    private fun doDownload(url: String?) {
        if (TextUtils.isEmpty(url)) {
            return
        }
        addDisposable(RxHttpManager.downloadFile(url, CurrApplication.DIAGRAM_PATH, null, object : DownloadListener() {
            override fun onFinish(filePath: String) {
                ZipUtils.UnZipFolder(filePath, CurrApplication.DIAGRAM_PATH)
            }

            override fun onError(e: Throwable?, errorMsg: String?) {
                handler.postDelayed({ doDownload(url) }, 3000)
            }
        }))
    }

    private val activityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityResumed(activity: Activity) {}

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityDestroyed(activity: Activity) {
            if (activity.javaClass == VideoPlayerActivity::class.java) {  //视频播放结束，关闭视频流
                rtmpIp?.let { closeVideo(it) }
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle?) {
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        }
    }

    /*关闭快拍直播流*/
    private fun closeVideo(rtmpIp: String) {
        doRequest(WebApiService.CLOSE_VIDEO, WebApiService.closeVideoParams(rtmpIp), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) Log.e("closeVideo", "视频流关闭成功")
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                Log.e("closeVideo", "视频流关闭失败")
                handler.postDelayed({ closeVideo(rtmpIp) }, 3000)
            }
        })
    }

    override fun onTerminate() {
        handler.removeCallbacksAndMessages(null)
        super.onTerminate()
    }
}