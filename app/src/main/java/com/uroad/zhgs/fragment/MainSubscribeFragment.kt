package com.uroad.zhgs.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RelativeLayout
import com.uroad.library.rxbus.RxBus
import com.uroad.zhgs.R
import com.uroad.zhgs.activity.RescueDetailActivity
import com.uroad.zhgs.activity.RescuePayActivity
import com.uroad.zhgs.activity.RoadNavigationActivity
import com.uroad.zhgs.adaptervp.UserSubscribePageAdapter
import com.uroad.zhgs.common.BaseFragment
import com.uroad.zhgs.model.SubscribeMDL
import com.uroad.zhgs.rxbus.MessageEvent
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_mainsubscribe.*

/**
 * @author MFB
 * @create 2018/11/22
 * @describe 首页我的订阅
 */
class MainSubscribeFragment : BaseFragment() {
    private val mdLs = ArrayList<SubscribeMDL>()   //我的订阅数据集（已登录状态）
    private lateinit var adapter: UserSubscribePageAdapter
    private var onSubscribeEvent: OnSubscribeEvent? = null
    private lateinit var handler: Handler

    fun setOnSubscribeEvent(onSubscribeEvent: OnSubscribeEvent?) {
        this.onSubscribeEvent = onSubscribeEvent
    }

    override fun setBaseLayoutResID(): Int = R.layout.fragment_mainsubscribe

    override fun setUp(view: View, savedInstanceState: Bundle?) {
        flBaseContent.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        initSubscribe()
        //注册rxBus 接收订阅取消的消息，将我的订阅列表中的相关信息移除
        addDisposable(RxBus.getDefault().toObservable(MessageEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { event -> onEvent(event) })
        handler = Handler(Looper.getMainLooper())
    }

    private fun initSubscribe() {
        adapter = UserSubscribePageAdapter(context, mdLs).apply {
            setOnPageTouchListener(object : UserSubscribePageAdapter.OnPageTouchListener {
                override fun onPageClick(position: Int, mdl: SubscribeMDL) {
                    if (position in 0 until mdLs.size) {
                        val bundle = Bundle()
                        bundle.putBoolean("fromHome", true)
                        if (mdl.getSubType() == SubscribeMDL.SubType.TrafficJam.code) {
                            bundle.putSerializable("mdl", mdl.getTrafficJamMDL().apply { if (subscribestatus != 1) subscribestatus = 1 })
                            openActivity(RoadNavigationActivity::class.java, bundle)
                        } else if (mdl.getSubType() == SubscribeMDL.SubType.Control.code
                                || mdl.getSubType() == SubscribeMDL.SubType.Emergencies.code
                                || mdl.getSubType() == SubscribeMDL.SubType.Planned.code) {
                            bundle.putSerializable("mdl", mdl.getEventMDL().apply { if (subscribestatus != 1) subscribestatus = 1 })
                            openActivity(RoadNavigationActivity::class.java, bundle)
                        } else if (mdl.getSubType() == SubscribeMDL.SubType.RescuePay.code) {
                            openActivity(RescuePayActivity::class.java, Bundle().apply { putString("rescueid", mdl.dataid) })
                        } else if (mdl.getSubType() == SubscribeMDL.SubType.RescueProgress.code) {
                            openActivity(RescueDetailActivity::class.java, Bundle().apply { putString("rescueid", mdl.rescueid) })
                        }
                    }
                }

                override fun onPageDown() {
                    bannerView.stopAutoScroll()
                }

                override fun onPageUp() {
                    bannerView.startAutoScroll()
                }
            })
        }
        bannerView.setAdapter(adapter)
    }

    private fun onEvent(event: MessageEvent?) {
        event?.obj.let {
            if (it is SubscribeMDL) {
                if (mdLs.contains(it)) mdLs.remove(it)
                onSubscribeEvent?.onEvent(mdLs.size <= 0)
            }
        }
    }

    interface OnSubscribeEvent {
        fun onEvent(isEmpty: Boolean)
    }

    override fun initData() {
        if (!isLogin()) return
        doRequest(WebApiService.USER_SUBSCRIBES, WebApiService.subscribeParams(getUserId()), object : HttpRequestCallback<String>() {
            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    val mdLs = GsonUtils.fromDataToList(data, SubscribeMDL::class.java)
                    updateData(mdLs)
                } else {
                    handler.postDelayed({ initData() }, MainFragment.DELAY_MILLIS)
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                //加载失败，延迟三秒重新加载
                handler.postDelayed({ initData() }, MainFragment.DELAY_MILLIS)
            }
        })
    }

    private fun updateData(mdLs: MutableList<SubscribeMDL>) {
        this.mdLs.clear()
        this.mdLs.addAll(mdLs)
        adapter.notifyDataSetChanged()
        onSubscribeEvent?.onEvent(this.mdLs.size <= 0)
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }
}