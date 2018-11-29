package com.uroad.zhgs.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.uroad.library.utils.BitmapUtils
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.adapteRv.RoadTollFeeAdapter
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.helper.RoadTollSearchHelper
import com.uroad.zhgs.model.RoadTollFeeMDL
import com.uroad.zhgs.model.RoadTollGSMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_roadtoll.*

/**
 * @author MFB
 * @create 2018/9/19
 * @describe 路径路费
 */
class RoadTollActivity : BaseActivity() {
    private val requestStart = 0x0001
    private val requestEnd = 0x0002
    private var startPoiId: String? = null
    private var endPoiId: String? = null
    private var mDatas = ArrayList<RoadTollFeeMDL.Fee>()
    private lateinit var adapter: RoadTollFeeAdapter

    private inner class HistoryAdapter(private val context: Context, mDatas: MutableList<String>)
        : BaseArrayRecyclerAdapter<String>(context, mDatas) {
        override fun onBindHoder(holder: RecyclerHolder, t: String, position: Int) {
            holder.setText(R.id.tvContent, RoadTollSearchHelper.content(t))
            holder.setOnClickListener(R.id.ivDelete, View.OnClickListener {
                RoadTollSearchHelper.deleteItem(context, t)
                mDatas.remove(t)
                notifyDataSetChanged()
                if (mDatas.size == 0) llHistory.visibility = View.GONE
            })
            holder.itemView.setOnClickListener {
                startPoiId = RoadTollSearchHelper.getStartPoiId(t)
                endPoiId = RoadTollSearchHelper.getEndPoiId(t)
                etStart.setText(RoadTollSearchHelper.getStartPoi(t))
                etEnd.setText(RoadTollSearchHelper.getEndPoi(t))
            }
        }

        override fun bindView(viewType: Int): Int {
            return R.layout.item_search_history
        }
    }

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_roadtoll)
        requestWindowFullScreen()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            ivBack.layoutParams = (ivBack.layoutParams as FrameLayout.LayoutParams).apply { topMargin = DisplayUtils.getStatusHeight(this@RoadTollActivity) }
        ivBack.setOnClickListener { onBackPressed() }
        setTopImage()
        initHistoryRv()
        initRv()
    }

    //重新计算图片高度 避免图片压缩
    private fun setTopImage() {
        val width = DisplayUtils.getWindowWidth(this)
        val height = (width * 0.7).toInt()
        ivTopPic.layoutParams = ivTopPic.layoutParams.apply {
            this.width = width
            this.height = height
        }
        ivTopPic.scaleType = ImageView.ScaleType.FIT_XY
        ivTopPic.setImageBitmap(BitmapUtils.decodeSampledBitmapFromResource(resources, R.mipmap.ic_roadtoll_top_bg, width, height))
    }

    private fun initHistoryRv() {
        rvHistory.layoutManager = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.VERTICAL }
        val data = RoadTollSearchHelper.getHistoryList(this)
        val adapter = HistoryAdapter(this, data)
        rvHistory.adapter = adapter
        if (data.size > 0) {
            llHistory.visibility = View.VISIBLE
        } else {
            llHistory.visibility = View.GONE
        }
        tvClear.setOnClickListener {
            RoadTollSearchHelper.clear(this)
            data.clear()
            adapter.notifyDataSetChanged()
            llHistory.visibility = View.GONE
        }
    }

    private fun initRv() {
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        adapter = RoadTollFeeAdapter(this, mDatas)
        recyclerView.adapter = adapter
    }

    override fun setListener() {
        etStart.setOnClickListener {
            openActivityForResult(RoadTollSearchActivity::class.java, Bundle().apply { putInt("type", 1) }, requestStart)
            overridePendingTransition(0, 0)
        }
        etEnd.setOnClickListener {
            openActivityForResult(RoadTollSearchActivity::class.java, Bundle().apply { putInt("type", 2) }, requestEnd)
            overridePendingTransition(0, 0)
        }
        ivChange.setOnClickListener {
            val tempT = etStart.text
            etStart.text = etEnd.text
            etEnd.text = tempT
            val tempId = startPoiId
            startPoiId = endPoiId
            endPoiId = tempId
        }
        btSearch.setOnClickListener { onSearch() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            requestStart -> {
                if (resultCode == RESULT_OK) {
                    data?.let {
                        startPoiId = it.getStringExtra("poiId")
                        etStart.setText(it.getStringExtra("poiName"))
                    }
                }
            }
            requestEnd -> {
                if (resultCode == RESULT_OK) {
                    data?.let {
                        endPoiId = it.getStringExtra("poiId")
                        etEnd.setText(it.getStringExtra("poiName"))
                    }
                }
            }
        }
    }

    private fun onSearch() {
        if (TextUtils.isEmpty(startPoiId) || TextUtils.isEmpty(etStart.text.toString())) {
            showShortToast(getString(R.string.roadToll_select_startPoi_hint))
        } else if (TextUtils.isEmpty(endPoiId) || TextUtils.isEmpty(etEnd.text.toString())) {
            showShortToast(getString(R.string.roadToll_select_endPoi_hint))
        } else {
            val startPoi = RoadTollGSMDL.Poi().apply {
                poiid = startPoiId
                name = etStart.text.toString()
            }
            val endPoi = RoadTollGSMDL.Poi().apply {
                poiid = endPoiId
                name = etEnd.text.toString()
            }
            RoadTollSearchHelper.saveContent(this, startPoi, endPoi)
            search()
        }
    }

    private fun search() {
        doRequest(WebApiService.QUERY_ROAD_TOLL, WebApiService.queryRoadTollParams(startPoiId, endPoiId), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                llResult.visibility = View.GONE
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    if (llHistory.visibility != View.GONE) llHistory.visibility = View.GONE
                    val mdl = GsonUtils.fromDataBean(data, RoadTollFeeMDL::class.java)
                    if (mdl == null) showShortToast(getString(R.string.page_nodata))
                    else updateData(mdl)
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

    private fun updateData(mdl: RoadTollFeeMDL) {
        if (llResult.visibility != View.VISIBLE) llResult.visibility = View.VISIBLE
        var length = getString(R.string.roadToll_shortest_length)
        mdl.length?.let { length += it }
        tvDistance.text = length
        mDatas.clear()
        mdl.fee?.let {
            mDatas.addAll(it)
            adapter.notifyDataSetChanged()
        }
    }
}