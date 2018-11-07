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
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author MFB
 * @create 2018/11/1
 * @describe
 */
class CustomDatePickerDialog(private val context: Activity)
    : Dialog(context, R.style.supportDialog) {
    private val currYear: Int
    private val currMonth: Int
    private var maxYear: Int
    private var minYear: Int
    private var onDateSelectedListener: OnDateSelectedListener? = null

    init {
        val calendar = Calendar.getInstance()
        currYear = calendar.get(Calendar.YEAR)
        currMonth = calendar.get(Calendar.MONTH) + 1
        maxYear = currYear + 50  //最大年份（往后50年）
        minYear = currYear - 50 //最小年份（往前50年）
    }

    fun maxYear(maxYear: Int): CustomDatePickerDialog {
        this.maxYear = maxYear
        return this
    }

    fun minYear(minYear: Int): CustomDatePickerDialog {
        this.minYear = minYear
        return this
    }

    fun setOnDateSelectedListener(onDateSelectedListener: OnDateSelectedListener?): CustomDatePickerDialog {
        this.onDateSelectedListener = onDateSelectedListener
        return this
    }

    override fun show() {
        super.show()
        initView()
    }

    private fun initView() {
        window?.let { window ->
            val yearData = initYear()
            val monthData = initMonth()
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_datepicker, LinearLayout(context), false)
            val tvCancel = contentView.findViewById<TextView>(R.id.tvCancel)
            val tvConfirm = contentView.findViewById<TextView>(R.id.tvConfirm)
            val wvYear = contentView.findViewById<WheelView>(R.id.wvYear)
            val wvMonth = contentView.findViewById<WheelView>(R.id.wvMonth)
            tvCancel.setOnClickListener { dismiss() }
            wvYear.setData(yearData)
            val defaultYear = yearData.indexOf("${currYear}年")
            if (defaultYear in 0 until yearData.size) {
                wvYear.setDefault(defaultYear)
            }
            val defaultMonth = monthData.indexOf("${currMonth}月")
            if (defaultMonth in 0 until monthData.size) {
                wvMonth.setDefault(defaultMonth)
            }
            wvMonth.setData(monthData)
            tvConfirm.setOnClickListener {
                val selectYear = wvYear.selected
                val selectMonth = wvMonth.selected
                if (selectYear !in 0 until yearData.size || selectMonth !in 0 until monthData.size) {
                    return@setOnClickListener
                }
                val year = yearData[selectYear].substring(0, yearData[selectYear].length - 1)
                val month = monthData[selectMonth].substring(0, monthData[selectMonth].length - 1)
                onDateSelectedListener?.onDateSelected(year.toInt(), month.toInt(), this@CustomDatePickerDialog)
            }
            window.setContentView(contentView)
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setWindowAnimations(R.style.dialog_anim)
            window.setGravity(Gravity.BOTTOM)
        }
    }

    private fun initYear() = ArrayList<String>().apply {
        for (i in minYear..maxYear) {
            add("${i}年")
        }
    }

    private fun initMonth() = ArrayList<String>().apply {
        for (i in 1..12) {
            if (i < 10) add("0${i}月")
            else add("${i}月")
        }
    }

    interface OnDateSelectedListener {
        fun onDateSelected(year: Int, month: Int, dialog: CustomDatePickerDialog)
    }
}