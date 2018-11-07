package com.uroad.share.wechat.model;

import android.graphics.Bitmap;

/**
 * Created by MFB on 2018/7/2.
 */
public abstract class WXShare {
    public abstract int getShareWay();

    public abstract String getContent();

    public abstract String getTitle();

    public abstract String getURL();

    public abstract Bitmap thumb();

    public String getPath() {
        return null;
    }
}
