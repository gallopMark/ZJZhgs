package com.uroad.zhgs.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import com.uroad.library.utils.DisplayUtils
import com.uroad.rxhttp.RxHttpManager
import com.uroad.zhgs.R
import com.uroad.zhgs.adapteRv.*
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.dialog.WheelViewDialog
import com.uroad.zhgs.model.*
import com.uroad.zhgs.photopicker.data.ImagePicker
import com.uroad.zhgs.utils.CheckUtils
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.ApiService
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import com.uroad.zhgs.widget.GridSpacingItemDecoration
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_rescue_request.*
import java.io.File

/**
 *Created by MFB on 2018/7/27.
 */
class RescueRequestActivity : BaseActivity() {

    private var roadid: String = ""
    private var roadname: String = ""
    private var miles: String = ""
    private var nCode: String = ""
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    private val directions = ArrayList<RescueRequestMDL.Direction>()  //所在方向集合
    private lateinit var directionAdapter: RRDirectionAdapter  //方向适配器
    private val lbData = ArrayList<RescueRequestMDL.Type>()   //车辆类别集合
    private lateinit var lbAdapter: RRCarCateGoryAdapter  //车辆类别适配器
    private val lxData = ArrayList<RescueRequestMDL.Sontype>() //车辆类型集合
    private lateinit var lxAdapter: RRCarTypeAdapter //车辆类型适配器
    private val jylxData = ArrayList<RescueRequestMDL.RescueType>()
    private lateinit var jylxAdapter: RRRescueTypeAdapter
    private val gzlxData = ArrayList<RescueRequestMDL.Sontype>() //故障类型集合
    private lateinit var gzlxAdapter: RRFaultTypeAdapter
    private val picData = ArrayList<MutilItem>()
    private lateinit var picAdapter: AddPicAdapter
    private val addItem = AddPicItem()
    private var directionno: String = "" //方向编号
    private var directionname: String = "" //方向名称
    private var carcategory: String = "" //车辆类别
    private var cartype: String = "" //车辆类型
    private var rescuetype: String = "" //救援类型
    private var subrescuetype: String = "" //故障类型
    private var usercarid: String = ""
    private var isSelection = true //是否点击选填
    private val imageUrlSB = StringBuilder() //图片
    private var isUpload = false   //图片是否已经上传

    override fun setUp(savedInstanceState: Bundle?) {
        withTitle(resources.getString(R.string.rescue_request_title))
        setBaseContentLayout(R.layout.activity_rescue_request)
        val id = intent.extras?.getString("roadid")
        id?.let { roadid = it }
        val name = intent.extras?.getString("roadname")
        name?.let { roadname = it }
        val pile = intent.extras?.getString("mile")
        pile?.let { miles = it }
        val code = intent.extras?.getString("n_code")
        code?.let { nCode = it }
        intent.extras?.let {
            longitude = it.getDouble("longitude")
            latitude = it.getDouble("latitude")
        }
        tvRoadName.text = roadname
        tvMile.text = miles
        initRv()
        initSelection()
        initCheckBox()
        initEditCarNum()
        etPhone.setText(getPhone())
        tvPicCount.text = "0/3"
        btSubmit.setOnClickListener { commit() }
    }

    private fun initRv() {
        rvDirection.isNestedScrollingEnabled = false
        rvCarCategory.isNestedScrollingEnabled = false
        rvCarType.isNestedScrollingEnabled = false
        rvRescueType.isNestedScrollingEnabled = false
        rvFaultType.isNestedScrollingEnabled = false
        rvPics.isNestedScrollingEnabled = false
        rvDirection.addItemDecoration(GridSpacingItemDecoration(2, DisplayUtils.dip2px(this, 10f), false))
        rvDirection.layoutManager = GridLayoutManager(this, 2).apply { orientation = GridLayoutManager.VERTICAL }
        rvDirection.addItemDecoration(GridSpacingItemDecoration(2, DisplayUtils.dip2px(this, 10f), false))
        rvCarCategory.layoutManager = GridLayoutManager(this, 2).apply { orientation = GridLayoutManager.VERTICAL }
        rvCarType.addItemDecoration(GridSpacingItemDecoration(4, DisplayUtils.dip2px(this, 10f), false))
        rvCarType.layoutManager = GridLayoutManager(this, 4).apply { orientation = GridLayoutManager.VERTICAL }
        rvRescueType.addItemDecoration(GridSpacingItemDecoration(2, DisplayUtils.dip2px(this, 10f), false))
        rvRescueType.layoutManager = GridLayoutManager(this, 2).apply { orientation = GridLayoutManager.VERTICAL }
        rvFaultType.addItemDecoration(GridSpacingItemDecoration(2, DisplayUtils.dip2px(this, 10f), false))
        rvFaultType.layoutManager = GridLayoutManager(this, 2).apply { orientation = GridLayoutManager.VERTICAL }
        rvPics.addItemDecoration(GridSpacingItemDecoration(3, DisplayUtils.dip2px(this, 5f), false))
        rvPics.layoutManager = GridLayoutManager(this, 3).apply { orientation = GridLayoutManager.VERTICAL }
        directionAdapter = RRDirectionAdapter(this, directions)
        rvDirection.adapter = directionAdapter
        lbAdapter = RRCarCateGoryAdapter(this, lbData)
        rvCarCategory.adapter = lbAdapter
        lxAdapter = RRCarTypeAdapter(this, lxData)
        rvCarType.adapter = lxAdapter
        jylxAdapter = RRRescueTypeAdapter(this, jylxData)
        rvRescueType.adapter = jylxAdapter
        gzlxAdapter = RRFaultTypeAdapter(this, gzlxData)
        rvFaultType.adapter = gzlxAdapter
        picData.add(addItem)
        picAdapter = AddPicAdapter(this, picData, 3, DisplayUtils.dip2px(this, 30f))
        rvPics.adapter = picAdapter
    }

