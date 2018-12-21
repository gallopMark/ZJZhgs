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
import kotlin.collections.ArrayList

/**
 * @author MFB
 * @create 2018/11/1
 * @describe 我的通行记录
 */
class MyPassRecordActivity : BaseActivity() {
    private var carno: String? = null
    private var startDate: String? = null
    private var type: String? = null
    private val cars = ArrayList<CarMDL>()
    private var selected: Int = 0
    private val mDatas = ArrayList<PassRecordMDL>()
    private lateinit var adapter: PassRecordAdapter

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_passrecord)
        withTitle(getString(R.string.mine_my_passRecord))
//        initDate()
        initRv()
    }

//    private fun initDate() {
//        val calendar = Calendar.getInstance()
//        val currYear = calendar.get(Calendar.YEAR)
//        val currMonth = calendar.get(Calendar.MONTH) + 1
//        val monthStr = if (currMonth < 10) "0$currMonth" else currMonth.toString()
//        val currDate = "$currYear.$monthStr"
//        startDate = "$currYear$monthStr"
//        tvCurrentDate.text = currDate
//        flDate.setOnClickListener {
//            CustomDatePickerDialog(this@MyPassRecordActivity).setOnDateSelectedListener(object : CustomDatePickerDialog.OnDateSelectedListener {
//                override fun onDateSelected(year: Int, month: Int, dialog: CustomDatePickerDialog) {
//                    val ms = if (month < 10) "0$month" else month.toString()
//                    startDate = "$year$ms"
//                    val date = "$year.$ms"
//                    tvCurrentDate.text = date
//                    dialog.dismiss()
//                    if (TextUtils.isEmpty(carno) || TextUtils.isEmpty(type)) return
//                    getCurrentRecordData()
//                }
//            }).show()
//        }
//    }

    private fun initRv() {
        recyclerView.layoutManager = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.VERTICAL }
        adapter = PassRecordAdapter(this, mDatas)
        recyclerView.adapter = adapter
    }

    override fun initData() {
        if (!isAuth()) {
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
        } else {
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
    }

    private fun onEmptyCar() {
        val dialog = MaterialDialog(this)
        dialog.setTitle(getString(R.string.dialog_default_title))
        dialog.setMessage(getString(R.string.empty_cars_inPassRecord))
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
        doRequest(WebApiService.PASS_RECORD, WebApiService.passRecordParams(carno, startDate, type), object : HttpRequestCallback<String>() {
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