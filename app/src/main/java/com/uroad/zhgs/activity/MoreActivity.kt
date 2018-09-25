package com.uroad.zhgs.activity

import android.os.Bundle
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity

/**
 * @author MFB
 * @create 2018/9/19
 * @describe 在线商城
 */
class MoreActivity : BaseActivity() {
    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_pleasewait)
        withTitle(getString(R.string.home_menu_more))
    }
}