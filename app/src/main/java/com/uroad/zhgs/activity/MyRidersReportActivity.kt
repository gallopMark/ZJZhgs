package com.uroad.zhgs.activity

import android.os.Bundle
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.common.ThemeStyleActivity
import com.uroad.zhgs.fragment.RidersReportFragment

/**
 * @author MFB
 * @create 2018/10/25
 * @describe 我的爆料列表
 */
class MyRidersReportActivity : BaseActivity() {
    override fun setUp(savedInstanceState: Bundle?) {
        withTitle(getString(R.string.mine_my_burst))
        supportFragmentManager.beginTransaction().replace(R.id.baseContent,
                RidersReportFragment().apply { arguments = Bundle().apply { putBoolean("isMy", true) } }).commit()
    }
}