package com.uroad.zhgs.fragment

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.View
import android.widget.RelativeLayout
import com.uroad.zhgs.R
import com.uroad.zhgs.R.id.flBaseContent
import com.uroad.zhgs.activity.MyNearByActivity
import com.uroad.zhgs.common.BaseFragment
import com.uroad.zhgs.common.BaseLocationFragment
import kotlinx.android.synthetic.main.fragment_mainnearby.*

/**
 * @author MFB
 * @create 2018/11/22
 * @describe 首页（我的附近）
 */
class MainNearByFragment : BaseLocationFragment() {

    private lateinit var tollFragment: NearByTollCFragment
    private lateinit var serviceFragment: NearByServiceCFragment
    private lateinit var scenicFragment: NearByScenicCFragment
    private var onRequestLocationListener: OnRequestLocationListener? = null

    fun setOnRequestLocationListener(onRequestLocationListener: OnRequestLocationListener?) {
        this.onRequestLocationListener = onRequestLocationListener
    }

    override fun setBaseLayoutResID(): Int = R.layout.fragment_mainnearby

    override fun setUp(view: View, savedInstanceState: Bundle?) {
        flBaseContent.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        initFragments()
        initTab()
        setTab(1)
    }

    private fun initTab() {
        val ts14 = context.resources.getDimension(R.dimen.font_14)
        val ts16 = context.resources.getDimension(R.dimen.font_16)
        tvNearByToll.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts16)
        tvNearByToll.isSelected = true
        tvNearByMore.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts16)
        tvNearByMore.isSelected = true
        val listener = View.OnClickListener { v ->
            tvNearByToll.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts14)
            tvNearByToll.isSelected = false
            tvNearByService.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts14)
            tvNearByService.isSelected = false
            tvNearByScenic.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts14)
            tvNearByScenic.isSelected = false
            when (v.id) {
                R.id.tvNearByToll -> {
                    tvNearByToll.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts16)
                    tvNearByToll.isSelected = true
                    setTab(1)
                }
                R.id.tvNearByService -> {
                    tvNearByService.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts16)
                    tvNearByService.isSelected = true
                    setTab(2)
                }
                R.id.tvNearByScenic -> {
                    tvNearByScenic.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts16)
                    tvNearByScenic.isSelected = true
                    setTab(3)
                }
            }
        }
        tvNearByToll.setOnClickListener(listener)
        tvNearByService.setOnClickListener(listener)
        tvNearByScenic.setOnClickListener(listener)
        tvNearByMore.setOnClickListener { openActivity(MyNearByActivity::class.java, Bundle().apply { putInt("type", 4) }) }
    }

    private fun initFragments() {
        tollFragment = NearByTollCFragment()
        serviceFragment = NearByServiceCFragment()
        scenicFragment = NearByScenicCFragment()
        val transaction = childFragmentManager.beginTransaction()
        if (!tollFragment.isAdded) transaction.add(R.id.flNearby, tollFragment)
        if (!serviceFragment.isAdded) transaction.add(R.id.flNearby, serviceFragment)
        if (!scenicFragment.isAdded) transaction.add(R.id.flNearby, scenicFragment)
        transaction.commitAllowingStateLoss()
    }

    /*我的附近tab 1->附近收费站 2->附近服务区 3->附近景点*/
    private fun setTab(tab: Int) {
        val transaction = childFragmentManager.beginTransaction()
        transaction.hide(tollFragment)
        transaction.hide(serviceFragment)
        transaction.hide(scenicFragment)
        when (tab) {
            1 -> transaction.show(tollFragment)
            2 -> transaction.show(serviceFragment)
            3 -> transaction.show(scenicFragment)
        }
        transaction.commitAllowingStateLoss()
    }

    fun locationUpdate(longitude: Double, latitude: Double) {
        if (tvLocationFailure.visibility != View.GONE) tvLocationFailure.visibility = View.GONE
        if (flNearby.visibility != View.VISIBLE) flNearby.visibility = View.VISIBLE
        if (tollFragment.isAdded) tollFragment.onLocationUpdate(longitude, latitude)
        if (serviceFragment.isAdded) serviceFragment.onLocationUpdate(longitude, latitude)
        if (scenicFragment.isAdded) scenicFragment.onLocationUpdate(longitude, latitude)
    }

    fun onLocationFailure() {
        tvLocationFailure.visibility = View.VISIBLE
        flNearby.visibility = View.INVISIBLE
        val text = context.resources.getString(R.string.home_location_failure_tips)
        val ss = SpannableString(text)
        val start = text.indexOf("，") + 1
        val end = text.length
        val clickSpan = object : ClickableSpan() {
            override fun onClick(p0: View?) {
                if (!hasLocationPermissions()) {
                    //申请位置权限时用户点击了“禁止不再提示”按钮 则引导用户到app设置页面重新打开
                    if (!isShouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_COARSE_LOCATION) || !isShouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                        openSettings()
                    } else {  //重新申请权限
                        onRequestLocationListener?.onRequest()
                    }
                } else {
                    flNearby.visibility = View.VISIBLE
                    tvLocationFailure.visibility = View.GONE
                    openLocation()
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
            }
        }
        ss.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorAccent)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvLocationFailure.text = ss
        tvLocationFailure.movementMethod = LinkMovementMethod.getInstance()
    }

    interface OnRequestLocationListener {
        fun onRequest()
    }
}