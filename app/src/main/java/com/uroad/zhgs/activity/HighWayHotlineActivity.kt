package com.uroad.zhgs.activity

import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.uroad.library.utils.BitmapUtils
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.enumeration.PhoneType
import com.uroad.zhgs.fragment.HighwayHotlineFragment
import kotlinx.android.synthetic.main.activity_highway_hotline.*

/**
 *Created by MFB on 2018/8/12.
 * 高速热线
 */
class HighWayHotlineActivity : BaseActivity() {

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_highway_hotline)
        requestWindowFullScreen()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            ivBack.layoutParams = (ivBack.layoutParams as FrameLayout.LayoutParams).apply { topMargin = DisplayUtils.getStatusHeight(this@HighWayHotlineActivity) }
        ivBack.setOnClickListener { onBackPressed() }
        setTopImage()
        radioGroup.setOnCheckedChangeListener { _, checkId ->
            when (checkId) {
                R.id.rbTab1 -> setCurrentTab(0)
                R.id.rbTab2 -> setCurrentTab(1)
                R.id.rbTab3 -> setCurrentTab(2)
            }
        }
        ivSearch.setOnClickListener {
            openActivity(HighwayHotlineSearchActivity::class.java)
            overridePendingTransition(0, 0)
        }
        initViewPager()
    }

    //重新计算图片高度 避免图片压缩
    private fun setTopImage() {
        val width = DisplayUtils.getWindowWidth(this)
        val height = (width * 0.573).toInt()
        ivTopPic.layoutParams = ivTopPic.layoutParams.apply {
            this.width = width
            this.height = height
        }
        ivTopPic.scaleType = ImageView.ScaleType.FIT_XY
        ivTopPic.setImageBitmap(BitmapUtils.decodeSampledBitmapFromResource(resources, R.mipmap.ic_highway_top_bg, width, height))
    }

    private fun initViewPager() {
        val fragments = ArrayList<Fragment>()
        fragments.add(HighwayHotlineFragment().apply { arguments = Bundle().apply { putString("phonetype", PhoneType.EMERGENCY.code) } })
        fragments.add(HighwayHotlineFragment().apply { arguments = Bundle().apply { putString("phonetype", PhoneType.INSURANCE.code) } })
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
        val ts14 = resources.getDimension(R.dimen.font_14)
        vTab1.visibility = View.INVISIBLE
        vTab2.visibility = View.INVISIBLE
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