package com.uroad.zhgs.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.uroad.library.widget.CompatToast
import com.uroad.zhgs.R


/**
 * @author MFB
 * @create 2018/11/6
 * @describe 微信分享回调
 */
class WXEntryActivity : Activity(), IWXAPIEventHandler {
    /*与微信通信的openapi接口*/
    private lateinit var wxApi: IWXAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wxApi = WXAPIFactory.createWXAPI(this, getString(R.string.WECHAT_APP_ID))
        wxApi.handleIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        wxApi.handleIntent(getIntent(), this)
    }

    override fun onResp(resp: BaseResp) {
        val result = when (resp.errCode) {
            BaseResp.ErrCode.ERR_OK -> getString(R.string.errcode_success)
            BaseResp.ErrCode.ERR_USER_CANCEL -> getString(R.string.errcode_cancel)
            BaseResp.ErrCode.ERR_AUTH_DENIED -> getString(R.string.errcode_deny)
            else -> getString(R.string.errcode_unknown)
        }
        showShortToast(result)
        finish()
    }

    private fun showShortToast(text: CharSequence?) {
        if (TextUtils.isEmpty(text)) return
        val v = LayoutInflater.from(this).inflate(R.layout.layout_base_toast, LinearLayout(this), false)
        val textView = v.findViewById<TextView>(R.id.tv_text)
        textView.text = text
        CompatToast(this, R.style.CompatToast).apply {
            duration = Toast.LENGTH_SHORT
            view = v
            show()
        }
    }

    override fun onReq(req: BaseReq) {
    }
}