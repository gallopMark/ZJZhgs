package com.uroad.zhgs.common

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.*
import android.widget.Toast
import com.google.gson.Gson
import com.uroad.rxhttp.exception.ApiException
import com.uroad.rxhttp.interceptor.Transformer
import com.uroad.zhgs.R
import com.uroad.zhgs.dialog.MaterialDialog
import com.uroad.zhgs.webservice.HttpRequestCallback
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_base.*
import com.uroad.library.widget.CompatToast
import android.widget.TextView
import android.widget.LinearLayout
import com.amap.api.maps.model.Poi
import com.amap.api.navi.AmapNaviPage
import com.amap.api.navi.AmapNaviParams
import com.amap.api.navi.AmapNaviType
import com.uroad.library.utils.NetworkUtils
import com.uroad.rxhttp.RxHttpManager
import com.uroad.zhgs.activity.LocationWebViewActivity
import com.uroad.zhgs.activity.ShowImageActivity
import com.uroad.zhgs.activity.WebViewActivity
import com.uroad.zhgs.dialog.LoadingDialog
import com.uroad.zhgs.helper.UserPreferenceHelper
import com.uroad.zhgs.model.UploadMDL
import com.uroad.zhgs.utils.AndroidBase64Utils
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.utils.MimeTypeTool
import com.uroad.zhgs.utils.StatusBarUtils
import com.uroad.zhgs.webservice.ApiService
import com.uroad.zhgs.webservice.upload.RequestBodyWrapper
import com.uroad.zhgs.webservice.upload.UploadFileCallback
import com.uroad.zhgs.widget.CurrencyLoadView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.lang.StringBuilder


/**
 * Created by MFB on 2018/6/4.
 * Copyright  2018年 浙江综合交通大数据开发有限公司.
 * 说明：封装的activity基础类
 */
abstract class BaseActivity : AppCompatActivity() {
    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    //添加RxJava请求，在activity退出时取消订阅，防止内存泄漏
    private val rxDisposables = CompositeDisposable()
    private val disposables = ArrayList<Disposable>()
    /*toast统一处理*/
    private var mShortToast: Toast? = null
    private var mLongToast: Toast? = null
    /*网络请求等待对话框统一处理*/
    private var loadingDialog: LoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        requestWindow()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT //强制竖屏
        setContentView(R.layout.activity_base)
//        initStatusBar()
        setToolbar()
        setUp(savedInstanceState)
        initData()
        setListener()
    }

    open fun requestWindow() {}

    open fun initStatusBar() {
//        // 系统 6.0 以上 状态栏白底黑字的实现方法
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//        }
        StatusBarUtils.statusBarMIUILightMode(window, true)
        StatusBarUtils.statusBarFlymeLightMode(window, true)
    }

    /**
     * 通过设置全屏，设置状态栏透明
     */
    open fun requestWindowFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
                //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
                window.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = Color.TRANSPARENT
                //导航栏颜色也可以正常设置
