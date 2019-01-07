package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.pl.wheelview.WheelView
import com.uroad.zhgs.R
import java.util.ArrayList

/**
 *Created by MFB on 2018/8/29.
 */
class WheelViewDialog(private val context: Activity) : Dialog(context, R.style.supportDialog) {

    private var onItemSelectListener: OnItemSelectListener? = null
    private var data: ArrayList<String?>? = null
    private var defaultIndex: Int = -1
    private var selected: Int = 0

    fun withData(data: ArrayList<String?>): WheelViewDialog {
        this.data = data
        return this
    }

    fun default(defaultIndex: Int): WheelViewDialog {
        this.defaultIndex = defaultIndex
        return this
    }

    fun withListener(onItemSelectListener: OnItemSelectListener): WheelViewDialog {
        this.onItemSelectListener = onItemSelectListener
        return this
    }

    override fun show() {
        super.show()
        data?.let { initView(it) }
    }

    private fun initView(data: ArrayList<String?>) {
        window?.let { window ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_wheelview, LinearLayout(context), false)
            val tvCancel = contentView.findViewById<TextView>(R.id.tvCancel)
            val tvConfirm = contentView.findViewById<TextView>(R.id.tvConfirm)
            val wheelView = contentView.findViewById<WheelView>(R.id.wheelView)
            tvCancel.setOnClickListener { dismiss() }
            wheelView.setData(data)
            if (defaultIndex in 0 until data.size) {
                selected = defaultIndex
                wheelView.setDefault(defaultIndex)
            }
            wheelView.setOnSelectListener(object : WheelView.OnSelectListener {
                override fun endSelect(id: Int, text: String?) {
                    setSelected(id)
                }

                override fun selecting(id: Int, text: String?) {

                }
            })
            tvConfirm.setOnClickListener {
                if (selected in 0 until data.size) onItemSelectListener?.onItemSelect(selected, data[selected], this@WheelViewDialog)
            }
            window.setContentView(contentView)
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setWindowAnimations(R.style.dialog_anim)
            window.setGravity(Gravity.BOTTOM)
        }
    }

    private fun setSelected(position: Int) {
        selected = position
    }

    interface OnItemSelectListener {
        fun onItemSelect(position: Int, text: String?, dialog: WheelViewDialog)
    }
}