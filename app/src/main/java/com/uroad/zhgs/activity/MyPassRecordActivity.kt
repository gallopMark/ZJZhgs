package com.uroad.zhgs.activity

import android.app.Activity
import android.app.AlertDialog
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.PopupWindowCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.uroad.library.utils.DisplayUtils
import com.uroad.library.utils.NetworkUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.adapteRv.PassRecordAdapter
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.dialog.CustomDatePickerDialog
import com.uroad.zhgs.dialog.MaterialDialog
import com.uroad.zhgs.model.CarMDL
import com.uroad.zhgs.model.PassRecordMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import com.uroad.zhgs.widget.CurrencyLoadView
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_passrecord.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author MFB
 * @create 2018/11/1
 * @describe 我的通行记录
 */
class MyPassRecordActivity : BaseActivity() {
    private var carno: String? = null
    private var startDate: String? = null
    private var endDate: String? = null
    private val point = "."
    private var type: String? = null
    private val cars = ArrayList<CarMDL>()
    private var selected: Int = 0
    private val mDatas = ArrayList<PassRecordMDL>()
    private lateinit var adapter: PassRecordAdapter

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_passrecord)
        withTitle(getString(R.string.mine_my_passRecord))
        initDate()
        initRv()
    }

    private fun initDate() {
        val calendar = Calendar.getInstance()
        initEndDate(calendar)
        initStartDate(calendar)
    }

    private fun initEndDate(calendar: Calendar) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val monthStr = if (month < 10) "0$month" else month.toString()
        endDate = "$year$monthStr"
        val currDate = "$year$point$monthStr"
        tvEndDate.text = currDate
    }

    private fun initStartDate(calendar: Calendar) {
        calendar.add(Calendar.MONTH, -2)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val monthStr = if (month < 10) "0$month" else month.toString()
        startDate = "$year$monthStr"
        val dDate = "$year$point$monthStr"
        tvStartDate.text = dDate
    }

    private fun onSelectDate(viewType: Int) {
        CustomDatePickerDialog(this@MyPassRecordActivity).setOnDateSelectedListener(object : CustomDatePickerDialog.OnDateSelectedListener {
            override fun onDateSelected(year: Int, month: Int, dialog: CustomDatePickerDialog) {
                val monthStr = if (month < 10) "0$month" else month.toString()
                val date = "$year$point$monthStr"
                if (viewType == 1) {
                    startDate = "$year$monthStr"
                    tvStartDate.text = date
                } else {
                    endDate = "$year$monthStr"
                    tvEndDate.text = date
                }
                dialog.dismiss()
            }
        }).show()
    }

