package com.uroad.zhgs.activity

import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.view.KeyEvent
import android.view.View
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.fragment.MainFragment
import com.uroad.zhgs.fragment.MineFragment
import com.uroad.zhgs.fragment.ShoppingFragment
import kotlinx.android.synthetic.main.activity_main.*

//app首页
class MainActivity : BaseActivity() {
    private var mainFragment: MainFragment? = null
    private var shoppingFragment: ShoppingFragment? = null
    private var mineFragment: MineFragment? = null
    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_main)
        initTab()
        setCurrentTab(1)
    }

    private fun setCurrentTab(tab: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        hideFragments(transaction)
        when (tab) {
            1 -> {
                if (mainFragment == null) mainFragment = MainFragment().apply {
                    setOnMenuClickListener(object : MainFragment.OnMenuClickListener {
                        override fun onMenuClick() {
                            this@MainActivity.radioGroup.check(R.id.rbShop)
                        }
                    })
                    if (!this.isAdded) transaction.add(R.id.content, this)
                }
                else mainFragment?.let { transaction.show(it) }
            }
            2 -> {
                if (shoppingFragment == null) shoppingFragment = ShoppingFragment().apply { if (!this.isAdded) transaction.add(R.id.content, this) }
                else shoppingFragment?.let { transaction.show(it) }
            }
            3 -> {
                if (mineFragment == null) mineFragment = MineFragment().apply { if (!this.isAdded) transaction.add(R.id.content, this) }
                else mineFragment?.let { transaction.show(it) }
            }
        }
        transaction.commit()
    }

    private fun hideFragments(transaction: FragmentTransaction) {
        mainFragment?.let { transaction.hide(it) }
        shoppingFragment?.let { transaction.hide(it) }
        mineFragment?.let { transaction.hide(it) }
    }

    //tab切换
    private fun initTab() {
        radioGroup.setOnCheckedChangeListener { _, checkId ->
            vTab1.visibility = View.INVISIBLE
            vTab2.visibility = View.INVISIBLE
            vTab3.visibility = View.INVISIBLE
            when (checkId) {
                R.id.rbHome -> {
                    vTab1.visibility = View.VISIBLE
                    setCurrentTab(1)
                }
                R.id.rbShop -> {
                    vTab2.visibility = View.VISIBLE
                    setCurrentTab(2)
                }
                R.id.rbMine -> {
                    vTab3.visibility = View.VISIBLE
                    setCurrentTab(3)
                }
            }
        }
        radioGroup.check(R.id.rbHome)
    }

    //记录用户首次点击返回键的时间
    private var firstTime: Long = 0

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val secondTime = System.currentTimeMillis()
            if (secondTime - firstTime > 2000) {
                showShortToast("再按一次退出${getString(R.string.app_name)}")
                firstTime = secondTime
                return true
            } else {
                finish()
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
