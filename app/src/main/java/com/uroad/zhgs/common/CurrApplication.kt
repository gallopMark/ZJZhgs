package com.uroad.zhgs.common

import android.app.Application
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.support.multidex.MultiDex
import android.text.TextUtils
import com.amap.api.maps.model.LatLng
import com.tencent.bugly.crashreport.CrashReport
import com.uroad.library.utils.VersionUtils
import com.uroad.library.utils.ZipUtils
import com.uroad.rxhttp.RxHttpManager
import com.uroad.rxhttp.RxHttpManager.addDisposable
import com.uroad.rxhttp.download.DownloadListener
import com.uroad.rxhttp.interceptor.Transformer
import com.uroad.zhgs.R
import com.uroad.zhgs.model.DiagramMDL
import com.uroad.zhgs.utils.AndroidBase64Utils
import com.uroad.zhgs.utils.DiagramUtils
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.ApiService
import com.uroad.zhgs.webservice.WebApiService
import java.io.File
import java.nio.file.Files.exists

/**
 *Created by MFB on 2018/7/26.
 */
class CurrApplication : Application() {

    companion object {
        val APP_LATLNG = LatLng(30.3, 120.2)   //杭州经纬度
        lateinit var DIAGRAM_PATH: String
        lateinit var COMPRESSOR_PATH: String
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }

    override fun onCreate() {
        super.onCreate()
        initHttpService()
        //    initXunFei()
        initCompressorPath()
        initDiagramPath()
        downloadDragram()
        initBugly()
    }

    /*配置接口地址*/
    private fun initHttpService() {
        val baseUrl = if (ApiService.isDebug) ApiService.Base_DEBUG_URL else ApiService.BASE_URL
        RxHttpManager.get().config().setBaseUrl(baseUrl)
    }

    /*初始化讯飞语音*/
    private fun initXunFei() {
        //SpeechUtility.createUtility(this, "${SpeechConstant.APPID}=${resources.getString(R.string.msc_appId)}")
    }

    private fun initCompressorPath() {
        COMPRESSOR_PATH = "${cacheDir.absolutePath}${File.separator}compressor"
        File(COMPRESSOR_PATH).apply { if (!exists()) this.mkdirs() }
    }

    //简图路径
    private fun initDiagramPath() {
        DIAGRAM_PATH = "${filesDir.absolutePath}${File.separator}diagram"
    }

    /*下载简图*/
    private fun downloadDragram() {
        val body = ApiService.createRequestBody(HashMap(), WebApiService.DIAGRAM)
        RxHttpManager.createApi(ApiService::class.java).doPost(body)
                .compose(Transformer.switchSchedulers())
                .subscribe({ json ->
                    val data = AndroidBase64Utils.decodeToString(json)
                    if (GsonUtils.isResultOk(data)) {
                        val mdl = GsonUtils.fromDataBean(data, DiagramMDL::class.java)
                        mdl?.let { updateData(mdl) }
                    }
                }, { })
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
                Handler().postDelayed({ doDownload(url) }, 3000)
            }
        }))
    }

    /*初始化tencent bugly*/
    private fun initBugly() {
        CrashReport.initCrashReport(this, resources.getString(R.string.bugly_appid), false)
    }
}