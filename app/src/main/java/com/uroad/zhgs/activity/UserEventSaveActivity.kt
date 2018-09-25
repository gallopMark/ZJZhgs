package com.uroad.zhgs.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import com.amap.api.location.AMapLocation
import com.uroad.library.utils.DisplayUtils
import com.uroad.rxhttp.RxHttpManager
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.enumeration.EventType
import com.uroad.zhgs.model.*
import com.uroad.zhgs.photopicker.data.ImagePicker
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.ApiService
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import com.uroad.zhgs.widget.GridSpacingItemDecoration
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_userevent_save.*
import top.zibin.luban.Luban
import java.io.File

/**
 *Created by MFB on 2018/8/8.
 */
class UserEventSaveActivity : BaseActivity(), View.OnClickListener {
    private var eventtype = EventType.TRAFFIC_JAM.code
    private var longitude: Double = CurrApplication.APP_LATLNG.longitude
    private var latitude: Double = CurrApplication.APP_LATLNG.latitude
    private val roads = ArrayList<RoadMDL>()
    private var currIndex = 0
    private var roadoldid: String = ""
    private var imageUrls: String = ""
    private val picData = ArrayList<MutilItem>()
    private val addItem = AddPicItem()
    private lateinit var picAdapter: AddPicAdapter

