package com.uroad.zhgs.common

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.view.View
import com.uroad.zhgs.R
import com.uroad.zhgs.dialog.MaterialDialog

/**
 * @author MFB
 * @create 2018/11/22
 * @describe
 */
abstract class CameraFragment : BaseFragment() {

    private var onRequestCameraCallback: OnRequestCameraCallback? = null
    fun hasCamera(): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }

    fun requestCamera(onRequestCameraCallback: OnRequestCameraCallback?) {
        this.onRequestCameraCallback = onRequestCameraCallback
        if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            showTipsDialog(0)
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 456)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 456 && grantResults.isNotEmpty()) {
            var allGranted = true
            for (result in grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    allGranted = false
                    break
                }
            }
            if (allGranted) {
                onRequestCameraCallback?.onGranted()
            } else {
                var prohibit = false
                for (permission in permissions) {
                    if (shouldShowRequestPermissionRationale(permission)) {
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
        val dialog = MaterialDialog(context)
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
                    requestCamera(this@CameraFragment.onRequestCameraCallback)
                else {
                    openSettings()
                }
            }
        })
        dialog.setNegativeButton(getString(R.string.dialog_button_cancel), object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                dialog.dismiss()
            }
        })
        dialog.show()
    }

    interface OnRequestCameraCallback {
        fun onGranted()
    }
}