//    private fun onSelectStartDate() {
//        CustomDatePickerDialog(this@MyPassRecordActivity).setOnDateSelectedListener(object : CustomDatePickerDialog.OnDateSelectedListener {
//            override fun onDateSelected(year: Int, month: Int, dialog: CustomDatePickerDialog) {
//                if (isDateOk(year, month)) {
//                    val endDate = tvEndDate.text.toString()
//                    if (!TextUtils.isEmpty(endDate)) {
//                        val endYear = (endDate.split(point)[0]).toInt()
//                        val endMonth = (endDate.split(point)[1]).toInt()
//                        if (year > endYear || (year == endYear && month > endMonth)) {
//                            showLongToast("开始日期不能大于结束日期")
//                        } else {
//                            val monthStr = if (month < 10) "0$month" else month.toString()
//                            startDate = "$year$monthStr"
//                            val dDate = "$year$point$monthStr"
//                            tvStartDate.text = dDate
//                            dialog.dismiss()
//                        }
//                    }
//                }
//            }
//        }).show()
//    }
//
//    private fun onSelectEndDate() {
//        CustomDatePickerDialog(this@MyPassRecordActivity).setOnDateSelectedListener(object : CustomDatePickerDialog.OnDateSelectedListener {
//            override fun onDateSelected(year: Int, month: Int, dialog: CustomDatePickerDialog) {
//                if (isDateOk(year, month)) {
//                    val startDate = tvStartDate.text.toString()
//                    if (!TextUtils.isEmpty(startDate)) {
//                        val startYear = (startDate.split(point)[0]).toInt()
//                        val startMonth = (startDate.split(point)[1]).toInt()
//                        if (year < startYear || (year == startYear && month < startMonth)) {
//                            showLongToast("结束日期必须大于开始日期")
//                        } else {
//                            val monthStr = if (month < 10) "0$month" else month.toString()
//                            endDate = "$year$monthStr"
//                            val dDate = "$year$point$monthStr"
//                            tvEndDate.text = dDate
//                            dialog.dismiss()
//                        }
//                    }
//                }
//            }
//        }).show()
//    }
//
//    /*判断选择的年月是否在当前年份内*/
//    private fun isDateOk(year: Int, month: Int): Boolean {
//        val calendar = Calendar.getInstance()
//        val currYear = calendar.get(Calendar.YEAR)
//        val currMonth = calendar.get(Calendar.MONTH) + 1
//        return if (year > currYear) {
//            showLongToast("年份无效，请重新选择")
//            false
//        } else {
//            if (month > currMonth) {
//                showLongToast("月份无效，请重新选择")
//                false
//            } else true
//        }
//    }

    private fun initRv() {
        recyclerView.layoutManager = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.VERTICAL }
        adapter = PassRecordAdapter(this, mDatas)
        recyclerView.adapter = adapter
    }

    override fun setListener() {
        flStartDate.setOnClickListener { onSelectDate(1) }
        flEndDate.setOnClickListener { onSelectDate(2) }
        tvSearch.setOnClickListener { getCurrentRecordData() }
    }

    override fun initData() {
        if (!isAuth()) {
            onUnauthorized()
        } else {
            getMyCars()
        }
    }

    /*未实名认证弹窗*/
    private fun onUnauthorized() {
        val dialog = MaterialDialog(this)
        dialog.setTitle(getString(R.string.dialog_default_title))
        dialog.setMessage(getString(R.string.empty_my_passRecord))
        dialog.hideDivider()
        dialog.setPositiveButton(getString(R.string.i_got_it), object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                dialog.dismiss()
            }
        })
        dialog.setOnDismissListener { finish() }
        dialog.show()
    }

    /*获取我的车辆*/
    private fun getMyCars() {
        doRequest(WebApiService.MYCAR, WebApiService.myCarParams(getUserId(), ""), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                setPageLoading()
            }

            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdLs = GsonUtils.fromDataToList(data, CarMDL::class.java)
                    if (mdLs.size > 0)
                        updateCars(mdLs)
                    else onEmptyCar()
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                setPageError()
            }
        })
    }

    private fun onEmptyCar() {
        val dialog = MaterialDialog(this)
        dialog.setTitle(getString(R.string.dialog_default_title))
        dialog.setMessage(getString(R.string.dialog_bindcar_content))
        dialog.hideDivider()
        dialog.setPositiveButton(getString(R.string.i_got_it), object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                dialog.dismiss()
            }
        })
        dialog.setOnDismissListener { finish() }
        dialog.show()
    }

    private fun updateCars(mdLs: MutableList<CarMDL>) {
        setPageEndLoading()
        cars.addAll(mdLs)
        tvBaseTitle.compoundDrawablePadding = DisplayUtils.dip2px(this, 5f)
        tvBaseTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(this, R.mipmap.ic_arrow_down_default), null)
        var hasDefault = false
        for (i in 0 until cars.size) {
            if (cars[i].isdefault == 1) {  //如果有默认车辆则显示默认车辆
                carno = cars[i].carno
                type = cars[i].carcategory
                hasDefault = true
                break
            }
        }
        if (!hasDefault) {  //如果没有默认车辆 则显示第一辆车
            carno = cars[0].carno
            type = cars[0].carcategory
        }
        withTitle(carno)
        tvBaseTitle.setOnClickListener { showPopTop() }
        getCurrentRecordData()
    }

    private fun showPopTop() {
        val recyclerView = RecyclerView(this).apply {
            setBackgroundColor(ContextCompat.getColor(this@MyPassRecordActivity, R.color.white))
            layoutManager = LinearLayoutManager(this@MyPassRecordActivity).apply { orientation = LinearLayoutManager.VERTICAL }
        }
        val popupWindow = PopupWindow(recyclerView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        recyclerView.adapter = CarAdapter(this, cars, selected).apply {
            setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
                override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                    if (position in 0 until cars.size) {
                        selected = position
                        carno = cars[position].carno
                        type = cars[position].carcategory
                        withTitle(carno)
                        getCurrentRecordData()
                        popupWindow.dismiss()
                    }
                }
            })
        }
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable())
        popupWindow.isOutsideTouchable = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val location = IntArray(2)
            baseLine.getLocationInWindow(location)
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) { // 7.1 版本处理
                val screenHeight = DisplayUtils.getWindowHeight(this@MyPassRecordActivity)
                popupWindow.height = screenHeight - location[1] - baseLine.height
            }
            popupWindow.showAtLocation(baseLine, Gravity.NO_GRAVITY, location[0], location[1] + baseLine.height)
        } else
            PopupWindowCompat.showAsDropDown(popupWindow, baseLine, 0, 0, Gravity.NO_GRAVITY)
    }

    //获取通行记录
    private fun getCurrentRecordData() {
        doRequest(WebApiService.PASS_RECORD, WebApiService.passRecordParams(carno, startDate, endDate, type), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                llContent.visibility = View.GONE
                loadView.setState(CurrencyLoadView.STATE_LOADING)
            }

            override fun onSuccess(data: String?) {
                loadView.setState(CurrencyLoadView.STATE_GONE)
                if (GsonUtils.isResultOk(data)) {
                    val mdLs = GsonUtils.fromDataToList(data, PassRecordMDL::class.java)
                    updateUI(mdLs)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                onError()
            }
        })
    }

    private fun updateUI(mdLs: MutableList<PassRecordMDL>) {
        if (mdLs.size > 0) {
            llContent.visibility = View.VISIBLE
            mDatas.clear()
            mDatas.addAll(mdLs)
            adapter.notifyDataSetChanged()
        } else {
            loadView.setState(CurrencyLoadView.STATE_EMPTY)
        }
    }

