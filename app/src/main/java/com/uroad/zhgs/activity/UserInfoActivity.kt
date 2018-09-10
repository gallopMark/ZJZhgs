package com.uroad.zhgs.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.helper.UserPreferenceHelper
import com.uroad.zhgs.dialog.EditDialog
import com.uroad.zhgs.model.UploadMDL
import com.uroad.zhgs.model.UserMDL
import com.uroad.zhgs.photopicker.data.ImagePicker
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import com.uroad.zhgs.webservice.upload.UploadFileCallback
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_userinfo.*

/**
 *Created by MFB on 2018/8/13.
 */
class UserInfoActivity : BaseActivity() {
    companion object {
        private const val REQUEST_IMAGE = 0x0001
        private const val REQUEST_PERFECT = 0x0002
    }

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_userinfo)
        withTitle(resources.getString(R.string.userinfo_title))
        setDataLocal()
    }

    //加载本地保存的用户资料信息
    private fun setDataLocal() {
        ImageLoaderV4.getInstance().displayImage(this, getIconFile(), ivUserIcon, R.mipmap.ic_user_default)
        tvUserName.text = getUserName()
        tvPhone.text = getPhone()
        //如果已经实名认证则显示已认证图标
        if (!TextUtils.isEmpty(getRealName()) && !TextUtils.isEmpty(getCardNo())) {
            ivStatus.visibility = View.VISIBLE
        } else {
            ivStatus.visibility = View.GONE
        }
    }

    override fun setListener() {
        llImage.setOnClickListener { ImagePicker.from(this).requestCode(REQUEST_IMAGE).start() }
        llUserName.setOnClickListener {
            EditDialog(this).withHint("请输入昵称").withButtonText("确定").withButtonClickListener(object : EditDialog.OnButtonClickListener {
                override fun onButtonClick(content: String, dialog: EditDialog) {
                    updateUserName(content)
                    dialog.dismiss()
                }
            }).show()
        }
        llPerfect.setOnClickListener { openActivityForResult(PerfectUserInfoActivity::class.java, REQUEST_PERFECT) }
    }

    override fun initData() {
        doRequest(WebApiService.USER_DATA, WebApiService.userDataParams(getUserId()), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, UserMDL::class.java)
                    if (mdl == null) showShortToast("数据解析异常")
                    else updateData(mdl)
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

    private fun updateData(mdl: UserMDL) {
        ImageLoaderV4.getInstance().displayImage(this, mdl.iconfile, ivUserIcon)
        tvUserName.text = mdl.username
        tvPhone.text = mdl.phone
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK && data != null) {
            val items = data.getStringArrayListExtra(ImagePicker.EXTRA_PATHS)
            if (items != null && items.size > 0) {
                uploadImage(items[0])
            }
        } else if (requestCode == REQUEST_PERFECT && resultCode == RESULT_OK) {
            setDataLocal()
        }
    }

    private fun uploadImage(filePath: String) {
        doUpload(filePath, "file", object : UploadFileCallback() {
            override fun onStart(disposable: Disposable) {
                showLoading("正在上传头像…")
            }

            override fun onSuccess(json: String) {
                endLoading()
                if (GsonUtils.isResultOk(json)) {
                    val imageMDL = GsonUtils.fromDataBean(json, UploadMDL::class.java)
                    val url = imageMDL?.imgurl?.file
                    url?.let { updateIcon(it) }
                } else {
                    showShortToast(GsonUtils.getMsg(json))
                }
            }

            override fun onFailure(e: Throwable) {
                endLoading()
                showShortToast("头像上传失败，请稍后再试")
            }
        })
    }

    //更新用户头像
    private fun updateIcon(iconfile: String) {
        doRequest(WebApiService.UPDATE_USER_DATA, WebApiService.updateUserData(getUserId(), getUserName(), iconfile), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    UserPreferenceHelper.saveIconFile(this@UserInfoActivity, iconfile)
                    setDataLocal()
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                onHttpError(e)
            }
        })
    }

    private fun updateUserName(username: String) {
        doRequest(WebApiService.UPDATE_USER_DATA, WebApiService.updateUserData(getUserId(), username, getIconFile()), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    UserPreferenceHelper.saveUserName(this@UserInfoActivity, username)
                    setDataLocal()
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