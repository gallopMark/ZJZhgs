package com.uroad.zhgs.activity

import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import com.uroad.library.utils.SecurityUtil
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_change_password.*

/**
 *Created by MFB on 2018/8/13.
 * 修改密码
 */
class ChangePasswordActivity : BaseActivity() {
    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_change_password)
        withTitle(resources.getString(R.string.changepassword_title))
        btFinish.setOnClickListener { onUpdatePw() }
    }

    private fun onUpdatePw() {
        val oldPw = etOldPW.text.toString()
        val newPw = etNewPW.text.toString()
        val newPw2 = etNewPW2.text.toString()
        if (TextUtils.isEmpty(oldPw.trim())) {
            showShortToast(etOldPW.hint)
        } else if (TextUtils.isEmpty(newPw.trim())) {
            showShortToast(etNewPW.hint)
        } else if (TextUtils.isEmpty(newPw2.trim())) {
            showShortToast(etNewPW2.hint)
        } else if (!TextUtils.equals(newPw, newPw2)) {
            showShortToast("前后密码不一致，请重新输入")
        } else {
            updatePw(SecurityUtil.EncoderByMd5(oldPw), SecurityUtil.EncoderByMd5(newPw))
        }
    }

    private fun updatePw(oldPw: String, newPw: String) {
        doRequest(WebApiService.CHANGE_PASSWORD, WebApiService.updatePasswordParams(getUserId(), oldPw, newPw), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading("正在修改密码…")
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    showShortToast("修改成功")
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
}