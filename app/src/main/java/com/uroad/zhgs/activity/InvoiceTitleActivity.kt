package com.uroad.zhgs.activity

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.GridLayoutManager
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.enumeration.InvoiceType
import com.uroad.zhgs.model.InvoiceTypeMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import com.uroad.zhgs.widget.GridSpacingItemDecoration
import kotlinx.android.synthetic.main.activity_invoice_title.*

/**
 *Created by MFB on 2018/7/29.
 */
class InvoiceTitleActivity : BaseActivity() {
    private class InvoiceTypeAdapter(context: Context, mDatas: MutableList<InvoiceTypeMDL.Type>)
        : BaseArrayRecyclerAdapter<InvoiceTypeMDL.Type>(context, mDatas) {
        private var onItemSelectedListener: OnItemSelectedListener? = null
        private var selectIndex = 0

        override fun onBindHoder(holder: RecyclerHolder, t: InvoiceTypeMDL.Type, position: Int) {
            val tv = holder.obtainView<TextView>(R.id.tv)
            tv.text = t.dictname
            tv.isSelected = selectIndex == position
            holder.itemView.setOnClickListener { setSelectIndex(position) }
        }

        private fun setSelectIndex(position: Int) {
            onItemSelectedListener?.onItemSelected(position)
            selectIndex = position
            notifyDataSetChanged()
        }

        override fun bindView(viewType: Int): Int {
            return R.layout.item_invoice_type
        }

        interface OnItemSelectedListener {
            fun onItemSelected(position: Int)
        }

        fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener) {
            this.onItemSelectedListener = onItemSelectedListener
        }
    }

    private class InvoiceTitleAdapter(context: Context, mDatas: MutableList<InvoiceTypeMDL.Type.SonType>)
        : BaseArrayRecyclerAdapter<InvoiceTypeMDL.Type.SonType>(context, mDatas) {
        private var onItemSelectedListener: OnItemSelectedListener? = null
        private var selectIndex = 0

        override fun onBindHoder(holder: RecyclerHolder, t: InvoiceTypeMDL.Type.SonType, position: Int) {
            val tv = holder.obtainView<TextView>(R.id.tv)
            tv.text = t.dictname
            tv.isSelected = selectIndex == position
            holder.itemView.setOnClickListener { setSelectIndex(position) }
        }

        private fun setSelectIndex(position: Int) {
            onItemSelectedListener?.onItemSelected(position)
            selectIndex = position
            notifyDataSetChanged()
        }

        override fun bindView(viewType: Int): Int {
            return R.layout.item_invoice_type
        }

        interface OnItemSelectedListener {
            fun onItemSelected(position: Int)
        }

        fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener) {
            this.onItemSelectedListener = onItemSelectedListener
        }
    }

    private val typeList = ArrayList<InvoiceTypeMDL.Type>()
    private lateinit var typeAdapter: InvoiceTypeAdapter
    private val titleList = ArrayList<InvoiceTypeMDL.Type.SonType>()
    private lateinit var titleAdapter: InvoiceTitleAdapter
    private var rescueId: String? = ""
    private var fpType: String = ""   //发票类型
    private var headtype: String = "" //抬头类型
    private var isSpecial = false //是否是专票
    private var isCompany = false //是否是企业抬头类型

    override fun setUp(savedInstanceState: Bundle?) {
        withTitle(resources.getString(R.string.invoice_title))
        setBaseContentLayout(R.layout.activity_invoice_title)
        intent.extras?.let { rescueId = it.getString("rescueid") }
        initRv()
        initRvData()
        btSubmit.setOnClickListener { onSubmit() }
    }

    private fun initRv() {
        rvInvoiceType.isNestedScrollingEnabled = false
        rvRiseType.isNestedScrollingEnabled = false
        rvInvoiceType.addItemDecoration(GridSpacingItemDecoration(2, DisplayUtils.dip2px(this, 10f), false))
        rvInvoiceType.layoutManager = GridLayoutManager(this, 2).apply { orientation = GridLayoutManager.VERTICAL }
        rvRiseType.addItemDecoration(GridSpacingItemDecoration(2, DisplayUtils.dip2px(this, 10f), false))
        rvRiseType.layoutManager = GridLayoutManager(this, 2).apply { orientation = GridLayoutManager.VERTICAL }
        typeAdapter = InvoiceTypeAdapter(this, typeList)
        rvInvoiceType.adapter = typeAdapter
        titleAdapter = InvoiceTitleAdapter(this, titleList)
        rvRiseType.adapter = titleAdapter
        typeAdapter.setOnItemSelectedListener(object : InvoiceTypeAdapter.OnItemSelectedListener {
            override fun onItemSelected(position: Int) {
                typeList[position].dictcode?.let { fpType = it }
                titleList.clear()
                typeList[position].sontype?.let { titleList.addAll(it) }
                titleAdapter.notifyDataSetChanged()
                llHeadType.visibility = if (titleList.size == 0) View.GONE else View.VISIBLE
                if (position == 0) {
                    hideStars()
                } else {
                    showStars()
                }
            }
        })
        titleAdapter.setOnItemSelectedListener(object : InvoiceTitleAdapter.OnItemSelectedListener {
            override fun onItemSelected(position: Int) {
                titleList[position].dictcode?.let { headtype = it }
                if (position == 0) {   //抬头类型为：企业
                    llContent.visibility = View.VISIBLE
                    isCompany = true
                } else { //抬头类型为：个人
                    llContent.visibility = View.GONE
                    isCompany = false
                }
            }
        })
    }

    //普通发票（单位地址、电话号码、开户银行、银行账号非必填）
    private fun hideStars() {
        isSpecial = false
        tvStar1.visibility = View.INVISIBLE
        tvStar2.visibility = View.INVISIBLE
        tvStar3.visibility = View.INVISIBLE
        tvStar4.visibility = View.INVISIBLE
    }

    //专票 （单位地址、电话号码、开户银行、银行账号必填）
    private fun showStars() {
        isSpecial = true
        tvStar1.visibility = View.VISIBLE
        tvStar2.visibility = View.VISIBLE
        tvStar3.visibility = View.VISIBLE
        tvStar4.visibility = View.VISIBLE
    }

    private fun initRvData() {
        val mdLs = ArrayList<InvoiceTypeMDL.Type>().apply {
            add(InvoiceTypeMDL.Type().apply {
                dictcode = InvoiceType.INVOICE_COMMON.code
                dictname = getString(R.string.invoice_common)
                sontype = ArrayList<InvoiceTypeMDL.Type.SonType>().apply {
                    add(InvoiceTypeMDL.Type.SonType().apply {
                        dictcode = InvoiceType.INVOICE_HEAD_COMPANY.code
                        dictname = getString(R.string.invoice_rise_company)
                    })
                    add(InvoiceTypeMDL.Type.SonType().apply {
                        dictcode = InvoiceType.INVOICE_HEAD_PERSONAL.code
                        dictname = getString(R.string.invoice_rise_personal)
                    })
                }
            })
            add(InvoiceTypeMDL.Type().apply {
                dictcode = InvoiceType.INVOICE_SPECIAL.code
                dictname = getString(R.string.invoice_special)
            })
        }
        typeList.addAll(mdLs)
        isCompany = true
        typeList[0].dictcode?.let { fpType = it }
        typeAdapter.notifyDataSetChanged()
        typeList[0].sontype?.let { sonList ->
            titleList.addAll(sonList)
            titleAdapter.notifyDataSetChanged()
            if (titleList.size > 0) {
                titleList[0].dictcode?.let { headtype = it }
            }
        }
    }

