package com.uroad.zhgs.photopicker.ui

import android.app.Activity
import android.os.Bundle
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.TextView
import com.uroad.zhgs.photopicker.widget.CropImageView
import kotlinx.android.synthetic.main.activity_photopicker_imagecrop.*
import java.io.File
import android.content.Intent


/**
 *Created by MFB on 2018/8/2.
 */
class ImageCropActivity : BaseActivity(), CropImageView.OnBitmapSaveCompleteListener {

    private var mBitmap: Bitmap? = null
    private var isSaveRectangle: Boolean = false
    private var mOutputX: Int = 0
    private var mOutputY: Int = 0

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_photopicker_imagecrop)
        withOption("移动和缩放")
        withOption("完成")
        val imagePath = intent.getStringExtra("imagePath")
        //获取需要的参数
        mOutputX = intent.extras.getInt("outPutX")
        mOutputY = intent.extras.getInt("outPutY")
        isSaveRectangle = intent.extras.getBoolean("isSaveRectangle")
        val focusWidth = intent.extras.getInt("focusWidth")
        val focusHeight = intent.extras.getInt("focusHeight")
        if (isSaveRectangle) cropImageView.focusStyle = CropImageView.Style.RECTANGLE
        else cropImageView.focusStyle = CropImageView.Style.CIRCLE
        cropImageView.focusWidth = focusWidth
        cropImageView.focusHeight = focusHeight
        //缩放图片
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imagePath, options)
        val displayMetrics = resources.displayMetrics
        options.inSampleSize = calculateInSampleSize(options, displayMetrics.widthPixels, displayMetrics.heightPixels)
        options.inJustDecodeBounds = false
        mBitmap = BitmapFactory.decodeFile(imagePath, options)
        cropImageView.setImageBitmap(mBitmap)
        cropImageView.setOnBitmapSaveCompleteListener(this)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val width = options.outWidth
        val height = options.outHeight
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = width / reqWidth
            } else {
                inSampleSize = height / reqHeight
            }
        }
        return inSampleSize
    }

    override fun onOptionClickListener(tvBaseOption: TextView) {
        val cropFile = File(externalCacheDir, "/crop/")
        cropImageView.saveBitmapToFile(cropFile, mOutputX, mOutputY, isSaveRectangle)
    }

    override fun onSavingBitmap() {
        showLoading("正在保存…")
    }

    override fun onBitmapSaveSuccess(file: File) {
        //裁剪后替换掉返回数据的内容，但是不要改变全局中的选中数据
        endLoading()
        val intent = Intent()
        intent.putExtra("crop_image", file.absolutePath)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onBitmapSaveError(file: File) {
        endLoading()
        showShortToast("图片保存失败")
    }
}