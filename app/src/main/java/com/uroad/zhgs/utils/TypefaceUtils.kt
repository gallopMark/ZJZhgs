package com.uroad.zhgs.utils

import android.content.Context
import android.graphics.Typeface

/**
 * @author MFB
 * @create 2018/9/21
 * @describe textView字体类型
 */
class TypefaceUtils {
    companion object {
        fun dinCondensed(context: Context): Typeface {
            return Typeface.createFromAsset(context.assets, "DINCondensed.ttf")
        }
    }
}