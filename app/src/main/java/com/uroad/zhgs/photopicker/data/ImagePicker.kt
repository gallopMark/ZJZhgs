package com.uroad.zhgs.photopicker.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.Image
import android.os.Bundle
import com.amap.api.col.sln3.it
import com.uroad.zhgs.photopicker.ui.ImageGridActivity

/**
 *Created by MFB on 2018/7/30.
 */
class ImagePicker private constructor(private val context: Activity) {
    private var mMutilyMode = false //选择模式（单选或多选）
    private var limit = 1   //最大图片选择数量
    private var crop = false    //是否需要裁剪
    private var isSaveRectangle = true  //裁剪后的图片是否是矩形，否者跟随裁剪框的形状
    private var outPutX = 800           //裁剪保存宽度
    private var outPutY = 800           //裁剪保存高度
    private var focusWidth = 280         //焦点框的宽度
    private var focusHeight = 280        //焦点框的高度
    private var requestCode: Int = 1

    companion object {
        const val EXTRA_PATHS = "extra_paths"
        fun from(context: Activity): ImagePicker {
            return ImagePicker(context)
        }
    }

    fun isMutilyChoice(limit: Int): ImagePicker {
        this.mMutilyMode = true
        if (limit <= 1) this.limit = 1
        else this.limit = limit
        return this
    }

    fun rectangleCrop(): ImagePicker {
        this.crop = true
        this.isSaveRectangle = true
        return this
    }

    fun circleCrop(): ImagePicker {
        this.crop = true
        this.isSaveRectangle = false
        return this
    }

    fun outPutX(outPutX: Int): ImagePicker {
        this.outPutX = outPutX
        return this
    }

    fun outPutY(outPutY: Int): ImagePicker {
        this.outPutY = outPutY
        return this
    }

    fun focusWidth(focusWidth: Int): ImagePicker {
        this.focusWidth = focusWidth
        return this
    }

    fun focusHeight(focusHeight: Int): ImagePicker {
        this.focusHeight = focusHeight
        return this
    }

    fun requestCode(requestCode: Int): ImagePicker {
        this.requestCode = requestCode
        return this
    }

    fun start() {
        val intent = Intent(context, ImageGridActivity::class.java)
        val bundle = Bundle().apply {
            putBoolean("mMutilyMode", mMutilyMode)
            putInt("limit", limit)
            putBoolean("isCrop", crop)
            putBoolean("isSaveRectangle", isSaveRectangle)
            putInt("outPutX", outPutX)
            putInt("outPutY", outPutY)
            putInt("focusWidth", focusWidth)
            putInt("focusHeight", focusHeight)
        }
        intent.putExtras(bundle)
        context.startActivityForResult(intent, requestCode)
    }
}