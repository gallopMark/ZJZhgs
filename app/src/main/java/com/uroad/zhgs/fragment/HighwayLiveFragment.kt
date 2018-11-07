package com.uroad.zhgs.fragment

import android.graphics.Rect
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.adapteRv.HighwayLiveAdapter
import com.uroad.zhgs.common.BasePageRefreshRvFragment
import com.uroad.zhgs.common.BaseRefreshRvFragment
import com.uroad.zhgs.model.SnapShotMDL

/**
 * @author MFB
 * @create 2018/10/8
 * @describe 高速直播
 */
class HighwayLiveFragment : BasePageRefreshRvFragment() {
    private val mDatas = ArrayList<SnapShotMDL>()
    private lateinit var adapter: HighwayLiveAdapter

    private class GridItemDecoration() : RecyclerView.ItemDecoration() {
        private var spanCount = 0
        private var space = 0

        constructor(spanCount: Int, space: Int) : this() {
            this.spanCount = spanCount
            this.space = space
        }

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            if (position == 0) {
                outRect.left = space
                outRect.right = space
                outRect.top = space
            } else {
                val column = (position - 1) % 2
                outRect.left = space - column * space / spanCount // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * space / spanCount
            }
            outRect.bottom = space
        }
    }

    override fun initViewData(view: View) {
        refreshLayout.isEnableRefresh = false
        refreshLayout.isEnableLoadMore = false
        recyclerView.addItemDecoration(GridItemDecoration(2, DisplayUtils.dip2px(context, 12f)))
        recyclerView.layoutManager = GridLayoutManager(context, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int = if (position == 0) 2 else 1
            }
        }
        mDatas.add(SnapShotMDL().apply {
            picurl = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1538996056022&di=d1791fc2660ddcf9615e61ca250cfba8&imgtype=0&src=http%3A%2F%2Fs1.ifengimg.com%2Fheadimg%2FwKgFk1KOuJOIFLgwAAMbFghh-LIAACHmgFWKy8AAxsu444_src.jpg"
            shortname = "沪昆高速(沪杭)"
            resname = "K135+701杭向"
        })
        mDatas.add(SnapShotMDL().apply {
            picurl = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1538996150209&di=53eb4dfcb237a2cbbc0da860b3cb5403&imgtype=0&src=http%3A%2F%2Fupload.techweb.com.cn%2F2014%2F0702%2F1404287804923.jpg"
            shortname = "沪昆高速(沪杭)"
            resname = "K135+701杭向"
        })
        mDatas.add(SnapShotMDL().apply {
            picurl = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1538996169931&di=0cf5b3c28127f7655ca6d7febf38533a&imgtype=0&src=http%3A%2F%2Fwww.aspku.com%2Fuploads%2Fallimg%2F180824%2F2301353200-0.png"
            shortname = "沪昆高速(沪杭)"
            resname = "K135+701杭向"
        })
        mDatas.add(SnapShotMDL().apply {
            picurl = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1539590921&di=a1199011b693e4ba4315749a2a192c66&imgtype=jpg&er=1&src=http%3A%2F%2Fimage.9game.cn%2F2017%2F1%2F14%2F15668497.jpg"
            shortname = "沪昆高速(沪杭)"
            resname = "K135+701杭向"
        })
        mDatas.add(SnapShotMDL().apply {
            picurl = "https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=2623937500,2578261986&fm=26&gp=0.jpg"
            shortname = "沪昆高速(沪杭)"
            resname = "K135+701杭向"
        })
        adapter = HighwayLiveAdapter(context, mDatas)
        recyclerView.adapter = adapter
    }

    override fun pullToRefresh() {
    }

    override fun pullToLoadMore() {
    }
}