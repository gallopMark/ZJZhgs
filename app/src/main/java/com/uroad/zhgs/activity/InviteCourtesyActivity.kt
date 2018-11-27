package com.uroad.zhgs.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.widget.TextView
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
import com.uroad.share.wechat.Utils
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.dialog.ShareDialog
import com.uroad.zhgs.model.ActivityMDL
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_invitecourtesy.*
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

/**
 * @author MFB
 * @create 2018/11/23
 * @describe 邀请有礼
 */
class InviteCourtesyActivity : BaseActivity() {
    private var activityId: String? = null
    private var activityMDL: ActivityMDL? = null
    private var mWXApi: IWXAPI? = null
    private var mTencent: Tencent? = null
    private var isDialogShare = false

    override fun setUp(savedInstanceState: Bundle?) {
        hideBaseLine(true)
        setBaseContentLayout(R.layout.activity_invitecourtesy)
        withTitle(getString(R.string.invitecourtesy_title))
        tvInviteCode.text = getRequestCode()
        activityId = intent.extras?.getString("activityId")
        tvShare.setOnClickListener { activityMDL?.let { mdl -> onShare(mdl) } }
        llBottom.setOnClickListener { openActivity(MyHarvestActivity::class.java, Bundle().apply { putString("activityId", activityId) }) }
    }

    override fun initData() {
        doRequest(WebApiService.ACTIVITY_DETAIL, WebApiService.activityDetailParams(activityId, getUserUUID()), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                setPageLoading()
            }

            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, ActivityMDL::class.java)
                    if (mdl == null) onJsonParseError()
                    else updateUI(mdl)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                    setPageError()
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                setPageError()
            }
        })
    }

    private fun updateUI(mdl: ActivityMDL) {
        setPageEndLoading()
        activityMDL = mdl
        tvTitle.text = mdl.title
        tvSubTitle.text = mdl.subtitle
        ImageLoaderV4.getInstance().displayImage(this, mdl.activityimg, ivActivityImg)
        var content = ""
        mdl.totalnum?.let { content += it }
        content += "位新用户已成功邀请"
        tvInviteNum.text = SpannableString(content).apply { setSpan(ForegroundColorSpan(ContextCompat.getColor(this@InviteCourtesyActivity, R.color.colorAccent)), 0, content.indexOf("位"), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE) }
        if (!TextUtils.isEmpty(mdl.activityrule)) withOption(getString(R.string.invitecourtesy_activity_rule))
    }

    private fun onShare(mdl: ActivityMDL) {
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
    private fun shareToWeChat(mdl: ActivityMDL, scene: Int) {
        Glide.with(this).asBitmap().load(mdl.shareicon).apply(RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        if (!isDialogShare) shareToWeChatByType(mdl.shareurl, mdl.sharetitle, mdl.sharedesc, resource, scene)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        if (!isDialogShare) shareToWeChatByType(mdl.shareurl, mdl.sharetitle, mdl.sharedesc, BitmapFactory.decodeResource(resources, R.mipmap.ic_logo), scene)
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
        mWXApi = WXAPIFactory.createWXAPI(this, getString(R.string.WECHAT_APP_ID)).apply { registerApp(getString(R.string.WECHAT_APP_ID)) }
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
    private fun shareToQQ(mdl: ActivityMDL) {
        mTencent = Tencent.createInstance(getString(R.string.QQ_APP_ID), this)
        val bundle = Bundle()
        bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT)
        bundle.putString(QQShare.SHARE_TO_QQ_TITLE, mdl.sharetitle)
        bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, mdl.sharedesc)
        bundle.putString(QQShare.SHARE_TO_QQ_TARGET_URL, mdl.shareurl)
        val imageUrl = mdl.shareicon
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

    override fun onOptionClickListener(tvBaseOption: TextView) {
        openActivity(ActivityRuleActivity::class.java, Bundle().apply { putString("rule", activityMDL?.activityrule) })
    }

    override fun onDestroy() {
        mWXApi?.unregisterApp()
        super.onDestroy()
    }
}