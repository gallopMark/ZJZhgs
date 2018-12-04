package com.uroad.zhgs.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import com.amap.api.location.AMapLocation
import com.uroad.zhgs.R
import com.uroad.zhgs.adapteRv.RoadTollGSAdapter
import com.uroad.zhgs.adapteRv.RoadTollZDAdapter
import com.uroad.zhgs.common.BaseLocationActivity
import com.uroad.zhgs.helper.RoadTollSearchHelper
import com.uroad.zhgs.model.RoadTollGSMDL
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.utils.InputMethodUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_roadtoll_search.*

/**
 * @author MFB
 * @create 2018/9/19
 * @describe 路径路费 高速站点查询页面
 */
class RoadTollSearchActivity : BaseLocationActivity() {
    private var type: Int = 1
    private var keyword: String = ""
    private var longitude: Double = 0.toDouble()
    private var latitude: Double = 0.toDouble()
    private var firstLoad = true  //是否是首次加载
    private var historyMDL: RoadTollGSMDL? = null
    private var mDatas: MutableList<RoadTollGSMDL>? = null
    private val gsData = ArrayList<RoadTollGSMDL>()
    private val zdData = ArrayList<RoadTollGSMDL.Poi>()
    private lateinit var gsAdapter: RoadTollGSAdapter
    private lateinit var zdAdapter: RoadTollZDAdapter

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_roadtoll_search)
        withTitle(getString(R.string.roadToll_title))
        intent.extras?.let { type = it.getInt("type", 1) }
        if (type == 1) {
            etContent.hint = getString(R.string.roadToll_startPoi_hint)
            tvTips.text = getString(R.string.roadToll_select_startPoi_hint)
        } else {
            etContent.hint = getString(R.string.roadToll_endPoi_hint)
            tvTips.text = getString(R.string.roadToll_select_endPoi_hint)
        }
        initSearch()
        initRv()
        initHistory()
        requestLocationPermissions(object : RequestLocationPermissionCallback {
            override fun doAfterGrand() {
                openLocation()
            }

            override fun doAfterDenied() {
                onRequestTollGate()
            }
        })
    }

    private fun initSearch() {
        etContent.addTextChangedListener(object : TextWatcher {
            private var isFirstChange = true
            override fun afterTextChanged(p0: Editable) {
            }

            override fun beforeTextChanged(cs: CharSequence, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(cs: CharSequence, p1: Int, p2: Int, p3: Int) {
                if (isFirstChange) isFirstChange = false
                else {
                    if (TextUtils.isEmpty(cs.trim())) {
                        mDatas?.let { updateData(it) }
                    }
                }
            }
        })
        ivSearch.setOnClickListener {
            InputMethodUtils.hideSoftInput(this@RoadTollSearchActivity, etContent)
            keyword = etContent.text.toString()
            onRequestTollGate()
        }
    }

    private fun initRv() {
        rvGS.layoutManager = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.VERTICAL }
        rvZD.layoutManager = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.VERTICAL }
        gsAdapter = RoadTollGSAdapter(this, gsData)
        rvGS.adapter = gsAdapter
        zdAdapter = RoadTollZDAdapter(this, zdData)
        rvZD.adapter = zdAdapter
        gsAdapter.setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                if (position in 0 until gsData.size) {
                    gsAdapter.setSelectPos(position)
                    gsData[position].pois?.let {
                        zdData.clear()
                        zdData.addAll(it)
                        zdAdapter.notifyDataSetChanged()
                    }
                }
            }
        })
        zdAdapter.setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                if (position in 0 until zdData.size) {
                    onResult(zdData[position])
                }
            }
        })
    }

    private fun initHistory() {
        val data = RoadTollSearchHelper.getHistoryList(this)
        val list = ArrayList<RoadTollGSMDL.Poi>()
        for (item in data) {
            val startMDL = RoadTollGSMDL.Poi().apply {
                poiid = RoadTollSearchHelper.getStartPoiId(item)
                name = RoadTollSearchHelper.getStartPoi(item)
            }
            val endMDL = RoadTollGSMDL.Poi().apply {
                poiid = RoadTollSearchHelper.getEndPoiId(item)
                name = RoadTollSearchHelper.getEndPoi(item)
            }
            if (!list.contains(startMDL)) list.add(startMDL)
            if (!list.contains(endMDL)) list.add(endMDL)
        }
        if (list.size > 0) {
            historyMDL = RoadTollGSMDL().apply {
                type = 0
                shortname = "我的历史"
                pois = list
            }
        }
    }

    /*选择站点后返回结果给上一级调用页面*/
    private fun onResult(poi: RoadTollGSMDL.Poi) {
        val intent = Intent().apply {
            putExtra("poiId", poi.poiid)
            putExtra("poiName", poi.name)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun afterLocation(location: AMapLocation) {
        this.longitude = location.longitude
        this.latitude = location.latitude
        onRequestTollGate()
        closeLocation()
    }

    override fun onLocationFail(errorInfo: String?) {
        onRequestTollGate()
    }

    private fun onRequestTollGate() {
        doRequest(WebApiService.TOLL_GATE_LIST, WebApiService.tollGateListParams(keyword,longitude, latitude), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdLs = GsonUtils.fromDataToList(data, RoadTollGSMDL::class.java)
                    updateData(mdLs)
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

    private fun updateData(mdLs: MutableList<RoadTollGSMDL>) {
        gsData.clear()
        zdData.clear()
        if (firstLoad) {
            mDatas = ArrayList<RoadTollGSMDL>().apply {
                historyMDL?.let { add(it) }
                addAll(mdLs)
                gsData.addAll(this)
            }
            firstLoad = false
        } else {
            gsData.addAll(mdLs)
        }
        if (gsData.size > 0) {
            llDefault.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
            gsData.addAll(mdLs)
            gsAdapter.setSelectPos(0)
            gsData[0].pois?.let {
                zdData.addAll(it)
                zdAdapter.notifyDataSetChanged()
            }
        } else {
            llDefault.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        }

    }
}