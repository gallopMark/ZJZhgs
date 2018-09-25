package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.pl.wheelview.WheelView
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import java.util.ArrayList

/**
 *Created by MFB on 2018/8/29.
 */
class WheelViewDialog(private val context: Activity)
    : AlertDialog(context, R.style.supportDialog) {

    private var onItemSelectListener: OnItemSelectListener? = null
    private var data: ArrayList<String>? = null
    private var itemNumber: Int = 7
    private var defaultIndex: Int = -1
    private var selectIndex: Int = -1
    private var text: String = ""

    fun withData(data: ArrayList<String>): WheelViewDialog {
        this.data = data
        return this
    }

    fun withItemNum(itemNumber: Int): WheelViewDialog {
        this.itemNumber = itemNumber
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        data?.let { initView(it) }
    }

    private fun initView(data: ArrayList<String>) {
        window?.let { window ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_wheelview, LinearLayout(context), false)
            val tvCancel = contentView.findViewById<TextView>(R.id.tvCancel)
            val tvConfirm = contentView.findViewById<TextView>(R.id.tvConfirm)
            val wheelView = contentView.findViewById<WheelView>(R.id.wheelView)
            tvCancel.setOnClickListener { dismiss() }
            wheelView.itemNumber = itemNumber
            wheelView.setData(data)
            if (defaultIndex in 0 until data.size) wheelView.setDefault(defaultIndex)
            wheelView.setOnSelectListener(object : WheelView.OnSelectListener {
                override fun selecting(id: Int, text: String) {

                }

                override fun endSelect(id: Int, text: String) {
                    setSelect(id, text)
                }
            })
            tvConfirm.setOnClickListener { onItemSelectListener?.onItemSelect(selectIndex, text, this@WheelViewDialog) }
            window.setContentView(contentView)
            window.setLayout(DisplayUtils.getWindowWidth(context), WindowManager.LayoutParams.WRAP_CONTENT)
            window.setWindowAnimations(R.style.dialog_anim)
            window.setGravity(Gravity.BOTTOM)
        }
    }

    fun setSelect(position: Int, text: String) {
        selectIndex = position
        this.text = text
    }

    interface OnItemSelectListener {
        fun onItemSelect(position: Int, text: String, dialog: WheelViewDialog)
    }
}