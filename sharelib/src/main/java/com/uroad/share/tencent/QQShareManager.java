package com.uroad.share.tencent;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzonePublish;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;

import java.util.ArrayList;
import java.util.List;

/*qq分享工具类*/
public class QQShareManager {
    private Activity context;
    private Tencent mTencent;

    private QQShareManager(Activity context, String appID) {
        this.context = context;
        if (mTencent == null)
            mTencent = Tencent.createInstance(appID, context.getApplicationContext());
    }

    public static QQShareManager from(Activity context, String appID) {
        return new QQShareManager(context, appID);
    }

//    /*授权登录*/
//    public void doLogin(String scope, IUiListener listener) {
//        mTencent.login(context, scope, listener);
//    }

    /**
     * QQ纯文字分享(QQsdk不支持纯文字分享功能，这里做特殊处理)
     **/
    public static void shareTextToQQ(Context context, String text) {
        //"com.tencent.mobileqq", "com.tencent.mobileqqi", "com.tencent.qqlite", "com.tencent.minihd.qq", "com.tencent.tim"
        String pkg = QQClientPackageName(context);
        if (!TextUtils.isEmpty(pkg)) {
            try {
                Intent intent = new Intent();
                ComponentName componentName = new ComponentName(pkg, pkg + ".activity.JumpActivity");
                intent.setComponent(componentName);
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain"); // 纯文本
                intent.putExtra(Intent.EXTRA_TEXT, text);
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "未安装QQ，或QQ版本过低！", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "未安装QQ，或QQ版本过低！", Toast.LENGTH_SHORT).show();
        }
    }

