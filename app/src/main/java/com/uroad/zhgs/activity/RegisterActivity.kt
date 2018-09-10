package com.uroad.zhgs.activity

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.text.*
import android.view.View
import com.uroad.library.utils.SecurityUtil
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.enumeration.VerificationCode
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_register.*
import java.lang.ref.WeakReference

/**
 *Created by MFB on 2018/8/6.
 */
class RegisterActivity : BaseActivity(), View.OnClickListener {
    private var handler: MHandler? = null

    companion object {
        private const val MSG_CODE = 0x0001
    }

    override fun setUp(savedInstanceState: Bundle?) {
        withTitle(resources.getString(R.string.register_title))
        setBaseContentLayout(R.layout.activity_register)
    }


    override fun setListener() {
        tvGetCode.setOnClickListener(this)
        btRegister.setOnClickListener(this)
        tvBottom.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvGetCode -> {
                val phone = etPhone.text.toString()
                if (TextUtils.isEmpty(phone)) {
                    showShortToast(resources.getString(R.string.login_phone_hint))
                } else {
                    getCode(phone)
                }
            }
            R.id.btRegister -> {
                register()
            }
            R.id.tvBottom -> {
                finish()
            }
        }
    }

    //获取验证码
    private fun getCode(phone: String) {
        doRequest(WebApiService.PUSH_CODE, WebApiService.pushCodeParams(phone, VerificationCode.REGISTER.code), object : HttpRequestCallback<String>() {
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

    private fun startTimer() {
        handler = MHandler(this).apply { sendEmptyMessage(MSG_CODE) }
    }

    private class MHandler(activity: RegisterActivity) : Handler() {
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

    private fun register() {
        val phone = etPhone.text.toString()
        if (TextUtils.isEmpty(phone)) {
            showShortToast(resources.getString(R.string.register_phone_hint))
            return
        }
        val password1 = etPassword1.text.toString()
        if (TextUtils.isEmpty(password1)) {
            showShortToast(resources.getString(R.string.register_password_hint))
            return
        }
        val password2 = etPassword2.text.toString()
        if (TextUtils.isEmpty(password2)) {
            showShortToast(resources.getString(R.string.register_passwrod2_hint))
            return
        }
        if (password1 != password2) {
            showLongToast(resources.getString(R.string.register_password_error))
            return
        }
        val code = etVerificationCode.text.toString()
        if (TextUtils.isEmpty(code)) {
            showShortToast(resources.getString(R.string.register_verificationCode_hint))
            return
        }
        if (!checkbox.isChecked) {
            showShortToast(resources.getString(R.string.register_bottom_tips_hint))
            return
        }
        doRegister(phone, SecurityUtil.EncoderByMd5(password1), code)
    }

    //执行注册请求
    private fun doRegister(phone: String, password: String, code: String) {
        doRequest(WebApiService.USER_REGISTER, WebApiService.userRegisterParams(phone, password, code), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading("正在注册…")
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    showShortToast("注册成功")
                    Handler().postDelayed({ if (!isFinishing) finish() }, 1500)
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

    override fun onDestroy() {
        handler?.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}