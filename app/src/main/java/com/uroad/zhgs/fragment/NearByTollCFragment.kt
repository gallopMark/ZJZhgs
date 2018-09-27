package com.uroad.zhgs.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import com.uroad.library.utils.NetworkUtils
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
                onBefore()
            }

            override fun onSuccess(data: String?) {
                onSuccess()
                if (GsonUtils.isResultOk(data)) {
                    val list = GsonUtils.fromDataToList(data, TollGateMDL::class.java)
                    update(list)
                } else {
                    onError()
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                onError()
            }
        })
    }

    private fun onBefore() {
        loading.visibility = View.VISIBLE
        tvError.visibility = View.GONE
        tvEmpty.visibility = View.GONE
        recyclerView.visibility = View.GONE
    }

    private fun onSuccess() {
        loading.visibility = View.GONE
        tvError.visibility = View.GONE
        tvEmpty.visibility = View.GONE
    }

    private fun onError() {
        loading.visibility = View.GONE
        tvError.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        recyclerView.visibility = View.GONE
        val text: String
        val drawable: Drawable?
        if (!NetworkUtils.isConnected(context)) {
            text = context.getString(R.string.nonetwork) + "请点击重试"
            drawable = ContextCompat.getDrawable(context, R.mipmap.ic_nonetwork)
        } else {
            text = context.getString(R.string.connect_error) + "请点击重试"
            drawable = ContextCompat.getDrawable(context, R.mipmap.ic_connect_error)
        }
        val start = text.indexOf("请")
        val end = text.length
        val clickSpan = object : ClickableSpan() {
            override fun onClick(p0: View?) {
                initData()
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
            }
        }
        val ss = SpannableString(text).apply {
            setSpan(clickSpan, start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorAccent)), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        tvError.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        tvError.text = ss
        tvError.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun update(list: MutableList<TollGateMDL>) {
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

    fun onLocationUpdate(longitude: Double, latitude: Double) {
        this.longitude = longitude
        this.latitude = latitude
        doRequest(WebApiService.MAP_DATA, WebApiService.mapDataByTypeParams(MapDataType.TOLL_GATE.code,
                longitude, latitude, "", "home"), object : HttpRequestCallback<String>() {

            override fun onSuccess(data: String?) {
                onSuccess()
                if (GsonUtils.isResultOk(data)) {
                    val list = GsonUtils.fromDataToList(data, TollGateMDL::class.java)
                    update(list)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
            }
        })
    }
}