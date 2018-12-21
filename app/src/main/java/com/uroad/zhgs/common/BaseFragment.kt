package com.uroad.zhgs.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.amap.api.maps.model.Poi
import com.amap.api.navi.AmapNaviPage
import com.amap.api.navi.AmapNaviParams
import com.amap.api.navi.AmapNaviType
import com.google.gson.Gson
import com.uroad.library.utils.NetworkUtils
import com.uroad.library.widget.CompatToast
import com.uroad.rxhttp.RxHttpManager
import com.uroad.rxhttp.exception.ApiException
import com.uroad.rxhttp.interceptor.Transformer
import com.uroad.zhgs.activity.LocationWebViewActivity
import com.uroad.zhgs.R
import com.uroad.zhgs.activity.ShowImageActivity
import com.uroad.zhgs.activity.WebViewActivity
import com.uroad.zhgs.dialog.LoadingDialog
import com.uroad.zhgs.dialog.MaterialDialog
import com.uroad.zhgs.helper.UserPreferenceHelper
import com.uroad.zhgs.model.UploadMDL
import com.uroad.zhgs.utils.AndroidBase64Utils
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.utils.MimeTypeTool
import com.uroad.zhgs.webservice.ApiService
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.upload.RequestBodyWrapper
import com.uroad.zhgs.webservice.upload.UploadFileCallback
import com.uroad.zhgs.widget.CurrencyLoadView
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.lang.StringBuilder
import java.net.URLConnection

