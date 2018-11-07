package com.uroad.share.wechat.model;

import android.graphics.Bitmap;

import com.uroad.share.wechat.WechatShareManager;


/**
 * Created by MFB on 2018/7/2.
 * 设置分享图片的内容
 */
public class WXSharePicture extends WXShare {
    private String path;
    private Bitmap bitmap;

    public WXSharePicture(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public WXSharePicture(String path) {
        this.path = path;
    }

    @Override
    public int getShareWay() {
        return WechatShareManager.WECHAT_SHARE_WAY_PICTURE;
    }

    @Override
    public String getContent() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getURL() {
        return null;
    }

    @Override
    public Bitmap thumb() {
        return bitmap;
    }

    @Override
    public String getPath() {
        return path;
    }
}
