package com.uroad.zhgs.activity

import android.os.Bundle
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity

/**
 * @author MFB
 * @create 2018/9/19
 * @describe 违法查询
 */
class BreakRulesActivity : BaseActivity() {
    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_pleasewait)
        withTitle(getString(R.string.home_menu_wfcx))
    }
}