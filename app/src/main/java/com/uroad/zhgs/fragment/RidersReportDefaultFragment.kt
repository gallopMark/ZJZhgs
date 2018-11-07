package com.uroad.zhgs.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.amap.api.col.sln3.it
import com.amap.api.location.AMapLocation
import com.uroad.library.rxbus.RxBus
import com.uroad.library.utils.DisplayUtils
import com.uroad.rxhttp.RxHttpManager
import com.uroad.zhgs.R
import com.uroad.zhgs.adapteRv.RidersReportPicAdapter
import com.uroad.zhgs.common.BaseFragment
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.enumeration.EventType
import com.uroad.zhgs.model.*
import com.uroad.zhgs.photopicker.data.ImagePicker
import com.uroad.zhgs.photopicker.ui.ImageGridActivity
import com.uroad.zhgs.rxbus.MessageEvent
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.ApiService
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import com.uroad.zhgs.webservice.upload.FileUploadObserver
import com.uroad.zhgs.webservice.upload.UploadFileCallback
import com.uroad.zhgs.widget.GridSpacingItemDecoration
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_ridersreport_default.*
import okhttp3.ResponseBody
import java.io.File

/**
 * @author MFB
 * @create 2018/10/20
 * @describe 车友爆料（文字图片发布）
 */
class RidersReportDefaultFragment : BaseFragment(), View.OnClickListener {
    private var eventtype = EventType.TRAFFIC_JAM.code
    private var longitude: Double = CurrApplication.APP_LATLNG.longitude
    private var latitude: Double = CurrApplication.APP_LATLNG.latitude
    private val roads = ArrayList<RoadMDL>()
    private var currIndex = 0
    private var roadoldid: String = ""
    private var imageUrls: String = ""
    private val picData = ArrayList<MutilItem>()
    private val addItem = AddPicItem()
    private lateinit var picAdapter: RidersReportPicAdapter
    override fun setBaseLayoutResID(): Int = R.layout.fragment_ridersreport_default
    override fun setUp(view: View, savedInstanceState: Bundle?) {
        applyLocationPermission(true)
        llYD.isSelected = true
        initRv()
        ivPos.setOnClickListener {
            if (roads.size > 0) {
                showRoadDialog()
            } else {
                getNearbyLoad()
            }
        }
        btSubmit.setOnClickListener { commit() }
    }

    private fun initRv() {
        rvPics.addItemDecoration(GridSpacingItemDecoration(3, DisplayUtils.dip2px(context, 10f), true))
        rvPics.layoutManager = GridLayoutManager(context, 3).apply { orientation = GridLayoutManager.VERTICAL }
        picData.add(addItem)
        picAdapter = RidersReportPicAdapter(context, picData)
        rvPics.adapter = picAdapter
    }

    override fun setListener() {
        llYD.setOnClickListener(this)
        llZC.setOnClickListener(this)
        llSG.setOnClickListener(this)
        llJS.setOnClickListener(this)
        llYS.setOnClickListener(this)
        llGZ.setOnClickListener(this)
        picAdapter.setOnItemOptionListener(object : RidersReportPicAdapter.OnItemOptionListener {
            override fun onAddPic() {
                openActivityForResult(ImageGridActivity::class.java, Bundle().apply {
                    putBoolean("mMutilyMode", true)
                    putInt("limit", 4 - picData.size)
                    putBoolean("isCompress", true)
                }, 1)
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
        val builder = AlertDialog.Builder(context)
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
                            val mdLs = GsonUtils.fromDataToList(data, RoadMDL::class.java)
                            roads.addAll(mdLs)
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
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
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
        if (TextUtils.isEmpty(roadoldid)) {
            showShortToast("请选择路段")
            return false
        } else {
            if (!needUploadPic() && TextUtils.isEmpty(etRemark.text.toString())) {
                showShortToast(etRemark.hint)
                return false
            }
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
            val files = ArrayList<File>()
            for (item in picData) {
                if (item.getItemType() == 2) files.add(File((item as PicMDL).path))
            }
            uploadFiles(files, object : UploadFileCallback() {
                override fun onStart(disposable: Disposable) {
                    showLoading("正在上传图片…")
                }

                override fun onSuccess(json: String) {
                    endLoading()
                    imageUrls = json
                    lastCommit()
                }

                override fun onFailure(e: Throwable) {
                    endLoading()
                    onHttpError(e)
                }
            })
        } else {
            lastCommit()
        }
    }

    private fun lastCommit() {
        val remark = etRemark.text.toString()
        doRequest(WebApiService.SAVE_USER_EVENT, WebApiService.saveUserEventParams(getUserId(), remark,
                roadoldid, eventtype, longitude, latitude, imageUrls, 1, ""), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading("正在提交…")
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    showShortToast("提交成功")
                    RxBus.getDefault().post(MessageEvent())
                    Handler().postDelayed({ if (!context.isFinishing) context.finish() }, 1500)
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