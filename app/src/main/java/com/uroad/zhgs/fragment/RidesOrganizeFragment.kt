package com.uroad.zhgs.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import com.amap.api.location.AMapLocation
import com.uroad.mqtt.IMqttCallBack
import com.uroad.zhgs.R
import com.uroad.zhgs.activity.RidersEditActivity
import com.uroad.zhgs.activity.RidersDetailActivity
import com.uroad.zhgs.common.BaseLocationFragment
import com.uroad.zhgs.dialog.RidersAgreementDialog
import com.uroad.zhgs.enumeration.NewsType
import com.uroad.zhgs.helper.AppLocalHelper
import com.uroad.zhgs.model.HtmlMDL
import com.uroad.zhgs.model.RidersDetailMDL
import com.uroad.zhgs.model.RidersMsgMDL
import com.uroad.zhgs.model.mqtt.AddTeamMDL
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.ApiService
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.fragment_riders_organize.*
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken

/**
 * @author MFB
 * @create 2018/10/9
 * @describe 车友组队
 */
class RidesOrganizeFragment : BaseLocationFragment() {
    private var location: AMapLocation? = null
    private var htmlMDL: HtmlMDL? = null
    private var isResultOk = false
    private var msgMDL: RidersMsgMDL? = null
    private lateinit var handler: Handler
    override fun setBaseLayoutResID(): Int = R.layout.fragment_riders_organize
    override fun setUp(view: View, savedInstanceState: Bundle?) {
        applyLocationPermission(false)
        handler = Handler(Looper.getMainLooper())
    }

    override fun setListener() {
        llCreateTeam.setOnClickListener {
            if (htmlMDL == null) getAgreement(true)
            else showAgreeDialog()
        }
        etInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (TextUtils.isEmpty(etInput.text.toString().trim())) {
                    showShortToast(etInput.hint)
                } else {
                    val location = this.location
                    if (location == null) {
                        showShortToast("获取地理位置信息失败，正在重新获取…")
                        applyLocationPermission(false)
                    } else {
                        val inToken = etInput.text.toString()
                        getCarTeamData(inToken)
                    }
                }
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    override fun initData() {
        getAgreement(false)
        checkCarTeamSituation(false)
    }

    /*获取组队协议书*/
    private fun getAgreement(showDialog: Boolean) {
        doRequest(WebApiService.NEWS_BY_TYPE, WebApiService.newsByTypeParams(NewsType.RIDERS_AGREEMENT.code), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                if (showDialog) showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, HtmlMDL::class.java)
                    if (mdl == null) {
                        if (showDialog) onJsonParseError()
                    } else htmlMDL = mdl
                    if (showDialog) showAgreeDialog()
                } else {
                    if (showDialog) showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                endLoading()
                if (showDialog) onHttpError(e)
            }
        })
    }

    /*是否有车队或者邀请*/
    private fun checkCarTeamSituation(showDialog: Boolean) {
        doRequest(WebApiService.CHECK_RIDERS, WebApiService.checkRidersParams(getUserId()), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                if (showDialog) showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    isResultOk = true
                    val mdl = GsonUtils.fromDataBean(data, RidersMsgMDL::class.java)
                    msgMDL = mdl
                    if (showDialog) withResult()
                } else {
                    if (showDialog) showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                endLoading()
                if (showDialog) onHttpError(e)
            }
        })
    }

    private fun withResult() {
        if (AppLocalHelper.isAuthCYZD(context) && !isAuth()) {
            showTipsDialog(context.getString(R.string.dialog_default_title), context.getString(R.string.without_auth))
        } else {
            val mdl = msgMDL
            if (mdl == null)  //不存在车队
                openActivity(RidersEditActivity::class.java)  //同意，则进入创建组队页面
            else {
                if (mdl.type == 1) { //已加入车队
                    showShortToast("你已加入车队，暂时不能再次创建")
                } else {
                    openActivity(RidersEditActivity::class.java)
                }
            }
        }
    }

    private fun showAgreeDialog() {
        RidersAgreementDialog(context)
                .message(htmlMDL?.html)
                .setOnViewClickListener(object : RidersAgreementDialog.OnViewClickListener {
                    override fun onViewClick(type: Int, dialog: RidersAgreementDialog) {
                        when (type) {
                            1 -> dialog.dismiss() //不同意
                            else -> {
                                if (isResultOk) {
                                    withResult()
                                } else {
                                    checkCarTeamSituation(true)
                                }
                                dialog.dismiss()
                            }
                        }
                    }
                }).show()
    }

    override fun afterLocation(location: AMapLocation) {
        this.location = location
        closeLocation()
    }

    override fun locationFailure() {
        handler.postDelayed({ openLocation() }, 3000)
    }

    /*根据口令获取车队信息*/
    private fun getCarTeamData(inToken: String?) {
        doRequest(WebApiService.CAR_TEAM_DETAIL, WebApiService.getCarTeamDataParams2(inToken), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, RidersDetailMDL::class.java)
                    mdl?.teammember?.let { members ->
                        var isMySelf = false
                        for (member in members) {
                            if (TextUtils.equals(member.userid, getUserId())) {
                                isMySelf = true
                                break
                            }
                        }
                        if (!isMySelf) {
                            mdl.team_data?.let { joinCarTeam(it.teamid) }
                        }
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

    /*输入口令加入车队*/
    private fun joinCarTeam(teamId: String?) {
        val mqttService = ApiService.buildMQTTService(context)
        mqttService.connect(object : IMqttCallBack {
            override fun messageArrived(topic: String?, message: String?, qos: Int) {
            }

            override fun connectionLost(throwable: Throwable?) {
            }

            override fun deliveryComplete(deliveryToken: IMqttDeliveryToken?) {
                openActivity(RidersDetailActivity::class.java, Bundle().apply { putString("teamId", teamId) })
                mqttService.disconnect()
            }

            override fun connectSuccess(token: IMqttToken?) {
                val mdl = AddTeamMDL().apply {
                    this.userid = getUserId()
                    this.username = getUserName()
                    this.usericon = getIconFile()
                    this.teamid = teamId
                    this.longitude = this@RidesOrganizeFragment.location?.longitude
                    this.latitude = this@RidesOrganizeFragment.location?.latitude
                }
                mqttService.publish("${ApiService.TOPIC_ADD_TEAM}$teamId", mdl.obtainMessage())
            }

            override fun connectFailed(token: IMqttToken?, throwable: Throwable?) {
            }
        })
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }
}