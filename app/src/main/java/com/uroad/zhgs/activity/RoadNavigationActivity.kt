package com.uroad.zhgs.activity

import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.fragment.DiagramFragment
import com.uroad.zhgs.fragment.NavStandardFragment
import kotlinx.android.synthetic.main.activity_road_navigation_main.*
import kotlinx.android.synthetic.main.layout_menu_right.*
import android.widget.*
import com.uroad.zhgs.R
import com.uroad.zhgs.dialog.NewFunctionDialog
import com.uroad.zhgs.helper.AppLocalHelper

/**
 *Created by MFB on 2018/8/9.
 * 路况导航首页
 */
class RoadNavigationActivity : BaseActivity() {

    private var standardFragment: NavStandardFragment? = null
    private var diagramFragment: DiagramFragment? = null
    private var currTab = 1
    private var isShow = false
    private var checkType = 0
    private var fromHome: Boolean = false

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_road_navigation_main)
        intent.extras?.let { fromHome = it.getBoolean("fromHome") }
        ivDiagramBack.setOnClickListener { onBackPressed() }
        initMenu()
        initNearBy()
        if (fromHome) {
            setCurrentTab(1)
        } else {
            val tab = AppLocalHelper.getLayer(this)
            if (tab == 1) {
                setCurrentTab(1)
            } else {
                setCurrentTab(2)
            }
        }
        if (AppLocalHelper.isFirstNav(this)) {
            NewFunctionDialog(this).show()
        }
    }

    private fun setCurrentTab(currTab: Int) {
        this.currTab = currTab
        val transaction = supportFragmentManager.beginTransaction()
        highFragments(transaction)
        resetTab()
        cbEventYD.visibility = View.GONE
        llOther.visibility = View.GONE
        ivNearBy.visibility = View.GONE
        when (currTab) {
            1 -> {
                if (standardFragment == null) {
                    standardFragment = NavStandardFragment().apply {
                        arguments = intent.extras
                        transaction.add(R.id.container, this)
                    }
                } else {
                    standardFragment?.let { transaction.show(it) }
                }
                cbEventYD.visibility = View.VISIBLE
                llOther.visibility = View.VISIBLE
                ivNearBy.visibility = View.VISIBLE
                AppLocalHelper.saveLayer(this@RoadNavigationActivity, 1)
                ivStandard.setBackgroundResource(R.drawable.bg_corners_1dp)
                tvStandard.setTextColor(ContextCompat.getColor(this@RoadNavigationActivity, R.color.colorAccent))
                llEventLayout1.visibility = View.VISIBLE
                cbTrafficSpot1.visibility = View.VISIBLE
                ivLocation.visibility = View.VISIBLE
            }
            2 -> {
                if (diagramFragment == null) {
                    diagramFragment = DiagramFragment().apply { transaction.add(R.id.container, this) }
                    diagramChecked()
                } else {
                    diagramFragment?.let { transaction.show(it) }
                }
                AppLocalHelper.saveLayer(this@RoadNavigationActivity, 2)
                ivDiagram.setBackgroundResource(R.drawable.bg_corners_1dp)
                tvDiagram.setTextColor(ContextCompat.getColor(this@RoadNavigationActivity, R.color.colorAccent))
                ivDiagramBack.visibility = View.VISIBLE
                llEventLayout2.visibility = View.VISIBLE
                llTraffic.visibility = View.VISIBLE
            }
        }
        transaction.commit()
    }

    private fun resetTab() {
        ivStandard.setBackgroundResource(0)
        tvStandard.setTextColor(ContextCompat.getColor(this@RoadNavigationActivity, R.color.grey))
        ivDiagram.setBackgroundResource(0)
        tvDiagram.setTextColor(ContextCompat.getColor(this@RoadNavigationActivity, R.color.grey))
        llEventLayout1.visibility = View.GONE
        llEventLayout2.visibility = View.GONE
        cbTrafficSpot1.visibility = View.GONE
        llTraffic.visibility = View.GONE
        ivDiagramBack.visibility = View.GONE
        ivLocation.visibility = View.GONE
    }

    //简图默认显示图层：事故、管制、施工、收费站、服务区、监控，图层图标需默认点亮状态
    private fun diagramChecked() {
        cbEventSG2.isChecked = true
        cbEventGZ2.isChecked = true
        cbEventShiG2.isChecked = false
        cbTrafficPile.isChecked = false
        cbTrafficToll.isChecked = true
        cbTrafficService.isChecked = true
        cbTrafficSpot.isChecked = true
    }

    private fun highFragments(transaction: FragmentTransaction) {
        standardFragment?.let { transaction.hide(it) }
        diagramFragment?.let { transaction.hide(it) }
    }

    //侧滑菜单初始化
    private fun initMenu() {
        setDrawerEdgeSize(0.7f)
        val modeClickListener = View.OnClickListener {
            //  if (drawerLayout.isDrawerOpen(Gravity.END)) drawerLayout.closeDrawer(Gravity.END)
            when (it.id) {
                R.id.llStandard -> setCurrentTab(1)
                R.id.llDiagram -> setCurrentTab(2)
            }
        }
        llStandard.setOnClickListener(modeClickListener)
        llDiagram.setOnClickListener(modeClickListener)
        val onCheckChangeListener = CompoundButton.OnCheckedChangeListener { cb, isChecked ->
            //   if (drawerLayout.isDrawerOpen(Gravity.END)) drawerLayout.closeDrawer(Gravity.END)
            when (cb.id) {
                R.id.cbEventSG -> onStandardEvent(1, isChecked)
                R.id.cbEventGZ -> onStandardEvent(2, isChecked)
                R.id.cbEventShiG -> onStandardEvent(3, isChecked)
                R.id.cbEventYD -> onStandardEvent(4, isChecked)  //TRAFFIC_JAM  地图才有
                R.id.cbEventSG2 -> onDiagram(1, isChecked) //简图
                R.id.cbEventGZ2 -> onDiagram(2, isChecked)
                R.id.cbEventShiG2 -> onDiagram(3, isChecked)
                R.id.cbTrafficSpot1 -> onStandardEvent(5, isChecked)  //地图快拍
                R.id.cbTrafficPile -> onDiagram(4, isChecked)   //桩号  简图才有
                R.id.cbTrafficToll -> onDiagram(5, isChecked)
                R.id.cbTrafficService -> onDiagram(6, isChecked)
                R.id.cbTrafficSpot -> onDiagram(7, isChecked)
                R.id.cbOtherWeather -> onStandardEvent(6, isChecked) //WEATHER    地图才有
            }
        }
        cbEventSG.setOnCheckedChangeListener(onCheckChangeListener)
        cbEventGZ.setOnCheckedChangeListener(onCheckChangeListener)
        cbEventShiG.setOnCheckedChangeListener(onCheckChangeListener)
        cbEventYD.setOnCheckedChangeListener(onCheckChangeListener)
        cbEventSG2.setOnCheckedChangeListener(onCheckChangeListener)
        cbEventGZ2.setOnCheckedChangeListener(onCheckChangeListener)
        cbEventShiG2.setOnCheckedChangeListener(onCheckChangeListener)
        cbTrafficSpot1.setOnCheckedChangeListener(onCheckChangeListener)
        cbTrafficPile.setOnCheckedChangeListener(onCheckChangeListener)
        cbTrafficToll.setOnCheckedChangeListener(onCheckChangeListener)
        cbTrafficService.setOnCheckedChangeListener(onCheckChangeListener)
        cbTrafficSpot.setOnCheckedChangeListener(onCheckChangeListener)
        cbOtherWeather.setOnCheckedChangeListener(onCheckChangeListener)
        if (fromHome) {  //从首页我的订阅点击进来  关闭所有默认开启
            cbEventSG.isChecked = false
            cbEventGZ.isChecked = false
            cbEventShiG.isChecked = false
            cbEventYD.isChecked = false
        } else {
            //路况导航-地图模式默认开启图层：事故、管制、拥堵、快拍
            cbEventSG.isChecked = true
            cbEventGZ.isChecked = true
            cbEventShiG.isChecked = false
            cbEventYD.isChecked = true
            cbTrafficSpot1.isChecked = true
        }
    }

    //地图回调
    private fun onStandardEvent(type: Int, isChecked: Boolean) {
        standardFragment?.let { if (it.isAdded) it.onEvent(type, isChecked) }
    }

    //简图回调
    private fun onDiagram(type: Int, isChecked: Boolean) {
        diagramFragment?.let { if (it.isAdded) it.onEvent(type, isChecked) }
    }

    //侧滑菜单占屏幕的7/10
    private fun setDrawerEdgeSize(displayWidthPercentage: Float) {
        val params = rightMenu.layoutParams
        params.width = (DisplayUtils.getWindowWidth(this) * displayWidthPercentage).toInt()
        rightMenu.layoutParams = params
    }

    private fun initNearBy() {
        ivNearBy.setOnClickListener {
            isShow = !isShow
            rlAddBill.visibility = if (isShow) View.VISIBLE else View.GONE
        }
        rlAddBill.setOnClickListener { hideBill() }
        tvMenuList.setOnClickListener { openActivity(MyNearByTabActivity::class.java) }
        radioGroup.setOnCheckedChangeListener { _, checkId ->
            if (checkType > 0) onStandardEvent(checkType, false)
            when (checkId) {
                R.id.rbRepair -> {
                    checkType = 7
                    onStandardEvent(7, true)
                }
                R.id.rbGas -> {
                    checkType = 8
                    onStandardEvent(8, true)
                }
                R.id.rbScenic -> {
                    checkType = 9
                    onStandardEvent(9, true)
                }
                R.id.rbService -> {
                    checkType = 10
                    onStandardEvent(10, true)
                }
                R.id.rbToll -> {
                    checkType = 11
                    onStandardEvent(11, true)
                }
            }
        }
    }

    private fun hideBill() {
        rlAddBill.visibility = View.GONE
        isShow = false
    }

    override fun setListener() {
        tvLayer.setOnClickListener {
            if (drawerLayout.isDrawerOpen(Gravity.END)) {
                drawerLayout.closeDrawer(Gravity.END)
            } else {
                drawerLayout.openDrawer(Gravity.END)
            }
        }
        tvList.setOnClickListener { openActivity(HighWayListActivty::class.java) }
        ivLocation.setOnClickListener { onStandard(1) }
        tvPlus.setOnClickListener {
            if (currTab == 1) {
                onStandard(2)
            } else {
                diagramZoom(true)
            }
        }
        tvMinus.setOnClickListener {
            if (currTab == 1) {
                onStandard(3)
            } else {
                diagramZoom(false)
            }
        }
        flBottom.setOnClickListener {
            if (!isLogin()) openActivity(LoginActivity::class.java)
            else {
                openActivity(RidersInteractionActivity::class.java, Bundle().apply { putBoolean("anim", true) })
                overridePendingTransition(R.anim.slide_bottom_in, 0)
            }
        }
    }

    //地图模式 定位回调、放大缩小
    private fun onStandard(type: Int) {
        standardFragment?.let {
            if (it.isAdded) {
                when (type) {
                    1 -> it.onLocation()
                    2 -> it.enlargeMap()
                    3 -> it.narrowMap()
                }
            }
        }
    }

    //简图放大缩小回调
    private fun diagramZoom(enLarge: Boolean) {
        diagramFragment?.let {
            if (it.isAdded) {
                if (enLarge) it.enlargeSVG()
                else it.narrowSVG()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && rlAddBill.visibility != View.GONE) {
            hideBill()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}