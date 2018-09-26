package com.uroad.zhgs.fragment

import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.uroad.zhgs.R
import com.uroad.zhgs.activity.MyNearByActivity
import com.uroad.zhgs.adapteRv.NearByTollAdapter
import com.uroad.zhgs.common.BaseFragment
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.enumeration.MapDataType
import com.uroad.zhgs.model.TollGateMDL
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.fragment_nearby_child.*

/**
 * @author MFB
 * @create 2018/9/26
 * @describe 首页附近收费站
 */
class NearByTollCFragment : BaseFragment() {
    private var longitude = CurrApplication.APP_LATLNG.longitude
    private var latitude = CurrApplication.APP_LATLNG.latitude
    private val mDatas = ArrayList<TollGateMDL>()
    private lateinit var adapter: NearByTollAdapter
    private val handler = Handler()
    override fun setBaseLayoutResID(): Int = R.layout.fragment_nearby_child
    override fun setUp(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            longitude = it.getDouble("longitude", longitude)
            latitude = it.getDouble("latitude", latitude)
        }
        recyclerView.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.HORIZONTAL }
        adapter = NearByTollAdapter(context, mDatas).apply {
            setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
                override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                    if (position in 0 until mDatas.size) {
                        openActivity(MyNearByActivity::class.java, Bundle().apply {
                            putInt("type", 1)
                            putSerializable("mdl", mDatas[position])
                        })
                    }
                }
            })
        }
        recyclerView.adapter = adapter
    }

    override fun initData() {
        doRequest(WebApiService.MAP_DATA, WebApiService.mapDataByTypeParams(MapDataType.TOLL_GATE.code,
                longitude, latitude, "", "home"), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                llLoading.visibility = View.VISIBLE
            }

            override fun onSuccess(data: String?) {
                llLoading.visibility = View.GONE
                if (GsonUtils.isResultOk(data)) {
                    val list = GsonUtils.fromDataToList(data, TollGateMDL::class.java)
                    updateToll(list)
                } else {
                    handler.postDelayed({ initData() }, 3000)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                llLoading.visibility = View.GONE
                handler.postDelayed({ initData() }, 3000)
            }
        })
    }

    private fun updateToll(list: MutableList<TollGateMDL>) {
        mDatas.clear()
        if (list.size > 0) {
            mDatas.addAll(list)
            adapter.notifyDataSetChanged()
            recyclerView.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
        } else {
            recyclerView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }
}