//    private fun updateMoney() {
//        var money = 0.00
//        for (item in mDatas) {
//            item.money?.let { money += it }
//        }
//        val bigDecimal = BigDecimal(money)
//        money = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toDouble()
//        val text = "本月消费 / 元\n"
//        val source = text + money
//        tvMoney.text = SpannableString(source).apply {
//            val ts26 = resources.getDimensionPixelOffset(R.dimen.font_26)
//            setSpan(AbsoluteSizeSpan(ts26, false), text.length, source.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
//        }
//    }

    private fun onError() {
        if (!NetworkUtils.isConnected(this)) loadView.setState(CurrencyLoadView.STATE_NO_NETWORK)
        else loadView.setState(CurrencyLoadView.STATE_ERROR)
        loadView.setOnRetryListener(object : CurrencyLoadView.OnRetryListener {
            override fun onRetry(view: View) {
                getCurrentRecordData()
            }
        })
    }

    private inner class CarAdapter(context: Activity
                                   , cars: MutableList<CarMDL>
                                   , private var selected: Int) : BaseArrayRecyclerAdapter<CarMDL>(context, cars) {
        override fun bindView(viewType: Int): Int = R.layout.item_select_default

        override fun onBindHoder(holder: RecyclerHolder, t: CarMDL, position: Int) {
            holder.setText(R.id.tvText, t.carno)
            if (position == selected) {
                holder.setVisibility(R.id.ivSelected, View.VISIBLE)
            } else {
                holder.setVisibility(R.id.ivSelected, View.GONE)
            }
        }
    }
}