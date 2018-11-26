package com.uroad.zhgs.activity

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import kotlinx.android.synthetic.main.activity_activityrule.*

/**
 * @author MFB
 * @create 2018/11/26
 * @describe 活动规则页面
 */
class ActivityRuleActivity : BaseActivity() {
    override fun setUp(savedInstanceState: Bundle?) {
        withTitle(getString(R.string.invitecourtesy_activity_rule))
        setBaseContentLayout(R.layout.activity_activityrule)
        val rule = intent.extras?.getString("rule")
        tvRule.text = rule
        tvRule.movementMethod = ScrollingMovementMethod.getInstance()
    }
}