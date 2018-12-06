package com.uroad.zhgs.activity

import android.os.Bundle
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.fragment.NewsFragment

/**
 * @author MFB
 * @create 2018/12/4
 * @describe 通告列表
 */
class NoticeListActivity : BaseActivity() {

    override fun setUp(savedInstanceState: Bundle?) {
        withTitle(getString(R.string.noticeList_title))
        supportFragmentManager.beginTransaction().replace(R.id.baseContent, NewsFragment().apply { arguments = Bundle().apply { putString("dictcode", "1100001") } }).commit()
    }
}