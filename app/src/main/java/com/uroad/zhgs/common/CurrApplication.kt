package com.uroad.zhgs.common

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import com.amap.api.maps.model.LatLng
import com.tencent.android.tpush.XGPushManager
import com.tencent.bugly.crashreport.CrashReport
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import com.uroad.library.utils.VersionUtils
import com.uroad.library.utils.ZipUtils
import com.uroad.rxhttp.RxHttpManager
import com.uroad.rxhttp.RxHttpManager.addDisposable
import com.uroad.rxhttp.download.DownloadListener
import com.uroad.rxhttp.interceptor.Transformer
import com.uroad.zhgs.R
import com.uroad.zhgs.activity.VideoPlayerActivity
import com.uroad.zhgs.helper.UserPreferenceHelper
import com.uroad.zhgs.model.ActivityMDL
import com.uroad.zhgs.model.CarMDL
import com.uroad.zhgs.model.YouZanMDL
import com.uroad.zhgs.model.sys.AppConfigMDL
import com.uroad.zhgs.utils.AssetsUtils
import com.uroad.zhgs.utils.DiagramUtils
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.ApiService
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import com.youzan.androidsdk.YouzanSDK
import com.youzan.androidsdk.YouzanToken
import com.youzan.androidsdkx5.YouZanSDKX5Adapter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File

/**
 *Created by MFB on 2018/7/26.
 */
class CurrApplication : BaseApplication() {

    companion object {
        private val handler = Handler()
        val APP_LATLNG = LatLng(30.3, 120.2)   //杭州经纬度
        lateinit var DIAGRAM_PATH: String
        lateinit var COMPRESSOR_PATH: String
        lateinit var MAP_STYLE_PATH: String //自定义地图存放的路径
        lateinit var RECORDER_PATH: String //录音文件存放的目录
        lateinit var VIDEO_PATH: String //视频录制存放的目录
        const val DELAY_MILLIS = 3000L
        var rtmpIp: String? = null
        var VOICE_MAX_SEC = 20  //语音最大录制时长
        var VIDEO_MAX_SEC = 20  //视频最大录制时长
        var WISDOM_URL: String? = null  //小智问问url
        var ALIVE_URL: String? = null   //直播url
        var BREAK_RULES_URL: String? = null //违章查询url
        var PRAISE_URL: String? = null //有赞url
        var PRAISE_USER_URL: String? = null //有赞用户中心url
        var cars: MutableList<CarMDL>? = null
        var activityMDL: ActivityMDL? = null  //是否有活动
    }

    override fun onCreate() {
        super.onCreate()
        initHttpService()
        initFilePath()
        downloadDiagram()
        initXG()
        initUM()
        initYouZan()
        initBugly()
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    /*配置接口地址*/
    private fun initHttpService() {
        val baseUrl = if (ApiService.isDebug) ApiService.BASE_DEBUG_URL else ApiService.BASE_URL
        RxHttpManager.get().config().setBaseUrl(baseUrl)
    }

    /*初始化相关文件存放的目录*/
    private fun initFilePath() {
        initCompressorPath()
        initDiagramPath()
        initMapStylePath()
        initRecorderPath()
        initVideoPath()
    }

    private fun initCompressorPath() {
        COMPRESSOR_PATH = "${cacheDir.absolutePath}${File.separator}compressor"
        File(COMPRESSOR_PATH).apply { if (!exists()) this.mkdirs() }
    }

    //简图路径
    private fun initDiagramPath() {
        DIAGRAM_PATH = "${filesDir.absolutePath}${File.separator}diagram"
    }

    private fun initMapStylePath() {
        val path = "${filesDir.absolutePath}${File.separator}/mapStyle"
        File(path).apply { if (!exists()) this.mkdirs() }
        val fileAssetPath = "mapStyle.data"
        MAP_STYLE_PATH = AssetsUtils.assets2SD(this, fileAssetPath, "$path${File.separator}$fileAssetPath")
    }

    /*录音文件存放的目录*/
    private fun initRecorderPath() {
        RECORDER_PATH = "${filesDir.absolutePath}${File.separator}/recorder"
        File(RECORDER_PATH).apply { if (!exists()) this.mkdirs() }
    }

    private fun initVideoPath() {
        VIDEO_PATH = "${filesDir.absolutePath}${File.separator}/video"
        File(RECORDER_PATH).apply { if (!exists()) this.mkdirs() }
    }

    /*初始化讯飞语音*/
//    private fun initXunFei() {
//        //SpeechUtility.createUtility(this, "${SpeechConstant.APPID}=${resources.getString(R.string.msc_appId)}")
//    }
    /*注册信鸽推送*/
    private fun initXG() {
        val pushID = UserPreferenceHelper.getPushID(this)
        if (!TextUtils.isEmpty(pushID)) {
            XGPushManager.bindAccount(this, pushID)
        } else {
            XGPushManager.registerPush(this)
        }
    }

    /*友盟统计初始化*/
    private fun initUM() {
        UMConfigure.init(this, getString(R.string.UMENG_APP_ID), "Umeng", UMConfigure.DEVICE_TYPE_PHONE, "")
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL)
    }

