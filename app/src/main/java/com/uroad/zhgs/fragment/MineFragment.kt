package com.uroad.zhgs.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.zhgs.*
import com.uroad.zhgs.activity.*
import com.uroad.zhgs.common.BaseFragment
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.model.ActivityMDL
import kotlinx.android.synthetic.main.fragment_mine.*

/**
 *Created by MFB on 2018/8/12.
 */
class MineFragment : BaseFragment(), View.OnClickListener {

    private lateinit var handler: Handler
    override fun setBaseLayoutResID(): Int {
        return R.layout.fragment_mine
    }

    override fun setUp(view: View, savedInstanceState: Bundle?) {
        handler = Handler(Looper.getMainLooper())
    }

    private fun setUserInfo() {
        llUserInfo.visibility = View.VISIBLE
        llNotLogged.visibility = View.GONE
        tvUserName.text = getUserName()
        ImageLoaderV4.getInstance().displayImage(context, getIconFile(), ivUserIcon, R.mipmap.ic_user_default)
    }

    override fun setListener() {
        llTopLayout.setOnClickListener {
            //跳转编辑个人信息页面
            if (isLogin()) openActivity(UserInfoActivity::class.java)
            else openActivity(LoginActivity::class.java)
        }
        tvMyBurst.setOnClickListener(this)
        tvMySubscribe.setOnClickListener(this)
        tvRescueRecord.setOnClickListener(this)
        rlMessage.setOnClickListener(this)
        tvShopping.setOnClickListener(this)
        tvMyCar.setOnClickListener(this)
        tvPassRecord.setOnClickListener(this)
        tvMyTracks.setOnClickListener(this)
        tvMyCode.setOnClickListener(this)
        tvSettings.setOnClickListener { openActivity(SettingsActivity::class.java) }
    }

    override fun onClick(v: View) {
        if (!isLogin()) {
            openActivity(LoginActivity::class.java)
        } else {
            when (v.id) {
                R.id.tvShopping -> openActivity(YouZanUserActivity::class.java)
                R.id.tvMyBurst -> openActivity(MyRidersReportActivity::class.java)  //我的报料
                R.id.tvMySubscribe -> openActivity(UserSubscribeActivity::class.java)  //我的订阅
                R.id.tvRescueRecord -> openActivity(RescueRecordActivity::class.java)  //救援记录
                R.id.rlMessage -> openActivity(UserMsgActivity::class.java) //消息中心
                R.id.tvMyCar -> openActivity(MyCar2Activity::class.java) //我的车辆
                R.id.tvPassRecord -> openActivity(MyPassRecordActivity::class.java) //通行记录
                R.id.tvMyTracks -> openActivity(MyTracksActivity::class.java) //我的足迹
                R.id.tvMyCode -> {
                    val activityMDL = CurrApplication.activityMDL
                    if (activityMDL == null) openActivity(MyInvitationCodeActivity::class.java) //我的邀请码
                    else {   //如果有活动
                        if (TextUtils.equals(activityMDL.transitionstype, ActivityMDL.Type.H5.code)) {  //跳转h5
                            var content = activityMDL.transitionscontent
                            if (content == null || content.isEmpty()) return
                            if (activityMDL.islogin == 1) {  //需要登录
                                if (isLogin()) {
                                    content += if (!content.contains("?"))  //是否已经拼了参数
                                        "?activityid=${activityMDL.activityid}&useruuid=${getUserId()}"
                                    else
                                        "&activityid=${activityMDL.activityid}&useruuid=${getUserId()}"
                                    openWebActivity(content, "")
                                } else {
                                    openActivity(LoginActivity::class.java)
                                }
                            } else {
                                openWebActivity(content, "")
                            }
                        } else if (TextUtils.equals(activityMDL.transitionstype, ActivityMDL.Type.NATIVE.code)) {
                            if (!isLogin()) openActivity(LoginActivity::class.java)
                            else openActivity(InviteCourtesyActivity::class.java, Bundle().apply { putString("activityId", activityMDL.activityid) })
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loginStatus()
    }

    private fun loginStatus() {
        if (isLogin()) {
            setUserInfo()
        } else {
            llUserInfo.visibility = View.GONE
            llNotLogged.visibility = View.VISIBLE
            ivUserIcon.setImageResource(R.mipmap.ic_user_default)
        }
    }
}