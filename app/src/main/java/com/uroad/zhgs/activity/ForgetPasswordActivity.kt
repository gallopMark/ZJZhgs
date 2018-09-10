package com.uroad.zhgs.activity

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import com.uroad.library.utils.SecurityUtil
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.enumeration.VerificationCode
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_forget_password.*
import java.lang.ref.WeakReference

/**
 *Created by MFB on 2018/8/15.
 * 忘记密码页面
 */
class ForgetPasswordActivity : BaseActivity() {
    private var handler: MHandler? = null

    companion object {
        private const val MSG_CODE = 0x0001
    }

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_forget_password)
        withTitle(resources.getString(R.string.forgetpw_title))
        tvGetCode.setOnClickListener { onGetCode() }
        btReset.setOnClickListener { onReset() }
    }

    private fun onGetCode() {
        val phone = etPhone.text.toString()
        if (TextUtils.isEmpty(phone.trim())) {
            showShortToast(etPhone.hint)
        } else {
            getCode(phone)
        }
    }

    //获取验证码
    private fun getCode(phone: String) {
        doRequest(WebApiService.PUSH_CODE, WebApiService.pushCodeParams(phone, VerificationCode.RETRIEVE.code), object : HttpRequestCallback<String>() {
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

    private class MHandler(activity: ForgetPasswordActivity) : Handler() {
        private var time = 60
        private val weakReference: WeakReference<Activity> = WeakReference(activity)
        override fun handleMessage(msg: Message?) {
            if (weakReference.get() == null) return
            val activity = weakReference.get()
            if (time <= 0) {
                activity?.tvGetCode?.let {
                    it.text = activity.resources.getString(R.string.login_getverificationCode)
                    it.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent))
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

    private fun onReset() {
        val phone = etPhone.text.toString()
        if (TextUtils.isEmpty(phone.trim())) {
            showShortToast(etPhone.hint)
            return
        }
        val password = etPassword.text.toString()
        if (TextUtils.isEmpty(password)) {
            showShortToast(etPassword.hint)
            return
        }
        val password2 = etPassword2.text.toString()
        if (TextUtils.isEmpty(password2)) {
            showShortToast(resources.getString(R.string.register_passwrod2_hint))
            return
        }
        if (password != password2) {
            showLongToast(resources.getString(R.string.forgetpw_password_error))
            return
        }
        val code = etVerificationCode.text.toString()
        if (TextUtils.isEmpty(code.trim())) {
            showShortToast(etVerificationCode.hint)
            return
        }
        reset(phone, code, SecurityUtil.EncoderByMd5(password))
    }

    private fun reset(phone: String, code: String, password: String) {
        doRequest(WebApiService.RETRIEVE_PW, WebApiService.retrievedPwParams(phone, password, code), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading("正在提交…")
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    showLongToast(GsonUtils.getDataAsString(data))
                    Handler().postDelayed({ if (!isFinishing) finish() }, 2000)
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
}