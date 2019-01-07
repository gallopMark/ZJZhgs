package com.uroad.zhgs.activity

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.enumeration.DiagramEventType
import com.uroad.zhgs.enumeration.MapDataType
import com.uroad.zhgs.helper.AppLocalHelper
import com.uroad.zhgs.helper.RoadNaviLayerHelper
import com.uroad.zhgs.model.NewsMDL
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService

/**
 *Created by MFB on 2018/8/9.
 * 路况导航首页
 */
class RoadNavigationActivity : BaseActivity() {

    //    private var standardFragment: NavStandardFragment? = null
//    private var diagramFragment: DiagramFragment? = null
    private var currTab = 1
    private var isShow = false
    private var fromHome: Boolean = false
    private lateinit var handler: Handler

    companion object {
        private const val TAG_STANDARD = "standard"
        private const val TAG_DIAGRAM = "diagram"
    }

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_road_navigation_main)
        intent.extras?.let { fromHome = it.getBoolean("fromHome") }
        initLayout()
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
        handler = Handler(Looper.getMainLooper())
    }

    private fun initLayout() {
        ivDiagramBack.setOnClickListener { onBackPressed() }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ivDiagramBack.layoutParams = (ivDiagramBack.layoutParams as FrameLayout.LayoutParams).apply {
                this.topMargin = this.topMargin + DisplayUtils.getStatusHeight(this@RoadNavigationActivity)
            }
            llLayer.layoutParams = (llLayer.layoutParams as FrameLayout.LayoutParams).apply {
                this.topMargin = this.topMargin + DisplayUtils.getStatusHeight(this@RoadNavigationActivity)
            }
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
                val standardFragment = supportFragmentManager.findFragmentByTag(TAG_STANDARD)
                if (standardFragment == null) {
                    transaction.add(R.id.container, NavStandardFragment().apply { arguments = intent.extras }, TAG_STANDARD)
                } else {
                    transaction.show(standardFragment)
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
                val diagramFragment = supportFragmentManager.findFragmentByTag(TAG_DIAGRAM)
                if (diagramFragment == null) {
                    transaction.add(R.id.container, DiagramFragment(), TAG_DIAGRAM)
                    diagramChecked()
                } else {
                    transaction.show(diagramFragment)
                }
                AppLocalHelper.saveLayer(this@RoadNavigationActivity, 2)
                ivDiagram.setBackgroundResource(R.drawable.bg_corners_1dp)
                tvDiagram.setTextColor(ContextCompat.getColor(this@RoadNavigationActivity, R.color.colorAccent))
                ivDiagramBack.visibility = View.VISIBLE
                llEventLayout2.visibility = View.VISIBLE
                llTraffic.visibility = View.VISIBLE
            }
        }
        transaction.commitAllowingStateLoss()
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
        cbEventSG2.isChecked = RoadNaviLayerHelper.isDiagramAccidentChecked(this)
        cbEventGZ2.isChecked = RoadNaviLayerHelper.isDiagramControlChecked(this)
        cbEventShiG2.isChecked = RoadNaviLayerHelper.isDiagramConstructionChecked(this)
        cbEventYD2.isChecked = RoadNaviLayerHelper.isDiagramJamChecked(this)
        cbEventZDGZ.isChecked = RoadNaviLayerHelper.isDiagramSiteControlChecked(this)
        cbEventELTQ2.isChecked = RoadNaviLayerHelper.isDiagramBadWeatherChecked(this)
        cbEventJTSG2.isChecked = RoadNaviLayerHelper.isDiagramTrafficAccChecked(this)
        cbTrafficPile.isChecked = RoadNaviLayerHelper.isDiagramPileChecked(this)
        cbTrafficToll.isChecked = RoadNaviLayerHelper.isDiagramTollChecked(this)
        cbTrafficService.isChecked = RoadNaviLayerHelper.isDiagramServiceChecked(this)
        cbTrafficSpot2.isChecked = RoadNaviLayerHelper.isDiagramMonitorChecked(this)
    }

    private fun highFragments(transaction: FragmentTransaction) {
        for (fragment in supportFragmentManager.fragments) transaction.hide(fragment)
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
                R.id.cbEventSG -> {
                    RoadNaviLayerHelper.onMapAccidentChecked(this@RoadNavigationActivity, isChecked)
                    onStandardEvent(MapDataType.ACCIDENT.code, isChecked)
                }
                R.id.cbEventGZ -> {
                    RoadNaviLayerHelper.onMapControlChecked(this@RoadNavigationActivity, isChecked)
                    onStandardEvent(MapDataType.CONTROL.code, isChecked)
                }
                R.id.cbEventShiG -> {
                    RoadNaviLayerHelper.onMapConstructionChecked(this@RoadNavigationActivity, isChecked)
                    onStandardEvent(MapDataType.CONSTRUCTION.code, isChecked)
                }
                R.id.cbEventYD -> {
                    RoadNaviLayerHelper.onMapJamChecked(this@RoadNavigationActivity, isChecked)
                    onStandardEvent(MapDataType.TRAFFIC_JAM.code, isChecked)
                }
                R.id.cbTrafficSpot1 -> {
                    RoadNaviLayerHelper.onMapMonitorChecked(this@RoadNavigationActivity, isChecked)
                    onStandardEvent(MapDataType.SNAPSHOT.code, isChecked)  //地图快拍
                }
                R.id.cbOtherWeather -> {
                    RoadNaviLayerHelper.onMapWeatherChecked(this@RoadNavigationActivity, isChecked)
                    onStandardEvent(MapDataType.WEATHER.code, isChecked) //WEATHER    地图才有
                }
                R.id.cbEventELTQ -> {
                    RoadNaviLayerHelper.onMapBadWeatherChecked(this@RoadNavigationActivity, isChecked)
                    onStandardEvent(MapDataType.BAD_WEATHER.code, isChecked)  //恶劣天气
                }
                R.id.cbEventJTSG -> {
                    RoadNaviLayerHelper.onMapTrafficAccChecked(this@RoadNavigationActivity, isChecked)
                    onStandardEvent(MapDataType.TRAFFIC_INCIDENT.code, isChecked)  //交通事件
                }
                R.id.cbEventSG2 -> {
                    RoadNaviLayerHelper.onDiagramAccidentChecked(this@RoadNavigationActivity, isChecked)
                    onDiagramEvent(DiagramEventType.Accident.code, isChecked) //简图
                }
                R.id.cbEventGZ2 -> {
                    RoadNaviLayerHelper.onDiagramControlChecked(this@RoadNavigationActivity, isChecked)
                    onDiagramEvent(DiagramEventType.Control.code, isChecked)
                }
                R.id.cbEventShiG2 -> {
                    RoadNaviLayerHelper.onDiagramConstructionChecked(this@RoadNavigationActivity, isChecked)
                    onDiagramEvent(DiagramEventType.Construction.code, isChecked)
                }
                R.id.cbEventYD2 -> {
                    RoadNaviLayerHelper.onDiagramJamChecked(this@RoadNavigationActivity, isChecked)
                    onDiagramEvent(DiagramEventType.TrafficJam.code, isChecked)
                }
                R.id.cbTrafficPile -> {
                    RoadNaviLayerHelper.onDiagramPileChecked(this@RoadNavigationActivity, isChecked)
                    onDiagramEvent(DiagramEventType.PileNumber.code, isChecked)   //桩号  简图才有
                }
                R.id.cbTrafficToll -> {
                    RoadNaviLayerHelper.onDiagramTollChecked(this@RoadNavigationActivity, isChecked)
                    onDiagramEvent(DiagramEventType.TollGate.code, isChecked)
                }
                R.id.cbTrafficService -> {
                    RoadNaviLayerHelper.onDiagramServiceChecked(this@RoadNavigationActivity, isChecked)
                    onDiagramEvent(DiagramEventType.ServiceArea.code, isChecked)
                }
                R.id.cbTrafficSpot2 -> {
                    RoadNaviLayerHelper.onDiagramMonitorChecked(this@RoadNavigationActivity, isChecked)
                    onDiagramEvent(DiagramEventType.Snapshot.code, isChecked)
                }
                R.id.cbEventELTQ2 -> {
                    RoadNaviLayerHelper.onDiagramBadWeatherChecked(this@RoadNavigationActivity, isChecked)
                    onDiagramEvent(DiagramEventType.BadWeather.code, isChecked)
                }
                R.id.cbEventJTSG2 -> {
                    RoadNaviLayerHelper.onDiagramTrafficAccChecked(this@RoadNavigationActivity, isChecked)
                    onDiagramEvent(DiagramEventType.TrafficIncident.code, isChecked)
                }
                R.id.cbEventZDGZ -> {
                    RoadNaviLayerHelper.onDiagramSiteControlChecked(this@RoadNavigationActivity, isChecked)
                    onDiagramEvent(DiagramEventType.StationControl.code, isChecked)
                }
            }
        }
        cbEventSG.setOnCheckedChangeListener(onCheckChangeListener)  //地图（事故）
        cbEventGZ.setOnCheckedChangeListener(onCheckChangeListener) //地图（管制）
        cbEventShiG.setOnCheckedChangeListener(onCheckChangeListener)   //地图（施工）
        cbEventYD.setOnCheckedChangeListener(onCheckChangeListener) //地图（拥堵）
        cbEventELTQ.setOnCheckedChangeListener(onCheckChangeListener)   //地图（恶劣天气）
        cbEventJTSG.setOnCheckedChangeListener(onCheckChangeListener)   //地图（交通事件）
        cbTrafficSpot1.setOnCheckedChangeListener(onCheckChangeListener) //地图（监控）
        cbOtherWeather.setOnCheckedChangeListener(onCheckChangeListener)    //地图（天气）
        cbEventSG2.setOnCheckedChangeListener(onCheckChangeListener)    //简图（事故）
        cbEventGZ2.setOnCheckedChangeListener(onCheckChangeListener)    //简图（管制）
        cbEventShiG2.setOnCheckedChangeListener(onCheckChangeListener)  //简图（施工）
        cbEventYD2.setOnCheckedChangeListener(onCheckChangeListener)    //简图（拥堵）
        cbEventZDGZ.setOnCheckedChangeListener(onCheckChangeListener)   //简图（站点管制）
        cbEventELTQ2.setOnCheckedChangeListener(onCheckChangeListener)  //简图（恶劣天气）
        cbEventJTSG2.setOnCheckedChangeListener(onCheckChangeListener)  //简图（交通事件）
        cbTrafficPile.setOnCheckedChangeListener(onCheckChangeListener) //简图（桩号）
        cbTrafficToll.setOnCheckedChangeListener(onCheckChangeListener) //简图（收费站）
        cbTrafficService.setOnCheckedChangeListener(onCheckChangeListener)  //简图（服务区）
        cbTrafficSpot2.setOnCheckedChangeListener(onCheckChangeListener) //简图（监控）
        if (fromHome) {  //从首页我的订阅点击进来  关闭所有默认开启
            clearLayers()
        } else {
            openDefaultLayer()
        }
    }

    //路况导航-地图模式默认开启图层：交通事件、施工默认关闭，其他开启
    private fun openDefaultLayer() {
        cbEventSG.isChecked = RoadNaviLayerHelper.isMapAccidentChecked(this)
        cbEventGZ.isChecked = RoadNaviLayerHelper.isMapControlChecked(this)
        cbEventShiG.isChecked = RoadNaviLayerHelper.isMapConstructionChecked(this)
        cbEventYD.isChecked = RoadNaviLayerHelper.isMapJamChecked(this)
        cbEventELTQ.isChecked = RoadNaviLayerHelper.isMapBadWeatherChecked(this)
        cbEventJTSG.isChecked = RoadNaviLayerHelper.isMapTrafficAccChecked(this)
        cbTrafficSpot1.isChecked = RoadNaviLayerHelper.isMapMonitorChecked(this)
        cbOtherWeather.isChecked = RoadNaviLayerHelper.isMapWeatherChecked(this)
    }

    //地图回调
    private fun onStandardEvent(codeType: String, isChecked: Boolean) {
        val standardFragment = supportFragmentManager.findFragmentByTag(TAG_STANDARD)
        if (standardFragment != null && standardFragment.isAdded) {
            (standardFragment as NavStandardFragment).onEvent(codeType, isChecked)
        }
    }

    //简图回调
    private fun onDiagramEvent(codeType: String, isChecked: Boolean) {
        val diagramFragment = supportFragmentManager.findFragmentByTag(TAG_DIAGRAM)
        if (diagramFragment != null && diagramFragment.isAdded) {
            (diagramFragment as DiagramFragment).onEvent(codeType, isChecked)
        }
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
        val onCheckChangeListener = CompoundButton.OnCheckedChangeListener { cb, isChecked ->
            //点击附近 移除地图所有图层
            clearLayers()
            when (cb.id) {
                R.id.cbRepair -> onStandardEvent(MapDataType.REPAIR_SHOP.code, isChecked)
                R.id.cbGas -> onStandardEvent(MapDataType.GAS_STATION.code, isChecked)
                R.id.cbScenic -> onStandardEvent(MapDataType.SCENIC.code, isChecked)
                R.id.cbService -> onStandardEvent(MapDataType.SERVICE_AREA.code, isChecked)
                R.id.cbToll -> onStandardEvent(MapDataType.TOLL_GATE.code, isChecked)
            }
        }
        cbRepair.setOnCheckedChangeListener(onCheckChangeListener)
        cbGas.setOnCheckedChangeListener(onCheckChangeListener)
        cbScenic.setOnCheckedChangeListener(onCheckChangeListener)
        cbService.setOnCheckedChangeListener(onCheckChangeListener)
        cbToll.setOnCheckedChangeListener(onCheckChangeListener)
    }

    /*关闭地图图层（施工、管制、事故、拥堵、快拍、天气等）*/
    private fun clearLayers() {
        cbEventSG.isChecked = false
        cbEventGZ.isChecked = false
        cbEventShiG.isChecked = false
        cbEventYD.isChecked = false
        cbEventELTQ.isChecked = false
        cbEventJTSG.isChecked = false
        cbTrafficSpot1.isChecked = false
        cbOtherWeather.isChecked = false
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
        tvList.setOnClickListener { openActivity(HighWayListActivity::class.java) }
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
        val standardFragment = supportFragmentManager.findFragmentByTag(TAG_STANDARD)
        if (standardFragment != null && standardFragment.isAdded) {
            when (type) {
                1 -> (standardFragment as NavStandardFragment).onLocation()
                2 -> (standardFragment as NavStandardFragment).enlargeMap()
                3 -> (standardFragment as NavStandardFragment).narrowMap()
            }
        }
    }

    //简图放大缩小回调
    private fun diagramZoom(enLarge: Boolean) {
        val diagramFragment = supportFragmentManager.findFragmentByTag(TAG_DIAGRAM)
        if (diagramFragment != null && diagramFragment.isAdded) {
            if (enLarge) (diagramFragment as DiagramFragment).enlargeSVG()
            else (diagramFragment as DiagramFragment).narrowSVG()
        }
    }

    override fun initData() {
        doRequest(WebApiService.NEWS_LIST, WebApiService.newsListParams("1100001", 10, 1),
                object : HttpRequestCallback<String>() {
                    override fun onSuccess(data: String?) {
                        if (GsonUtils.isResultOk(data)) {
                            val dataMDLs = GsonUtils.fromDataToList(data, NewsMDL::class.java)
                            if (dataMDLs.size > 0) updateData(dataMDLs)
                        } else {
                            handler.postDelayed({ initData() }, CurrApplication.DELAY_MILLIS)
                        }
                    }

                    override fun onFailure(e: Throwable, errorMsg: String?) {
                        handler.postDelayed({ initData() }, CurrApplication.DELAY_MILLIS)
                    }
                })
    }

    private fun updateData(dataMDLs: MutableList<NewsMDL>) {
        llNotice.visibility = View.VISIBLE
        var content = ""
        for (item in dataMDLs) item.title?.let { content += "$it\u3000\u3000\u3000\u3000\u3000\u3000" }
        tvNotice.text = content
        tvNotice.isSelected = true
        llNotice.setOnClickListener { openActivity(NoticeListActivity::class.java) }
        ivNoticeClose.setOnClickListener { llNotice.visibility = View.GONE }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && rlAddBill.visibility != View.GONE) {
            hideBill()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}