//    override fun initData() {
//        doRequest(WebApiService.INVOICE_TYPE, WebApiService.invoiceTypeParams("115"), object : HttpRequestCallback<String>() {
//            override fun onPreExecute() {
//                setPageLoading()
//            }
//
//            override fun onSuccess(data: String?) {
//                if (GsonUtils.isResultOk(data)) {
//                    val mdl = GsonUtils.fromDataBean(data, InvoiceTypeMDL::class.java)
//                    if (mdl == null) showShortToast("数据解析异常")
//                    else {
//                        setPageEndLoading()
//                        updateData(mdl)
//                    }
//                } else {
//                    showShortToast(GsonUtils.getMsg(data))
//                }
//            }
//
//            override fun onFailure(e: Throwable, errorMsg: String?) {
//                setPageError()
//            }
//        })
//    }
//
//    private fun updateData(typeMDL: InvoiceTypeMDL) {
//        typeMDL.type?.let { list ->
//            typeList.addAll(list)
//            typeAdapter.notifyDataSetChanged()
//            if (typeList.size > 0) {
//                typeList[0].dictcode?.let { fpType = it }
//                typeList[0].sontype?.let { sonList ->
//                    titleList.addAll(sonList)
//                    titleAdapter.notifyDataSetChanged()
//                    if (titleList.size > 0) {
//                        titleList[0].dictcode?.let { headtype = it }
//                    }
//                }
//            }
//        }
//    }

    private fun onSubmit() {
        if (TextUtils.isEmpty(fpType)) {
            showShortToast("请选择发票类型")
        } else if (TextUtils.isEmpty(headtype)) {
            showShortToast("请选择抬头类型")
        } else if (TextUtils.isEmpty(etName.text.toString().trim())) {
            showShortToast(etName.hint)
        } else {
            if (isSpecial) {   //专票类型
                when {
                    TextUtils.isEmpty(etDutyNum.text.toString().trim()) -> showShortToast(etDutyNum.hint)
                    TextUtils.isEmpty(etUnit.text.toString().trim()) -> showShortToast(etUnit.hint)
                    TextUtils.isEmpty(etPhone.text.toString().trim()) -> showShortToast(etPhone.hint)
                    TextUtils.isEmpty(etBank.text.toString().trim()) -> showShortToast(etBank.hint)
                    TextUtils.isEmpty(etAccount.text.toString().trim()) -> showShortToast(etAccount.hint)
                    TextUtils.isEmpty(etContants.text.toString().trim()) -> showShortToast(etContants.hint)
                    TextUtils.isEmpty(etTelPhone.text.toString().trim()) -> showShortToast(etTelPhone.hint)
                    TextUtils.isEmpty(etAddress.text.toString().trim()) -> showShortToast(etAddress.hint)
                    else -> submit()
                }
            } else {
                if (isCompany && TextUtils.isEmpty(etDutyNum.text.toString().trim())) {
                    showShortToast(etDutyNum.hint)
                    return
                }
                when {
                    TextUtils.isEmpty(etContants.text.toString().trim()) -> showShortToast(etContants.hint)
                    TextUtils.isEmpty(etTelPhone.text.toString().trim()) -> showShortToast(etTelPhone.hint)
                    TextUtils.isEmpty(etAddress.text.toString().trim()) -> showShortToast(etAddress.hint)
                    else -> submit()
                }
            }
        }
    }

    /**
     * fptype	发票类型	否	1150001 普通发票 ； 1150002 增值税发票
    headtype	抬头类型	否	1160001 个人 ； 1160002 企业
    fphead	抬头名称	否
    taxnumber	税号	否
    companyaddress	单位地址	否
    companyphone	单位电话	否
    bankno	银行号码	否
    bankname	银行名称	否
    consigneeaddress	收件人地址	否
    consignee	收件人名称	否
    consigneephone	收件人电话	否
    rescueid	救援ID	否
     */
    private fun submit() {
        val params = HashMap<String, String?>().apply {
            put("fptype", fpType)
            put("headtype", headtype)
            put("fphead", etName.text.toString())
            if (isSpecial || isCompany) {
                put("taxnumber", etDutyNum.text.toString())
                put("companyaddress", etUnit.text.toString())
                put("companyphone", etPhone.text.toString())
                put("bankno", etAccount.text.toString())
                put("bankname", etBank.text.toString())
            } else {
                put("taxnumber", "")
                put("companyaddress", "")
                put("companyphone", "")
                put("bankno", "")
                put("bankname", "")
            }
            put("consigneeaddress", etAddress.text.toString())
            put("consignee", etContants.text.toString())
            put("consigneephone", etTelPhone.text.toString())
            put("rescueid", rescueId)
        }
        doRequest(WebApiService.SAVE_INVOICE, params, object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading("正在提交…")
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    showShortToast("提交成功")
                    setResult(RESULT_OK)
                    Handler().postDelayed({ if (!isFinishing) finish() }, 1500)
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
}