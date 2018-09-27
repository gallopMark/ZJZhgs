package com.uroad.zhgs.activity

import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.View
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.adapteRv.RescueEvaluateAdapter
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.model.EvaluateMDL
import com.uroad.zhgs.model.RescueDetailMDL
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import com.uroad.zhgs.widget.GridSpacingItemDecoration
import com.uroad.zhgs.widget.StarBar
import kotlinx.android.synthetic.main.activity_rescue_evaluate.*

/**
 *Created by MFB on 2018/7/29.
 * 救援评价
 */
class RescueEvaluateActivity : BaseActivity() {

    private var rescueid: String = "1"
    private var evaluate: String? = ""
    private var evaluatetag: String? = ""

    override fun setUp(savedInstanceState: Bundle?) {
        withTitle(resources.getString(R.string.rescue_evaluate_title))
        setBaseContentLayout(R.layout.activity_rescue_evaluate)
        intent.extras?.getString("rescueid")?.let { rescueid = it }
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.addItemDecoration(GridSpacingItemDecoration(2, DisplayUtils.dip2px(this, 10f), false))
        recyclerView.layoutManager = GridLayoutManager(this, 2).apply { orientation = GridLayoutManager.VERTICAL }
    }

    override fun initData() {
        getDetail()
        getEvaluate()
    }

    private fun getDetail() {
        doRequest(WebApiService.RESCUE_DETAIL, WebApiService.rescueDetailParams(rescueid), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, RescueDetailMDL::class.java)
                    mdl?.detail?.let { update(it) }
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                Handler().postDelayed({ if (!isFinishing) getDetail() }, 3000)
            }
        })
    }

    private fun update(mdl: RescueDetailMDL.Detail) {
        var person = resources.getString(R.string.rescue_detail_request_num)
        mdl.rescueno?.let { person += it }
        tvRescuePerson.text = SpannableString(person).apply { setSpan(ForegroundColorSpan(ContextCompat.getColor(this@RescueEvaluateActivity, R.color.appTextColor)), person.indexOf("：") + 1, person.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE) }
        var rescueCarNum = resources.getString(R.string.rescue_detail_rescue_carNum)
        mdl.dispatchvehicles?.let { rescueCarNum += it }
        tvRescueCarNum.text = SpannableString(rescueCarNum).apply { setSpan(ForegroundColorSpan(ContextCompat.getColor(this@RescueEvaluateActivity, R.color.appTextColor)), rescueCarNum.indexOf("："), rescueCarNum.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE) }
        var rescueUnit = resources.getString(R.string.rescue_detail_rescue_unit)
        mdl.helpteam?.let { rescueUnit += it }
        tvRescueUnit.text = SpannableString(rescueUnit).apply { setSpan(ForegroundColorSpan(ContextCompat.getColor(this@RescueEvaluateActivity, R.color.appTextColor)), rescueUnit.indexOf("："), rescueUnit.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE) }
    }

    private fun getEvaluate() {
        doRequest(WebApiService.EVALUATE_TEXT, WebApiService.evaluateTextParams("104"),
                object : HttpRequestCallback<String>() {
                    override fun onPreExecute() {
                        setPageLoading()
                    }

                    override fun onSuccess(data: String?) {
                        if (GsonUtils.isResultOk(data)) {
                            setPageEndLoading()
                            val mdl = GsonUtils.fromDataBean(data, EvaluateMDL::class.java)
                            if (mdl == null)
                                showShortToast("数据解析异常")
                            else
                                updateData(mdl)
                        } else {
                            showShortToast(GsonUtils.getMsg(data))
                        }
                    }

                    override fun onFailure(e: Throwable, errorMsg: String?) {
                        setPageError()
                    }
                })
    }

    private fun updateData(mdl: EvaluateMDL) {
        val mDatas = ArrayList<EvaluateMDL.Type>()
        mdl.type?.let { mDatas.addAll(it) }
        if (mDatas.size == 0) return
        starBar.setStarCount(mDatas.size)
        val default = mDatas.size - 1
        starBar.setOnStarChangeListener(object : StarBar.OnStarChangeListener {
            override fun onStarChange(mark: Float) {
                val pos = (mark - 1).toInt()
                if (pos in 0 until mDatas.size) {
                    tvTips.text = mDatas[pos].dictname
                    mDatas[pos].dictcode?.let { evaluate = it }
                    mDatas[pos].sontype?.let { drawSonType(it) }
                } else {
                    evaluate = null
                    tvTips.text = mDatas[0].dictname
                    mDatas[0].dictcode?.let { evaluate = it }
                }
            }
        })
        starBar.starMark = 100f
        mDatas[default].dictcode?.let { evaluate = it }
        tvTips.text = mDatas[default].dictname
        mDatas[default].sontype?.let { drawSonType(it) }
    }

    private fun drawSonType(mDatas: MutableList<EvaluateMDL.Type.SonType>) {
        evaluatetag = null
        recyclerView.adapter = RescueEvaluateAdapter(this, mDatas).apply {
            setOnSelectListener(object : RescueEvaluateAdapter.OnSelectListener {
                override fun onSelected(evaluatetag: String) {
                    this@RescueEvaluateActivity.evaluatetag = evaluatetag
                }
            })
        }
    }

    override fun setListener() {
        btSubmit.setOnClickListener {
            when {
                TextUtils.isEmpty(rescueid) -> showShortToast("救援id不存在")
                TextUtils.isEmpty(evaluate) -> showShortToast("请选择评价等级")
                TextUtils.isEmpty(evaluatetag) -> showShortToast("请选择评价标签")
                TextUtils.isEmpty(etContent.text.toString().trim()) -> showShortToast("请填写评价内容")
                else -> commit()
            }
        }
    }

    private fun commit() {
        val evaluateother = etContent.text.toString()
        doRequest(WebApiService.COMMIT_EVALUATE, WebApiService.commitEvaluateParams(rescueid, evaluate, evaluatetag, evaluateother)
                , object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    showShortToast("评价已提交")
                    setResult(RESULT_OK)
                    Handler().postDelayed({
                        if (!isFinishing) {
                            finish()
                        }
                    }, 1500)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                onHttpError(e)
            }
        })
    }

    override fun onReload(view: View) {
        getEvaluate()
    }
}