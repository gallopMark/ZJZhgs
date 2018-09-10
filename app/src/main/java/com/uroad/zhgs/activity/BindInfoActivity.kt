package com.uroad.zhgs.activity

import android.os.Bundle
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.R

/**
 *Created by MFB on 2018/7/29.
 * 信息绑定
 */
class BindInfoActivity : BaseActivity() {
    override fun setUp(savedInstanceState: Bundle?) {
        withTitle(resources.getString(R.string.bindinfo_title))
        setBaseContentLayout(R.layout.activity_bindinfo)
    }
}