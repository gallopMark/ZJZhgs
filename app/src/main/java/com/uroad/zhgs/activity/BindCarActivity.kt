package com.uroad.zhgs.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.text.TextUtils
import android.view.View
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.model.EvaluateMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import com.uroad.zhgs.widget.GridSpacingItemDecoration
import kotlinx.android.synthetic.main.activity_bindcar.*
import android.widget.*
import android.os.Handler
import android.text.method.ReplacementTransformationMethod
import com.amap.api.col.sln3.et
import com.uroad.zhgs.R
import com.uroad.zhgs.R.id.*
import com.uroad.zhgs.common.CarNoType
import com.uroad.zhgs.dialog.CarNoInputDialog
import com.uroad.zhgs.dialog.MaterialDialog
import com.uroad.zhgs.enumeration.Carcategory
import com.uroad.zhgs.model.CarDetailMDL
import com.uroad.zhgs.utils.CheckUtils
import com.uroad.zhgs.utils.InputMethodUtils


/**
 *Created by MFB on 2018/8/14.
 * 绑定车辆或查看车辆信息页面
 */
class BindCarActivity : BaseActivity() {

    class CarCategoryAdapter(context: Context, mDatas: MutableList<EvaluateMDL.Type>)
        : BaseArrayRecyclerAdapter<EvaluateMDL.Type>(context, mDatas) {

        private var onItemSelectedListener: OnItemSelectedListener? = null
        private var selectIndex = 0
        override fun onBindHoder(holder: RecyclerHolder, t: EvaluateMDL.Type, position: Int) {
            val tv = holder.obtainView<TextView>(R.id.tv)
            tv.text = t.dictname
            tv.isSelected = selectIndex == position
            holder.itemView.setOnClickListener {
                onItemSelectedListener?.onItemSelected(position)
                setSelectIndex(position)
            }
        }

        fun setSelectIndex(position: Int) {
            selectIndex = position
            notifyDataSetChanged()
        }

        override fun bindView(viewType: Int): Int {
            return R.layout.item_rescue_car
        }

        interface OnItemSelectedListener {
            fun onItemSelected(position: Int)
        }

        fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener) {
            this.onItemSelectedListener = onItemSelectedListener
        }
    }

    class CarTypeAdapter(context: Context, mDatas: MutableList<EvaluateMDL.Type.SonType>) :
            BaseArrayRecyclerAdapter<EvaluateMDL.Type.SonType>(context, mDatas) {

        private var onItemSelectedListener: OnItemSelectedListener? = null
        private var selectIndex = 0
        override fun onBindHoder(holder: RecyclerHolder, t: EvaluateMDL.Type.SonType, position: Int) {
            val tv = holder.obtainView<TextView>(R.id.tv)
            tv.text = t.dictname
            tv.isSelected = selectIndex == position
            holder.itemView.setOnClickListener {
                onItemSelectedListener?.onItemSelected(position)
                setSelectIndex(position)
            }
        }

        override fun bindView(viewType: Int): Int {
            return R.layout.item_rescue_car
        }

        fun setSelectIndex(position: Int) {
            selectIndex = position
            notifyDataSetChanged()
        }

        interface OnItemSelectedListener {
            fun onItemSelected(position: Int)
        }

        fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener) {
            this.onItemSelectedListener = onItemSelectedListener
        }
    }

    class AxisAdapter(context: Context, mDatas: MutableList<EvaluateMDL.Type>) :
            BaseArrayRecyclerAdapter<EvaluateMDL.Type>(context, mDatas) {

        private var onItemSelectedListener: OnItemSelectedListener? = null
        private var selectIndex = 0
        override fun onBindHoder(holder: RecyclerHolder, t: EvaluateMDL.Type, position: Int) {
            val tv = holder.obtainView<TextView>(R.id.tv)
            tv.text = t.dictname
            tv.isSelected = selectIndex == position
            holder.itemView.setOnClickListener {
                onItemSelectedListener?.onItemSelected(position)
                setSelectIndex(position)
            }
        }

        override fun bindView(viewType: Int): Int {
            return R.layout.item_rescue_car
        }

        fun setSelectIndex(position: Int) {
            selectIndex = position
            notifyDataSetChanged()
        }

        interface OnItemSelectedListener {
            fun onItemSelected(position: Int)
        }

        fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener) {
            this.onItemSelectedListener = onItemSelectedListener
        }
    }

    private val lbDatas = ArrayList<EvaluateMDL.Type>()
    private lateinit var lbAdapter: CarCategoryAdapter
    private val lxDatas = ArrayList<EvaluateMDL.Type.SonType>()
    private lateinit var lxAdapter: CarTypeAdapter
    private val axDatas = ArrayList<EvaluateMDL.Type>()
    private lateinit var axAdapter: AxisAdapter
    private var carcategory: String = "" //车辆类别
    private var cartype: String = "" //车辆类型
    private var axisnum: String = "" //轴数
    private var isdefault: Int = 0 // 是否默认车辆 否    0 否 ； 1 是
    private var isTruck = false
    private var isDetails = false   //是否是从列表进来查看详情
    private var carId = ""
    private var detailMDL: CarDetailMDL? = null
    private var usercarid: String = ""

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_bindcar)
        hideBaseLine(true)
        intent.extras?.let {
            isDetails = it.getBoolean("carDetails")
            carId = it.getString("carId")
            val code = it.getString("code")
            if (code == Carcategory.TRUCK.code) isTruck = true
        }
        if (isDetails) {  //详情显示删除和修改按钮
            withTitle(resources.getString(R.string.bindcar_detail_title))
            withOption(resources.getString(R.string.delete))
            llCarCategory.visibility = View.GONE
            btSubmit.text = resources.getString(R.string.modify)
            btSubmit.isEnabled = false
        } else {
            withTitle(resources.getString(R.string.bindcar_title))
            llCarCategory.visibility = View.VISIBLE
            btSubmit.text = resources.getString(R.string.submit)
        }
        if (isTruck) {
            llContent.visibility = View.VISIBLE
        } else {
            llContent.visibility = View.GONE
        }
        initNumTv()
        initEtNum()
        initRv()
        checkbox.setOnCheckedChangeListener { _, isChecked -> isdefault = if (isChecked) 1 else 0 }
        btSubmit.setOnClickListener { onSubmit() }
    }

    private fun initNumTv() {
        try {
            val setSoftInputShownOnFocus = EditText::class.java.getMethod("setShowSoftInputOnFocus", Boolean::class.java)
            setSoftInputShownOnFocus.isAccessible = true
            setSoftInputShownOnFocus.invoke(etNumType, false)
        } catch (e: Exception) {
        }
        etNumType.setText((CarNoType.getCarMulti()[10] as CarNoType.TextType).text)
        etNumType.setSelection(etNumType.text.length)
        etNumType.setOnClickListener { showCustomInput() }
        etNumType.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showCustomInput()
            }
        }
    }

    private fun showCustomInput(){
        InputMethodUtils.hideSoftInput(this)
        CarNoInputDialog(this@BindCarActivity).setOnCarNoClickListener(object : CarNoInputDialog.OnCarNoClickListener {
            override fun onCarNoClick(province: String, option: Int, dialog: CarNoInputDialog) {
                if (option == 1 || option == 2) {
                    dialog.dismiss()
                } else {
                    etNumType.setText(province)
                    etNumType.setSelection(etNumType.text.length)
                }
            }
        }).show()
    }

    /*要将输入的小写字母自动转化为大写字母并显示在EditText上,比较简便的方法是设置EditText的setTransformationMethod*/
    private fun initEtNum() {
        etCarNum.transformationMethod = object : ReplacementTransformationMethod() {
            override fun getOriginal(): CharArray {
                return charArrayOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z')
            }

            override fun getReplacement(): CharArray {
                return charArrayOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')
            }
        }
    }

    private fun initRv() {
        rvCarCategory.isNestedScrollingEnabled = false
        rvCarType.isNestedScrollingEnabled = false
        rvAxis.isNestedScrollingEnabled = false
        rvCarCategory.addItemDecoration(GridSpacingItemDecoration(3, DisplayUtils.dip2px(this, 10f), false))
        rvCarCategory.layoutManager = GridLayoutManager(this, 3).apply { orientation = GridLayoutManager.VERTICAL }
        rvCarType.addItemDecoration(GridSpacingItemDecoration(3, DisplayUtils.dip2px(this, 10f), false))
        rvCarType.layoutManager = GridLayoutManager(this, 3).apply { orientation = GridLayoutManager.VERTICAL }
        rvAxis.addItemDecoration(GridSpacingItemDecoration(4, DisplayUtils.dip2px(this, 10f), false))
        rvAxis.layoutManager = GridLayoutManager(this, 4).apply { orientation = GridLayoutManager.VERTICAL }
        lbAdapter = BindCarActivity.CarCategoryAdapter(this, lbDatas)
        rvCarCategory.adapter = lbAdapter
        lxAdapter = BindCarActivity.CarTypeAdapter(this, lxDatas)
        rvCarType.adapter = lxAdapter
        axAdapter = BindCarActivity.AxisAdapter(this, axDatas)
        rvAxis.adapter = axAdapter
    }

    override fun initData() {
        getCarData()
        getAxisData()
        if (isDetails) requestCarDetail()
    }

    private fun getCarData() {
        doRequest(WebApiService.EVALUATE_TEXT, WebApiService.evaluateTextParams("100"), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                setPageLoading()
            }

            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, EvaluateMDL::class.java)
                    if (mdl == null) showShortToast("数据解析异常")
                    else {
                        setPageEndLoading()
                        updateData(mdl)
                    }
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                setPageError()
            }
        })
    }

    //获取车辆信息
    private fun updateData(mdl: EvaluateMDL) {
        lbDatas.clear()
        lxDatas.clear()
        mdl.type?.let {
            lbDatas.addAll(it)
            lbAdapter.notifyDataSetChanged()
        }
        if (lbDatas.size > 0) {
            lbDatas[0].dictcode?.let { carcategory = it }  //车辆类别默认第一个
            lbDatas[0].sontype?.let { it ->
                lxDatas.addAll(it)
                lxAdapter.notifyDataSetChanged()
                if (lxDatas.size > 0) {
                    lxDatas[0].dictcode?.let { cartype = it }  //车辆类型默认第一个
                }
            }
        }
        lbAdapter.setOnItemSelectedListener(object : BindCarActivity.CarCategoryAdapter.OnItemSelectedListener {
            override fun onItemSelected(position: Int) {
                if (position == 1) {
                    isTruck = true
                    llContent.visibility = View.VISIBLE
                } else {
                    isTruck = false
                    llContent.visibility = View.GONE
                }
                lxDatas.clear()
                lbDatas[position].sontype?.let { lxDatas.addAll(it) }
                lxAdapter.notifyDataSetChanged()
                lbDatas[position].dictcode?.let { carcategory = it }
            }
        })
        lxAdapter.setOnItemSelectedListener(object : BindCarActivity.CarTypeAdapter.OnItemSelectedListener {
            override fun onItemSelected(position: Int) {
                lxDatas[position].dictcode?.let { cartype = it }
            }
        })
        detailMDL?.let { updateCarData(it) }
    }

    //获取轴数
    private fun getAxisData() {
        doRequest(WebApiService.AXIS_NUM, WebApiService.axisNumParams("111"), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, EvaluateMDL::class.java)
                    if (mdl == null) showShortToast("数据解析异常")
                    else {
                        setPageEndLoading()
                        updateAxs(mdl)
                    }
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                setPageError()
            }
        })
    }

    private fun updateAxs(mdl: EvaluateMDL) {
        axDatas.clear()
        mdl.type?.let {
            axDatas.addAll(it)
            axAdapter.notifyDataSetChanged()
        }
        if (axDatas.size > 0) {
            axDatas[0].dictcode?.let { axisnum = it }  //车辆类别默认第一个
        }
        axAdapter.setOnItemSelectedListener(object : BindCarActivity.AxisAdapter.OnItemSelectedListener {
            override fun onItemSelected(position: Int) {
                axDatas[position].dictcode?.let { axisnum = it }
            }
        })
        detailMDL?.let { updateCarData(it) }
    }

    //车辆详情
    private fun requestCarDetail() {
        doRequest(WebApiService.CAR_DETAILS, WebApiService.carDetailsParams(carId), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, CarDetailMDL::class.java)
                    if (mdl == null) showShortToast("数据解析异常")
                    else updateCarData(mdl)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                onHttpError(e)
            }
        })
    }

    private fun updateCarData(mdl: CarDetailMDL) {
        detailMDL = mdl
        mdl.usercarid?.let { usercarid = it }
        etNumType.setText(mdl.getCarNum()[0])
        etCarNum.setText(mdl.getCarNum()[1])
        etEnginno.setText(mdl.enginno)
        etTotalWeight.setText(mdl.total)
        etFixedLoad.setText(mdl.fixedload)
        etCarLength.setText(mdl.carlength)
        etCarWidth.setText(mdl.carwide)
        etCarHigh.setText(mdl.carheight)
        checkbox.isChecked = mdl.isdefault == 1
        mdl.carcategory?.let { carcategory = it }
        mdl.cartype?.let { cartype = it }
        for (i in 0 until lbDatas.size) {
            if (TextUtils.equals(mdl.carcategory, lbDatas[i].dictcode)) {
                lbAdapter.setSelectIndex(i)
                break
            }
        }
        for (i in 0 until lxDatas.size) {
            if (TextUtils.equals(mdl.cartype, lxDatas[i].dictcode)) {
                lxAdapter.setSelectIndex(i)
                break
            }
        }
        for (i in 0 until axDatas.size) {
            if (TextUtils.equals(mdl.axisnum, axDatas[i].dictcode)) {
                axAdapter.setSelectIndex(i)
                break
            }
        }
        btSubmit.isEnabled = true
    }

    private fun onSubmit() {
        if (TextUtils.isEmpty(etCarNum.text.toString().trim())) {
            showShortToast(etCarNum.hint)
        } else if (!CheckUtils.isCarNum("${etNumType.text}${etCarNum.text.toString().trim()}")) {
            showShortToast(getString(R.string.error_carNo_tips))
        } else if (TextUtils.isEmpty(carcategory)) {
            showShortToast("请选择车辆类别")
        } else if (TextUtils.isEmpty(cartype)) {
            showShortToast("请选择车辆类型")
        } else if (TextUtils.isEmpty(etEnginno.text.toString().trim())) {
            showShortToast(etEnginno.hint)
        } else {
            if (isTruck) {  //如果选择了货车，则需要填如下信息
                when {
                    TextUtils.isEmpty(etTotalWeight.text.toString().trim()) -> showShortToast(etTotalWeight.hint)
                    TextUtils.isEmpty(etFixedLoad.text.toString().trim()) -> showShortToast(etFixedLoad.hint)
                    TextUtils.isEmpty(etCarLength.text.toString().trim()) -> showShortToast(etCarLength.hint)
                    TextUtils.isEmpty(etCarWidth.text.toString().trim()) -> showShortToast(etCarWidth.hint)
                    TextUtils.isEmpty(etCarHigh.text.toString().trim()) -> showShortToast(etCarHigh.hint)
                    TextUtils.isEmpty(axisnum) -> showShortToast("请选择轴数")
                    else -> bindCar()
                }
            } else {
                bindCar()
            }
        }
    }

    /**
     * carno	车牌	否
    carcategory	车辆类别	否
    cartype	车辆类型	否
    userid	用户ID	否
    total	总重量	否
    fixedload	核定载重	否
    carlength	车长	否
    carwide	车宽	否
    carheight	车高	否
    axisnum	轴数	否
    usercarid	绑定ID	否	新增就传空
    isdefault	是否默认车辆	否	0 否 ； 1 是
     */
    private fun bindCar() {
        val params = HashMap<String, String?>().apply {
            put("carno", "${etNumType.text}${etCarNum.text.toString().toUpperCase()}")
            put("enginno", etEnginno.text.toString())
            put("carcategory", carcategory)
            put("cartype", cartype)
            put("userid", getUserId())
            if (isTruck) {  //选择了货车需要传值
                put("total", etTotalWeight.text.toString())
                put("fixedload", etFixedLoad.text.toString())
                put("carlength", etCarLength.text.toString())
                put("carwide", etCarWidth.text.toString())
                put("carheight", etCarHigh.text.toString())
                put("axisnum", axisnum)
            } else {
                put("total", "")
                put("fixedload", "")
                put("carlength", "")
                put("carwide", "")
                put("carheight", "")
                put("axisnum", "")
            }
            if (isDetails) {
                put("usercarid", usercarid)
            } else {
                put("usercarid", "")
            }
            put("isdefault", isdefault.toString())
        }
        doRequest(WebApiService.BINDCAR, params, object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading("正在提交…")
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    if (isDetails) {
                        showShortToast("修改车辆信息成功")
                        setResult(RESULT_OK, Intent().apply { putExtra("type", "alert") })
                    } else {
                        setResult(RESULT_OK)
                        showShortToast("车辆绑定成功")
                    }
                    finish()
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

    override fun onReload(view: View) {
        initData()
    }

    override fun onOptionClickListener(tvBaseOption: TextView) {
        showDialog("温馨提示", "确定删除此车辆信息吗？", "取消", "确定", object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                dialog.dismiss()
            }
        }, object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                dialog.dismiss()
                deleteCar()
            }
        })
    }

    //删除绑定车辆
    private fun deleteCar() {
        doRequest(WebApiService.UPDATE_CAR_STATUS, WebApiService.deleteCarParams(carId, "0"), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading("正在删除…")
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    showShortToast("已删除车辆信息")
                    setResult(RESULT_OK, Intent().apply { putExtra("type", "delete") })
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

    override fun finish() {
        InputMethodUtils.hideSoftInput(this)
        super.finish()
    }
}