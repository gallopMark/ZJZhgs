package com.uroad.zhgs.activity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import com.uroad.cameralibrary.listener.JCameraListener
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.common.CurrApplication
import kotlinx.android.synthetic.main.activity_camera.*
import android.view.WindowManager
import com.uroad.cameralibrary.JCameraView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


/**
 * @author MFB
 * @create 2018/10/23
 * @describe 仿微信拍照或短视频录制
 */
class CameraActivity : BaseActivity(), JCameraListener {
    override fun requestWindow() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_camera)
        jCameraView.setSaveVideoPath(CurrApplication.VIDEO_PATH)
        jCameraView.setMediaQuality(JCameraView.MEDIA_QUALITY_HIGH)
        jCameraView.setFeatures(JCameraView.BUTTON_STATE_ONLY_RECORDER)
        jCameraView.setDuration(CurrApplication.VIDEO_MAX_SEC * 1000)
        jCameraView.setJCameraLisenter(this)
        jCameraView.setLeftClickListener { onBackPressed() }
    }

    override fun captureSuccess(bitmap: Bitmap?) {

    }

    override fun recordSuccess(url: String?, firstFrame: Bitmap?) {
        val firstFrameFile = compressImage(firstFrame)
        if (url != null && firstFrame != null && firstFrameFile != null) {
            setResult(RESULT_OK, Intent().apply {
                putExtra("url", url)
                putExtra("firstFrame", firstFrameFile.absolutePath)
            })
            finish()
        }
    }

    override fun onResume() {
        jCameraView.onResume()
        super.onResume()
    }

    override fun onPause() {
        jCameraView.onPause()
        super.onPause()
    }

    private fun compressImage(bitmap: Bitmap?): File? {
        if (bitmap == null) return null
        val outStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        val format = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        val date = Date(System.currentTimeMillis())
        //图片名
        val filename = format.format(date)
        val file = File(CurrApplication.VIDEO_PATH, "$filename.png")
        val fos: FileOutputStream?
        try {
            fos = FileOutputStream(file)
            fos.write(outStream.toByteArray())
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            return null
        }
        return file
    }
}