package com.uroad.zhgs.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.tencent.connect.common.Constants
import com.tencent.connect.share.QQShare
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.library.utils.DisplayUtils
import com.uroad.share.wechat.Utils
import com.uroad.zhgs.R
import com.uroad.zhgs.common.ThemeStyleActivity
import com.uroad.zhgs.dialog.ShareDialog
import com.uroad.zhgs.helper.UserPreferenceHelper
import com.uroad.zhgs.model.ShareMDL
import com.uroad.zhgs.model.UserMDL
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_invitation_code.*
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

/**
 * @author MFB
 * @create 2018/10/17
 * @describe 我的邀请码页面
 */
class MyInvitationCodeActivity : ThemeStyleActivity() {

    private var shareMDL: ShareMDL? = null
    private var mWXApi: IWXAPI? = null
    private var mTencent: Tencent? = null
    private var isDialogShare = false

    override fun themeSetUp(savedInstanceState: Bundle?) {
        setLayoutResIdWithOutTitle(R.layout.activity_invitation_code)
        requestWindowFullScreen()
        setSupportActionBar(customToolbar)
        supportActionBar?.title = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            customToolbar.layoutParams = (customToolbar.layoutParams as LinearLayout.LayoutParams).apply { topMargin = DisplayUtils.getStatusHeight(this@MyInvitationCodeActivity) }
        customToolbar.setNavigationOnClickListener { onBackPressed() }
        customToolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.share) {
                val mdl = shareMDL
                if (mdl == null)
                    getShareInfo()
                else
                    share(mdl)
            }
            return@setOnMenuItemClickListener true
        }
        initCode()
    }

    /*二维码图片宽高占屏幕的1/3*/
    private fun initCode() {
        tvQRCode.text = getRequestCode()
        val size = DisplayUtils.getWindowWidth(this) / 3
        ivQRCode.layoutParams = (ivQRCode.layoutParams as LinearLayout.LayoutParams).apply {
            width = size
            height = size
        }
        if (!TextUtils.isEmpty(getQRCode())) ImageLoaderV4.getInstance().displayImage(this, getQRCode(), ivQRCode, R.color.color_f2)
        tvCopy.setOnClickListener {
            if (!TextUtils.isEmpty(tvQRCode.text.toString())) {
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.primaryClip = ClipData.newPlainText(null, tvQRCode.text.toString())
                showShortToast("复制成功")
            }
        }
    }

    override fun initData() {
        /*用户的邀请码已保存到本地，则不调接口*/
        if (!TextUtils.isEmpty(getRequestCode()) && !TextUtils.isEmpty(getQRCode())) return
        doRequest(WebApiService.USER_LOGIN, WebApiService.userLoginParams(getPhone(), "1", UserPreferenceHelper.getUserPassword(this)), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, UserMDL::class.java)
                    if (mdl == null) onJsonParseError()
                    else updateUI(mdl)
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

    private fun updateUI(mdl: UserMDL) {
        tvQRCode.text = mdl.requestcode
        UserPreferenceHelper.saveRequestCode(this, mdl.requestcode)
        ImageLoaderV4.getInstance().displayImage(this, mdl.QRCode, ivQRCode, R.color.color_f2)
    }

    /*获取分享信息*/
    private fun getShareInfo() {
        doRequest(WebApiService.SHARE_LIST, WebApiService.shareListParams(getUserId()), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, ShareMDL::class.java)
                    if (mdl == null) onJsonParseError()
                    else {
                        shareMDL = mdl
                        share(mdl)
                    }
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                endLoading()
            }
        })
    }

    private fun share(mdl: ShareMDL) {
        ShareDialog(this).shareListener(object : ShareDialog.OnShareListener {
            override fun share(type: Int, dialog: ShareDialog) {
                when (type) {
                    1 -> {
                        isDialogShare = false
                        shareToWeChat(mdl, SendMessageToWX.Req.WXSceneSession)
                    }
                    2 -> {
                        isDialogShare = false
                        shareToWeChat(mdl, SendMessageToWX.Req.WXSceneTimeline)
                    }
                    3 -> shareToQQ(mdl)
                }
                dialog.dismiss()
            }
        }).show()
    }

    /*分享到微信好友*/
    private fun shareToWeChat(mdl: ShareMDL, scene: Int) {
        Glide.with(this).asBitmap().load(mdl.icon).apply(RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        if (!isDialogShare) shareToWeChatByType(mdl.linkurl, mdl.title, mdl.desc, resource, scene)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        if (!isDialogShare) shareToWeChatByType(mdl.linkurl, mdl.title, mdl.desc, BitmapFactory.decodeResource(resources, R.mipmap.ic_logo), scene)
                    }
                })
    }

    /**
     * 微信分享（好友、朋友圈）
     * @param webPageUrl 点击跳转链接
     * @param title 标题
     * @param description   描述
     * @param thumbData    缩略图
     * @param scene  分享渠道
     */
    private fun shareToWeChatByType(webPageUrl: String?, title: String?, description: String?,
                                    thumbData: Bitmap, scene: Int) {
        mWXApi = WXAPIFactory.createWXAPI(this@MyInvitationCodeActivity, getString(R.string.WECHAT_APP_ID)).apply { registerApp(getString(R.string.WECHAT_APP_ID)) }
        mWXApi?.sendReq(SendMessageToWX.Req().apply {
            this.scene = scene
            this.transaction = "webPage${System.currentTimeMillis()}"
            this.message = WXMediaMessage(WXWebpageObject().apply { this.webpageUrl = webPageUrl }).apply {
                this.title = title
                this.description = description
                this.thumbData = Utils.bmpToByteArray(thumbData, true)
            }
        })
        isDialogShare = true
    }

    /*分享到QQ*/
    private fun shareToQQ(mdl: ShareMDL) {
        mTencent = Tencent.createInstance(getString(R.string.QQ_APP_ID), this)
        val bundle = Bundle()
        bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT)
        bundle.putString(QQShare.SHARE_TO_QQ_TITLE, mdl.title)
        bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, mdl.desc)
        bundle.putString(QQShare.SHARE_TO_QQ_TARGET_URL, mdl.linkurl)
        val imageUrl = mdl.icon
        if (imageUrl != null && imageUrl.startsWith("http")) {
            bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrl)
        } else {
            bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, resToSDCard())
        }
        bundle.putString(QQShare.SHARE_TO_QQ_APP_NAME, getString(R.string.app_name))
        mTencent?.shareToQQ(this, bundle, mUiListener)
    }

    /*复制logo图片到sd卡*/
    private fun resToSDCard(): String? {
        val path = "${filesDir.absolutePath}${File.separator}/logo"
        val fileName = "ic_logo.png"
        val filePath = "$path${File.separator}$fileName"
        val file = File(filePath)
        if (file.exists()) return file.absolutePath
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(filePath)
            val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_logo)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            return filePath
        } catch (e: Exception) {
        } finally {
            try {
                fos?.close()
            } catch (e: Exception) {
            }
        }
        return null
    }

    private val mUiListener = object : IUiListener {
        override fun onComplete(o: Any) {
            showShortToast("分享成功")
        }

        override fun onCancel() {
            showShortToast("分享取消")
        }

        override fun onError(uiError: UiError?) {
            uiError?.errorDetail?.let { showShortToast(it) }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Tencent.onActivityResultData(requestCode, resultCode, data, mUiListener)
        if (requestCode == Constants.REQUEST_QQ_SHARE) {
            Tencent.handleResultData(data, mUiListener)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_riders_share, menu)
        return true
    }

    override fun onDestroy() {
        mWXApi?.unregisterApp()
        super.onDestroy()
    }
}