package com.uroad.zhgs.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.enumeration.Carcategory
import com.uroad.zhgs.fragment.MyCarFragment
import kotlinx.android.synthetic.main.activity_mycar.*

/**
 *Created by MFB on 2018/8/13.
 * 我的车辆
 */
@Deprecated("旧版我的车辆页面")
class MyCarActivity : BaseActivity() {
    private val requestCode = 0x0001
    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_mycar)
        withTitle(resources.getString(R.string.mycar_title))
        radioGroup.setOnCheckedChangeListener { _, checkId ->
            when (checkId) {
                R.id.rbTab1 -> setCurrentTab(0)
                R.id.rbTab2 -> setCurrentTab(1)
            }
        }
        initViewPager()
        ivBindCar.setOnClickListener { openActivityForResult(BindCarActivity::class.java, requestCode) }
    }

    private fun initViewPager() {
        val fragments = ArrayList<Fragment>()
        fragments.add(MyCarFragment().apply { arguments = Bundle().apply { putString("cartype", Carcategory.COACH.code) } })
        fragments.add(MyCarFragment().apply { arguments = Bundle().apply { putString("cartype", Carcategory.TRUCK.code) } })
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
        rbTab1.textSize = 14f
        rbTab2.textSize = 14f
    }

    private fun setVisable(tab: Int) {
        when (tab) {
            0 -> {
                vTab1.visibility = View.VISIBLE
                rbTab1.textSize = 16f
            }
            1 -> {
                vTab2.visibility = View.VISIBLE
                rbTab2.textSize = 16f
            }
        }
    }

    private class MPageAdapter(fm: FragmentManager, private val fragments: MutableList<Fragment>) : FragmentPagerAdapter(fm) {
        override fun getCount(): Int {
            return fragments.size
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }
    }
}