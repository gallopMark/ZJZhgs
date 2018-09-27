package com.uroad.zhgs.activity

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.view.View
import com.alipay.sdk.app.PayTask
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.model.PayMDL
import com.uroad.zhgs.model.RescuePayMDL
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_rescue_pay.*

/**
 *Created by MFB on 2018/7/29.
 */
class RescuePayActivity : BaseActivity() {
    private var rescueid: String? = ""
    private var paymethod = "1030001"  //默认微信支付

    override fun setUp(savedInstanceState: Bundle?) {
        withTitle(resources.getString(R.string.rescue_pay_title))
        setBaseContentLayout(R.layout.activity_rescue_pay)
        intent.extras?.let { rescueid = it.getString("rescueid") }
        radioGroup.setOnCheckedChangeListener { _, checkId ->
            when (checkId) {
                R.id.rbWechat -> paymethod = "1030001"
                R.id.rbAlipay -> paymethod = "1030002"
            }
        }
        radioGroup.check(R.id.rbWechat)
        btPay.setOnClickListener { onPay() }
    }

    override fun onReload(view: View) {
        initData()
    }

    override fun initData() {
        doRequest(WebApiService.RESCUE_PAY_INFO, WebApiService.rescuePayParams(rescueid), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                setPageLoading()
            }

            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    setPageEndLoading()
                    val mdl = GsonUtils.fromDataBean(data, RescuePayMDL::class.java)
                    mdl?.let { updateData(it) }
                } else {
                    setPageError()
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                setPageError()
            }
        })
    }

    private fun updateData(payMDL: RescuePayMDL) {
        var rescueNo = resources.getString(R.string.rescue_detail_request_num)
        payMDL.rescueno?.let { rescueNo += it }
        tvRequestNum.text = rescueNo
        var rescueType = resources.getString(R.string.rescue_detail_rescue_type)
        payMDL.rescuetypename?.let { rescueType += it }
        tvRescueType.text = rescueType
        var rescueAddres = resources.getString(R.string.rescue_detail_rescue_address)
        payMDL.rescue_address?.let { rescueAddres += it }
        tvRescueAddress.text = rescueAddres
        var money = "¥"
        payMDL.paymoney?.let { money += it }
        val ts25 = resources.getDimensionPixelOffset(R.dimen.font_25)
        tvMoney.text = SpannableString(money).apply { setSpan(AbsoluteSizeSpan(ts25, false), 1, money.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE) }
        payMDL.paymoneydetail?.let {
            llDetail.visibility = View.VISIBLE
            tvDetailContent.text = it
            val down = ContextCompat.getDrawable(this@RescuePayActivity, R.mipmap.ic_arrow_down)
            down?.setBounds(0, 0, down.minimumWidth, down.minimumHeight)
            val up = ContextCompat.getDrawable(this@RescuePayActivity, R.mipmap.ic_arrow_up)
            up?.setBounds(0, 0, up.minimumWidth, up.minimumHeight)
            tvDetail.setOnClickListener { _ ->
                if (tvDetailContent.visibility == View.VISIBLE) {
                    tvDetail.setCompoundDrawables(null, null, down, null)
                    tvDetailContent.visibility = View.GONE
                } else {
                    tvDetail.setCompoundDrawables(null, null, up, null)
                    tvDetailContent.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun onPay() {
        placeAnOrder()
    }

    //下单支付
    private fun placeAnOrder() {
        doRequest(WebApiService.SIGN_PAY, WebApiService.signPayParams(rescueid, "1030002"), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, PayMDL::class.java)
                    if (mdl == null) onJsonParseError()
                    else pay(mdl)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                endLoading()
                onHttpError(e)
            }
        })
    }

    //支付
    private fun pay(mdl: PayMDL) {
        /*调起支付宝支付*/
        addDisposable(Flowable.fromCallable { PayTask(this@RescuePayActivity).pay(mdl.paystr, true) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ result ->
                    if (result == null || result.isEmpty()) {
                        showShortToast("支付失败")
                    } else {
                        val results = result.split(";")
                        if (results.isNotEmpty() && results[0].contains("9000")) {
                            openActivity(RescuePaySuccessActivity::class.java, Bundle().apply { putString("rescueid", rescueid) })
                            setResult(RESULT_OK)
                            finish()
                        } else if (results.isNotEmpty() && results[0].contains("6001")) {
                            showShortToast("支付已取消")
                        } else {
                            showShortToast("支付失败")
                        }
                    }
                }, { onHttpError(it) }))
    }
}