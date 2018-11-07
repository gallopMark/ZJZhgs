package com.uroad.share.wechat.model;

import android.graphics.Bitmap;

import com.uroad.share.wechat.WechatShareManager;


/**
 * Created by MFB on 2018/7/2.
 * 设置分享视频的内容
 */
public class WXShareVideo extends WXShare {
    private String title, content, url;
    private Bitmap thumb;

    public WXShareVideo(String url, String title, String content, Bitmap thumb) {
        this.url = url;
        this.title = title;
        this.content = content;
        this.thumb = thumb;
    }

    @Override
    public int getShareWay() {
        return WechatShareManager.WECHAT_SHARE_WAY_VIDEO;
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