@Suppress("UNCHECKED_CAST")
abstract class BaseFragment : Fragment() {
    lateinit var context: Activity
    private var rootView: View? = null
    open lateinit var baseParent: RelativeLayout
    lateinit var flBaseContent: FrameLayout
    lateinit var flBaseLoad: FrameLayout
    private val rxDisposables = CompositeDisposable()
    private val disposables = ArrayList<Disposable>()
    private var mShortToast: Toast? = null
    private var mLongToast: Toast? = null
    private var loadingDialog: LoadingDialog? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.context = context as Activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_base, container, false).apply {
                baseParent = findViewById(R.id.baseParent)
                flBaseContent = findViewById(R.id.flBaseContent)
                flBaseLoad = findViewById(R.id.flBaseLoad)
                LayoutInflater.from(context).inflate(setBaseLayoutResID(), flBaseContent)
            }
        }
        // 缓存的rootView需要判断是否已经被加过parent，如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        rootView?.parent?.let { (it as ViewGroup).removeView(rootView) }
        return rootView
    }

    abstract fun setBaseLayoutResID(): Int

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUp(view, savedInstanceState)
        setListener()
    }

    open fun setUp(view: View, savedInstanceState: Bundle?) {}

    open fun setListener() {}

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initData()
    }

    open fun initData() {}

    open fun setPageLoading() {
        flBaseContent.visibility = View.GONE
        val cLoadView = CurrencyLoadView(context)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        cLoadView.layoutParams = params
        cLoadView.setState(CurrencyLoadView.STATE_LOADING)
        flBaseLoad.removeAllViews()
        flBaseLoad.addView(cLoadView)
        flBaseLoad.visibility = View.VISIBLE
    }

    open fun setPageEndLoading(showContent: Boolean) {
        if (showContent) setPageEndLoading()
        else flBaseContent.visibility = View.GONE
    }

    open fun setPageEndLoading() {
        flBaseLoad.removeAllViews()
        flBaseLoad.visibility = View.GONE
        if (flBaseContent.visibility != View.VISIBLE)
            flBaseContent.visibility = View.VISIBLE
    }

    open fun setPageError() {
        flBaseContent.visibility = View.GONE
        val cLoadView = CurrencyLoadView(context)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        cLoadView.layoutParams = params
        if (!NetworkUtils.isConnected(context)) cLoadView.setState(CurrencyLoadView.STATE_NO_NETWORK)
        else cLoadView.setState(CurrencyLoadView.STATE_ERROR)
        cLoadView.setOnRetryListener(object : CurrencyLoadView.OnRetryListener {
            override fun onRetry(view: View) {
                onReLoad(view)
            }
        })
        flBaseLoad.removeAllViews()
        flBaseLoad.addView(cLoadView)
        flBaseLoad.visibility = View.VISIBLE
    }

    open fun setPageNoData() {
        flBaseContent.visibility = View.GONE
        val cLoadView = CurrencyLoadView(context)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        cLoadView.layoutParams = params
        cLoadView.setState(CurrencyLoadView.STATE_EMPTY)
        flBaseLoad.removeAllViews()
        flBaseLoad.addView(cLoadView)
        flBaseLoad.visibility = View.VISIBLE
    }

    open fun onReLoad(view: View) {}

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

    open fun <T> doRequest(flowable: Flowable<String>, callBack: HttpRequestCallback<in T>?) {
        val disposable = flowable.compose(Transformer.transSchedulers())
                .subscribe({ onHttpSuccess(it, callBack) }, { onHttpError(it, callBack) }, { callBack?.onComplete() }, {
                    it.request(1)
                    callBack?.onPreExecute()
                })
        addDisposable(disposable)
    }

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

    //文件上传
    open fun doUpload(filePath: String?, fileKey: String?, callback: UploadFileCallback?) {
        if (TextUtils.isEmpty(filePath) && !File(filePath).exists()) {
            showShortToast("上传的文件不存在")
            return
        }
        doUpload(File(filePath), fileKey, null, callback)
    }

    //文件上传
    open fun doUpload(file: File?, fileKey: String?, callback: UploadFileCallback?) {
        doUpload(file, fileKey, null, callback)
    }

    //文件上传
    open fun doUpload(filePath: String, fileKey: String?, params: Map<String, String>?, callback: UploadFileCallback?) {
        if (!File(filePath).exists()) {
            showShortToast("上传的文件不存在")
            return
        }
        doUpload(File(filePath), fileKey, params, callback)
    }

    //文件上传
    open fun doUpload(file: File?, fileKey: String?, params: Map<String, String>?, callback: UploadFileCallback?) {
        if (file == null || !file.exists()) {
            showShortToast("上传的文件不存在")
            return
        }
        val requestBody = RequestBody.create(MediaType.parse(contentTypeFor(file)), file)
        val wrapper = RequestBodyWrapper(requestBody, callback)
        var key = fileKey
        if (key == null) key = "file"
        val part = MultipartBody.Part.createFormData(key, file.name, wrapper)
        val observable = if (params == null) RxHttpManager.createApi(ApiService::class.java).uploadFile(part)
        else RxHttpManager.createApi(ApiService::class.java).uploadFile(part, params)
        val disposable = observable
                .compose(Transformer.switchSchedulers())
                .subscribe({
                    val json = it?.string()
                    if (json != null) {
                        callback?.onSuccess(json)
                    } else {
                        callback?.onFailure(Throwable())
                    }
                }, {
                    callback?.onFailure(it)
                }, {
                    callback?.onComplete()
                }, {
                    callback?.onStart(it)
                })
        addDisposable(disposable)
    }

    open fun uploadFiles(files: MutableList<File>, callback: UploadFileCallback?) {
        addDisposable(Observable.fromCallable {
            val sb = StringBuilder()
            for (i in 0 until files.size) {
                val part = createMultipart(files[i], "file")
                RxHttpManager.createApi(ApiService::class.java).uploadFile(part)
                        .subscribe({ body ->
                            val json = body?.string()
                            if (GsonUtils.isResultOk(json)) {
                                val imageMDL = GsonUtils.fromDataBean(json, UploadMDL::class.java)
                                imageMDL?.imgurl?.file?.let { sb.append("$it,") }
                            }
                        }, {})
            }
            sb
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ sb ->
                    sb.deleteCharAt(sb.lastIndexOf(","))
                    callback?.onSuccess(sb.toString())
                }, {
                    callback?.onFailure(it)
                }, {
                    callback?.onComplete()
                }, {
                    callback?.onStart(it)
                }))
    }

    private fun contentTypeFor(file: File): String = MimeTypeTool.getMimeType(file)

    open fun createMultipart(file: File, fileKey: String?): MultipartBody.Part {
        return createMultipart(file, fileKey, null)
    }

    open fun createMultipart(file: File, fileKey: String?, params: Map<String, String>?): MultipartBody.Part {
        val fileNameMap = URLConnection.getFileNameMap()
        var contentTypeFor: String? = fileNameMap.getContentTypeFor(file.name)
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream"
        }
        val requestBody = RequestBody.create(MediaType.parse(contentTypeFor), file)
        var key = fileKey
        if (key == null) key = "file"
        return MultipartBody.Part.createFormData(key, file.name, requestBody)
    }

    fun onHttpError(e: Throwable) {
        if (!NetworkUtils.isConnected(context)) {
            showShortToast("未连接网络，请检查WiFi或数据是否开启")
        } else {
            var error = "网络连接异常，请检查您的网络状态，稍后重试！"
            ApiException.handleException(e).message?.let { error = it }
            showShortToast(error)
        }
    }

    fun onJsonParseError() {
        showShortToast("数据解析错误")
    }

    open fun addDisposable(d: Disposable?) {
        d?.let { rxDisposables.add(it) }
        d?.let { disposables.add(it) }
    }

    fun showBigPic(position: Int, photos: ArrayList<String>) {
        val intent = Intent(context, ShowImageActivity::class.java).apply {
            putExtra("position", position)
            putStringArrayListExtra("photos", photos)
        }
        openActivity(intent)
    }

    // 封装跳转
    fun openActivity(c: Class<*>) {
        openActivity(c, null)
    }

    // 跳转 传递数据 bundel
    fun openActivity(c: Class<*>, bundle: Bundle?) {
        openActivity(c, bundle, null)
    }

    fun openActivity(c: Class<*>, bundle: Bundle?, uri: Uri?) {
        if (activity == null) return
        val intent = Intent(context, c)
        bundle?.let { intent.putExtras(it) }
        uri?.let { intent.data = it }
        startActivity(intent)
    }

    fun openActivity(intent: Intent) {
        openActivity(intent, null)
    }

    fun openActivity(intent: Intent, bundle: Bundle?) {
        openActivity(intent, bundle, null)
    }

    fun openActivity(intent: Intent, bundle: Bundle?, uri: Uri?) {
        if (activity == null) return
        bundle?.let { intent.putExtras(it) }
        uri?.let { intent.data = uri }
        startActivity(intent)
    }

    fun openActivityForResult(c: Class<*>, requestCode: Int) {
        openActivityForResult(c, null, requestCode)
    }

    fun openActivityForResult(c: Class<*>, bundle: Bundle?, requestCode: Int) {
        openActivityForResult(c, bundle, null, requestCode)
    }

    fun openActivityForResult(c: Class<*>, bundle: Bundle?, uri: Uri?, requestCode: Int) {
        if (activity == null) return
        val intent = Intent(context, c)
        bundle?.let { intent.putExtras(it) }
        uri?.let { intent.data = it }
        startActivityForResult(intent, requestCode)
    }

    fun openActivityForResult(intent: Intent, requestCode: Int) {
        openActivityForResult(intent, null, requestCode)
    }

    fun openActivityForResult(intent: Intent, bundle: Bundle?, requestCode: Int) {
        openActivityForResult(intent, bundle, null, requestCode)
    }

    fun openActivityForResult(intent: Intent, bundle: Bundle?, uri: Uri?, requestCode: Int) {
        if (activity == null) return
        bundle?.let { intent.putExtras(it) }
        uri?.let { intent.data = it }
        startActivityForResult(intent, requestCode)
    }

    fun openLocationWebActivity(url: String?, title: String?) {
        openActivity(LocationWebViewActivity::class.java, Bundle().apply {
            putString(LocationWebViewActivity.WEB_URL, url)
            putString(LocationWebViewActivity.WEB_TITLE, title)
        })
    }

    fun openWebActivity(url: String?, title: String?) {
        openActivity(WebViewActivity::class.java, Bundle().apply {
            putString(WebViewActivity.WEB_URL, url)
            putString(WebViewActivity.WEB_TITLE, title)
        })
    }

    open fun showDialog(title: String?, message: String,
                        textCancel: String, textConfirm: String,
                        cancelListener: MaterialDialog.ButtonClickListener?,
                        confirmListener: MaterialDialog.ButtonClickListener?) {
        val dialog = MaterialDialog(context)
        dialog.setTitle(title)
        dialog.setMessage(message)
        dialog.setNegativeButton(textCancel, cancelListener)
        dialog.setPositiveButton(textConfirm, confirmListener)
        dialog.show()
    }

    fun showShortToast(text: CharSequence?) {
        if (TextUtils.isEmpty(text)) return
        val v = LayoutInflater.from(context).inflate(R.layout.layout_base_toast, LinearLayout(context), false)
        val textView = v.findViewById<TextView>(R.id.tv_text)
        textView.text = text
        if (mShortToast == null) {
            mShortToast = CompatToast(context, R.style.CompatToast).apply {
                duration = Toast.LENGTH_SHORT
            }
        }
        mShortToast?.let {
            it.view = v
            it.show()
        }
    }

    fun showLongToast(text: CharSequence?) {
        if (TextUtils.isEmpty(text)) return
        val v = LayoutInflater.from(context).inflate(R.layout.layout_base_toast, LinearLayout(context), false)
        val textView = v.findViewById<TextView>(R.id.tv_text)
        textView.text = text
        if (mLongToast == null) {
            mLongToast = CompatToast(context, R.style.CompatToast).apply {
                duration = Toast.LENGTH_LONG
            }
        }
        mLongToast?.view = v
        mLongToast?.show()
    }

    fun showLoading() {
        showLoading(null)
    }

    fun showLoading(msg: CharSequence?) {
        loadingDialog?.let {
            it.dismiss()
            loadingDialog = null
        }
        loadingDialog = LoadingDialog(context).setMsg(msg).apply { show() }
    }

    fun endLoading() {
        loadingDialog?.let {
            it.dismiss()
            loadingDialog = null
        }
    }

    fun getUserId(): String {
        return UserPreferenceHelper.getUserId(context)
    }

    fun getUserUUID(): String = UserPreferenceHelper.getUserUUID(context)
    fun getRealName(): String {
        return UserPreferenceHelper.getRealName(context)
    }

    fun getCardNo(): String {
        return UserPreferenceHelper.getCardNo(context)
    }

    fun getPhone(): String {
        return UserPreferenceHelper.getPhone(context)
    }

    fun getUserName(): String {
        return UserPreferenceHelper.getUserName(context)
    }

    fun getStatus(): Int {
        return UserPreferenceHelper.getStatus(context)
    }

    fun getIconFile(): String {
        return UserPreferenceHelper.getIconFile(context)
    }

    fun getSex(): Int {
        return UserPreferenceHelper.getSex(context)
    }

    fun isLogin(): Boolean {
        return UserPreferenceHelper.isLogin(context)
    }

    fun isAuth() = UserPreferenceHelper.isAuth(context)
    //启动导航页面
    fun openNaviPage(start: Poi?, end: Poi) {
        AmapNaviPage.getInstance().showRouteActivity(context, AmapNaviParams(start, null, end, AmapNaviType.DRIVER), null)
    }

    fun openSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context.applicationContext.packageName, null)
            intent.data = uri
            startActivity(intent)
        } catch (e: Exception) {
        }
    }

    override fun onDestroyView() {
        cancelRequest()
        rxDisposables.dispose()
        mShortToast?.cancel()
        mLongToast?.cancel()
        loadingDialog?.dismiss()
        super.onDestroyView()
    }

    private fun cancelRequest() {
        for (d in disposables) {
            if (!d.isDisposed) {
                d.dispose()
            }
        }
    }
}