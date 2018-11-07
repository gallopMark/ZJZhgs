package com.uroad.zhgs.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import com.uroad.cameralibrary.listener.JCameraListener
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.common.CurrApplication
import kotlinx.android.synthetic.main.activity_camera.*
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.WindowManager
import com.uroad.cameralibrary.JCameraView
import com.uroad.zhgs.dialog.MaterialDialog
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
    private var granted = false
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
        requestPermissions()
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

    private fun requestPermissions() {
        granted = if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                showTipsDialog(0)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 1)
            }
            false
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1 && grantResults.isNotEmpty()) {
            granted = true
            for (result in grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    granted = false
                    break
                }
            }
            if (granted) jCameraView.onResume()
            else {
                var prohibit = false
                for (permission in permissions) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        prohibit = true
                        break
                    }
                }
                if (prohibit) {
                    showTipsDialog(0)
                } else {
                    showTipsDialog(1)
                }
            }
        }
    }

    private fun showTipsDialog(type: Int) {
        val dialog = MaterialDialog(this)
        dialog.setTitle(getString(R.string.dialog_default_title))
        if (type == 1)
            dialog.setMessage("相机权限已被禁止，请重新打开")
        else
            dialog.setMessage("相机权限已被禁止，请到设置——权限管理中开启“相机”")
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setPositiveButton(getString(R.string.reopen), object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                dialog.dismiss()
                if (type == 1)
                    requestPermissions()
                else {
                    openSettings()
                }
            }
        })
        dialog.setNegativeButton(getString(R.string.dialog_button_cancel), object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                dialog.dismiss()
                finish()
            }
        })
        dialog.show()
    }

    override fun onResume() {
        if (granted) jCameraView.onResume()
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