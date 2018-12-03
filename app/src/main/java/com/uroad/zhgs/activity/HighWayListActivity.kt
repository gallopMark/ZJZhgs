package com.uroad.zhgs.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.util.TypedValue
import android.view.View
import com.amap.api.location.AMapLocation
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.fragment.HighwayListFragment
import kotlinx.android.synthetic.main.activity_highway_list.*

/**
 *Created by MFB on 2018/8/9.
 * 高速列表
 */
class HighWayListActivity : BaseActivity() {
    private var location: AMapLocation? = null
    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_highway_list)
        withTitle(resources.getString(R.string.highwayList_title))
        requestLocationPermissions(object : RequestLocationPermissionCallback {
            override fun doAfterGrand() {
                openLocation()
            }

            override fun doAfterDenied() {
            }
        })
    }

    private fun initView() {
        radioGroup.setOnCheckedChangeListener { _, checkId ->
            when (checkId) {
                R.id.rbTab1 -> setCurrentTab(0)
                R.id.rbTab2 -> setCurrentTab(1)
            }
        }
        ivSearch.setOnClickListener {
            openActivity(HighwaySearchActivity::class.java, Bundle().apply {
                location?.let { location ->
                    putDouble("longitude", location.longitude)
                    putDouble("latitude", location.latitude)
                }
            })
            overridePendingTransition(0, 0)
        }
    }

    override fun afterLocation(location: AMapLocation) {
        this.location = location
        initView()
        initViewPager()
        closeLocation()
    }

    override fun onLocationFail(errorInfo: String?) {
        openLocation()
    }

    private fun initViewPager() {
        val fragments = ArrayList<Fragment>()
        fragments.add(HighwayListFragment().apply {
            arguments = Bundle().apply {
                putInt("type", 1)
                location?.let {
                    putDouble("longitude", it.longitude)
                    putDouble("latitude", it.latitude)
                }
            }
        })
        fragments.add(HighwayListFragment().apply {
            arguments = Bundle().apply {
                putInt("type", 2)
                location?.let {
                    putDouble("longitude", it.longitude)
                    putDouble("latitude", it.latitude)
                }
            }
        })
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
                }
            }
        })
    }

    private fun setCurrentTab(tab: Int) {
        reset()
        when (tab) {
            0 -> {
                setVisable(0)
                viewPager.setCurrentItem(0, false)
            }
            1 -> {
                setVisable(1)
                viewPager.setCurrentItem(1, false)
            }
        }
    }

    private fun reset() {
        vTab1.visibility = View.INVISIBLE
        vTab2.visibility = View.INVISIBLE
        val ts14 = resources.getDimension(R.dimen.font_14)
        rbTab1.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts14)
        rbTab2.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts14)
    }

    private fun setVisable(tab: Int) {
        val ts16 = resources.getDimension(R.dimen.font_16)
        when (tab) {
            0 -> {
                vTab1.visibility = View.VISIBLE
                rbTab1.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts16)
            }
            1 -> {
                vTab2.visibility = View.VISIBLE
                rbTab2.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts16)
            }
        }
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