package com.uroad.zhgs.activity

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import com.uroad.mqtt.IMqttCallBack
import com.uroad.mqtt.MqttService
import com.uroad.zhgs.R
import com.uroad.zhgs.common.ThemeStyleActivity
import com.uroad.zhgs.dialog.MaterialDialog
import com.uroad.zhgs.model.AddPicItem
import com.uroad.zhgs.model.MutilItem
import com.uroad.zhgs.model.RidersDetailMDL
import com.uroad.zhgs.model.mqtt.CloseTeamMDL
import com.uroad.zhgs.model.mqtt.QuitTeamMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.ApiService
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_riders_settings.*
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken

/**
 * @author MFB
 * @create 2018/10/18
 * @describe 车队设置页面
 */
class RidersSettingsActivity : ThemeStyleActivity() {
    private var teamId: String? = null
    private var isTeamHeader: Boolean = false
    private val mDatas = ArrayList<MutilItem>().apply { add(AddPicItem()) }
    private lateinit var adapter: MemberAdapter
    private lateinit var mqttService: MqttService
    private var type: Int = 0

    override fun themeSetUp(savedInstanceState: Bundle?) {
        setLayoutResID(R.layout.activity_riders_settings)
        setThemeTitle(getString(R.string.riders_settings))
        initBundleData()
        initRv()
        initMQTT()
    }

    private fun initBundleData() {
        teamId = intent.extras?.getString("teamId")
        tvTeamName.text = intent.extras?.getString("teamName")
        tvInToken.text = intent.extras?.getString("intoken")
        intent.extras?.let { isTeamHeader = it.getBoolean("isTeamHeader", false) }
        if (isTeamHeader) {
            tvBottom.text = getString(R.string.riders_dissolution)
            tvBottom.setOnClickListener { onDialogTips(1) }
        } else {
            tvBottom.text = getString(R.string.riders_quit)
            tvBottom.setOnClickListener { onDialogTips(2) }
        }
    }

    private fun initRv() {
        recyclerView.layoutManager = GridLayoutManager(this, 4).apply { orientation = GridLayoutManager.VERTICAL }
        adapter = MemberAdapter(this, mDatas).apply {
            setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
                override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                    if (position in 0 until mDatas.size) {
                        if (mDatas[position].getItemType() == 1) {
                            openActivity(RidersInvitingActivity::class.java, Bundle().apply { putString("teamId", teamId) })
                        }
                    }
                }
            })
        }
        recyclerView.adapter = adapter
    }

    private fun initMQTT() {
        mqttService = ApiService.buildMQTTService(this)
        mqttService.connect(object : IMqttCallBack {
            override fun messageArrived(topic: String?, message: String?, qos: Int) {
            }

            override fun connectionLost(throwable: Throwable?) {
                endLoading()
                throwable?.let { onHttpError(it) }
            }

            override fun deliveryComplete(deliveryToken: IMqttDeliveryToken?) {
                endLoading()
                if (type == 1)
                    finishTips("已解散车队")
                else
                    finishTips("您已退出车队")
            }

            override fun connectSuccess(token: IMqttToken?) {
            }

            override fun connectFailed(token: IMqttToken?, throwable: Throwable?) {
            }
        })
    }

    override fun initData() {
        doRequest(WebApiService.CAR_TEAM_DETAIL, WebApiService.getCarTeamDataParams(teamId), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, RidersDetailMDL::class.java)
                    mdl?.teammember?.let { updateUI(it) }
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
            }
        })
    }

    private fun updateUI(mdLs: MutableList<RidersDetailMDL.TeamMember>) {
        mDatas.clear()
        mDatas.addAll(mdLs)
        setMemberSize(mDatas.size)
        mDatas.add(AddPicItem())
        adapter.notifyDataSetChanged()
    }

    private fun setMemberSize(size: Int) {
        var source = getString(R.string.riders_members)
        val start = source.length
        source += "${size}人"
        val end = source.length - 1
        tvMembers.text = SpannableString(source).apply { setSpan(ForegroundColorSpan(ContextCompat.getColor(this@RidersSettingsActivity, R.color.riders_inTeam)), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE) }
    }

    private fun onDialogTips(type: Int) {
        this.type = type
        val dialog = MaterialDialog(this)
        dialog.setTitle(getString(R.string.dialog_default_title))
        if (type == 1) {  //解散车队
            dialog.setMessage(getString(R.string.riders_dissolution_msg))
        } else {  //退出车队
            dialog.setMessage(getString(R.string.riders_quit_msg))
        }
        dialog.setNegativeButton(getString(R.string.dialog_button_cancel), object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                dialog.dismiss()
            }
        })
        dialog.setPositiveButton(getString(R.string.dialog_button_confirm), object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                dialog.dismiss()
                if (type == 1) dissolution()
                else quit()
            }
        })
        dialog.show()
    }

    /*解散车队请求*/
    private fun dissolution() {
        val mdl = CloseTeamMDL().apply {
            username = getUserName()
            teamid = this@RidersSettingsActivity.teamId
        }
        showLoading()
        mqttService.publish("${ApiService.TOPIC_CLOSE_TEAM}$teamId", mdl.obtainMessage())
    }

    /*退出车队*/
    private fun quit() {
        val mdl = QuitTeamMDL().apply {
            userid = getUserId()
            username = getUserName()
            teamid = this@RidersSettingsActivity.teamId
        }
        showLoading()
        mqttService.publish("${ApiService.TOPIC_QUIT_TEAM}$teamId", mdl.obtainMessage())
    }

    private fun finishTips(message: String) {
        val dialog = MaterialDialog(this)
        dialog.setTitle(getString(R.string.dialog_default_title))
        dialog.setMessage(message)
        dialog.hideDivider()
        dialog.setPositiveButton(getString(R.string.i_got_it), object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                dialog.dismiss()
            }
        })
        dialog.show()
        dialog.setOnDismissListener { finish() }
    }

    private class MemberAdapter(context: Context, mDatas: MutableList<MutilItem>)
        : BaseArrayRecyclerAdapter<MutilItem>(context, mDatas) {
        override fun bindView(viewType: Int): Int = R.layout.item_riders_member

        override fun onBindHoder(holder: RecyclerHolder, t: MutilItem, position: Int) {
            val itemType = t.getItemType()
            if (itemType == 1) {
                holder.setImageResource(R.id.ivIcon, R.mipmap.ic_add_pic)
                holder.setText(R.id.tvName, "邀请")
            } else {
                val mdl = t as RidersDetailMDL.TeamMember
                holder.displayImage(R.id.ivIcon, mdl.iconfile, R.mipmap.ic_user_default)
                holder.setText(R.id.tvName, t.username)
            }
        }
    }

    override fun onDestroy() {
        mqttService.disconnect()
        super.onDestroy()
    }
}