    class AddPicAdapter(context: Activity, mDatas: MutableList<MutilItem>)
        : BaseArrayRecyclerAdapter<MutilItem>(context, mDatas) {
        private val size = (DisplayUtils.getWindowWidth(context) - DisplayUtils.dip2px(context, 44f)) / 3
        private var onItemOptionListener: OnItemOptionListener? = null
        override fun onBindHoder(holder: RecyclerHolder, t: MutilItem, position: Int) {
            holder.itemView.layoutParams = LinearLayout.LayoutParams(size, size)
            if (holder.itemViewType == 1) {
                holder.itemView.setOnClickListener { onItemOptionListener?.onAddPic() }
            } else {
                val mdl = t as PicMDL
                holder.displayImage(R.id.ivPic, mdl.path)
                holder.setOnClickListener(R.id.ivCancel, View.OnClickListener {
                    mDatas.removeAt(position)
                    notifyDataSetChanged()
                    onItemOptionListener?.onItemRemove(mDatas)
                })
            }
        }

        override fun getItemViewType(position: Int): Int {
            return mDatas[position].getItemType()
        }

        override fun bindView(viewType: Int): Int {
            if (viewType == 1) return R.layout.item_addpic_button2
            return R.layout.item_addpic2
        }

        interface OnItemOptionListener {
            fun onAddPic()
            fun onItemRemove(mDatas: MutableList<MutilItem>)
        }

        fun setOnItemOptionListener(onItemOptionListener: OnItemOptionListener) {
            this.onItemOptionListener = onItemOptionListener
        }
    }

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_userevent_save)
        customToolbar.title = resources.getString(R.string.userEvent_title)
        initTitleView()
        tvTitle.text = resources.getString(R.string.userEvent_burst)
        customToolbar.setNavigationOnClickListener { onBackPressed() }
        llYD.isSelected = true
        initRv()
        applyLocationPermission(true)
        ivPos.setOnClickListener {
            if (roads.size > 0) {
                showRoadDialog()
            } else {
                getNearbyLoad()
            }
        }
        btSubmit.setOnClickListener { commit() }
    }

    /**
     * 获取TitleTextView 通过反射获取toolbar titleView
     */
    private fun initTitleView() {
        try {
            val field = Toolbar::class.java.getDeclaredField("mTitleTextView").apply { isAccessible = true }
            val titleView = field.get(customToolbar) as View
            titleView.setOnClickListener { onBackPressed() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initRv() {
        rvPics.addItemDecoration(GridSpacingItemDecoration(3, DisplayUtils.dip2px(this, 10f), true))
        rvPics.layoutManager = GridLayoutManager(this, 3).apply { orientation = GridLayoutManager.VERTICAL }
        picData.add(addItem)
        picAdapter = AddPicAdapter(this, picData)
        rvPics.adapter = picAdapter
    }

    override fun setListener() {
        llYD.setOnClickListener(this)
        llZC.setOnClickListener(this)
        llSG.setOnClickListener(this)
        llJS.setOnClickListener(this)
        llYS.setOnClickListener(this)
        llGZ.setOnClickListener(this)
        picAdapter.setOnItemOptionListener(object : AddPicAdapter.OnItemOptionListener {
            override fun onAddPic() {
                ImagePicker.from(this@UserEventSaveActivity)
                        .isMutilyChoice(4 - picData.size)
                        .isCompress(true)
                        .requestCode(1)
                        .start()
            }

            override fun onItemRemove(mDatas: MutableList<MutilItem>) {
                picData.remove(addItem)
                picData.add(addItem)
                picAdapter.notifyDataSetChanged()
            }
        })
    }

    override fun onClick(v: View) {
        llYD.isSelected = false
        llZC.isSelected = false
        llSG.isSelected = false
        llJS.isSelected = false
        llYS.isSelected = false
        llGZ.isSelected = false
        when (v.id) {
            R.id.llYD -> {
                llYD.isSelected = true
                eventtype = EventType.TRAFFIC_JAM.code
            }
            R.id.llZC -> {
                llZC.isSelected = true
                eventtype = EventType.ACCIDENT.code
            }
            R.id.llSG -> {
                llSG.isSelected = true
                eventtype = EventType.CONSTRUCTION.code
            }
            R.id.llJS -> {
                llJS.isSelected = true
                eventtype = EventType.SEEPER.code
            }
            R.id.llYS -> {
                llYS.isSelected = true
                eventtype = EventType.SPILT.code
            }
            R.id.llGZ -> {
                llGZ.isSelected = true
                eventtype = EventType.CONTROL.code
            }
        }
    }

    override fun afterLocation(location: AMapLocation) {
        longitude = location.longitude
        latitude = location.latitude
        getNearbyLoad()
        closeLocation()
    }

    private fun showRoadDialog() {
        val items = arrayOfNulls<String>(roads.size)
        for (i in 0 until roads.size) {
            items[i] = roads[i].shortname
        }
        val builder = AlertDialog.Builder(this)
        builder.setSingleChoiceItems(items, currIndex) { dialog, position ->
            currIndex = position
            roads[currIndex].roadoldid?.let { roadoldid = it }
            tvLocation.text = roads[currIndex].shortname
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun getNearbyLoad() {
        doRequest(WebApiService.NEARBY_LOAD, WebApiService.nearbyLoadParams(longitude, latitude),
                object : HttpRequestCallback<String>() {
                    override fun onSuccess(data: String?) {
                        if (GsonUtils.isResultOk(data)) {
                            val mdls = GsonUtils.fromDataToList(data, RoadMDL::class.java)
                            roads.addAll(mdls)
                            if (roads.size > 0) {
                                roads[0].roadoldid?.let { roadoldid = it }
                                tvLocation.text = roads[0].shortname
                            }
                        } else {
                            showShortToast(GsonUtils.getMsg(data))
                        }
                    }

                    override fun onFailure(e: Throwable, errorMsg: String?) {
                        onHttpError(e)
                    }
                })
    }

    //取图片回调
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val items = data?.getStringArrayListExtra(ImagePicker.EXTRA_PATHS)
            items?.let { list ->
                picData.remove(addItem)
                for (item in list) picData.add(PicMDL().apply { path = item })
                if (picData.size < 3) picData.add(addItem)
                picAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun isOptionOK(): Boolean {
        if (etRemark.text.toString().trim().isEmpty()) {
            showShortToast(resources.getString(R.string.userEvent_remark_tips))
            return false
        } else if (TextUtils.isEmpty(roadoldid)) {
            showShortToast("请选择路段")
            return false
        }
        return true
    }

    private fun needUploadPic(): Boolean {
        for (item in picData) {
            return item.getItemType() == 2
        }
        return false
    }

    private fun commit() {
        if (!isOptionOK()) return
        if (needUploadPic()) {   //如果选择了图片 则先上传图片
            addDisposable(Observable.fromArray(picData)
                    .map { it ->
                        val sb = StringBuffer()
                        for (item in it) {
                            if (item.getItemType() == 2) {
                                val picItem = item as PicMDL
                                RxHttpManager.createApi(ApiService::class.java)
                                        .uploadFile(createMultipart(File(picItem.path), "file"))
                                        .subscribe { body ->
                                            val json = body?.string()
                                            if (GsonUtils.isResultOk(json)) {
                                                val imageMDL = GsonUtils.fromDataBean(json, UploadMDL::class.java)
                                                imageMDL?.imgurl?.file?.let {
                                                    sb.append("$it,")
                                                }
                                            }
                                        }
                            }
                        }
                        sb
                    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        imageUrls = it.toString()
                        endLoading()
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

    private fun lastCommit() {
        val remark = etRemark.text.toString()
        doRequest(WebApiService.SAVE_USER_EVENT, WebApiService.saveUserEventParams(getUserId(), remark,
                roadoldid, eventtype, longitude, latitude, imageUrls), object : HttpRequestCallback<String>() {
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