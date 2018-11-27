package com.uroad.zhgs.activity

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import kotlinx.android.synthetic.main.activity_activityrule.*

/**
 * @author MFB
 * @create 2018/11/26
 * @describe 活动规则页面
 */
@Suppress("DEPRECATION")
class ActivityRuleActivity : BaseActivity() {
    override fun setUp(savedInstanceState: Bundle?) {
        withTitle(getString(R.string.invitecourtesy_activity_rule))
        setBaseContentLayout(R.layout.activity_activityrule)
        val rule = intent.extras?.getString("rule")
        if (!TextUtils.isEmpty(rule)) {
            tvRule.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                Html.fromHtml(rule, Html.FROM_HTML_MODE_LEGACY)
            else
                Html.fromHtml(rule)
        }
        tvRule.movementMethod = ScrollingMovementMethod.getInstance()
    }
}