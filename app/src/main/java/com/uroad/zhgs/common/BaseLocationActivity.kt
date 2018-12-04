package com.uroad.zhgs.common

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.uroad.zhgs.R
import com.uroad.zhgs.dialog.MaterialDialog

/**
 * @author MFB
 * @create 2018/12/4
 * @describe 定位基础
 */
abstract class BaseLocationActivity : BaseActivity(), AMapLocationListener {
    private var mLocationClient: AMapLocationClient? = null
    private var permissionCallback: RequestLocationPermissionCallback? = null

    companion object {
        private const val CODE_PERMISSION = 999
    }

    fun openLocation() {
        openLocation(null)
    }

    fun openLocation(option: AMapLocationClientOption?) {
        if (mLocationClient == null) {
            val mOption: AMapLocationClientOption
            if (option == null) {
                mOption = AMapLocationClientOption().apply {
                    locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                }
            } else {
                option.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                mOption = option
            }
            mLocationClient = AMapLocationClient(this).apply {
                setLocationOption(mOption)
                setLocationListener(this@BaseLocationActivity)
                startLocation()
            }
        } else {
            mLocationClient?.startLocation()
        }
    }

    override fun onLocationChanged(location: AMapLocation?) {
        if (location != null && location.errorCode == AMapLocation.LOCATION_SUCCESS) {
            afterLocation(location)
        } else {
            onLocationFail(location?.errorInfo)
        }
    }

    open fun afterLocation(location: AMapLocation) {

    }

    open fun onLocationFail(errorInfo: String?) {

    }

    open fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun requestLocationPermissions(permissionCallback: RequestLocationPermissionCallback) {
        this.permissionCallback = permissionCallback
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), CODE_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CODE_PERMISSION -> {
                var allGranted = true
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false
                        break
                    }
                }
                if (allGranted) {
                    permissionCallback?.doAfterGrand()
                } else {
                    permissionCallback?.doAfterDenied()
                    for (permission in permissions) {
                        //可以推断出用户选择了“不在提示”选项，在这种情况下需要引导用户至设置页手动授权
                        if (!isShouldShowRequestPermissionRationale(permission)) {
                            showProhibitLocationDialog()
                            break
                        }
                    }
                }
            }
        }
    }

    open fun isShouldShowRequestPermissionRationale(permission: String): Boolean = ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

    open fun applyLocationPermission(finishAfterDenied: Boolean) {
        requestLocationPermissions(object : RequestLocationPermissionCallback {
            override fun doAfterGrand() {
                openLocation()
            }

            override fun doAfterDenied() {
                var isOpen = false
                val dialog = MaterialDialog(this@BaseLocationActivity)
                dialog.setTitle(getString(R.string.dialog_default_title))
                dialog.setMessage(getString(R.string.dismiss_location_message))
                dialog.setNegativeButton(getString(R.string.dialog_button_cancel), object : MaterialDialog.ButtonClickListener {
                    override fun onClick(v: View, dialog: AlertDialog) {
                        dialog.dismiss()
                    }
                })
                dialog.setPositiveButton(getString(R.string.reopen), object : MaterialDialog.ButtonClickListener {
                    override fun onClick(v: View, dialog: AlertDialog) {
                        isOpen = true
                        dialog.dismiss()
                        applyLocationPermission(finishAfterDenied)
                    }
                })
                dialog.show()
                dialog.setOnDismissListener { if (!isOpen && finishAfterDenied) finish() }
            }
        })
    }

    /*用户选择了禁止不再提示 则显示此对话框，引导用户到app应用设置页面打开*/
    open fun showProhibitLocationDialog() {
        showDialog(getString(R.string.rescue_main_without_location_title), getString(R.string.rescue_main_location_ban),
                getString(R.string.dialog_button_cancel), getString(R.string.gotoSettings)
                , object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                dialog.dismiss()
            }
        }, object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                dialog.dismiss()
                openSettings() //引导用户至设置页手动授权
            }
        })
    }

    open fun closeLocation() {
        mLocationClient?.let {
            it.stopLocation()
            it.onDestroy()
            mLocationClient = null
        }
    }

    override fun onDestroy() {
        closeLocation()
        super.onDestroy()
    }

    interface RequestLocationPermissionCallback {
        fun doAfterGrand()

        fun doAfterDenied()
    }
}