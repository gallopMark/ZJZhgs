package com.uroad.zhgs.activity

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.util.ArrayMap
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXTextObject
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.library.utils.DisplayUtils
import com.uroad.library.widget.CircleImageView
import com.uroad.mqtt.IMqttCallBack
import com.uroad.mqtt.MqttService
import com.uroad.share.tencent.QQShareManager
import com.uroad.zhgs.R
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.common.ThemeStyleLocationActivity
import com.uroad.zhgs.dialog.RidersInvitingDialog
import com.uroad.zhgs.dialog.MaterialDialog
import com.uroad.zhgs.dialog.RidersTokenInvitingDialog
import com.uroad.zhgs.model.RidersDetailMDL
import com.uroad.zhgs.model.UploadMDL
import com.uroad.zhgs.model.mqtt.*
import com.uroad.zhgs.recorder.TouchAudioButton
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.ApiService
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import com.uroad.zhgs.webservice.upload.UploadFileCallback
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_riders_detail.*
import kotlinx.android.synthetic.main.layout_theme_style_toolbar.*
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer

/**
 * @author MFB
 * @create 2018/10/18
 * @describe 车队聊天页面
 */
class RidersDetailActivity : ThemeStyleLocationActivity() {
    private lateinit var aMap: AMap
    private var location: AMapLocation? = null
    private var toPlaceMarker: Marker? = null  //目的地marker
    private var teamId: String? = null
    private var teamName: String? = null
    private var toPlace: String? = null
    private var latLng: LatLng? = null
    private var detailMDL: RidersDetailMDL? = null
    private var isTeamHeader = false  //是否是队长
    private val members = ArrayList<RidersDetailMDL.TeamMember>()
    private lateinit var mqttService: MqttService
    private val msgDatas = ArrayList<TeamSendMsgMDL>()
    private var mediaPlayer: IjkMediaPlayer? = null
    private var playerOnPause = false
    private val markerMap = ArrayMap<String, Marker>()
    private var animMarker: Marker? = null
    private var msgTarget: TeamSendMsgMDL? = null
    private var isOpenNav = false
    private lateinit var handler: Handler
    private var mWXApi: IWXAPI? = null

    override fun themeSetUp(savedInstanceState: Bundle?) {
        setLayoutResID(R.layout.activity_riders_detail)
        intent.extras?.let { teamId = intent.extras?.getString("teamId") }
        tvEdit.visibility = View.GONE
        initToolbar()
        mapView.onCreate(savedInstanceState)
        initMapView()
        initMQTT()
        initAudioButton()
        applyLocation()
    }

    private fun initToolbar() {
        themeToolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.settings) {
                detailMDL?.team_data?.let {
                    openActivity(RidersSettingsActivity::class.java, Bundle().apply {
                        putString("teamId", teamId)
                        putString("teamName", teamName)
                        putString("intoken", it.intoken)
                        putBoolean("isTeamHeader", isTeamHeader)
                    })
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun initMapView() {
        handler = Handler(Looper.getMainLooper())
        aMap = mapView.map.apply { moveCamera(CameraUpdateFactory.newLatLngZoom(CurrApplication.APP_LATLNG, this.cameraPosition.zoom)) }
        aMap.setInfoWindowAdapter(object : AMap.InfoWindowAdapter {
            override fun getInfoContents(marker: Marker): View? = null

            override fun getInfoWindow(marker: Marker): View? {
                if (marker.`object` != null) {
                    val member = marker.`object` as RidersDetailMDL.TeamMember
                    val view: View = drawView(member)
                    view.findViewById<ImageView>(R.id.ivPosArrow).visibility = View.GONE
                    val imageView = view.findViewById<CircleImageView>(R.id.ivIcon)
                    ImageLoaderV4.getInstance().displayCircleImage(this@RidersDetailActivity, member.iconfile, imageView, R.color.blow_gray)
                    val lottieView = view.findViewById<LottieAnimationView>(R.id.lottieView)
                    lottieView.visibility = View.VISIBLE
                    lottieView.playAnimation()
                    return view
                }
                return null
            }
        })
        aMap.setOnMarkerClickListener { return@setOnMarkerClickListener true }
        aMap.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
            override fun onCameraChange(position: CameraPosition) {
                handler.removeCallbacks(runnable)
            }

            override fun onCameraChangeFinish(position: CameraPosition) {
                handler.postDelayed(runnable, 10 * 1000L)
            }
        })
    }

