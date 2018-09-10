package com.uroad.zhgs.activity

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.text.TextUtils
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.model.RescueFeeMDL
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_rescue_fee.*
import com.uroad.zhgs.adapteRv.RFCarCategoryAdapter
import com.uroad.zhgs.adapteRv.RFCarTypeAdapter
import com.uroad.zhgs.adapteRv.RFPicTextAdapter
import com.uroad.zhgs.model.RescueFeeContentMDL
import com.uroad.zhgs.widget.GridSpacingItemDecoration


/**
 *Created by MFB on 2018/8/2.
 * 救援资费
 */
class RescueFeeActivity : BaseActivity() {

    private val types = ArrayList<RescueFeeMDL.Type>()
    private lateinit var typeAdapter: RFCarCategoryAdapter
    private val sonTypes = ArrayList<RescueFeeMDL.Type.SonType>()
    private lateinit var sonTypeAdapter: RFCarTypeAdapter
    private val workTypes = ArrayList<RescueFeeMDL.WorkType>()
    private lateinit var picAdapter: RFPicTextAdapter
    /**
     * type	服务类型	否
    cartype	车辆类别	否
    carweight	车辆类型	否
     */
    private var workTypeCode = ""
    private var cartypeCode = ""
    private var carweightCode = ""

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_rescue_fee)
        withTitle(resources.getString(R.string.rescue_fee_title))
        rvCarCategory.isNestedScrollingEnabled = false
        rvCarType.isNestedScrollingEnabled = false
        rvPics.isNestedScrollingEnabled = false
        initRv()
    }

    private fun initRv() {
        val itemDecoration1 = GridSpacingItemDecoration(2, DisplayUtils.dip2px(this, 10f), true)
        rvCarCategory.addItemDecoration(itemDecoration1)
        val gridManager1 = GridLayoutManager(this, 2).apply { orientation = GridLayoutManager.VERTICAL }
        rvCarCategory.layoutManager = gridManager1
        val itemDecoration2 = GridSpacingItemDecoration(4, DisplayUtils.dip2px(this, 10f), true)
        rvCarType.addItemDecoration(itemDecoration2)
        val gridManager2 = GridLayoutManager(this, 4).apply { orientation = GridLayoutManager.VERTICAL }
        rvCarType.layoutManager = gridManager2
        val itemDecoration3 = GridSpacingItemDecoration(4, DisplayUtils.dip2px(this, 10f), true)
        rvPics.addItemDecoration(itemDecoration3)
        val gridManager3 = GridLayoutManager(this, 4).apply { orientation = GridLayoutManager.VERTICAL }
        rvPics.layoutManager = gridManager3
        typeAdapter = RFCarCategoryAdapter(this, types)
        rvCarCategory.adapter = typeAdapter
        sonTypeAdapter = RFCarTypeAdapter(this, sonTypes)
        rvCarType.adapter = sonTypeAdapter
        picAdapter = RFPicTextAdapter(this, workTypes)
        rvPics.adapter = picAdapter
    }

    override fun initData() {
        doRequest(WebApiService.ACCESS_RESCUE_CHARGES, HashMap(), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                setPageLoading()
            }

            override fun onSuccess(data: String?) {
                setPageEndLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, RescueFeeMDL::class.java)
                    if (mdl == null) setPageNoData()
                    else updateData(mdl)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                setPageError()
                onHttpError(e)
            }
        })
    }

    private fun updateData(mdl: RescueFeeMDL) {
        mdl.type?.let { typeList ->
            types.addAll(typeList)
            typeAdapter.notifyDataSetChanged()
            if (types.size > 0) {
                types[0].dictcode?.let { cartypeCode = it }
                types[0].sontype?.let {
                    sonTypes.addAll(it)
                    sonTypeAdapter.notifyDataSetChanged()
                }
            }
        }
        mdl.worktype?.let {
            workTypes.addAll(it)
            picAdapter.notifyDataSetChanged()
        }
        tvRemark.text = mdl.chargebasis
        typeAdapter.setOnItemSelectedListener(object : RFCarCategoryAdapter.OnItemSelectedListener {
            override fun onItemSelected(position: Int) {
                types[position].dictcode?.let { cartypeCode = it }
                sonTypes.clear()
                types[position].sontype?.let { sonTypes.addAll(it) }
                sonTypeAdapter.setSelectIndex(-1)
                carweightCode = ""
                clearText()
            }
        })
        sonTypeAdapter.setOnItemSelectedListener(object : RFCarTypeAdapter.OnItemSelectedListener {
            override fun onItemSelected(position: Int) {
                if (position in 0 until sonTypes.size) {
                    sonTypes[position].dictcode?.let { carweightCode = it }
                    getFeeContent()
                }
            }
        })
        picAdapter.setOnItemSelectedListener(object : RFPicTextAdapter.OnItemSelectedListener {
            override fun onItemSelected(position: Int) {
                workTypes[position].dictcode?.let { workTypeCode = it }
                getFeeContent()
            }
        })
    }

    private fun clearText() {
        tvMoney.text = ""
        tvNotice.text = ""
    }

    //获取资费内容
    private fun getFeeContent() {
        if (TextUtils.isEmpty(workTypeCode) || TextUtils.isEmpty(cartypeCode)
                || TextUtils.isEmpty(carweightCode)) return
        doRequest(WebApiService.GET_FEE_CONTENT, WebApiService.feeContentParams(workTypeCode, cartypeCode, carweightCode), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                clearText()
            }

            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, RescueFeeContentMDL::class.java)
                    tvMoney.text = mdl?.money
                    tvNotice.text = mdl?.notice
                } else {
                    tvMoney.text = GsonUtils.getMsg(data)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {

            }
        })
    }
}