    private fun initSelection() {
        val drawable1 = ContextCompat.getDrawable(this, R.mipmap.ic_arrow_right)
        drawable1?.setBounds(0, 0, drawable1.minimumWidth, drawable1.minimumHeight)
        val drawable2 = ContextCompat.getDrawable(this, R.mipmap.ic_arrow_down)
        drawable2?.setBounds(0, 0, drawable2.minimumWidth, drawable2.minimumHeight)
        tvSelection.setOnClickListener {
            if (isSelection) {
                tvSelection.setCompoundDrawables(null, null, drawable2, null)
                llSelection.visibility = View.VISIBLE
                isSelection = false
            } else {
                tvSelection.setCompoundDrawables(null, null, drawable1, null)
                llSelection.visibility = View.GONE
                isSelection = true
            }
        }
    }

    private fun initCheckBox() {
        val text = resources.getString(R.string.rescue_request_agreement)
        val ss = SpannableString(text)
        val start = text.indexOf("《")
        val end = text.length
        val clickSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                openActivity(RescueBookActivity::class.java)
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
            }
        }
        ss.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorAccent))
                , start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        checkbox.text = ss
        checkbox.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun initEditCarNum() {
        etMyCarNum.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                usercarid = "0"
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }

    override fun initData() {
        doRequest(WebApiService.RESCUE_REQUEST_FORM, WebApiService.requestForm(getUserId(), roadid), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                setPageLoading()
            }

            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    setPageEndLoading()
                    val mdl = GsonUtils.fromDataBean(data, RescueRequestMDL::class.java)
                    mdl?.let { updateData(it) }
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                    setPageError()
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                e.printStackTrace()
                setPageError()
            }
        })
    }

    private fun updateData(mdl: RescueRequestMDL) {
        mdl.direction?.let { it ->
            directions.addAll(it)
            directionAdapter.notifyDataSetChanged()
            if (directions.size > 0) {  //默认选中第一个方向
                directions[0].directionid?.let { directionno = it }
                directions[0].directionname?.let { directionname = it }
            }
        }
        mdl.type?.let {
            lbData.addAll(it)
            lbAdapter.notifyDataSetChanged()
        }
        if (lbData.size > 0) {
            lbData[0].dictcode?.let { carcategory = it }  //车辆类别默认第一个
            lbData[0].sontype?.let { it ->
                lxData.addAll(it)
                lxAdapter.notifyDataSetChanged()
                if (lxData.size > 0) {
                    lxData[0].dictcode?.let { cartype = it }  //车辆类型默认第一个
                }
            }
        }
        mdl.rescuetype?.let { it ->
            jylxData.addAll(it)
            jylxAdapter.notifyDataSetChanged()
            if (jylxData.size > 0) {  //救援类型默认第一个
                jylxData[0].dictcode?.let { rescuetype = it }
                jylxData[0].sontype?.let {
                    gzlxData.addAll(it)
                    gzlxAdapter.notifyDataSetChanged()
                }
            }
        }
        directionAdapter.setOnItemSelectedListener(object : RRDirectionAdapter.OnItemSelectedListener {
            override fun onItemSelected(position: Int) {
                directions[position].directionid?.let { directionno = it }
                directions[position].directionname?.let { directionname = it }
            }
        })
        lbAdapter.setOnItemSelectedListener(object : RRCarCateGoryAdapter.OnItemSelectedListener {
            override fun onItemSelected(position: Int) {
                lxData.clear()
                lbData[position].sontype?.let { lxData.addAll(it) }
                lxAdapter.notifyDataSetChanged()
                lbData[position].dictcode?.let { carcategory = it }
            }
        })
        lxAdapter.setOnItemSelectedListener(object : RRCarTypeAdapter.OnItemSelectedListener {
            override fun onItemSelected(position: Int) {
                lxData[position].dictcode?.let { cartype = it }
            }
        })
        jylxAdapter.setOnItemSelectedListener(object : RRRescueTypeAdapter.OnItemSelectedListener {
            override fun onItemSelected(position: Int) {
                jylxData[position].dictcode?.let { rescuetype = it }
                gzlxData.clear()
                jylxData[position].sontype?.let { gzlxData.addAll(it) }
                gzlxAdapter.notifyDataSetChanged()
            }
        })
        mdl.myCar?.let { updateMyCar(it) }
    }

    private fun updateMyCar(mdLs: MutableList<CarMDL>) {
        if (mdLs.size > 0) {
            tvChangeCarNum.visibility = View.VISIBLE
            etMyCarNum.setText(mdLs[0].carno)
            etMyCarNum.setSelection(etMyCarNum.text.length)
            mdLs[0].carid?.let { usercarid = it }
            tvChangeCarNum.setOnClickListener { _ ->
                WheelViewDialog(this@RescueRequestActivity).withData(ArrayList<String>().apply {
                    for (item in mdLs) {
                        item.carno?.let { this.add(it) }
                    }
                }).withItemNum(5).withListener(object : WheelViewDialog.OnItemSelectListener {
                    override fun onItemSelect(position: Int, text: String, dialog: WheelViewDialog) {
                        if (position in 0 until mdLs.size) {
                            mdLs[position].carid?.let { usercarid = it }
                            etMyCarNum.setText(text)
                            etMyCarNum.setSelection(etMyCarNum.text.length)
                        }
                        dialog.dismiss()
                    }
                }).show()
            }
        }
    }

    override fun setListener() {
        picAdapter.setOnItemOptionListener(object : AddPicAdapter.OnItemOptionListener {
            override fun onAddPic() {
                ImagePicker.from(this@RescueRequestActivity)
                        .isMutilyChoice(4 - picData.size)
                        .isCompress(true)
                        .requestCode(1)
                        .start()
            }

            override fun onItemRemove(mDatas: MutableList<MutilItem>) {
                picData.remove(addItem)
                setPicCount()
                picData.add(addItem)
                picAdapter.notifyDataSetChanged()
            }
        })
    }

    override fun onReload(view: View) {
        initData()
    }

    //取图片回调
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val items = data?.getStringArrayListExtra(ImagePicker.EXTRA_PATHS)
            items?.let { paths ->
                picData.remove(addItem)
                for (item in paths) picData.add(PicMDL().apply { path = item })
                setPicCount()
                if (picData.size < 3) picData.add(addItem)
                picAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun setPicCount() {
        val picCount = "${picData.size}/3"
        tvPicCount.text = picCount
    }

    private fun isOptionOK(): Boolean {
        if (lbAdapter.getSelectIndex() in 0 until lbData.size)
            lbData[lbAdapter.getSelectIndex()].dictcode?.let { carcategory = it }
        if (lxAdapter.getSelectIndex() in 0 until lxData.size)
            lxData[lxAdapter.getSelectIndex()].dictcode?.let { cartype = it }
        if (jylxAdapter.getSelectIndex() in 0 until jylxData.size)
            jylxData[jylxAdapter.getSelectIndex()].dictcode?.let { rescuetype = it }
        if (gzlxAdapter.getSelectIndex() in 0 until gzlxData.size)
            gzlxData[gzlxAdapter.getSelectIndex()].dictcode?.let { subrescuetype = it }
        when {
            TextUtils.isEmpty(roadid) -> {
                showShortToast("路段ID不可为空")
                return false
            }
            TextUtils.isEmpty(roadname) -> {
                showShortToast("路段名称不可为空")
                return false
            }
            TextUtils.isEmpty(miles) -> {
                showShortToast("桩号不可为空")
                return false
            }
            TextUtils.isEmpty(directionno) -> {
                showShortToast("所在方向编号不可为空")
                return false
            }
            TextUtils.isEmpty(directionname) -> {
                showShortToast("所在方向名称不可为空")
                return false
            }
            TextUtils.isEmpty(etMyCarNum.text.toString()) -> {
                showShortToast(etMyCarNum.hint)
                return false
            }
            !CheckUtils.isCarNum(etMyCarNum.text.toString().trim()) -> {
                showShortToast(getString(R.string.error_carNo_tips))
                return false
            }
//            TextUtils.isEmpty(etName.text.toString()) -> {
//                showShortToast(resources.getString(R.string.rescue_request_username_hint))
//                return false
//            }
            TextUtils.isEmpty(etPhone.text.toString()) -> {
                showShortToast(resources.getString(R.string.rescue_request_userphone_hint))
                return false
            }
            !CheckUtils.isMobile(etPhone.text.toString()) -> {
                showShortToast(getString(R.string.error_phone_tips))
                return false
            }
            TextUtils.isEmpty(carcategory) -> {
                showShortToast("请选择车辆类别")
                return false
            }
            TextUtils.isEmpty(cartype) -> {
                showShortToast("请选择车辆类型")
                return false
            }
            TextUtils.isEmpty(rescuetype) -> {
                showShortToast("请选择救援类型")
                return false
            }
            !checkbox.isChecked -> {
                showShortToast("请同意《救援服务协议》")
                return false
            }
            else -> {
                return true
            }
        }
    }

    private fun needUploadPic(): Boolean {
        for (item in picData) {
            return item.getItemType() == 2
        }
        return false
    }

    private fun commit() {
        if (!isOptionOK()) return
        if (needUploadPic() && !isUpload) {   //如果选择了图片 则先上传图片
            addDisposable(Observable.fromArray(picData)
                    .map { it ->
                        for (item in it) {
                            if (item.getItemType() == 2) {
                                val picItem = item as PicMDL
                                RxHttpManager.createApi(ApiService::class.java)
                                        .uploadFile(createMultipart(File(picItem.path), "file"))
                                        .subscribe({ body ->
                                            val json = body?.string()
                                            if (GsonUtils.isResultOk(json)) {
                                                val imageMDL = GsonUtils.fromDataBean(json, UploadMDL::class.java)
                                                imageMDL?.imgurl?.file?.let {
                                                    imageUrlSB.append("$it,")
                                                }
                                            }
                                        }, {})
                            }
                        }
                    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        endLoading()
                        isUpload = true
                        lastCommit()
                    }, {
                        showShortToast("图片上传失败")
                    }, {
                        endLoading()
                    }, {
                        showLoading("正在上传图片…")
                    }))
        } else {
            lastCommit()
        }
    }

    /**
     * 参数项	名称	是否可为空	备注
    roadno	路段ID	否
    roadname	路段名称	否
    miles	桩号	否
    directionno	方向编号	否
    directionname	方向名称	否
    carphone	联系电话	否
    carcategory	车辆类别	否
    cartype	车辆类型	否
    rescuetype	救援类型	否
    subrescuetype	故障类型	否
    place	目的地	是
    userid	用户ID	是
    remark	备注	是
    photourl	图片	是	多张则逗号隔开
    username	用户名	否
    n_code	定位接口来的	否
    longitude	经度	否
    latitude	纬度	否
    usercarid	车辆ID	否	当用户不是选择已绑定的车牌时传0；选择车牌就传对应ID
    carno	车牌号	否
     */
    private fun lastCommit() {
        val params = HashMap<String, String?>().apply {
            put("roadno", roadid)
            put("roadname", roadname)
            put("miles", miles)
            put("directionno", directionno)
            put("directionname", directionname)
            put("carphone", etPhone.text.toString())
            put("carcategory", carcategory)
            put("cartype", cartype)
            put("rescuetype", rescuetype)
            put("subrescuetype", subrescuetype)
            put("place", etDestination.text.toString())
            put("userid", getUserId())
            put("username", getUserName())
            put("n_code", nCode)
            put("longitude", longitude.toString())
            put("latitude", latitude.toString())
            put("usercarid", usercarid)
            put("carno", etMyCarNum.text.toString())
            put("remark", etRemark.text.toString())
            if (!TextUtils.isEmpty(imageUrlSB.toString())) {
                put("photourl", imageUrlSB.toString())
            } else {
                put("photourl", "")
            }
        }
        doRequest(WebApiService.SUBMIT_RESCUE_INFO, params, object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading("正在提交…")
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, RescueRequestResultMDL::class.java)
                    if (mdl == null) showShortToast("数据解析异常")
                    else {
                        val intent = Intent().apply {
                            putExtras(Bundle().apply {
                                putString("rescueid", mdl.rescueid)
                                putString("rescueno", mdl.rescueno)
                            })
                        }
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                endLoading()
                showShortToast("提交失败，请稍后再试")
            }
        })
    }
}