    private val runnable = Runnable {
        val builder = LatLngBounds.builder()
        toPlaceMarker?.let { builder.include(it.position) }
        for ((_, marker) in markerMap) {
            builder.include(marker.position)
        }
        aMap.animateCamera(CameraUpdateFactory.newLatLngBoundsRect(builder.build(), DisplayUtils.dip2px(this, 70f), DisplayUtils.dip2px(this, 50f), DisplayUtils.dip2px(this, 180f), DisplayUtils.dip2px(this, 180f)))
    }

    private fun initMQTT() {
        mqttService = ApiService.buildMQTTService(this).apply {
            connect(object : IMqttCallBack {
                override fun messageArrived(topic: String?, message: String?, qos: Int) {
                    when (topic) {
                        "${ApiService.TOPIC_ADD_TEAM}$teamId" -> dealWithMsgByType(1, message) //加入车队
                        "${ApiService.TOPIC_QUIT_TEAM}$teamId" -> dealWithMsgByType(2, message)
                        "${ApiService.TOPIC_CLOSE_TEAM}$teamId" -> dealWithMsgByType(3, message)
                        "${ApiService.TOPIC_SEND_MSG}$teamId" -> dealWithMsgByType(4, message)
                        "${ApiService.TOPIC_LATLNG_UPDATE}$teamId" -> dealWithMsgByType(5, message)
                        "${ApiService.TOPIC_PLACE_UPDATE}$teamId" -> dealWithMsgByType(6, message)
                        "${ApiService.TOPIC_MSG_CALLBACK}$teamId" -> dealWithMsgByType(7, message)
                    }
                }

                override fun connectionLost(throwable: Throwable?) {
                }

                override fun deliveryComplete(deliveryToken: IMqttDeliveryToken?) {

                }

                override fun connectSuccess(token: IMqttToken?) {
                    subscribeTopic()
                }

                override fun connectFailed(token: IMqttToken?, throwable: Throwable?) {
                }
            })
        }
    }

    /*订阅消息主题*/
    private fun subscribeTopic() {
        val topics = arrayOf("${ApiService.TOPIC_ADD_TEAM}$teamId",
                "${ApiService.TOPIC_SEND_MSG}$teamId",
                "${ApiService.TOPIC_LATLNG_UPDATE}$teamId",
                "${ApiService.TOPIC_QUIT_TEAM}$teamId",
                "${ApiService.TOPIC_CLOSE_TEAM}$teamId",
                "${ApiService.TOPIC_PLACE_UPDATE}$teamId",
                "${ApiService.TOPIC_MSG_CALLBACK}$teamId/${getUserId()}")
        mqttService.subscribe(topics, intArrayOf(1, 1, 1, 1, 1, 1, 1))
    }

    /*更新消息主题对消息进行处理*/
    private fun dealWithMsgByType(type: Int, message: String?) {
        when (type) {
            1 -> {   //加入车队
                val mdl = GsonUtils.fromJsonToObject(message, AddTeamMDL::class.java)
                mdl?.let {
                    val member = RidersDetailMDL.TeamMember().apply {
                        this.iconfile = mdl.usericon
                        this.userid = mdl.userid
                        this.username = mdl.username
                        this.longitude = mdl.longitude
                        this.latitude = mdl.latitude
                        this.isown = 0
                    }
                    var containMember = false
                    for (m in members) {
                        if (TextUtils.equals(m.userid, member.userid)) {
                            containMember = true
                            break
                        }
                    }
                    if (!containMember) {
                        members.add(member)
                        updateTitle()
                        addMarker(member)
                    }
                }
            }
            2 -> {  //退出车队
                val mdl = GsonUtils.fromJsonToObject(message, QuitTeamMDL::class.java)
                if (TextUtils.equals(mdl?.userid, getUserId())) {
                    finish()
                } else {
                    mdl?.username?.let { showShortToast("$it\u2000已退出") }
                }
            }
            3 -> { //解散车队
                if (isTeamHeader) finish()
                else onCloseTeamTips()
            }
            4 -> { //发送语音消息
                val mdl = GsonUtils.fromJsonToObject(message, TeamSendMsgMDL::class.java)
                mdl?.let { msgMDL ->
                    if (!TextUtils.equals(msgMDL.userid, getUserId())) {
                        msgDatas.add(msgMDL)
                        playVoice()
                    }
                }
            }
            5 -> { // 成员位置发送变动后需要通知车队其他人员更新信息
                val mdl = GsonUtils.fromJsonToObject(message, TeamLocUpdateMDL::class.java)
                mdl?.let {
                    val index = members.indexOf(RidersDetailMDL.TeamMember().apply { this.userid = it.userid })
                    if (index in 0 until members.size) {
                        val member = members[index]
                        member.longitude = it.longitude
                        member.latitude = it.latitude
                        addMarker(member)
                    }
                }
            }
            6 -> {  //修改车队信息处理
                val mdl = GsonUtils.fromJsonToObject(message, TeamPlaceUpdateMDL::class.java)
                mdl?.let {
                    this@RidersDetailActivity.teamName = it.teamname
                    this@RidersDetailActivity.toPlace = it.toplace
                    this@RidersDetailActivity.latLng = it.getLatLng()
                    updateTitle()
                    updatePlace()
                    updateAMap(it.getLatLng())
                }
            }
        }
    }

