package com.uroad.zhgs.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.util.ArrayMap
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.umeng.analytics.MobclickAgent
import com.uroad.library.utils.DisplayUtils
import com.uroad.library.utils.VersionUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.activity.*
import com.uroad.zhgs.adaptervp.MainMenuVpAdapter
import com.uroad.zhgs.common.BaseFragment
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.dialog.BindCarDialog
import com.uroad.zhgs.enumeration.UMEvent
import com.uroad.zhgs.helper.AppLocalHelper
import com.uroad.zhgs.model.CarMDL
import com.uroad.zhgs.model.sys.AppConfigMDL
import com.uroad.zhgs.model.sys.MainMenuMDL
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.fragment_mainmenu.*

/**
 * @author MFB
 * @create 2018/10/30
 * @describe 首页菜单
 */
class MainMenuFragment : BaseFragment() {

    private lateinit var handler: Handler
    private var onShopClickListener: OnShopClickListener? = null
    fun setOnShopClickListener(onShopClickListener: OnShopClickListener) {
        this.onShopClickListener = onShopClickListener
    }

    override fun setBaseLayoutResID(): Int = R.layout.fragment_mainmenu

    override fun setUp(view: View, savedInstanceState: Bundle?) {
        flBaseContent.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        initPageRv()
        handler = Handler(Looper.getMainLooper())
    }

    /*菜单列表*/
    private fun initPageRv() {
        val data = AppLocalHelper.getNaviData(context)
        if (!TextUtils.isEmpty(data)) {
            val mdLs = GsonUtils.fromJsonToList(data, MainMenuMDL::class.java)
            updatePageRv(mdLs)
        }
    }