    private static String QQClientPackageName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List list = packageManager.getInstalledPackages(0);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                String packageName = ((PackageInfo) list.get(i)).packageName;
                if (packageName.equals("com.tencent.mobileqq")
                        || packageName.equals("com.tencent.mobileqqi")
                        || packageName.equals("com.tencent.qqlite")
                        || packageName.equals("com.tencent.minihd.qq")
                        || packageName.equals("com.tencent.tim")) {
                    return packageName;
                }
            }
        }
        return null;
    }

    /*分享图文消息*/
    public void shareToQQ(Param param, IUiListener listener) {
        if (TextUtils.isEmpty(param.TITLE) && TextUtils.isEmpty(param.IMAGE_URL) && TextUtils.isEmpty(param.SUMMARY)) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        bundle.putString(QQShare.SHARE_TO_QQ_TITLE, param.TITLE);
        bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, param.SUMMARY);
        bundle.putString(QQShare.SHARE_TO_QQ_TARGET_URL, param.TARGET_URL);
        bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, param.IMAGE_URL);
        bundle.putString(QQShare.SHARE_TO_QQ_APP_NAME, param.APPNAME);
        mTencent.shareToQQ(context, bundle, listener);
    }

    /*分享本地图片*/
    public void shareLocalImageToQQ(String appName, String imagePath, IUiListener listener) {
        Bundle bundle = new Bundle();
        bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, imagePath);
        bundle.putString(QQShare.SHARE_TO_QQ_APP_NAME, appName);
        bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
        bundle.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE);
        mTencent.shareToQQ(context, bundle, listener);
    }

    /*分享网络图片*/
    public void shareImageUrlToQQ(String appName, String imageUrl, IUiListener listener) {
        Bundle bundle = new Bundle();
        bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
        bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrl);
        bundle.putString(QQShare.SHARE_TO_QQ_APP_NAME, appName);
        bundle.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE);
        mTencent.shareToQQ(context, bundle, listener);
    }

    /*分享音乐*/
    public void shareMusicToQQ(Param param, IUiListener listener) {
        final Bundle bundle = new Bundle();
        bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_AUDIO);
        bundle.putString(QQShare.SHARE_TO_QQ_TITLE, param.TITLE);
        bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, param.SUMMARY);
        bundle.putString(QQShare.SHARE_TO_QQ_TARGET_URL, param.TARGET_URL);
        bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, param.IMAGE_URL);
        bundle.putString(QQShare.SHARE_TO_QQ_AUDIO_URL, param.AUDIO_URL);
        bundle.putString(QQShare.SHARE_TO_QQ_APP_NAME, param.APPNAME);
        bundle.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE);
        mTencent.shareToQQ(context, bundle, listener);
    }

    /*分享应用*/
    public void shareAppToQQ(Param param, IUiListener listener) {
        Bundle bundle = new Bundle();
        bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_APP);
        bundle.putString(QQShare.SHARE_TO_QQ_TITLE, param.TITLE);
        bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, param.SUMMARY);
        bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, param.IMAGE_URL);
        bundle.putString(QQShare.SHARE_TO_QQ_APP_NAME, param.APPNAME);
        bundle.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE);
        mTencent.shareToQQ(context, bundle, listener);
    }

    /*分享到QQ空间
     * 完善了分享到QZone功能，分享类型参数Tencent.SHARE_TO_QQ_KEY_TYPE，目前只支持图文分享
     * */
    public void shareToQzone(Param param, IUiListener listener) {
        //分享类型
        Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, param.TITLE);//必填
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, param.SUMMARY);//选填
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, param.TARGET_URL);//必填
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, param.IMAGE_URLS);
        mTencent.shareToQzone(context, params, listener);
    }

    /**
     * 发表说说、视频或上传图片
     */
    public void publishToQzone(Param param, IUiListener listener) {
        Bundle params = new Bundle();
        if (!TextUtils.isEmpty(param.VIDEO_PATH)) {
            params.putInt(QzonePublish.PUBLISH_TO_QZONE_KEY_TYPE, QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHVIDEO);
            /*
             * 发表的视频，只支持本地地址，发表视频时必填；
             * 上传视频的大小最好控制在100M以内（因为QQ普通用户上传视频必须在100M以内，
             * 黄钻用户可上传1G以内视频，大于1G会直接报错。）
             */
            params.putString(QzonePublish.PUBLISH_TO_QZONE_VIDEO_PATH, param.VIDEO_PATH);
        } else {
            params.putInt(QzonePublish.PUBLISH_TO_QZONE_KEY_TYPE, QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD);
        }
        //说说正文（传图和传视频接口会过滤第三方传过来的自带描述，目的为了鼓励用户自行输入有价值信息）
        params.putString(QzonePublish.PUBLISH_TO_QZONE_SUMMARY, param.SUMMARY);
        //说说的图片, 以ArrayList<String>的类型传入，以便支持多张图片
        // （注：<=9张图片为发表说说，>9张为上传图片到相册），只支持本地图片
        params.putStringArrayList(QzonePublish.PUBLISH_TO_QZONE_IMAGE_URL, param.IMAGE_URLS);
        // params.putStringArrayList(QzonePublish.PUBLISH_TO_QZONE_IMAGE_URL, param.IMAGE_URLS);
        //   params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, param.IMAGE_URLS);
        //       params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, param.IMAGE_URLS);
        //       params.putString(QzonePublish.PUBLISH_TO_QZONE_VIDEO_PATH, param.VIDEO_PATH);
        mTencent.publishToQzone(context, params, listener);
    }

    public static class Param {
        public String TARGET_URL;  //这条分享消息被好友点击后的跳转URL。
        public String TITLE;  //分享的标题。注：PARAM_TITLE、PARAM_IMAGE_URL、PARAM_SUMMARY不能全为空，最少必须有一个是有值的。
        public String LOCAL_URL;  //本地图片路径
        public String IMAGE_URL; //分享的图片URL
        public String SUMMARY; //分享的消息摘要，最长50个字
        public String APPNAME;  //手Q客户端顶部，替换“返回”按钮文字，如果为空，用返回代替
        public String AUDIO_URL;  //音乐链接
        public String VIDEO_PATH; //本地视频路径
        public ArrayList<String> IMAGE_URLS;   //分享QQ空间图片集合
    }
}
