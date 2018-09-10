package com.uroad.zhgs.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import com.uroad.zhgs.R
import com.uroad.zhgs.adapteRv.NewsTabAdapter
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.fragment.NewsFragment
import com.uroad.zhgs.model.NewsTabMDL
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_news_main.*

/**
 *Created by MFB on 2018/8/6.
 * 资讯页面
 */
class NewsMainActivity : BaseActivity() {
    private val mDatas = ArrayList<NewsTabMDL.Type>()
    private lateinit var adapter: NewsTabAdapter

    override fun setUp(savedInstanceState: Bundle?) {
        withTitle(resources.getString(R.string.news_title))
        setBaseContentLayout(R.layout.activity_news_main)
        rvTab.layoutManager = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.HORIZONTAL }
        adapter = NewsTabAdapter(this, mDatas)
        rvTab.adapter = adapter
    }

    override fun initData() {
        doRequest(WebApiService.NEWS_TAB, WebApiService.newsTabParams(), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, NewsTabMDL::class.java)
                    if (mdl == null) showShortToast("数据异常")
                    else updateData(mdl)
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                onHttpError(e)
            }
        })
    }

    private fun updateData(mdl: NewsTabMDL) {
        mdl.type?.let {
            mDatas.addAll(it)
            adapter.notifyDataSetChanged()
        }
        val fragments = ArrayList<Fragment>()
        for (i in 0 until mDatas.size) {
            val fragment = NewsFragment().apply {
                arguments = Bundle().apply { putString("dictcode", mDatas[i].dictcode) }
            }
            fragments.add(fragment)
        }
        val pagerAdapter = FragmentAdapter(supportFragmentManager, fragments)
        viewPager.adapter = pagerAdapter
        adapter.setOnSelectedListener(object : NewsTabAdapter.OnSelectedListener {
            override fun onSelected(position: Int) {
                viewPager.setCurrentItem(position, false)
            }
        })
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(position: Int) {
                adapter.setSelectPos(position)
            }
        })
    }

    private class FragmentAdapter(fm: FragmentManager, private val fragments: MutableList<Fragment>)
        : FragmentPagerAdapter(fm) {
        override fun getItem(p0: Int): Fragment {
            return fragments[p0]
        }

        override fun getCount(): Int {
            return fragments.size
        }

    }
}