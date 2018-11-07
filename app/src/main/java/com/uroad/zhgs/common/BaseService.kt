package com.uroad.zhgs.common

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.gson.Gson
import com.uroad.rxhttp.RxHttpManager
import com.uroad.rxhttp.exception.ApiException
import com.uroad.rxhttp.interceptor.Transformer
import com.uroad.zhgs.helper.UserPreferenceHelper
import com.uroad.zhgs.utils.AndroidBase64Utils
import com.uroad.zhgs.webservice.ApiService
import com.uroad.zhgs.webservice.HttpRequestCallback
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * @author MFB
 * @create 2018/10/21
 * @describe 后台服务基础类
 */
abstract class BaseService : Service() {
    //添加RxJava请求，在activity退出时取消订阅，防止内存泄漏
    private var rxDisposables: CompositeDisposable? = null

    override fun onBind(intent: Intent): IBinder? = getBinder(intent)

    abstract fun getBinder(intent: Intent): IBinder?
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

    open fun addDisposable(d: Disposable?) {
        val compositeDisposable = rxDisposables
        if (compositeDisposable == null) {
            rxDisposables = CompositeDisposable().apply { d?.let { add(it) } }
        } else {
            d?.let { compositeDisposable.add(it) }
        }
    }

    fun getUserId(): String = UserPreferenceHelper.getUserId(this)

    fun isLogin(): Boolean = UserPreferenceHelper.isLogin(this)
    override fun onDestroy() {
        rxDisposables?.dispose()
        super.onDestroy()
    }
}