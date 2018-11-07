//package com.uroad.zhgs.activity
//
//import android.app.Activity
//import android.content.Intent
//import android.os.Bundle
//import android.support.v4.util.ArrayMap
//import android.support.v7.widget.LinearLayoutManager
//import android.text.Editable
//import android.text.TextUtils
//import android.text.TextWatcher
//import android.view.View
//import com.scwang.smartrefresh.layout.api.RefreshLayout
//import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
//import com.uroad.library.utils.NetworkUtils
//import com.uroad.zhgs.R
//import com.uroad.zhgs.adapteRv.RidersReportAdapter
//import com.uroad.zhgs.common.BaseActivity
//import com.uroad.zhgs.model.RidersReportMDL
//import com.uroad.zhgs.utils.GsonUtils
//import com.uroad.zhgs.utils.InputMethodUtils
//import com.uroad.zhgs.webservice.HttpRequestCallback
//import com.uroad.zhgs.webservice.WebApiService
//import kotlinx.android.synthetic.main.activity_userevent_list.*
//
///**
// *Created by MFB on 2018/8/7.
// */
//@Deprecated("用fragment替换")
//class UserEventListActivity : BaseActivity() {
//    private val mDatas = ArrayList<RidersReportMDL>()
//    private lateinit var adapter: RidersReportAdapter
//    private var index = 1
//    private val size = 10
//    private val hashMap = ArrayMap<Int, String>()
//    private var isAnim = false
//    private var isMy = false
//    private var type: String? = null
//
//    override fun setUp(savedInstanceState: Bundle?) {
//        setBaseContentLayoutWithoutTitle(R.layout.activity_userevent_list)
//        intent.extras?.let {
//            isAnim = it.getBoolean("anim", false)
//            isMy = it.getBoolean("isMy", false)
//        }
//        if (isMy) type = WebApiService.REPORT_TYPE_MY
//        tvSubTitle.text = resources.getString(R.string.userEvent_title)
//        tvTitle.text = resources.getString(R.string.userEvent_burst)
//        ivBack.setOnClickListener { onBackPressed() }
//        recyclerView.layoutManager = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.VERTICAL }
//        adapter = RidersReportAdapter(this, mDatas)
//        recyclerView.adapter = adapter
//        recyclerView.isNestedScrollingEnabled = false
//        refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
//            override fun onRefresh(refreshLayout: RefreshLayout?) {
//                index = 1
//                initData()
//            }
//
//            override fun onLoadMore(refreshLayout: RefreshLayout?) {
//                initData()
//            }
//        })
//        tvTitle.setOnClickListener { openActivityForResult(RidersReportActivity::class.java, 1) }
//    }
//
//    override fun initData() {
//        doRequest(WebApiService.USER_EVELT_LIST, WebApiService.userEventListParams(getUserId(), type, index, size),
//                object : HttpRequestCallback<String>() {
//                    override fun onSuccess(data: String?) {
//                        finishLoad()
//                        if (GsonUtils.isResultOk(data)) {
//                            val list = GsonUtils.fromDataToList(data, RidersReportMDL::class.java)
//                            updateData(list)
//                        } else {
//                            showShortToast(GsonUtils.getMsg(data))
//                        }
//                    }
//
//                    override fun onFailure(e: Throwable, errorMsg: String?) {
//                        finishLoad()
//                        if (index == 1) {
//                            onPageError()
//                        } else
//                            onHttpError(e)
//                    }
//                })
//    }
//
//    private fun finishLoad() {
//        if (refreshLayout.isRefreshing) refreshLayout.finishRefresh()
//        if (refreshLayout.isLoading) refreshLayout.finishLoadMore()
//    }
//
//    private fun updateData(list: MutableList<RidersReportMDL>) {
//        if (index == 1) mDatas.clear()
//        mDatas.addAll(list)
//        adapter.notifyDataSetChanged()
//        if (list.size < size) {
//            refreshLayout.setNoMoreData(true)
//        } else {
//            refreshLayout.setNoMoreData(false)
//        }
//        if (index == 1 && mDatas.size == 0) {
//            onPageNoData()
//        } else index += 1
//    }
//
//    private fun onPageNoData() {
//        refreshLayout.visibility = View.GONE
//        tvEmpty.visibility = View.VISIBLE
//    }
//
//    private fun onPageError() {
//        refreshLayout.visibility = View.GONE
//        llError.visibility = View.VISIBLE
//        if (!NetworkUtils.isConnected(this)) {
//            tvErrorTips.text = resources.getString(R.string.nonetwork)
//            ivErrorIcon.setImageResource(R.mipmap.ic_nonetwork)
//        } else {
//            tvErrorTips.text = resources.getString(R.string.connect_error)
//            ivErrorIcon.setImageResource(R.mipmap.ic_connect_error)
//        }
//        tvReload.setOnClickListener {
//            llError.visibility = View.GONE
//            refreshLayout.visibility = View.VISIBLE
//            initData()
//        }
//    }
//
//    override fun setListener() {
//        adapter.setOnAdapterChildClickListener(object : RidersReportAdapter.OnAdapterChildClickListener {
//            override fun onImageClick(position: Int, photos: MutableList<String>) {
//                val pics = ArrayList<String>()
//                for (i in 0 until photos.size) {
//                    if (!TextUtils.isEmpty(photos[i])) {
//                        pics.add(photos[i])
//                    }
//                }
//                showBigPic(position, pics)
//            }
//
//            override fun onFollowClick(position: Int) {
//
//            }
//
//            override fun onCommentClick(position: Int, view: View) {
//                showBottomView(view, position, 0.toString(), "", "")
//            }
//
//            override fun onChildCommentClick(view: View, position: Int, parentid: String?, userid: String?, username: String?) {
//                showBottomView(view, position, parentid, userid, username)
//            }
//
//            override fun onSupportClick(position: Int, view: View) {
//                if (TextUtils.isEmpty(mDatas[position].issupport)) {   //没有点赞过
//                    support(position, 1)
//                } else {  //已经点赞,取消点赞
//                    support(position, 2)
//                }
//            }
//        })
//        recyclerView.setOnTouchListener { _, _ ->
//            etComment.clearFocus()
//            InputMethodUtils.hideSoftInput(this@UserEventListActivity, etComment)
//            llComment.visibility = View.GONE
//            return@setOnTouchListener false
//        }
//    }
//
//    //点赞或取消点赞  type=1点赞 type=2取消点赞
//    private fun support(position: Int, type: Int) {
//        val eventuserid = mDatas[position].eventid ?: ""
//        val status = if (type == 1) 1 else 0
//        doRequest(WebApiService.SUPPORT, WebApiService.supportParams(eventuserid, getUserId(), status.toString()), object : HttpRequestCallback<String>() {
//            override fun onSuccess(data: String?) {
//                if (GsonUtils.isResultOk(data)) {
//                    var supportCount = mDatas[position].supportcount
//                    if (type == 1) {   //点赞成功
//                        mDatas[position].issupport = "support"
//                        supportCount += 1
//                        mDatas[position].supportcount = supportCount
//                    } else {   //取消点赞成功
//                        mDatas[position].issupport = null
//                        supportCount -= 1
//                        mDatas[position].supportcount = supportCount
//                    }
//                    adapter.notifyDataSetChanged()
//                } else {
//                    showShortToast(GsonUtils.getMsg(data))
//                }
//            }
//
//            override fun onFailure(e: Throwable, errorMsg: String?) {
//                onHttpError(e)
//            }
//        })
//    }
//
//    private fun showBottomView(itemBottomView: View, position: Int,
//                               parentid: String?, touserid: String?, tousername: String?) {
//        //注意，由于弹出输入法需要一定的时间，所以该方法要延迟500ms计算，500ms为经验值，仅供参考。
//        InputMethodUtils.showSoftInput(this@UserEventListActivity, etComment)
//        llComment.visibility = View.VISIBLE
//        etComment.requestFocus()
//        etComment.isFocusable = true
//        etComment.setText(hashMap[position])
//        etComment.setSelection(etComment.text.length)
//        mButton.isEnabled = !TextUtils.isEmpty(etComment.text)
//        if (mButton.isEnabled) {
//            mButton.setBackgroundResource(R.drawable.bg_button_comment_selector)
//        } else {
//            mButton.setBackgroundResource(R.drawable.bg_button_comment_default_corners_5dp)
//        }
//        if (position == mDatas.size - 1) {   //最后一个
//            recyclerView.postDelayed({ recyclerView.scrollToPosition(mDatas.size - 1) }, 500)
//        } else {
//            llComment.postDelayed({
//                val position1 = IntArray(2)
//                itemBottomView.getLocationOnScreen(position1)
//                val position2 = IntArray(2)
//                llComment.getLocationOnScreen(position2)
//                recyclerView.scrollBy(0, position1[1] - position2[1])
//            }, 500)
//        }
//        etComment.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(p0: Editable?) {
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                hashMap[position] = s.toString()
//                mButton.isEnabled = !TextUtils.isEmpty(s.toString().trim())
//                if (mButton.isEnabled) {
//                    mButton.setBackgroundResource(R.drawable.bg_button_comment_selector)
//                } else {
//                    mButton.setBackgroundResource(R.drawable.bg_button_comment_default_corners_5dp)
//                }
//            }
//        })
//        mButton.setOnClickListener {
//            etComment.clearFocus()
//            InputMethodUtils.hideSoftInput(this@UserEventListActivity, etComment)
//            llComment.visibility = View.GONE
//            eventComment(position, etComment.text.toString(), parentid, touserid, tousername)
//        }
//    }
//
//    //用户报料-评论
//    private fun eventComment(position: Int, content: String, parentid: String?,
//                             touserid: String?, tousername: String?) {
//        val eventid = mDatas[position].eventid ?: ""
//        val username = getUserName()
//        doRequest(WebApiService.EVENT_COMMENT, WebApiService.eventCommentParams(eventid, getUserId(), username,
//                content, parentid, touserid, tousername), object : HttpRequestCallback<String>() {
//            override fun onSuccess(data: String?) {
//                if (GsonUtils.isResultOk(data)) {   //评论成功
//                    val comment = RidersReportMDL.Comment().apply {
//                        this.eventid = eventid
//                        this.userid = getUserId()
//                        this.usercomment = content
//                        this.username = username
//                        this.touserid = touserid
//                        this.tousername = tousername
//                    }
//                    val commentCount = mDatas[position].commentcount
//                    mDatas[position].commentcount = commentCount + 1
//                    mDatas[position].getCommentList().add(comment)
//                    adapter.notifyDataSetChanged()
//                    etComment.setText("")
//                } else {
//                    showShortToast(GsonUtils.getMsg(data))
//                }
//            }
//
//            override fun onFailure(e: Throwable, errorMsg: String?) {
//                onHttpError(e)
//            }
//        })
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
//            index = 1
//            initData()
//        }
//    }
//
//    override fun finish() {
//        super.finish()
//        if (isAnim) overridePendingTransition(0, R.anim.slide_bottom_out)
//    }
//}