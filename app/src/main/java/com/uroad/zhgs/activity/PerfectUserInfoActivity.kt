package com.uroad.zhgs.activity

import android.os.Bundle
import android.text.TextUtils
import com.uroad.library.utils.IdCardUtil
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.helper.UserPreferenceHelper
import com.uroad.zhgs.utils.CheckUtils
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_perfect_userinfo.*

/**
 *Created by MFB on 2018/8/13.
 * 完善个人信息
 */
class PerfectUserInfoActivity : BaseActivity() {
    private var isAlert = false
    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_perfect_userinfo)
        withTitle(resources.getString(R.string.perfect_userinfo_title))
        setLocalData()
        btSave.setOnClickListener { onSave() }
    }

    private fun setLocalData() {
        if (!TextUtils.isEmpty(getRealName())) {
            etName.setText(getRealName())
            etName.setSelection(etName.text.length)
        }
        if (!TextUtils.isEmpty(getCardNo())) {
            etCardNo.setText(getCardNo())
            etCardNo.setSelection(etCardNo.text.length)
        }
        if (!TextUtils.isEmpty(getRealName()) || !TextUtils.isEmpty(getCardNo())) {
            btSave.text = resources.getString(R.string.perfect_userinfo_change)
            isAlert = true
        } else {
            btSave.text = resources.getString(R.string.perfect_userinfo_save)
            isAlert = false
        }
    }

    private fun onSave() {
        val name = etName.text.toString()
        val cardNo = etCardNo.text.toString()
        when {
            TextUtils.isEmpty(name.trim()) -> showShortToast(etName.hint)
            TextUtils.isEmpty(cardNo) -> showShortToast(etCardNo.hint)
            !CheckUtils.isIDCard(cardNo) -> showShortToast(getString(R.string.error_idCard_tips))
            else -> save(name, cardNo)
        }
    }

    private fun save(name: String, cardno: String) {
        doRequest(WebApiService.PERFECT_DATA, WebApiService.perfectDataParams(getUserId(), name, cardno), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    if (isAlert) showShortToast("修改成功")
                    else showShortToast("保存成功")
                    UserPreferenceHelper.saveRealName(this@PerfectUserInfoActivity, name)
                    UserPreferenceHelper.saveCardNo(this@PerfectUserInfoActivity, cardno)
                    setResult(RESULT_OK)
                    finish()
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                onHttpError(e)
            }
        })
    }
}