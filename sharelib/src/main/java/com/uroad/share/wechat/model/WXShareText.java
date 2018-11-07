package com.uroad.share.wechat.model;

import android.graphics.Bitmap;

import com.uroad.share.wechat.WechatShareManager;


/**
 * Created by MFB on 2018/7/2.
 * 设置分享文字的内容
 */
public class WXShareText extends WXShare {
    private String title;
    private String content;

    public WXShareText(String content) {
        this.content = content;
    }

    public WXShareText(String title, String content){
        this.title = title;
        this.content = content;
    }
    @Override
    public int getShareWay() {
        return WechatShareManager.WECHAT_SHARE_WAY_TEXT;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getURL() {
        return null;
    }

    @Override
    public Bitmap thumb() {
        return null;
    }
}
