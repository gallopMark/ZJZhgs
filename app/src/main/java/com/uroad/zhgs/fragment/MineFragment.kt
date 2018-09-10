package com.uroad.zhgs.fragment

import android.os.Bundle
import android.view.View
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.zhgs.*
import com.uroad.zhgs.activity.*
import com.uroad.zhgs.common.BaseFragment
import com.uroad.zhgs.helper.UserPreferenceHelper
import kotlinx.android.synthetic.main.fragment_mine.*

/**
 *Created by MFB on 2018/8/12.
 */
class MineFragment : BaseFragment(), View.OnClickListener {

    override fun setBaseLayoutResID(): Int {
        return R.layout.fragment_mine
    }

    override fun setUp(view: View, savedInstanceState: Bundle?) {
        //loginStatus()
    }

    private fun loginStatus() {
        if (UserPreferenceHelper.isLogin(context)) {
            setUserInfo()
        } else {
            llUserInfo.visibility = View.GONE
            llNotLogged.visibility = View.VISIBLE
            ivUserIcon.setImageResource(R.mipmap.ic_user_default)
        }
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
        tvMyCar.setOnClickListener(this)
        tvSettings.setOnClickListener { openActivity(SettingsActivity::class.java) }
    }

    override fun onClick(v: View) {
        if (!UserPreferenceHelper.isLogin(context)) {
            openActivity(LoginActivity::class.java)
            return
        }
        when (v.id) {
            R.id.tvMyBurst -> openActivity(UserEventListActivity::class.java)  //我的报料
            R.id.tvMySubscribe -> openActivity(UserSubscribeActivity::class.java)  //我的订阅
            R.id.tvRescueRecord -> openActivity(RescueRecordActivity::class.java)  //救援记录
            R.id.rlMessage -> openActivity(UserMsgActivity::class.java) //消息中心
            R.id.tvMyCar -> openActivity(MyCar2Activity::class.java) //我的车辆
        }
    }

    override fun onResume() {
        super.onResume()
        loginStatus()
    }
}