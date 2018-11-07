package com.uroad.zhgs.adapteRv

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.widget.TextView
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.model.sys.MainMenuMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

class MainMenuAdapter(private val context: Context, mDatas: MutableList<MainMenuMDL>)
    : BaseArrayRecyclerAdapter<MainMenuMDL>(context, mDatas) {
    override fun bindView(viewType: Int): Int = R.layout.item_mainmenu

    override fun onBindHoder(holder: RecyclerHolder, t: MainMenuMDL, position: Int) {
        val tvMenu = holder.obtainView<TextView>(R.id.tvMenu)
        tvMenu.text = t.menuname
        val iconName = t.iconname?.toLowerCase()
        val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
        if (position > 3) {
            params.topMargin = DisplayUtils.dip2px(context, 10f)
        } else {
            params.topMargin = 0
        }
        holder.itemView.layoutParams = params
        val drawable = when {
            TextUtils.equals(iconName, MainMenuMDL.LJLF_ICON) -> ContextCompat.getDrawable(context, R.mipmap.ic_menu_ljlf)
            TextUtils.equals(iconName, MainMenuMDL.FWQ_ICON) -> ContextCompat.getDrawable(context, R.mipmap.ic_menu_service)
            TextUtils.equals(iconName, MainMenuMDL.GSRX_ICON) -> ContextCompat.getDrawable(context, R.mipmap.ic_menu_gsrx)
            TextUtils.equals(iconName, MainMenuMDL.ZXSC_ICON) -> ContextCompat.getDrawable(context, R.mipmap.ic_menu_zxsc)
            TextUtils.equals(iconName, MainMenuMDL.CYBL_ICON) -> ContextCompat.getDrawable(context, R.mipmap.ic_menu_share)
            TextUtils.equals(iconName, MainMenuMDL.WZCX_ICON) -> ContextCompat.getDrawable(context, R.mipmap.ic_menu_wzcx)
            TextUtils.equals(iconName, MainMenuMDL.GSZX_ICON) -> ContextCompat.getDrawable(context, R.mipmap.ic_menu_gszx)
            TextUtils.equals(iconName, MainMenuMDL.CXCX_ICON) -> ContextCompat.getDrawable(context, R.mipmap.ic_menu_cxcx)
            TextUtils.equals(iconName, MainMenuMDL.GSZB_ICON) -> ContextCompat.getDrawable(context, R.mipmap.ic_menu_gszb)
            else -> null
        }
        tvMenu.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
    }
}