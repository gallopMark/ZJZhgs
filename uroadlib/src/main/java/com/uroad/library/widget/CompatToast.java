package com.uroad.library.widget;

import android.content.Context;
import android.view.WindowManager;
import android.widget.Toast;

import java.lang.reflect.Field;

/**
 * 描述:自定义Toast动画适配各个版本的手机
 */
public class CompatToast extends Toast {
    private int mAnim = 0; //配置默认动画

    public CompatToast(Context context) {
        super(context);
        initTN();
    }

    public CompatToast(Context context, int AnimStyle) {
        super(context);
        setmAnim(AnimStyle);
        initTN();
    }

    private void initTN() {
        Class<Toast> clazz = Toast.class;
        int anim = getAnim();
        try {
            Field mTN = clazz.getDeclaredField("mTN");
            mTN.setAccessible(true);
            Object mObj = mTN.get(this);
            Field field = mObj.getClass().getDeclaredField("mParams");
            if (field != null) {
                field.setAccessible(true);
                Object mParams = field.get(mObj);
                if (mParams != null && mParams instanceof WindowManager.LayoutParams) {
                    WindowManager.LayoutParams params = (WindowManager.LayoutParams) mParams;
                    params.windowAnimations = anim;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getAnim() {
        return mAnim;
    }

    public void setmAnim(int mAnim) {
        this.mAnim = mAnim;
    }
}
