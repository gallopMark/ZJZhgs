package com.uroad.zhgs.common

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import com.uroad.library.utils.NetworkUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.widget.CurrencyLoadView
import kotlinx.android.synthetic.main.activity_theme_style.*
import kotlinx.android.synthetic.main.layout_theme_style_toolbar.*

/**
 * @author MFB
 * @create 2018/10/19
 * @describe 深蓝色主题activity基础类
 */
abstract class ThemeStyleActivity : BaseActivity() {
    override fun requestWindow() {
        setTheme(R.style.AppTheme2)
    }

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_theme_style)
        initToolbar()
        themeSetUp(savedInstanceState)
    }

    open fun themeSetUp(savedInstanceState: Bundle?) {

    }

    private fun initToolbar() {
        themeToolbar.title = ""
        setSupportActionBar(themeToolbar)
        themeToolbar.setNavigationOnClickListener { onBackPressed() }
    }

    open fun setThemeTitle(title: CharSequence?) {
        themeTitle.text = title
    }

    open fun setThemeOption(options: CharSequence?, onClickListener: View.OnClickListener?) {
        if (TextUtils.isEmpty(options)) {
            themeOption.visibility = View.GONE
        } else {
            themeOption.text = options
            themeOption.visibility = View.VISIBLE
            themeOption.setOnClickListener(onClickListener)
        }
    }

    open fun setLayoutResID(layoutId: Int) {
        layoutInflater.inflate(layoutId, themeContentView, true)
    }

    open fun setLayoutResIdWithOutTitle(layoutId: Int) {
        themeContainer.removeView(themeToolbar)
        setLayoutResID(layoutId)
    }

    open fun setThemeLoading() {
        themeContentView.visibility = View.GONE
        val cLoadView = CurrencyLoadView(this)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        cLoadView.layoutParams = params
        cLoadView.setState(CurrencyLoadView.STATE_LOADING)
        themeLoadView.removeAllViews()
        themeLoadView.addView(cLoadView)
        themeLoadView.visibility = View.VISIBLE
    }

    open fun setThemeEndLoading() {
        themeLoadView.removeAllViews()
        themeLoadView.visibility = View.GONE
        themeContentView.visibility = View.VISIBLE
    }

    open fun setThemePageError() {
        themeContentView.visibility = View.GONE
        val cLoadView = CurrencyLoadView(this)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        cLoadView.layoutParams = params
        if (!NetworkUtils.isConnected(this)) cLoadView.setState(CurrencyLoadView.STATE_NONETWORK)
        else cLoadView.setState(CurrencyLoadView.STATE_ERROR)
        cLoadView.setOnRetryListener(object : CurrencyLoadView.OnRetryListener {
            override fun onRetry(view: View) {
                onReload(view)
            }
        })
        themeLoadView.removeAllViews()
        themeLoadView.addView(cLoadView)
        themeLoadView.visibility = View.VISIBLE
    }

    open fun setThemePageNoData() {
        setThemePageNoData(null)
    }

    open fun setThemePageNoData(emptyTips: CharSequence?) {
        themeContentView.visibility = View.GONE
        val cLoadView = CurrencyLoadView(this)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        cLoadView.layoutParams = params
        cLoadView.setState(CurrencyLoadView.STATE_EMPTY)
        if (!TextUtils.isEmpty(emptyTips)) {
            cLoadView.setEmptyText(emptyTips)
        }
        themeLoadView.removeAllViews()
        themeLoadView.addView(cLoadView)
        themeLoadView.visibility = View.VISIBLE
    }
}