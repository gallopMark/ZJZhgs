package com.uroad.zhgs.common

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import com.google.gson.Gson
import com.tencent.bugly.crashreport.CrashReport
import com.uroad.rxhttp.RxHttpManager
import com.uroad.rxhttp.RxHttpManager.addDisposable
import com.uroad.rxhttp.exception.ApiException
import com.uroad.rxhttp.interceptor.Transformer
import com.uroad.zhgs.R
import com.uroad.zhgs.utils.AndroidBase64Utils
import com.uroad.zhgs.webservice.ApiService
import com.uroad.zhgs.webservice.HttpRequestCallback
import io.reactivex.Observable
import java.io.File

/**
 * @author MFB
 * @create 2018/9/26
 * @describe application基础类
 */
abstract class BaseApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }

    override fun onCreate() {
        super.onCreate()
        initBugly()
        initHttpService()
        //    initXunFei()
        initCompressorPath()
        initDiagramPath()
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
        CurrApplication.COMPRESSOR_PATH = "${cacheDir.absolutePath}${File.separator}compressor"
        File(CurrApplication.COMPRESSOR_PATH).apply { if (!exists()) this.mkdirs() }
    }

    //简图路径
    private fun initDiagramPath() {
        CurrApplication.DIAGRAM_PATH = "${filesDir.absolutePath}${File.separator}diagram"
    }

    /*初始化tencent bugly*/
    private fun initBugly() {
        CrashReport.initCrashReport(this, resources.getString(R.string.bugly_appid), false)
    }

    //网络请求
    open fun <T> doRequest(method: String, params: HashMap<String, String?>, callBack: HttpRequestCallback<in T>?) {
        val body = ApiService.createRequestBody(params, method)
        doRequest(RxHttpManager.createApi(ApiService::class.java).doPost(body), callBack)
    }

    //网络请求
    open fun <T> doRequest(observable: Observable<String>, callBack: HttpRequestCallback<in T>?) {
        val disposable = observable.compose(Transformer.switchSchedulers())
                .subscribe({ onHttpSuccess(it, callBack) }, { onHttpError(it, callBack) }, { callBack?.onComplete() }, { callBack?.onPreExecute() })
        addDisposable(disposable)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> onHttpSuccess(json: String, callBack: HttpRequestCallback<T>?) {
        callBack?.let {
            val result = AndroidBase64Utils.decodeToString(json)
            if (callBack.mType == String::class.java) {
                it.onSuccess(result as T)
            } else {
                it.onSuccess(Gson().fromJson(result, callBack.mType))
            }
        }
    }

    private fun onHttpError(e: Throwable, callBack: HttpRequestCallback<*>?) {
        callBack?.let {
            val error = ApiException.handleException(e).message
            it.onFailure(e, error)
        }
    }
}