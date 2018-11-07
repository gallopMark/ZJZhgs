package com.uroad.zhgs.activity

import android.os.Bundle
import com.uroad.zhgs.R
import com.uroad.zhgs.common.ThemeStyleActivity
import com.uroad.zhgs.fragment.RidersReportDefaultFragment
import com.uroad.zhgs.fragment.RidersReportVideoFragment
import com.uroad.zhgs.fragment.RidersReportVoiceFragment

/**
 *Created by MFB on 2018/8/8.
 */
class RidersReportActivity : ThemeStyleActivity() {
    companion object {
        const val TYPE_DEFAULT = "DEFAULT"
        const val TYPE_VOICE = "VOICE"
        const val TYPE_VIDEO = "VIDEO"
    }

    override fun themeSetUp(savedInstanceState: Bundle?) {
        setLayoutResID(R.layout.activity_riders_report)
        setThemeTitle(getString(R.string.userEvent_burst))
        val type = intent.type
        withType(type)
    }

    private fun withType(type: String?) {
        when (type) {
            TYPE_VOICE -> supportFragmentManager.beginTransaction()
                    .replace(R.id.container, RidersReportVoiceFragment())
                    .commit()
            TYPE_VIDEO -> supportFragmentManager.beginTransaction()
                    .replace(R.id.container, RidersReportVideoFragment().apply { arguments = intent.extras })
                    .commit()
            else -> supportFragmentManager.beginTransaction()
                    .replace(R.id.container, RidersReportDefaultFragment())
                    .commit()
        }
    }
}