package com.uroad.zhgs.activity

import android.os.Bundle
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.utils.PackageInfoUtils
import kotlinx.android.synthetic.main.activity_aboutus.*

/**
 * Created by MFB on 2018/8/28.
 * Copyright  2018年 浙江综合交通大数据开发有限公司.
 * 说明：关于我们
 */
class AboutUsActivity : BaseActivity() {
    override fun setUp(savedInstanceState: Bundle?) {
        withTitle(resources.getString(R.string.aboutUs_title))
        setBaseContentLayout(R.layout.activity_aboutus)
        setVersionName()
    }

    /*获取和显示当前app版本号*/
    private fun setVersionName() {
        val versionName = "v${PackageInfoUtils.getVersionName(this)}"
        tvVersionCode.text = versionName
    }

    override fun setListener() {
        tvGotoScore.setOnClickListener { }
        tvTermsOfUse.setOnClickListener { openActivity(TermsOfUseActivity::class.java) }
        tvPrivacy.setOnClickListener { openActivity(PrivacyStateActivity::class.java) }
    }
}