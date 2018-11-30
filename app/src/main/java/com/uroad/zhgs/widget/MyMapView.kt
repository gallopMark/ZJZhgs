package com.uroad.zhgs.widget

import android.content.Context
import android.util.AttributeSet
import com.amap.api.maps.AMap
import com.amap.api.maps.MapView
import com.uroad.library.utils.DisplayUtils

/**
 *Created by MFB on 2018/7/28.
 */
class MyMapView : MapView {
    private lateinit var amap: AMap

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    private fun initView() {
        amap = map.apply {
            uiSettings.isMyLocationButtonEnabled = false// 设置默认定位按钮是否显示
            uiSettings.isZoomControlsEnabled = false // 设置缩放控制键不可见
            uiSettings.isTiltGesturesEnabled = false// 禁用倾斜手势
            uiSettings.isRotateGesturesEnabled = false// 禁用旋转手势 }
            //隐藏左下角Logo
            uiSettings.setLogoBottomMargin(-DisplayUtils.dip2px(context, 50f))
            isTrafficEnabled = true    //显示路况信息
        }
    }
}