    /*车队解散弹窗提示*/
    private fun onCloseTeamTips() {
        val dialog = MaterialDialog(this)
        dialog.setTitle(getString(R.string.dialog_default_title))
        dialog.setMessage(getString(R.string.riders_dissolution_tips))
        dialog.hideDivider()
        dialog.setPositiveButton(getString(R.string.i_got_it), object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                dialog.dismiss()
            }
        })
        dialog.setOnDismissListener { finish() }
        dialog.show()
    }

    /*播放录音*/
    private fun playVoice() {
        if (isOpenNav || msgDatas.size == 0) return   //打开了导航页面，不播放语音
        msgTarget = msgDatas[0]
        if (mediaPlayer == null) {
            mediaPlayer = IjkMediaPlayer().apply {
                reset()
                dataSource = msgTarget?.voicefile
            }
        } else {
            mediaPlayer?.let {
                when {
                    it.isPlaying -> return
                    else -> {
                        it.reset()
                        it.dataSource = msgTarget?.voicefile
                    }
                }
            }
        }
        try {
            mediaPlayer?.let {
                it.prepareAsync()
                it.setOnPreparedListener(mOnPreparedListener)
                it.setOnInfoListener(mOnInfoListener)
                it.setOnCompletionListener(mOnCompleteListener)
                it.setOnErrorListener(mOnErrorListener)
            }
        } catch (e: Exception) {
        }
    }

    private val mOnPreparedListener = IMediaPlayer.OnPreparedListener { it.start() }
    private val mOnInfoListener = IMediaPlayer.OnInfoListener { _, _, _ ->
        msgTarget?.let { renderMarker(it.userid) }
        return@OnInfoListener true
    }

    private val mOnCompleteListener = IMediaPlayer.OnCompletionListener { onCompleteOrError() }

    private val mOnErrorListener = IMediaPlayer.OnErrorListener { _, _, _ ->
        onCompleteOrError()
        return@OnErrorListener true
    }

    private fun onCompleteOrError() {
        msgTarget?.let { recyclerMarker(it.userid) }
        if (msgDatas.size > 0) {
            msgDatas.removeAt(0)
            if (msgDatas.size > 0) {
                playVoice()
            }
        }
    }

    private fun renderMarker(userId: String?) {
        val index = members.indexOf(RidersDetailMDL.TeamMember().apply { this.userid = userId })
        if (index in 0 until members.size) {
            val member = members[index]
            val marker = markerMap[member.userid]
            if (marker != null) {
                marker.isVisible = false
                val view = LayoutInflater.from(this).inflate(R.layout.mapview_riders_arrow, LinearLayout(this), true)
                if (member.isown == 1) view.findViewById<ImageView>(R.id.ivPosArrow).setImageResource(R.mipmap.ic_riders_captain_pos)
                else view.findViewById<ImageView>(R.id.ivPosArrow).setImageResource(R.mipmap.ic_riders_member_pos)
                val options = MarkerOptions()
                        .anchor(0.5f, 1f)
                        .title(member.username)
                        .position(marker.position)
                        .setInfoWindowOffset(0, DisplayUtils.dip2px(this, 4f))
                        .visible(true)
                        .infoWindowEnable(true)
                        .setFlat(true)
                        .draggable(false)
                        .icon(BitmapDescriptorFactory.fromView(view))
                animMarker = aMap.addMarker(options)
                animMarker?.`object` = member
                animMarker?.showInfoWindow()
            }
        }
    }

    private fun recyclerMarker(userid: String?) {
        markerMap[userid]?.isVisible = true
        animMarker?.let {
            it.remove()
            it.destroy()
        }
        animMarker = null
        msgTarget = null
    }

    private fun initAudioButton() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            audioButton.setHasRecordPermission(true)
        }
        lottieView.playAnimation()
        audioButton.setOnRecordListener(object : TouchAudioButton.RecordListener {

            override fun onDismissPermission() {
                ActivityCompat.requestPermissions(this@RidersDetailActivity, arrayOf(android.Manifest.permission.RECORD_AUDIO), 10)
            }

            override fun onRecording(level: Int, seconds: Float, remainTime: Int) {
                playAnim()
            }

            override fun onFinished(seconds: Float, filePath: String?) {
                stopAnim()
                sendMsg(filePath)
            }

            override fun onRecordFailure(minRecordTime: Int, tooShort: Boolean) {
                stopAnim()
                if (tooShort) showShortToast("录制时间太短，请重新录制")
            }
        })
    }

    private fun playAnim() {
        ivVoice.visibility = View.GONE
        if (lottieView.visibility != View.VISIBLE) lottieView.visibility = View.VISIBLE
        if (!lottieView.isAnimating) lottieView.playAnimation()
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                playerOnPause = true
            }
        }
        msgTarget?.let { recyclerMarker(it.userid) }
    }

    private fun stopAnim() {
        ivVoice.visibility = View.VISIBLE
        lottieView.visibility = View.GONE
        lottieView.cancelAnimation()
        if (playerOnPause) mediaPlayer?.start()
        playerOnPause = false
    }

    /*发送语音文件*/
    private fun sendMsg(filePath: String?) {
        doUpload(filePath, "file", object : UploadFileCallback() {
            override fun onStart(disposable: Disposable) {
                showLoading("正在发送…")
            }

            override fun onSuccess(json: String) {
                endLoading()
                if (GsonUtils.isResultOk(json)) {
                    val imageMDL = GsonUtils.fromDataBean(json, UploadMDL::class.java)
                    imageMDL?.imgurl?.file?.let {
                        val mdl = TeamSendMsgMDL().apply {
                            teamid = this@RidersDetailActivity.teamId
                            userid = getUserId()
                            username = getUserName()
                            voicefile = it
                        }
                        mqttService.publish("${ApiService.TOPIC_SEND_MSG}$teamId", mdl.obtainMessage())
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

    private fun applyLocation() {
        requestLocationPermissions(object : RequestLocationPermissionCallback {
            override fun doAfterGrand() {
                openLocation(AMapLocationClientOption().apply {
                    interval = 5 * 1000L
                    locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                })
            }

            override fun doAfterDenied() {
                val dialog = MaterialDialog(this@RidersDetailActivity)
                dialog.setTitle(getString(R.string.dialog_default_title))
                dialog.setMessage(getString(R.string.dismiss_location_message))
                dialog.setPositiveButton(getString(R.string.reopen), object : MaterialDialog.ButtonClickListener {
                    override fun onClick(v: View, dialog: AlertDialog) {
                        applyLocation()
                    }
                })
                dialog.setNegativeButton(getString(R.string.dialog_button_cancel), object : MaterialDialog.ButtonClickListener {
                    override fun onClick(v: View, dialog: AlertDialog) {
                        dialog.dismiss()
                        finish()
                    }
                })
                dialog.setCancelable(false)
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                audioButton.setHasRecordPermission(true)
            } else if (permissions.isNotEmpty() && !ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                val dialog = MaterialDialog(this)
                dialog.setTitle(getString(R.string.dialog_default_title))
                dialog.setMessage("录音权限已被禁止，无法正常录音，请重新打开")
                dialog.setNegativeButton(getString(R.string.dialog_button_cancel), object : MaterialDialog.ButtonClickListener {
                    override fun onClick(v: View, dialog: AlertDialog) {
                        dialog.dismiss()
                    }
                })
                dialog.setPositiveButton(getString(R.string.reopen), object : MaterialDialog.ButtonClickListener {
                    override fun onClick(v: View, dialog: AlertDialog) {
                        dialog.dismiss()
                        openSettings()
                    }
                })
                dialog.show()
            }
        }
    }

    /*车队详情*/
    override fun initData() {
        doRequest(WebApiService.CAR_TEAM_DETAIL, WebApiService.getCarTeamDataParams(teamId), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, RidersDetailMDL::class.java)
                    val teamData = mdl?.team_data
                    if (teamData == null) {
                        errorTips()
                    } else {
                        updateUI(mdl)
                    }
                } else {
                    errorTips()
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                onError(errorMsg)
            }
        })
    }

    private fun errorTips() {
        val dialog = MaterialDialog(this)
        dialog.setTitle(getString(R.string.dialog_default_title))
        dialog.setMessage("车队不存在或已解散")
        dialog.hideDivider()
        dialog.setPositiveButton(getString(R.string.i_got_it), object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                dialog.dismiss()
            }
        })
        dialog.show()
        dialog.setOnDismissListener { finish() }
    }

    private fun updateUI(mdl: RidersDetailMDL) {
        detailMDL = mdl
        toPlace = mdl.team_data?.toplace
        teamName = mdl.team_data?.teamname
        latLng = mdl.team_data?.getLatLng()
        mdl.teammember?.let { members.addAll(it) }
        updateTitle()
        updatePlace()
        updateMembers()
        mdl.team_data?.let { updateAMap(it.getLatLng()) }
    }

    /*更新标题*/
    private fun updateTitle() {
        var title = ""
        teamName?.let { title += it }
        if (!TextUtils.isEmpty(title)) title += "/${members.size}人"
        setThemeTitle(title)
    }

    /*更新目的地*/
    private fun updatePlace() {
        tvDestination.text = toPlace
    }

    private fun updateMembers() {
        for (member in members) {
            /*判断当前用户是否是队长*/
            if (member.isown == 1 && TextUtils.equals(member.userid, getUserId())) {
                isTeamHeader = true
                tvEdit.visibility = View.VISIBLE   //队长才可以修改
                break
            }
        }
        for (member in members) addMarker(member)
    }

    private fun updateAMap(latLng: LatLng) {
        toPlaceMarker?.let {
            it.remove()
            it.destroy()
        }
        val options = MarkerOptions()
                .anchor(0.5f, 1f)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_riders_target))
                .position(latLng)
                .visible(true)
                .infoWindowEnable(false)
        toPlaceMarker = aMap.addMarker(options)
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, aMap.cameraPosition.zoom))
    }

    private fun addMarker(member: RidersDetailMDL.TeamMember) {
        if (member.getLatLng() == null) return
        val containMarker = markerMap[member.userid]
        if (containMarker != null) {
            drawMarker(member)
        } else {
            val options = createOptions(member.username, member.getLatLng(), render(member))
            val marker = aMap.addMarker(options)
            marker.isClickable = false
            markerMap[member.userid] = marker
            drawMarker(member)
        }
        handler.postDelayed(runnable, 500)
    }

    private fun render(member: RidersDetailMDL.TeamMember): View {
        val view = drawView(member)
        view.findViewById<CircleImageView>(R.id.ivIcon).setImageResource(R.mipmap.ic_user_default)
        return view
    }

    private fun drawMarker(member: RidersDetailMDL.TeamMember) {
        Glide.with(this).load(member.iconfile)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).circleCrop())
                .into(object : SimpleTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        val view = drawView(member)
                        val imageView = view.findViewById<CircleImageView>(R.id.ivIcon)
                        imageView.setImageDrawable(resource)
                        markerMap[member.userid]?.let {
                            it.position = member.getLatLng()
                            it.setIcon(BitmapDescriptorFactory.fromView(view))
                        }
                    }
                })
    }

    private fun drawView(member: RidersDetailMDL.TeamMember): View {
        val view: View
        if (member.isown == 1) {
            view = LayoutInflater.from(this).inflate(R.layout.mapview_riders_captain_pos, LinearLayout(this), false)
            view.findViewById<TextView>(R.id.tvCaptain).text = "队长"
        } else {
            view = LayoutInflater.from(this).inflate(R.layout.mapview_riders_member_pos, LinearLayout(this), false)
        }
        view.findViewById<TextView>(R.id.tvName).text = member.username
        return view
    }

    private fun createOptions(title: String?, latLng: LatLng?, view: View): MarkerOptions {
        return MarkerOptions()
                .anchor(0.5f, 1f)
                .title(title)
                .position(latLng)
                .visible(true)
                .infoWindowEnable(false)
                .draggable(false)
                .icon(BitmapDescriptorFactory.fromView(view))
    }

    /*加载失败弹窗*/
    private fun onError(errorMsg: String?) {
        val dialog = MaterialDialog(this)
        dialog.setTitle(getString(R.string.dialog_default_title))
        dialog.setMessage(errorMsg)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setNegativeButton(getString(R.string.dialog_button_cancel), object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                dialog.dismiss()
                finish()
            }
        })
        dialog.setPositiveButton("", object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                dialog.dismiss()
                initData()
            }
        })
        dialog.show()
    }

    override fun setListener() {
        ivInvitation.setOnClickListener {
            detailMDL?.team_data?.let { data ->
                RidersInvitingDialog(this).token(data.intoken).viewClickListener(object : RidersInvitingDialog.OnViewClickListener {
                    override fun onViewClick(type: Int, dialogRiders: RidersInvitingDialog) {
                        when (type) {
                            1 -> {
                                dialogRiders.dismiss()
                                copyToken()
                            }
                            else -> {
                                openActivity(RidersInvitingActivity::class.java, Bundle().apply { putString("teamId", teamId) })
                                dialogRiders.dismiss()
                            }
                        }
                    }
                }).show()
            }
        }
        ivLocation.setOnClickListener { location?.let { location -> aMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(LatLng(location.latitude, location.longitude), aMap.cameraPosition.zoom, 0f, 0f))) } }
        tvEdit.setOnClickListener {
            detailMDL?.team_data?.let {
                val bundle = Bundle().apply {
                    putBoolean("isModify", true)
                    putString("teamId", teamId)
                    putString("teamName", teamName)
                    putString("destination", toPlace)
                    putParcelable("latLng", latLng)
                }
                openActivity(RidersEditActivity::class.java, bundle)
            }
        }
        tvNavigation.setOnClickListener {
            detailMDL?.team_data?.let { data ->
                isOpenNav = true
                val end = Poi(data.toplace, data.getLatLng(), "")
                openNaviPage(null, end)
            }
        }
    }

    /*复制口令*/
    private fun copyToken() {
        detailMDL?.team_data?.intoken?.let {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.primaryClip = ClipData.newPlainText("text", it)
        }
        RidersTokenInvitingDialog(this).token(detailMDL?.team_data?.token_text).onViewClickListener(object : RidersTokenInvitingDialog.OnViewClickListener {
            override fun onViewClick(type: Int, dialog: RidersTokenInvitingDialog) {
                when (type) {
                    1 -> {    //分享至微信
                        dialog.dismiss()
                        detailMDL?.team_data?.token_text?.let { shareToWeChat(it) }
//                        WechatShareManager.from(this@RidersDetailActivity, getString(R.string.WECHAT_APP_ID))
//                                .scene(WechatShareManager.WECHAT_SHARE_TYPE_TALK)
//                                .shareByWebchat(WXShareText(detailMDL?.team_data?.token_text))
                    }
                    else -> {  //分享至QQ
                        dialog.dismiss()
                        detailMDL?.team_data?.token_text?.let { shareToQQ(it) }
                    }
                }
            }
        }).show()
    }

    private fun shareToWeChat(text: String?) {
        val message = WXMediaMessage(WXTextObject().apply { this.text = text }).apply { this.description = text }
        mWXApi = WXAPIFactory.createWXAPI(this, getString(R.string.WECHAT_APP_ID)).apply { registerApp(getString(R.string.WECHAT_APP_ID)) }.apply {
            this.sendReq(SendMessageToWX.Req().apply {
                this.scene = SendMessageToWX.Req.WXSceneSession
                this.transaction = "text${System.currentTimeMillis()}"
                this.message = message
            })
        }
    }

    private fun shareToQQ(text: String?) {
        QQShareManager.shareTextToQQ(this@RidersDetailActivity, text)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_riders_settings, menu)
        return true
    }

    override fun afterLocation(location: AMapLocation) {
        this.location = location
        detailMDL?.team_data?.let {
            val mdl = TeamLocUpdateMDL().apply {
                this.teamid = this@RidersDetailActivity.teamId
                userid = getUserId()
                username = getUserName()
                latitude = location.latitude
                longitude = location.longitude
            }
            mqttService.publish("${ApiService.TOPIC_LATLNG_UPDATE}$teamId", mdl.obtainMessage())
        }
    }

    override fun onLocationFail(errorInfo: String?) {
        Handler().postDelayed({ if (!isFinishing) openLocation() }, 500)
    }

    override fun onResume() {
        mapView.onResume()
        isOpenNav = false
        super.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mWXApi?.unregisterApp()
        mapView.onDestroy()
        mqttService.disconnect()
        mediaPlayer?.let {
            it.stop()
            it.release()
            mediaPlayer = null
        }
        super.onDestroy()
    }
}