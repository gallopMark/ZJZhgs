package com.uroad.zhgs.adapteRv

import android.content.Context
import com.amap.api.services.core.PoiItem
import com.uroad.zhgs.R
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 * @author MFB
 * @create 2018/10/18
 * @describe 高德API搜索的PoiItem列表适配器
 */
class AMapPoiAdapter(context: Context, mDatas: MutableList<PoiItem>)
    : BaseArrayRecyclerAdapter<PoiItem>(context, mDatas) {
    override fun bindView(viewType: Int): Int = R.layout.item_amap_poiitem
    override fun onBindHoder(holder: RecyclerHolder, t: PoiItem, position: Int) {
        holder.setText(R.id.tv, t.title)
    }
}