package com.uroad.zhgs.fragment

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import com.amap.api.location.AMapLocation
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.library.rxbus.RxBus
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.activity.VideoActivity
import com.uroad.zhgs.common.BaseFragment
import com.uroad.zhgs.common.BaseLocationFragment
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.enumeration.EventType
import com.uroad.zhgs.model.RoadMDL
import com.uroad.zhgs.rxbus.MessageEvent
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import com.uroad.zhgs.webservice.upload.UploadFileCallback
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_ridersreport_video.*
import java.io.File

/**
 * @author MFB
 * @create 2018/10/20
 * @describe 车友爆料（发送语音、文字）
 */
class RidersReportVideoFragment : BaseLocationFragment(), View.OnClickListener {
    private var eventtype = EventType.TRAFFIC_JAM.code
    private var longitude: Double = CurrApplication.APP_LATLNG.longitude
    private var latitude: Double = CurrApplication.APP_LATLNG.latitude
    private val roads = ArrayList<RoadMDL>()
    private var currIndex = 0
    private var roadoldid: String = ""
    private val files = ArrayList<File>()
    private var imageUrls: String? = null
    override fun setBaseLayoutResID(): Int = R.layout.fragment_ridersreport_video

    override fun setUp(view: View, savedInstanceState: Bundle?) {
        applyLocationPermission(true)
        init()
    }

    private fun init() {
        val width = (DisplayUtils.getWindowWidth(context) * 0.4).toInt()
        val height = (width * 1.3).toInt()
        flVideo.layoutParams = (flVideo.layoutParams as LinearLayout.LayoutParams).apply {
            this.width = width
            this.height = height
        }
        llYD.isSelected = true
        val url = arguments?.getString("url")
        val firstFrame = arguments?.getString("firstFrame")
        url?.let { files.add(File(it)) }
        firstFrame?.let { files.add(File(it)) }
        ImageLoaderV4.getInstance().displayImage(context, url, ivThumb)
        flVideo.setOnClickListener { openActivity(VideoActivity::class.java, Bundle().apply { putString("url", url) }) }
        ivPos.setOnClickListener {
            if (roads.size > 0) {
                showRoadDialog()
            } else {
                getNearbyLoad()
            }
        }
        btSubmit.setOnClickListener { onCommit() }
    }

    override fun setListener() {
        llYD.setOnClickListener(this)
        llZC.setOnClickListener(this)
        llSG.setOnClickListener(this)
        llJS.setOnClickListener(this)
        llYS.setOnClickListener(this)
        llGZ.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        llYD.isSelected = false
        llZC.isSelected = false
        llSG.isSelected = false
        llJS.isSelected = false
        llYS.isSelected = false
        llGZ.isSelected = false
        when (view.id) {
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

    private fun onCommit() {
        if (TextUtils.isEmpty(roadoldid)) {
            showShortToast("请选择路段")
        } else {
            commit()
        }
    }

    private fun commit() {
        val urls = imageUrls
        if (urls == null) {
            uploadFiles(files, object : UploadFileCallback() {
                override fun onStart(disposable: Disposable) {
                    showLoading("上传视频文件…")
                }

                override fun onSuccess(json: String) {
                    endLoading()
                    imageUrls = json
                    lastCommit(json)
                }

                override fun onFailure(e: Throwable) {
                    endLoading()
                    onHttpError(e)
                }
            })
        } else {
            lastCommit(urls)
        }
    }

    private fun lastCommit(imageUrls: String) {
        val urls = imageUrls.split(",").toTypedArray()
        if (urls.size >= 2) {
            val remark = etRemark.text.toString()
            doRequest(WebApiService.SAVE_USER_EVENT, WebApiService.saveUserEventParams(getUserId(), remark, roadoldid, eventtype, longitude, latitude, urls[0], 2, urls[1]), object : HttpRequestCallback<String>() {
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
        } else {
            showShortToast("视频上传异常，请稍后再试")
            return
        }
    }

    override fun onDestroyView() {
        for (file in files) file.delete()
        super.onDestroyView()
    }
}