package com.uroad.zhgs.activity

import android.os.Bundle
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_rescue_applysuccess.*

/**
 *Created by MFB on 2018/7/28.
 * 救援申请成功页
 */
class RescueSubmissionActivity : BaseActivity() {

    override fun setUp(savedInstanceState: Bundle?) {
        baseParent.setBackgroundResource(R.mipmap.ic_rescue_applysuccess_bg)
        setBaseContentLayoutWithoutTitle(R.layout.activity_rescue_applysuccess)
        ivBack.setOnClickListener { onBackPressed() }
        btCheckProgress.setOnClickListener {
            openActivity(RescueDetailActivity::class.java, intent.extras)
            finish()
        }
    }
}