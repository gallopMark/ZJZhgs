package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.helper.AppLocalHelper

class AuthenticationDialog(private val context: Activity) : Dialog(context) {
    private var onViewClickListener: OnViewClickListener? = null

    fun onViewClickListener(onViewClickListener: OnViewClickListener?): AuthenticationDialog {
        this.onViewClickListener = onViewClickListener
        return this
    }

    override fun show() {
        super.show()
        initView()
        setOnDismissListener { AppLocalHelper.saveAuth(context) }
    }

    private fun initView() {
        window?.let { window ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_authentication, LinearLayout(context), false)
            window.setContentView(contentView)
            val ivLicensePlate = contentView.findViewById<ImageView>(R.id.ivLicensePlate)
            val ivRealName = contentView.findViewById<ImageView>(R.id.ivRealName)
            val ivBottom = contentView.findViewById<ImageView>(R.id.ivBottom)
            ivLicensePlate.setOnClickListener { onViewClickListener?.onViewClick(1, this@AuthenticationDialog) }
            ivRealName.setOnClickListener { onViewClickListener?.onViewClick(2, this@AuthenticationDialog) }
            ivBottom.setOnClickListener { onViewClickListener?.onViewClick(3, this@AuthenticationDialog) }
            window.setBackgroundDrawableResource(R.drawable.bg_corners_white_10dp)
            window.setLayout((DisplayUtils.getWindowWidth(context) / 5 * 4), WindowManager.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER)
        }
    }

    interface OnViewClickListener {
        fun onViewClick(type: Int, dialog: AuthenticationDialog)
    }
}