package com.uroad.share.wechat;

/*
 * Created by MFB on 2018/7/2.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXMusicObject;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXVideoObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.uroad.share.R;
import com.uroad.share.wechat.model.WXShare;

/**
 * 实现微信分享功能的核心类
 *
 * @author chengcj1
 */
public class WechatShareManager {

    private static final int THUMB_SIZE = 150;

    public static final int WECHAT_SHARE_WAY_TEXT = 1;   //文字
    public static final int WECHAT_SHARE_WAY_PICTURE = 2; //图片
    public static final int WECHAT_SHARE_WAY_WEBPAGE = 3;  //链接
    public static final int WECHAT_SHARE_WAY_VIDEO = 4; //视频
    public static final int WECHAT_SHARE_WAY_MUSIC = 5; //音乐
    public static final int WECHAT_SHARE_TYPE_TALK = SendMessageToWX.Req.WXSceneSession;  //会话
    public static final int WECHAT_SHARE_TYPE_FRENDS = SendMessageToWX.Req.WXSceneTimeline; //朋友圈
    public static final int WECHAT_SHARE_TYPE_FAVORITE = SendMessageToWX.Req.WXSceneFavorite; //微信收藏
    private int mTargetScene = WECHAT_SHARE_TYPE_TALK;
    private IWXAPI mWXApi;
    private Context mContext;

    private WechatShareManager(Context context, String appID) {
        this.mContext = context;
        //初始化数据
        //初始化微信分享代码
        initWechatShare(context, appID);
    }

    /**
     * 获取WeixinShareManager实例
     * 非线程安全，请在UI线程中操作
     */
    public static WechatShareManager from(Context context, String appID) {
        return new WechatShareManager(context, appID);
    }

    public WechatShareManager scene(int scene) {
        this.mTargetScene = scene;
        return this;
    }

    private void initWechatShare(Context context, String appID) {
        if (mWXApi == null) {
            mWXApi = WXAPIFactory.createWXAPI(context, appID, true);
        }
        mWXApi.registerApp(appID);
    }

    /**
     * 通过微信分享
     */
    public void shareByWebchat(WXShare WXShare) {
        switch (WXShare.getShareWay()) {
            case WECHAT_SHARE_WAY_TEXT:
                shareText(WXShare);
                break;
            case WECHAT_SHARE_WAY_PICTURE:
                sharePicture(WXShare);
                break;
            case WECHAT_SHARE_WAY_WEBPAGE:
                shareWebPage(WXShare);
                break;
            case WECHAT_SHARE_WAY_VIDEO:
                shareVideo(WXShare);
                break;
            case WECHAT_SHARE_WAY_MUSIC:
                shareMusic(WXShare);
                break;
        }
    }

    /*
     * 分享文字
     */
    private void shareText(WXShare WXShare) {
        String text = WXShare.getContent();
        //初始化一个WXTextObject对象
        WXTextObject textObj = new WXTextObject();
        textObj.text = text;
        //用WXTextObject对象初始化一个WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        msg.description = text;
        //构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        //transaction字段用于唯一标识一个请求
        req.transaction = buildTransaction("text");
        req.message = msg;
        //发送的目标场景， 可以选择发送到会话 WXSceneSession 或者朋友圈 WXSceneTimeline。 默认发送到会话。
        req.scene = mTargetScene;
        mWXApi.sendReq(req);
    }

    /*
     * 分享图片
     */
    private void sharePicture(WXShare WXShare) {
        WXImageObject imgObj = new WXImageObject();
        Bitmap thumb = null;
        if (!TextUtils.isEmpty(WXShare.getPath())) {
            imgObj.imagePath = WXShare.getPath();
            thumb = Utils.adjustImage(mContext, WXShare.getPath());
        } else if (WXShare.thumb() != null) {
            imgObj = new WXImageObject(WXShare.thumb());
            thumb = WXShare.thumb();
        }
        if (thumb == null) return;
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;
        Bitmap thumbBitmap = Bitmap.createScaledBitmap(thumb, THUMB_SIZE, THUMB_SIZE, true);
        thumb.recycle();
        msg.thumbData = Utils.bmpToByteArray(thumbBitmap, true);  //设置缩略图
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("img");
        req.message = msg;
        req.scene = mTargetScene;
        mWXApi.sendReq(req);
    }

    /*
     * 分享链接
     */
    private void shareWebPage(WXShare WXShare) {
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = WXShare.getURL();
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = WXShare.getTitle();
        msg.description = WXShare.getContent();
        Bitmap thumb = WXShare.thumb();
        if (thumb == null) {
            return;
        } else {
            msg.thumbData = Utils.bmpToByteArray(thumb, true);
        }
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = mTargetScene;
        mWXApi.sendReq(req);
    }

    /*
     * 分享视频
     */
    private void shareVideo(WXShare WXShare) {
        WXVideoObject video = new WXVideoObject();
        video.videoUrl = WXShare.getURL();
        WXMediaMessage msg = new WXMediaMessage(video);
        msg.title = WXShare.getTitle();
        msg.description = WXShare.getContent();
        Bitmap thumb;
        if (WXShare.thumb() != null)
            thumb = WXShare.thumb();
        else
            thumb = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_video);
//		BitmapFactory.decodeStream(new URL(video.videoUrl).openStream());
        /*
         * 测试过程中会出现这种情况，会有个别手机会出现调不起微信客户端的情况。
         * 造成这种情况的原因是微信对缩略图的大小、title、description等参数的大小做了限制，
         * 所以有可能是大小超过了默认的范围。
         * 一般情况下缩略图超出比较常见。Title、description都是文本，一般不会超过。
         */
        Bitmap thumbBitmap = Bitmap.createScaledBitmap(thumb, THUMB_SIZE, THUMB_SIZE, true);
        thumb.recycle();
        msg.thumbData = Utils.bmpToByteArray(thumbBitmap, true);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("video");
        req.message = msg;
        req.scene = mTargetScene;
        mWXApi.sendReq(req);
    }

    private void shareMusic(WXShare WXShare) {
        WXMusicObject music = new WXMusicObject();
        music.musicUrl = WXShare.getURL();
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = music;
        msg.description = WXShare.getURL();
        if (!TextUtils.isEmpty(WXShare.getTitle()))
            msg.title = WXShare.getTitle();
        if (!TextUtils.isEmpty(WXShare.getContent()))
            msg.description = WXShare.getContent();
        Bitmap thumb;
        if (WXShare.thumb() != null)
            thumb = WXShare.thumb();
        else
            thumb = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_music);
        Bitmap thumbBmp = Bitmap.createScaledBitmap(thumb, THUMB_SIZE, THUMB_SIZE, true);
        thumb.recycle();
        msg.thumbData = Utils.bmpToByteArray(thumbBmp, true);
        //构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("music");
        req.message = msg;
        req.scene = mTargetScene;
        //调用接口发送数据到微信
        mWXApi.sendReq(req);
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    public void unregisterApp() {
        if (mWXApi != null) mWXApi.unregisterApp();
    }
}
