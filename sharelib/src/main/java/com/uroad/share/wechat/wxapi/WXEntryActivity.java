//package com.uroad.share.wechat.wxapi;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.os.Bundle;
//import android.widget.Toast;
//
//import com.tencent.mm.opensdk.constants.ConstantsAPI;
//import com.tencent.mm.opensdk.modelbase.BaseReq;
//import com.tencent.mm.opensdk.modelbase.BaseResp;
//import com.tencent.mm.opensdk.openapi.IWXAPI;
//import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
//import com.tencent.mm.opensdk.openapi.WXAPIFactory;
//import com.uroad.share.R;
//
//public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
//
//    /*与微信通信的openapi接口*/
//    public IWXAPI wxApi;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        String APP_ID = getResources().getString(R.string.wechat_appkey);
//        wxApi = WXAPIFactory.createWXAPI(this, APP_ID);
//        wxApi.handleIntent(getIntent(), this);
//    }
//
//    // 如果分享的时候，该已经开启，那么微信开始这个activity时，会调用onNewIntent，所以这里要处理微信的返回结果
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        setIntent(intent);
//        wxApi.handleIntent(getIntent(), this);
//    }
//
//    @Override
//    public void onReq(BaseReq req) {
//
//    }
//
//    @Override
//    public void onResp(BaseResp resp) {
//        if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
//            String result;
//            switch (resp.errCode) {
//                case BaseResp.ErrCode.ERR_OK:
//                    result = getResources().getString(R.string.errcode_auth_success);
//                    break;
//                case BaseResp.ErrCode.ERR_USER_CANCEL:
//                    result = getResources().getString(R.string.errcode_auth_cancel);
//                    break;
//                case BaseResp.ErrCode.ERR_AUTH_DENIED:
//                    result = getResources().getString(R.string.errcode_auth_deny);
//                    break;
//                case BaseResp.ErrCode.ERR_UNSUPPORT:
//                    result = getResources().getString(R.string.errcode_auth_unsupported);
//                    break;
//                default:
//                    result = getResources().getString(R.string.errcode_auth_unknown);
//                    break;
//            }
//            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
//        } else {
//            String result;
//            switch (resp.errCode) {
//                case BaseResp.ErrCode.ERR_OK:
//                    result = getResources().getString(R.string.errcode_success);
//                    break;
//                case BaseResp.ErrCode.ERR_USER_CANCEL:
//                    result = getResources().getString(R.string.errcode_cancel);
//                    break;
//                case BaseResp.ErrCode.ERR_AUTH_DENIED:
//                    result = getResources().getString(R.string.errcode_deny);
//                    break;
//                case BaseResp.ErrCode.ERR_UNSUPPORT:
//                    result = getResources().getString(R.string.errcode_unsupported);
//                    break;
//                default:
//                    result = getResources().getString(R.string.errcode_unknown);
//                    break;
//            }
//            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
//            finish();
//        }
//    }
//}
