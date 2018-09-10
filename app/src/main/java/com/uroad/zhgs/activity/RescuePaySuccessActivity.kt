package com.uroad.zhgs.activity

import android.os.Bundle
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_rescue_paysuccess.*

/**
 *Created by MFB on 2018/7/29.
 * 救援支付完成页面
 */
class RescuePaySuccessActivity : BaseActivity() {

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_rescue_paysuccess)
        baseParent.setBackgroundResource(R.mipmap.ic_rescue_applysuccess_bg)
        var rescueid = ""
        intent.extras?.let { rescueid = it.getString("rescueid") }
        ivBack.setOnClickListener { onBackPressed() }
        llEvaluate.setOnClickListener { openActivity(RescueEvaluateActivity::class.java, Bundle().apply { putString("rescueid", rescueid) }) }
        llInvoice.setOnClickListener { openActivity(InvoiceTitleActivity::class.java, Bundle().apply { putString("rescueid", rescueid) }) }
    }
}