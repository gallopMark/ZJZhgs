package com.uroad.share.wechat.model;

import android.graphics.Bitmap;

import com.uroad.share.wechat.WechatShareManager;


/**
 * Created by MFB on 2018/7/2.
 * 设置分享链接的内容
 */
public class WXShareWebpage extends WXShare {
    private String title, content, url;
    private Bitmap thumb;

    public WXShareWebpage(String title, String content, String url, Bitmap thumb) {
        this.title = title;
        this.content = content;
        this.url = url;
        this.thumb = thumb;
    }

    @Override
    public int getShareWay() {
        return WechatShareManager.WECHAT_SHARE_WAY_WEBPAGE;
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
        return url;
    }

    @Override
    public Bitmap thumb() {
        return thumb;
    }
}
