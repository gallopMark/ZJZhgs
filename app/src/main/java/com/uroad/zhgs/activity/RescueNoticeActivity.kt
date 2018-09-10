package com.uroad.zhgs.activity

import android.os.Bundle
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import kotlinx.android.synthetic.main.activity_rescue_notice.*

/**
 *Created by MFB on 2018/8/28.
 */
class RescueNoticeActivity : BaseActivity() {
    override fun setUp(savedInstanceState: Bundle?) {
        withTitle(resources.getString(R.string.rescue_notice_title))
        setBaseContentLayout(R.layout.activity_rescue_notice)
        btApply.setOnClickListener {
            openActivity(RescueMainActivity::class.java)
            finish()
        }
    }
}