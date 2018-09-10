package com.uroad.zhgs.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import com.amap.api.location.AMapLocation
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.fragment.*
import kotlinx.android.synthetic.main.activity_mynearby_tab.*

/**
 *Created by MFB on 2018/8/23.
 */
class MyNearByTabActivity : BaseActivity() {

    override fun setUp(savedInstanceState: Bundle?) {
        withTitle(resources.getString(R.string.mynearby_title))
        setBaseContentLayout(R.layout.activity_mynearby_tab)
        requestLocationPermissions(object : RequestLocationPermissionCallback {
            override fun doAfterGrand() {
                openLocation()
            }

            override fun doAfterDenied() {
                showDismissLocationDialog()
            }
        })
    }

    private fun initTab() {
        radioGroup.setOnCheckedChangeListener { _, checkId ->
            restore()
            when (checkId) {
                R.id.rbToll -> {
                    rbToll.textSize = 16f
                    vTab1.visibility = View.VISIBLE
                    viewPager.setCurrentItem(0, false)
                }
                R.id.rbService -> {
                    rbService.textSize = 16f
                    vTab2.visibility = View.VISIBLE
                    viewPager.setCurrentItem(1, false)
                }
                R.id.rbScenic -> {
                    rbScenic.textSize = 16f
                    vTab3.visibility = View.VISIBLE
                    viewPager.setCurrentItem(2, false)
                }
                R.id.rbGas -> {
                    rbGas.textSize = 16f
                    vTab4.visibility = View.VISIBLE
                    viewPager.setCurrentItem(3, false)
                }
                R.id.rbRepair -> {
                    rbRepair.textSize = 16f
                    vTab5.visibility = View.VISIBLE
                    viewPager.setCurrentItem(4, false)
                }
            }
        }
    }

    private fun restore() {
        rbToll.textSize = 14f
        rbService.textSize = 14f
        rbScenic.textSize = 14f
        rbGas.textSize = 14f
        rbRepair.textSize = 14f
        vTab1.visibility = View.INVISIBLE
        vTab2.visibility = View.INVISIBLE
        vTab3.visibility = View.INVISIBLE
        vTab4.visibility = View.INVISIBLE
        vTab5.visibility = View.INVISIBLE
    }

    override fun afterLocation(location: AMapLocation) {
        val poiName = location.poiName
        val longitude = location.longitude
        val latitude = location.latitude
        initTab()
        initViewPager(poiName, longitude, latitude)
        closeLocation()
    }

    private fun initViewPager(poiName: String, longitude: Double, latitude: Double) {
        val fragments = ArrayList<Fragment>()
        val bundle = Bundle().apply {
            putString("poiName", poiName)
            putDouble("longitude", longitude)
            putDouble("latitude", latitude)
        }
        fragments.add(NearByTollFragment().apply { arguments = bundle })
        fragments.add(NearByServiceFragment().apply { arguments = bundle })
        fragments.add(NearByScenicFragment().apply { arguments = bundle })
        fragments.add(NearByGasFragment().apply { arguments = bundle })
        fragments.add(NearByRepairFragment().apply { arguments = bundle })
        viewPager.adapter = MPageAdapter(supportFragmentManager, fragments)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> radioGroup.check(R.id.rbToll)
                    1 -> radioGroup.check(R.id.rbService)
                    2 -> radioGroup.check(R.id.rbScenic)
                    3 -> radioGroup.check(R.id.rbGas)
                    4 -> radioGroup.check(R.id.rbRepair)
                }
            }
        })
    }

    private class MPageAdapter(fm: FragmentManager, private val fragments: MutableList<Fragment>) : FragmentPagerAdapter(fm) {
        override fun getCount(): Int = fragments.size
        override fun getItem(position: Int): Fragment = fragments[position]
    }
}