    /*有赞SDK初始化*/
    private fun initYouZan() {
        // 腾讯X5版本使用
        YouzanSDK.init(this, getString(R.string.PRAISE_CLIENT_ID), YouZanSDKX5Adapter())
        //(可选)预下载店铺主页------------------------------
        // YouzanPreloader.preloadHtml(this, "preload_html_url");
        // 对html预加载时调用， 提升html打开速度，preload_html_url不能是重定向页面
        /**
         * 自6.4.5以后，获取到token信息后，同步到SDK时，需要做出一定变更。
        //调用login接口, 获取数据, 组装成YouzanToken, 回传给SDK
        YouzanToken token = new YouzanToken()
        token.setAccessToken("接口返回的access_token")
        token.setCookieKey("接口返回的cookie_key")
        token.setCookieValue("接口返回的cookie_value")
        // 注意：这里注意调用顺序。先传递给sdk，再刷新view
        YouzanSDK.sync(getApplicationContext(), token);
        mView.sync(token);
         */
    }

    fun onPraiseLogin(useruuid: String?, deviceID: String?, extra: String?) {
        doRequest(WebApiService.PRAISE_LOGIN, WebApiService.praiseLoginParams(useruuid, deviceID, extra), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, YouZanMDL::class.java)
                    if (mdl == null) handler.postDelayed({ onPraiseLogin(useruuid, deviceID, extra) }, 3000)
                    else initYouZanToken(mdl.access_token, mdl.cookie_key, mdl.cookie_value)
                } else {
                    handler.postDelayed({ onPraiseLogin(useruuid, deviceID, extra) }, 3000)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                handler.postDelayed({ onPraiseLogin(useruuid, deviceID, extra) }, 3000)
            }
        })
    }

    private fun initYouZanToken(access_token: String?, cookie_key: String?, cookie_value: String?) {
        val token = YouzanToken().apply {
            this.accessToken = access_token
            this.cookieKey = cookie_key
            this.cookieValue = cookie_value
        }
        YouzanSDK.sync(this, token)
    }

    /*初始化tencent bugly*/
    private fun initBugly() {
//        if (ApiService.isDebug) return
        CrashReport.initCrashReport(this, resources.getString(R.string.bugly_appid), false)
    }

    /*下载简图*/
    private fun downloadDiagram() {
        doRequest(WebApiService.APP_CONFIG, WebApiService.getBaseParams(), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                val mdLs = GsonUtils.fromDataToList(data, AppConfigMDL::class.java)
                if (mdLs.size == 0) {
                    handler.postDelayed({ downloadDiagram() }, 3000)
                } else {
                    for (item in mdLs) {
                        if (TextUtils.equals(item.confid, AppConfigMDL.Type.DIAGRAM_VER.CODE)) {
                            updateData(item)
                            break
                        }
                    }
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                handler.postDelayed({ downloadDiagram() }, 3000)
            }
        })
    }

    private fun updateData(mdl: AppConfigMDL) {
        val verLocal = DiagramUtils.getVersionLocal(this)
        if (VersionUtils.isNeedUpdate(mdl.conf_ver, verLocal)) {  //判断服务器版本是否大于本地保存的版本号
            deleteDiagramFiles()
            doDownload(mdl.url)
        } else {
            if (!DiagramUtils.diagramExists()) {
                doDownload(mdl.url)
            }
        }
        DiagramUtils.saveVersionSer(this, mdl.conf_ver)  //保存服务器版本号到本地
    }

    /*简图版本升级 删除旧版本的简图所有文件*/
    private fun deleteDiagramFiles() {
        addDisposable(Observable.fromCallable { DiagramUtils.deleteAllFile() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {}))
    }

    //下载简图
    private fun doDownload(url: String?) {
        if (TextUtils.isEmpty(url)) {
            return
        }
        addDisposable(RxHttpManager.downloadFile(url, CurrApplication.DIAGRAM_PATH, null, object : DownloadListener() {
            override fun onFinish(filePath: String) {
                unZipFile(filePath)
            }

            override fun onError(e: Throwable?, errorMsg: String?) {
                handler.postDelayed({ doDownload(url) }, 3000)
            }
        }))
    }

    /*解压zip简图文件 异步解压避免ARN*/
    private fun unZipFile(filePath: String) {
        addDisposable(Observable.fromCallable { ZipUtils.UnZipFolder(filePath, CurrApplication.DIAGRAM_PATH) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {}))
    }

    private val activityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
        override fun onActivityPaused(activity: Activity) {
            MobclickAgent.onPause(activity)
        }

        override fun onActivityResumed(activity: Activity) {
            MobclickAgent.onResume(activity)
        }

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
                if (!GsonUtils.isResultOk(data)) handler.postDelayed({ closeVideo(rtmpIp) }, 3000)
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                handler.postDelayed({ closeVideo(rtmpIp) }, 3000)
            }
        })
    }

    override fun onTerminate() {
        handler.removeCallbacksAndMessages(null)
        super.onTerminate()
    }
}