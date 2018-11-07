//package com.uroad.share.wechat;
//
//import android.content.Context;
//
//import com.tencent.mm.opensdk.modelpay.PayReq;
//import com.tencent.mm.opensdk.openapi.IWXAPI;
//import com.tencent.mm.opensdk.openapi.WXAPIFactory;
//import com.uroad.share.R;
//
///*微信支付工具类*/
//public class WeChatPayLib {
//    private IWXAPI wxApi;
//
//    private WeChatPayLib(Context context) {
//        if (wxApi == null) {
//            String APP_ID = context.getResources().getString(R.string.wechat_appkey);
//            wxApi = WXAPIFactory.createWXAPI(context, APP_ID, true);
//            wxApi.registerApp(APP_ID);  //将应用的APP_ID注册到微信
//        }
//    }
//
//    public static WeChatPayLib from(Context context) {
//        return new WeChatPayLib(context);
//    }
//
//    /*判断手机客户端时候安装微信*/
//    public boolean isWXAppInstalled() {
//        return wxApi.isWXAppInstalled();
//    }
//
//    public void pay(Param param) {
//        PayReq req = new PayReq();
//        req.appId = param.appId;
//        req.partnerId = param.partnerId;
//        req.prepayId = param.prepayId;
//        req.nonceStr = param.nonceStr;
//        req.timeStamp = param.timeStamp;
//        req.packageValue = param.packageValue;
//        req.sign = param.sign;
//        req.extData = param.extData;
//        req.signType = param.signType;
//        wxApi.sendReq(req);
//    }
//
//    public static class Param {
//        public String appId;
//        public String partnerId;
//        public String prepayId;
//        public String nonceStr;
//        public String timeStamp;
//        public String packageValue;
//        public String sign;
//        public String extData;
//        public String signType;
//    }
//}