//                window.setNavigationBarColor(Color.TRANSPARENT)
            } else {
                val attributes = window.attributes
                val flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                attributes.flags = flagTranslucentStatus
                window.attributes = attributes
            }
        }
    }

    private fun setToolbar() {
        toolbar.title = ""
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        tvBaseOption.setOnClickListener { onOptionClickListener(tvBaseOption) }
    }

    fun setNavigationIconVisiable(visiable: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(visiable)
    }

    fun setNavigationIcon(resID: Int) {
        toolbar.setNavigationIcon(resID)
    }

    fun withTitle(title: CharSequence?) {
        tvBaseTitle.text = title
    }

    fun withOption(text: CharSequence) {
        if (!TextUtils.isEmpty(text)) {
            tvBaseOption.text = text
            tvBaseOption.visibility = View.VISIBLE
        } else {
            tvBaseOption.visibility = View.GONE
        }
    }

    fun withOption(resID: Int, text: CharSequence) {
        val drawable = ContextCompat.getDrawable(this, resID)
        if (drawable == null || TextUtils.isEmpty(text)) return
        drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
        tvBaseOption.setCompoundDrawables(drawable, null, null, null)
        if (!TextUtils.isEmpty(text)) {
            tvBaseOption.text = text
        }
        tvBaseOption.visibility = View.VISIBLE
    }

    fun getToolbar(): Toolbar = toolbar

    fun getBaseTitle(): TextView = tvBaseTitle

    fun getOptionView(): TextView = tvBaseOption

    open fun setBaseContentLayout(layoutId: Int) {
        layoutInflater.inflate(layoutId, baseContent, true)
    }

    open fun setBaseContentLayout(contentView: View?) {
        contentView?.let { baseContent.addView(it) }
    }

    open fun setBaseContentLayoutWithoutTitle(layoutId: Int) {
        //toolbar.visibility = View.GONE
        baseParent.removeView(toolbar)
        hideBaseLine(true)
        setBaseContentLayout(layoutId)
    }

    open fun setBaseContentLayoutWithoutTitle(contentView: View?) {
        // toolbar.visibility = View.GONE
        baseParent.removeView(toolbar)
        setBaseContentLayout(contentView)
    }

    open fun setUp(savedInstanceState: Bundle?) {}

    open fun initData() {}

    open fun setListener() {}

    open fun onOptionClickListener(tvBaseOption: TextView) {

    }

    open fun hideBaseLine(isRemove: Boolean) {
        if (isRemove) baseParent.removeView(baseLine)
        else baseLine.visibility = View.GONE
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

    //网络请求
    open fun <T> doRequest(flowable: Flowable<String>, callBack: HttpRequestCallback<T>?) {
        val disposable = flowable.compose(Transformer.transSchedulers())
                .subscribe({ onHttpSuccess(it, callBack) }, { onHttpError(it, callBack) }, { callBack?.onComplete() }, {
                    it.request(1)
                    callBack?.onPreExecute()
                })
        addDisposable(disposable)
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
    open fun doUpload(file: File, fileKey: String?, callback: UploadFileCallback?) {
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
    open fun doUpload(file: File, fileKey: String?, params: Map<String, String>?, callback: UploadFileCallback?) {
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
        addDisposable(Observable.fromArray(files).map { fileList ->
            val sb = StringBuilder()
            for (file in fileList) {
                val part = createMultipart(file, "file")
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
        val requestBody = RequestBody.create(MediaType.parse(contentTypeFor(file)), file)
        var key = fileKey
        if (key == null) key = "file"
        return MultipartBody.Part.createFormData(key, file.name, requestBody)
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
        d?.let { rxDisposables.add(it) }
        d?.let { disposables.add(it) }
    }

    open fun setPageLoading() {
        baseContent.visibility = View.GONE
        val cLoadView = CurrencyLoadView(this)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        cLoadView.layoutParams = params
        cLoadView.setState(CurrencyLoadView.STATE_LOADING)
        flBaseLoad.removeAllViews()
        flBaseLoad.addView(cLoadView)
        flBaseLoad.visibility = View.VISIBLE
    }

    open fun setPageEndLoading() {
        flBaseLoad.removeAllViews()
        flBaseLoad.visibility = View.GONE
        setPageResponse()
    }

    open fun setPageError() {
        baseContent.visibility = View.GONE
        val cLoadView = CurrencyLoadView(this)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        cLoadView.layoutParams = params
        if (!NetworkUtils.isConnected(this)) cLoadView.setState(CurrencyLoadView.STATE_NO_NETWORK)
        else cLoadView.setState(CurrencyLoadView.STATE_ERROR)
        cLoadView.setOnRetryListener(object : CurrencyLoadView.OnRetryListener {
            override fun onRetry(view: View) {
                onReload(view)
            }
        })
        flBaseLoad.removeAllViews()
        flBaseLoad.addView(cLoadView)
        flBaseLoad.visibility = View.VISIBLE
    }

    open fun setPageNoData() {
        setPageNoData(null)
    }

    open fun setPageNoData(emptyTips: CharSequence?) {
        baseContent.visibility = View.GONE
        val cLoadView = CurrencyLoadView(this)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        cLoadView.layoutParams = params
        cLoadView.setState(CurrencyLoadView.STATE_EMPTY)
        if (!TextUtils.isEmpty(emptyTips)) {
            cLoadView.setEmptyText(emptyTips)
        } else {
            cLoadView.setEmptyText(getString(R.string.page_nodata))
        }
        flBaseLoad.removeAllViews()
        flBaseLoad.addView(cLoadView)
        flBaseLoad.visibility = View.VISIBLE
    }

    open fun setPageResponse() {
        if (baseContent.visibility != View.VISIBLE)
            baseContent.visibility = View.VISIBLE
        flBaseLoad.visibility = View.GONE
    }

    open fun onReload(view: View) {
        initData()
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
        val intent = Intent(this, c)
        bundle?.let { intent.putExtras(it) }
        uri?.let { intent.data = it }
        startActivity(intent)
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

    fun showBigPic(position: Int, photos: ArrayList<String>) {
        val intent = Intent(this, ShowImageActivity::class.java).apply {
            putExtra("position", position)
            putStringArrayListExtra("photos", photos)
        }
        openActivity(intent)
    }

    fun openActivity(intent: Intent) {
        openActivity(intent, null)
    }

    fun openActivity(intent: Intent, bundle: Bundle?) {
        openActivity(intent, bundle, null)
    }

    fun openActivity(intent: Intent, bundle: Bundle?, uri: Uri?) {
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
        val intent = Intent(this, c)
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
        bundle?.let { intent.putExtras(it) }
        uri?.let { intent.data = it }
        startActivityForResult(intent, requestCode)
    }

    fun showTipsDialog(title: CharSequence?, message: CharSequence) {
        showTipsDialog(title, message, resources.getString(R.string.dialog_button_confirm))
    }

    open fun showTipsDialog(title: CharSequence?, message: CharSequence, textConfirm: CharSequence) {
        showTipsDialog(title, message, textConfirm, null)
    }

    open fun showTipsDialog(title: CharSequence?, message: CharSequence, textConfirm: CharSequence, listener: MaterialDialog.ButtonClickListener?) {
        val dialog = MaterialDialog(this)
        dialog.setTitle(title)
        dialog.setMessage(message)
        dialog.hideDivider()
        dialog.setPositiveButton(textConfirm, listener)
        dialog.show()
    }

    fun showDialog(title: CharSequence?, message: CharSequence, cancelListener: MaterialDialog.ButtonClickListener?,
                   confirmListener: MaterialDialog.ButtonClickListener?) {
        showDialog(title, message, resources.getString(R.string.dialog_button_cancel),
                resources.getString(R.string.dialog_button_confirm), cancelListener, confirmListener)
    }

    fun showDialog(title: CharSequence?, message: CharSequence,
                   textCancel: CharSequence, textConfirm: CharSequence,
                   cancelListener: MaterialDialog.ButtonClickListener?,
                   confirmListener: MaterialDialog.ButtonClickListener?) {
        val dialog = MaterialDialog(this)
        dialog.setTitle(title)
        dialog.setMessage(message)
        dialog.setNegativeButton(textCancel, cancelListener)
        dialog.setPositiveButton(textConfirm, confirmListener)
        dialog.show()
    }

    fun onHttpError(e: Throwable) {
        if (!NetworkUtils.isConnected(this)) {
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

    fun showShortToast(text: CharSequence?) {
        if (TextUtils.isEmpty(text)) return
        val v = LayoutInflater.from(this).inflate(R.layout.layout_base_toast, LinearLayout(this), false)
        val textView = v.findViewById<TextView>(R.id.tv_text)
        textView.text = text
        if (mShortToast == null) {
            mShortToast = CompatToast(this, R.style.CompatToast).apply {
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
        val v = LayoutInflater.from(this).inflate(R.layout.layout_base_toast, LinearLayout(this), false)
        val textView = v.findViewById<TextView>(R.id.tv_text)
        textView.text = text
        if (mLongToast == null) {
            mLongToast = CompatToast(this, R.style.CompatToast).apply {
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
        loadingDialog = LoadingDialog(this).setMsg(msg).apply { show() }
    }

    fun endLoading() {
        loadingDialog?.let {
            it.dismiss()
            loadingDialog = null
        }
    }

    fun openSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", applicationContext.packageName, null)
            intent.data = uri
            startActivity(intent)
        } catch (e: Exception) {
        }
    }

    fun getUserId(): String {
        return UserPreferenceHelper.getUserId(this)
    }

    fun getUserUUID(): String = UserPreferenceHelper.getUserUUID(this)

    fun getRealName(): String {
        return UserPreferenceHelper.getRealName(this)
    }

    fun getCardNo(): String {
        return UserPreferenceHelper.getCardNo(this)
    }

    fun getPhone(): String {
        return UserPreferenceHelper.getPhone(this)
    }

    fun getUserName(): String {
        return UserPreferenceHelper.getUserName(this)
    }

    fun getStatus(): Int {
        return UserPreferenceHelper.getStatus(this)
    }

    fun getIconFile(): String {
        return UserPreferenceHelper.getIconFile(this)
    }

    fun getSex(): Int {
        return UserPreferenceHelper.getSex(this)
    }

    fun isLogin(): Boolean {
        return UserPreferenceHelper.isLogin(this)
    }

    fun getRequestCode() = UserPreferenceHelper.getRequestCode(this)

    fun getQRCode() = UserPreferenceHelper.getQRCode(this)

    fun isAuth() = UserPreferenceHelper.isAuth(this)
    //启动导航页面
    fun openNaviPage(start: Poi?, end: Poi) {
        AmapNaviPage.getInstance().showRouteActivity(this, AmapNaviParams(start, null, end, AmapNaviType.DRIVER), null)
    }

    override fun onDestroy() {
        cancelRequest()
        loadingDialog?.dismiss()
        rxDisposables.dispose()
        mShortToast?.cancel()
        mLongToast?.cancel()
        super.onDestroy()
    }

    private fun cancelRequest() {
        for (d in disposables) {
            if (!d.isDisposed) {
                d.dispose()
            }
        }
    }
}