package com.uroad.zhgs.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.fragment.HighwayEventFragment
import com.uroad.zhgs.fragment.HighwayPreviewFragment
import com.uroad.zhgs.fragment.HighwaySnapFragment
import kotlinx.android.synthetic.main.activity_highway_preview.*

/**
 *Created by MFB on 2018/8/16.
 * 高速快览
 */
class HighwayPreViewActivity : BaseActivity() {
    private var roadoldid = ""
    private var poiname = ""
    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_highway_preview)
        intent.extras?.let {
            roadoldid = it.getString("roadoldid")
            val shortname = it.getString("shortname")
            poiname = it.getString("poiname")
            withTitle(shortname)
        }
        radioGroup.setOnCheckedChangeListener { _, checkId ->
            when (checkId) {
                R.id.rbTab1 -> setCurrentTab(0)
                R.id.rbTab2 -> setCurrentTab(1)
                R.id.rbTab3 -> setCurrentTab(2)
            }
        }
        initViewPager()
    }

    private fun setCurrentTab(tab: Int) {
        vTab1.visibility = View.INVISIBLE
        vTab2.visibility = View.INVISIBLE
        vTab3.visibility = View.INVISIBLE
        when (tab) {
            0 -> {
                vTab1.visibility = View.VISIBLE
                viewPager.setCurrentItem(0, false)
            }
            1 -> {
                vTab2.visibility = View.VISIBLE
                viewPager.setCurrentItem(1, false)
            }
            2 -> {
                vTab3.visibility = View.VISIBLE
                viewPager.setCurrentItem(2, false)
            }
        }
    }

    private fun initViewPager() {
        val fragments = ArrayList<Fragment>()
        fragments.add(HighwayPreviewFragment().apply {
            arguments = Bundle().apply {
                putString("roadoldid", roadoldid)
                putString("poiname", poiname)
            }
        })
        fragments.add(HighwaySnapFragment().apply { arguments = Bundle().apply { putString("roadoldid", roadoldid) } })
        fragments.add(HighwayEventFragment().apply { arguments = Bundle().apply { putString("roadoldid", roadoldid) } })
        val adapter = MPageAdapter(supportFragmentManager, fragments)
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> radioGroup.check(R.id.rbTab1)
                    1 -> radioGroup.check(R.id.rbTab2)
                    2 -> radioGroup.check(R.id.rbTab3)
                }
            }
        })
    }

    private class MPageAdapter(fm: FragmentManager, private val fragments: MutableList<Fragment>) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }
    }
}