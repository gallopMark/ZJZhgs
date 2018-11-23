package com.uroad.zhgs.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.text.*
import android.view.View
import com.tencent.android.tpush.XGPushManager
import com.uroad.library.utils.DeviceUtils
import com.uroad.library.utils.SecurityUtil
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.enumeration.VerificationCode
import com.uroad.zhgs.helper.UserPreferenceHelper
import com.uroad.zhgs.model.UserMDL
import com.uroad.zhgs.service.MyTracksService
import com.uroad.zhgs.utils.CheckUtils
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.utils.InputMethodUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_login.*
import java.lang.ref.WeakReference

/**
 *Created by MFB on 2018/8/6.
 *Copyright  2018年 浙江综合交通大数据开发有限公司.
 *说明：登录页面
 */
class LoginActivity : BaseActivity(), View.OnClickListener {
    private var withPassword = true   //是否是密码登录
    private var handler: MHandler? = null
    private var firstLogin: Boolean = false

    companion object {
        private const val MSG_CODE = 0x0001
    }

    override fun setUp(savedInstanceState: Bundle?) {
        withTitle(resources.getString(R.string.login_title))
        setBaseContentLayout(R.layout.activity_login)
        intent.extras?.let { firstLogin = it.getBoolean("firstLogin", false) }
        tvRegister.setOnClickListener { openActivity(RegisterActivity::class.java) }
        tvForgetPw.setOnClickListener { openActivity(ForgetPasswordActivity::class.java) }
    }

    override fun setListener() {
        tvGetCode.setOnClickListener(this)
        btLogin.setOnClickListener(this)
        tvBottom.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvGetCode -> {   //获取验证码
                val phone = etPhone.text.toString()
                if (TextUtils.isEmpty(phone)) {
                    showShortToast(resources.getString(R.string.login_phone_hint))
                } else if (!CheckUtils.isMobile(phone)) {
                    showShortToast(getString(R.string.error_phone_tips))
                } else {
                    getCode(phone)
                }
            }
            R.id.btLogin -> {  //登录按钮
                login()
            }
            R.id.tvBottom -> {
                if (withPassword) {
                    llVerificationCode.visibility = View.VISIBLE
                    llPassword.visibility = View.GONE
                    tvBottom.text = resources.getString(R.string.login_with_password)
                    withPassword = false
                } else {
                    llVerificationCode.visibility = View.GONE
                    llPassword.visibility = View.VISIBLE
                    tvBottom.text = resources.getString(R.string.login_with_verificationCode)
                    withPassword = true
                }
            }
        }
    }

    //获取验证码
    private fun getCode(phone: String) {
        doRequest(WebApiService.PUSH_CODE, WebApiService.pushCodeParams(phone, VerificationCode.LOGIN.code), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading("获取验证码…")
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    showShortToast("验证码发送成功")
                    startTimer()
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                endLoading()
                onHttpError(e)
            }
        })
    }

    //开始倒计时1分钟，再次允许发送验证码
    private fun startTimer() {
        handler = MHandler(this).apply { sendEmptyMessage(MSG_CODE) }
    }

    private class MHandler(activity: LoginActivity) : Handler() {
        private var time = 60
        private val weakReference: WeakReference<Activity> = WeakReference(activity)
        override fun handleMessage(msg: Message?) {
            if (weakReference.get() == null) return
            val activity = weakReference.get()
            if (time <= 0) {
                activity?.tvGetCode?.let {
                    it.text = activity.resources.getString(R.string.login_getverificationCode)
                    it.setBackgroundResource(R.drawable.bg_corners_30dp)
                    it.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent))
                    it.isEnabled = true
                }
                removeMessages(MSG_CODE)
            } else {
                val text = "${time}s后重新获取"
                activity?.tvGetCode?.let {
                    it.text = text
                    it.setTextColor(ContextCompat.getColor(activity, R.color.gainsboro))
                    it.setBackgroundResource(R.drawable.bg_corners_gainsboro_30dp)
                    it.isEnabled = false
                }
                time--
                sendEmptyMessageDelayed(MSG_CODE, 1000)
            }
        }
    }

    //登录
    private fun login() {
        if (withPassword) {  //如果是密码登录  则需要输入手机号和密码
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()
            when {
                TextUtils.isEmpty(phone) -> showShortToast(resources.getString(R.string.login_phone_hint))
                !CheckUtils.isMobile(phone) -> showShortToast(getString(R.string.error_phone_tips))
                TextUtils.isEmpty(password) -> showShortToast(resources.getString(R.string.login_password_hint))
                else -> loginWithType(phone, "1", SecurityUtil.EncoderByMd5(password))
            }
        } else {
            val phone = etPhone.text.toString().trim()
            val code = etVerificationCode.text.toString().trim()
            when {
                TextUtils.isEmpty(phone) -> showShortToast(resources.getString(R.string.login_phone_hint))
                TextUtils.isEmpty(code) -> showShortToast(resources.getString(R.string.login_verificationCode_hint))
                else -> loginWithType(phone, "2", code)
            }
        }
    }

    //1.密码登录  2.验证码登录
    private fun loginWithType(phone: String, type: String, password: String) {
        InputMethodUtils.hideSoftInput(this)
        doRequest(WebApiService.USER_LOGIN, WebApiService.userLoginParams(phone, type, password), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading("正在登录…")
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, UserMDL::class.java)
                    if (mdl == null) showShortToast("数据解析异常")
                    else handleLoginResult(mdl)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                endLoading()
                onHttpError(e)
            }
        })
    }

    /**
     * 登录成功，本地保存用户相关信息
     */
    private fun handleLoginResult(mdl: UserMDL) {
        mdl.isLogin = true
        UserPreferenceHelper.save(this, mdl)
        if (!TextUtils.isEmpty(mdl.pushid)) {
            XGPushManager.bindAccount(this, mdl.pushid)
        }
        val currApplication = application as CurrApplication
        currApplication.onPraiseLogin(mdl.userid, DeviceUtils.getAndroidID(this), DeviceUtils.getFingerprint())
        /*登录成功，开启记录足迹的后台服务*/
        startService(Intent(this, MyTracksService::class.java))
        if (firstLogin) {   //如果是首次登录（从未登录过，从启动页进来）
            openActivity(MainActivity::class.java)
            finish()
        } else {
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun finish() {
        InputMethodUtils.hideSoftInput(this)
        super.finish()
    }

    override fun onDestroy() {
        handler?.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}