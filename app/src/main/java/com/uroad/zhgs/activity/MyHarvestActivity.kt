package com.uroad.zhgs.activity

import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.adapteRv.MyHarvestPersonAdapter
import com.uroad.zhgs.adapteRv.MyHarvestPrizeAdapter
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.model.HarvestMDL
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_myharvest.*

/**
 * @author MFB
 * @create 2018/11/24
 * @describe 我的成果页面
 */
class MyHarvestActivity : BaseActivity() {

    private val personnel = ArrayList<HarvestMDL.Person>()
    private val prize = ArrayList<HarvestMDL.Prize>()
    private lateinit var personAdapter: MyHarvestPersonAdapter
    private lateinit var prizeAdapter: MyHarvestPrizeAdapter

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_myharvest)
        requestWindowFullScreen()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            customToolbar.layoutParams = (customToolbar.layoutParams as LinearLayout.LayoutParams).apply { topMargin = DisplayUtils.getStatusHeight(this@MyHarvestActivity) }
        customToolbar.setNavigationOnClickListener { onBackPressed() }
        initTab()
        initRv()
    }

    private fun initTab() {
        llTab1.setOnClickListener { setCurrentTab(1) }
        llTab2.setOnClickListener { setCurrentTab(2) }
        setCurrentTab(1)
    }

    private fun initRv() {
        rvPersonal.layoutManager = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.VERTICAL }
        rvReward.layoutManager = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.VERTICAL }
        personAdapter = MyHarvestPersonAdapter(this, personnel)
        rvPersonal.adapter = personAdapter
        prizeAdapter = MyHarvestPrizeAdapter(this, prize)
        rvReward.adapter = prizeAdapter
    }

    private fun setCurrentTab(tab: Int) {
        tvTotal.isSelected = false
        tvReward.isSelected = false
        vTab1.visibility = View.INVISIBLE
        vTab2.visibility = View.INVISIBLE
        rvPersonal.visibility = View.GONE
        rvReward.visibility = View.GONE
        when (tab) {
            1 -> {
                tvTotal.isSelected = true
                vTab1.visibility = View.VISIBLE
                rvPersonal.visibility = View.VISIBLE
            }
            else -> {
                tvReward.isSelected = true
                vTab2.visibility = View.VISIBLE
                rvReward.visibility = View.VISIBLE
            }
        }
    }

    override fun initData() {
        val activityId = intent.extras?.getString("activityId")
        doRequest(WebApiService.MY_HARVEST, WebApiService.myHarvestParams(getUserUUID(), activityId), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    val mdl = GsonUtils.fromDataBean(data, HarvestMDL::class.java)
                    if (mdl == null) onJsonParseError()
                    else updateUI(mdl)
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

    private fun updateUI(mdl: HarvestMDL) {
        mdl.personnel?.let {
            val total = "${getString(R.string.myHarvest_total_personal)}\n${it.size}"
            tvTotal.text = total
            this.personnel.addAll(it)
            personAdapter.notifyDataSetChanged()
        }
        mdl.prize?.let {
            val prize = "${getString(R.string.myHarvest_reward)}\n${it.size}"
            tvReward.text = prize
            this.prize.addAll(it)
            prizeAdapter.notifyDataSetChanged()
        }
    }
}