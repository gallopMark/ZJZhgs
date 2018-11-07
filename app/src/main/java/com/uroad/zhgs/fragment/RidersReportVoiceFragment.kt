package com.uroad.zhgs.fragment

import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.View
import com.amap.api.location.AMapLocation
import com.uroad.library.rxbus.RxBus
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseFragment
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.dialog.MaterialDialog
import com.uroad.zhgs.enumeration.EventType
import com.uroad.zhgs.model.RoadMDL
import com.uroad.zhgs.model.UploadMDL
import com.uroad.zhgs.recorder.AudioButton
import com.uroad.zhgs.rxbus.MessageEvent
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.utils.TimeUtil
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import com.uroad.zhgs.webservice.upload.UploadFileCallback
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_ridersreport_voice.*
import java.io.File

/**
 * @author MFB
 * @create 2018/10/20
 * @describe 车友爆料（发送语音）
 */
class RidersReportVoiceFragment : BaseFragment(), AudioButton.OnRecordListener, View.OnClickListener {
    private var eventtype = EventType.TRAFFIC_JAM.code
    private var longitude: Double = CurrApplication.APP_LATLNG.longitude
    private var latitude: Double = CurrApplication.APP_LATLNG.latitude
    private val roads = ArrayList<RoadMDL>()
    private var currIndex = 0
    private var roadoldid: String = ""
    private val normalTips = "点击开始录音,再次点击结束录音"
    private lateinit var animationDrawable: AnimationDrawable
    private var voiceFile: File? = null
    private var seconds: Int = 0
    private var imgUrl: String? = null
    private var mediaPlayer: MediaPlayer? = null
    override fun setBaseLayoutResID(): Int = R.layout.fragment_ridersreport_voice
    override fun setUp(view: View, savedInstanceState: Bundle?) {
        applyLocationPermission(true)
        audioButton.setOnRecordListener(this)
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            audioButton.setHasRecordPermission(true)
        }
        init()
    }

    private fun init() {
        animationDrawable = ivVoice.drawable as AnimationDrawable
        tvRecordTime.text = TimeUtil.milliSecond2Second(0L)
        tvRecordTips.text = normalTips
        audioButton.setMinRecordTime(3)
        audioButton.setMaxRecordTime(20)
        llYD.isSelected = true
        ivPos.setOnClickListener {
            if (roads.size > 0) {
                showRoadDialog()
            } else {
                getNearbyLoad()
            }
        }
    }

    override fun setListener() {
        llYD.setOnClickListener(this)
        llZC.setOnClickListener(this)
        llSG.setOnClickListener(this)
        llJS.setOnClickListener(this)
        llYS.setOnClickListener(this)
        llGZ.setOnClickListener(this)
        tvReset.setOnClickListener { onReset() }
        ivPlayVoice.setOnClickListener { playVoice() }
        tvSubmit.setOnClickListener { onSubmit() }
    }

    private fun onReset() {
        tvRecordTime.text = TimeUtil.milliSecond2Second(0L)
        tvRecordTips.text = normalTips
        voiceFile = null
        seconds = 0
        imgUrl = null
        audioButton.cancel()
        llBottom.visibility = View.INVISIBLE
        ivVoice.visibility = View.VISIBLE
        audioButton.visibility = View.VISIBLE
        ivPlayVoice.visibility = View.GONE
    }

    private fun playVoice() {
        mediaPlayer?.let {
            it.stop()
            it.release()
        }
        mediaPlayer = MediaPlayer().apply {
            try {
                reset()
                setDataSource(voiceFile?.absolutePath)
                prepare()
                start()
            } catch (e: Exception) {
            }
        }
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

    override fun onDismissPermission() {
        requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), 10)
    }

    override fun onRecording(level: Int, seconds: Float, remainTime: Int) {
        animationDrawable.start()
        tvRecordTime.text = TimeUtil.milliSecond2Second((seconds * 1000).toLong())
        if (remainTime < 5) {
            val remind = "您还可以说${remainTime}秒"
            tvRecordTips.text = remind
        }
    }

    override fun onFinished(seconds: Float, filePath: String?) {
        stopAnim()
        if (filePath == null) {
            showShortToast("录制失败")
            onReset()
        } else {
            this.seconds = seconds.toInt()
            voiceFile = File(filePath)
            tvRecordTime.text = TimeUtil.milliSecond2Second((seconds * 1000).toLong())
            ivVoice.visibility = View.INVISIBLE
            audioButton.visibility = View.INVISIBLE
            ivPlayVoice.visibility = View.VISIBLE
            llBottom.visibility = View.VISIBLE
        }
    }

    override fun onRecordFailure(minRecordTime: Int, tooShort: Boolean) {
        showShortToast("录音时长不能低于${minRecordTime}秒")
        stopAnim()
    }

    private fun stopAnim() {
        animationDrawable.stop()
        animationDrawable.selectDrawable(0)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                audioButton.setHasRecordPermission(true)
            } else if (!shouldShowRequestPermissionRationale(android.Manifest.permission.RECORD_AUDIO)) {
                val dialog = MaterialDialog(context)
                dialog.setTitle(getString(R.string.dialog_default_title))
                dialog.setMessage("麦克风权限已被禁止，无法正常录音，请到设置——权限管理中开启“麦克风”")
                dialog.setNegativeButton(getString(R.string.dialog_button_cancel), object : MaterialDialog.ButtonClickListener {
                    override fun onClick(v: View, dialog: android.app.AlertDialog) {
                        dialog.dismiss()
                    }
                })
                dialog.setPositiveButton(getString(R.string.reopen), object : MaterialDialog.ButtonClickListener {
                    override fun onClick(v: View, dialog: android.app.AlertDialog) {
                        dialog.dismiss()
                        openSettings()
                    }
                })
                dialog.show()
            }
        }
    }

    private fun onSubmit() {
        when {
            TextUtils.isEmpty(roadoldid) -> showShortToast("请选择路段")
            voiceFile == null -> showShortToast("请先录制语音")
            else -> submit()
        }
    }

    private fun submit() {
        val url = imgUrl
        if (url == null) {
            uploadVoice()
        } else {
            lastCommit(url)
        }
    }

    /*上传语音文件*/
    private fun uploadVoice() {
        doUpload(voiceFile, "file", object : UploadFileCallback() {
            override fun onStart(disposable: Disposable) {
                showLoading("上传语音文件…")
            }

            override fun onSuccess(json: String) {
                endLoading()
                if (GsonUtils.isResultOk(json)) {
                    val imageMDL = GsonUtils.fromDataBean(json, UploadMDL::class.java)
                    imageMDL?.imgurl?.file?.let {
                        imgUrl = it
                        lastCommit(it)
                    }
                } else {
                    showShortToast(GsonUtils.getMsg(json))
                }
            }

            override fun onFailure(e: Throwable) {
                endLoading()
                onHttpError(e)
            }
        })
    }

    private fun lastCommit(url: String) {
        doRequest(WebApiService.SAVE_USER_EVENT, WebApiService.saveUserEventParams(getUserId(), "", roadoldid, eventtype, longitude, latitude, url, 3, seconds.toString()), object : HttpRequestCallback<String>() {
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

    override fun onDestroyView() {
        mediaPlayer?.let {
            it.stop()
            it.release()
            mediaPlayer = null
        }
        audioButton.cancel()
        super.onDestroyView()
    }
}