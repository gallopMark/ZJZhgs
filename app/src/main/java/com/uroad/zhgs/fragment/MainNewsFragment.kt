package com.uroad.zhgs.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.RelativeLayout
import com.uroad.zhgs.R
import com.uroad.zhgs.activity.NewsMainActivity
import com.uroad.zhgs.adapteRv.NewsAdapter
import com.uroad.zhgs.common.BaseFragment
import com.uroad.zhgs.model.NewsMDL
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.fragment_mainnews.*

/**
 * @author MFB
 * @create 2018/11/22
 * @describe 首页最新资讯
 */
class MainNewsFragment : BaseFragment() {
    private val mdLs = ArrayList<NewsMDL>()     //推荐资讯数据集合
    private lateinit var adapter: NewsAdapter   //资讯列表适配器
    private lateinit var handler: Handler
    private var onRequestCallback: OnRequestCallback? = null

    fun setOnRequestCallback(onRequestCallback: OnRequestCallback?) {
        this.onRequestCallback = onRequestCallback
    }

    override fun setBaseLayoutResID(): Int = R.layout.fragment_mainnews

    override fun setUp(view: View, savedInstanceState: Bundle?) {
        flBaseContent.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        initRv()
        tvMore.setOnClickListener { openActivity(NewsMainActivity::class.java) }//更多资讯
        handler = Handler(Looper.getMainLooper())
    }

    private fun initRv() {
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
        adapter = NewsAdapter(context, mdLs).apply {
            setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
                override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                    if (position in 0 until mdLs.size) openWebActivity(mdLs[position].detailurl, resources.getString(R.string.news_detail_title))
                }
            })
        }
        recyclerView.adapter = adapter
    }

    /*获取资讯列表*/
    override fun initData() {
        doRequest(WebApiService.HOME_NEWS, HashMap(), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                tvOver.visibility = View.GONE
            }

            override fun onSuccess(data: String?) {
                onRequestCallback?.callback()
                if (GsonUtils.isResultOk(data)) {
                    val mdLs = GsonUtils.fromDataToList(data, NewsMDL::class.java)
                    updateData(mdLs)
                } else {
                    handler.postDelayed({ initData() }, MainFragment.DELAY_MILLIS)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                onRequestCallback?.callback()
                handler.postDelayed({ initData() }, MainFragment.DELAY_MILLIS)
            }
        })
    }

    private fun updateData(mdLs: MutableList<NewsMDL>) {
        this.mdLs.clear()
        this.mdLs.addAll(mdLs)
        adapter.notifyDataSetChanged()
        tvOver.visibility = View.VISIBLE
    }

    interface OnRequestCallback {
        fun callback()
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }
}