    override fun initData() {
        doRequest(WebApiService.APP_CONFIG, WebApiService.getBaseParams(), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdLs = GsonUtils.fromDataToList(data, AppConfigMDL::class.java)
                    for (item in mdLs) {
                        if (TextUtils.equals(item.confid, AppConfigMDL.Type.NAV_MENU_VER.CODE)) {
                            checkNavVer(item)
                            break
                        }
                    }
                } else {
                    handler.postDelayed({ initData() }, MainFragment.DELAY_MILLIS)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                handler.postDelayed({ initData() }, MainFragment.DELAY_MILLIS)
            }
        })
    }

    /*判断菜单本地版本和服务器是否要更新*/
    private fun checkNavVer(configMDL: AppConfigMDL) {
        val currVer = AppLocalHelper.getNaviVer(context)
        if (VersionUtils.isNeedUpdate(configMDL.conf_ver, currVer)) {
            updateMenu(configMDL)
        }
    }

    /*获取最新菜单，并保存到本地*/
    private fun updateMenu(configMDL: AppConfigMDL) {
        doRequest(WebApiService.MAIN_MENU, WebApiService.getBaseParams(), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdLs = GsonUtils.fromDataToList(data, MainMenuMDL::class.java)
                    AppLocalHelper.saveNaviVer(context, configMDL.conf_ver)
                    AppLocalHelper.saveNaviData(context, GsonUtils.getData(data))
                    updatePageRv(mdLs)
                } else {
                    handler.postDelayed({ updateMenu(configMDL) }, MainFragment.DELAY_MILLIS)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                handler.postDelayed({ updateMenu(configMDL) }, MainFragment.DELAY_MILLIS)
            }
        })
    }

    private fun updatePageRv(mdLs: MutableList<MainMenuMDL>) {
        llIndicator.removeAllViews()
        val totalCount = mdLs.size
        val pageSize = 8
        val m = totalCount % pageSize
        val pageCount = if (m > 0) {
            totalCount / pageSize + 1
        } else {
            totalCount / pageSize
        }
        val arrayMap = ArrayMap<Int, MutableList<MainMenuMDL>>()
        for (i in 1..pageCount) {
            val list = if (m == 0) {
                mdLs.subList((i - 1) * pageSize, pageSize * i)
            } else {
                if (i == pageCount) {
                    mdLs.subList((i - 1) * pageSize, totalCount)
                } else {
                    mdLs.subList((i - 1) * pageSize, pageSize * i)
                }
            }
            arrayMap[i - 1] = list
        }
        viewPager.adapter = MainMenuVpAdapter(context, arrayMap).apply {
            setOnPageItemClickListener(object : MainMenuVpAdapter.OnPageItemClickListener {
                override fun onPageItemClick(page: Int, itemPos: Int, mdl: MainMenuMDL) {
                    val key = mdl.menukey?.toLowerCase()
                    when {
                        TextUtils.equals(key, MainMenuMDL.LJLF) -> {//路径路费
                            MobclickAgent.onEvent(context, UMEvent.ROAD_TOLL.CODE)
                            openActivity(RoadTollActivity::class.java)
                        }
                        TextUtils.equals(key, MainMenuMDL.FWQ) -> {//服务区
                            MobclickAgent.onEvent(context, UMEvent.SERVICE_AREA.CODE)
                            openActivity(ServiceAreaActivity::class.java)
                        }
                        TextUtils.equals(key, MainMenuMDL.GSRX) -> {//高速热线
                            MobclickAgent.onEvent(context, UMEvent.HIGHWAY_HOTLINE.CODE)
                            openActivity(HighWayHotlineActivity::class.java)
                        }
                        TextUtils.equals(key, MainMenuMDL.ZXSC) -> {
                            onShopClickListener?.onShopClick()
                        }
                        TextUtils.equals(key, MainMenuMDL.CYBL) -> {
                            if (!isLogin()) openActivity(LoginActivity::class.java)
                            else {
                                MobclickAgent.onEvent(context, UMEvent.RIDERS_REPORT.CODE)
                                openActivity(RidersInteractionActivity::class.java)
                            }
                        }
                        TextUtils.equals(key, MainMenuMDL.WZCX) -> {//违法查询
                            MobclickAgent.onEvent(context, UMEvent.ILLEGAL_INQUIRY.CODE)
                            CurrApplication.BREAK_RULES_URL?.let { openWebActivity(it, mdl.menuname) }
                        }
                        TextUtils.equals(key, MainMenuMDL.GSZX) -> {//高速资讯
                            MobclickAgent.onEvent(context, UMEvent.LATEST_NEWS_MORE.CODE)
                            openActivity(NewsMainActivity::class.java)
                        }
                        TextUtils.equals(key, MainMenuMDL.CXCX) -> { //诚信查询
                            if (!isLogin()) openActivity(LoginActivity::class.java)
                            else {
                                MobclickAgent.onEvent(context, UMEvent.INTEGRITY_INQUIRY.CODE)
                                getMyCar()
                            }
                        }
                        TextUtils.equals(key, MainMenuMDL.GSZB) -> {
                            MobclickAgent.onEvent(context, UMEvent.HIGHWAY_ALIVE.CODE)
                            CurrApplication.ALIVE_URL?.let {
                                openActivity(X5WebViewActivity::class.java, Bundle().apply {
                                    putString("url", it)
                                    putString("title", mdl.menuname)
                                })
                            }
                        }
                    }
                }
            })
        }
        if (arrayMap.size <= 1) return
        val indicators = ArrayList<ImageView>()
        for (i in 0 until arrayMap.size) {
            val imageView = ImageView(context).apply { layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { rightMargin = DisplayUtils.dip2px(context, 5f) } }
            if (i == 0) imageView.setImageResource(R.mipmap.ic_indicator_selected)
            else imageView.setImageResource(R.mipmap.ic_indicator_default)
            indicators.add(imageView)
            llIndicator.addView(imageView)
        }
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(position: Int) {
                for (i in 0 until indicators.size) {
                    //选中的页面改变小圆点为选中状态，反之为未选中
                    if (position == i) indicators[i].setImageResource(R.mipmap.ic_indicator_selected)
                    else indicators[i].setImageResource(R.mipmap.ic_indicator_default)
                }
            }
        })
    }

    /*获取用户车辆（仅客车）*/
    private fun getMyCar() {
        doRequest(WebApiService.MYCAR, WebApiService.myCarParams(getUserId(), ""), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdLs = GsonUtils.fromDataToList(data, CarMDL::class.java)
                    if (mdLs.size > 0) {
                        CurrApplication.cars?.clear()
                        CurrApplication.cars = ArrayList<CarMDL>().apply { addAll(mdLs) }
                        openActivity(CarInquiryActivity::class.java)
                    } else {
                        BindCarDialog(context).setOnConfirmClickListener(object : BindCarDialog.OnConfirmClickListener {
                            override fun onConfirm(dialog: BindCarDialog) {
                                dialog.dismiss()
                                openActivity(BindCarActivity::class.java)
                            }
                        }).show()
                    }
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                endLoading()
                onHttpError(e)
            }
        })
    }

    interface OnShopClickListener {
        fun onShopClick()
    }
}