package com.uroad.zhgs.common

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.view.View
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.uroad.zhgs.R
import com.uroad.zhgs.dialog.MaterialDialog

abstract class BaseLocationFragment : BaseFragment(), AMapLocationListener {

    companion object {
        private const val CODE_PERMISSION = 10000
    }

    private var permissionCallback: RequestLocationPermissionCallback? = null
    private var mLocationClient: AMapLocationClient? = null
    open fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun requestLocationPermissions(permissionCallback: RequestLocationPermissionCallback) {
        this.permissionCallback = permissionCallback
        requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION), CODE_PERMISSION)
    }

    fun applyLocationPermission(finishAfterDenied: Boolean) {
        requestLocationPermissions(object : RequestLocationPermissionCallback {
            override fun doAfterGrand() {
                openLocation()
            }

            override fun doAfterDenied() {
                var isOpen = false
                val dialog = MaterialDialog(context)
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
                dialog.setOnDismissListener { if (!isOpen && finishAfterDenied) context.finish() }
            }
        })
    }

    open fun openLocation() {
        openLocation(null)
    }

    open fun openLocation(option: AMapLocationClientOption?) {
        if (mLocationClient == null) {
            val mOption: AMapLocationClientOption
            if (option == null) {
                mOption = AMapLocationClientOption().apply { locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy }
            } else {
                option.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                mOption = option
            }
            mLocationClient = AMapLocationClient(context).apply {
                setLocationOption(mOption)
                setLocationListener(this@BaseLocationFragment)
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
            locationFailure()
        }
    }

    open fun afterLocation(location: AMapLocation) {

    }

    open fun locationFailure() {

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
                            showDismissLocationDialog()
                            break
                        }
                    }
                }
            }
        }
    }

    open fun isShouldShowRequestPermissionRationale(permission: String): Boolean = shouldShowRequestPermissionRationale(permission)

    open fun showDismissLocationDialog() {
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

    override fun onDestroyView() {
        closeLocation()
        super.onDestroyView()
    }

    interface RequestLocationPermissionCallback {
        fun doAfterGrand()

        fun doAfterDenied()
    }
}