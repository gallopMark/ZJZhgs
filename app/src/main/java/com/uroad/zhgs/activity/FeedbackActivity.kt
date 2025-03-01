package com.uroad.zhgs.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.GridLayoutManager
import android.text.TextUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.library.utils.DisplayUtils
import com.uroad.rxhttp.RxHttpManager
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.model.*
import com.uroad.zhgs.photopicker.data.ImagePicker
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.ApiService
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import com.uroad.zhgs.widget.GridSpacingItemDecoration
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_feedback.*
import java.io.File

/**
 *Created by MFB on 2018/8/13.
 * 意见反馈
 */
class FeedbackActivity : BaseActivity() {

    private var isFromRoad = false
    private val data = ArrayList<FeedbackTypeMDL>()
    private lateinit var typeAdapter: FBTypeAdapter
    private val picData = ArrayList<MutilItem>()
    private val addItem = AddPicItem()
    private lateinit var picAdapter: AddPicAdapter
    private var fdtype: String? = ""
    private var imageUrls: String = ""

    private inner class FBTypeAdapter(context: Activity,
                                      data: MutableList<FeedbackTypeMDL>)
        : BaseArrayRecyclerAdapter<FeedbackTypeMDL>(context, data) {
        private var selectIndex = 0

        override fun bindView(viewType: Int): Int = R.layout.item_feedbacktype

        override fun onBindHoder(holder: RecyclerHolder, t: FeedbackTypeMDL, position: Int) {
            val tv = holder.obtainView<TextView>(R.id.tv)
            tv.text = t.dictname
            tv.isSelected = selectIndex == position
            holder.itemView.setOnClickListener {
                fdtype = t.dictcode
                setSelectIndex(position)
            }
        }

        fun setSelectIndex(position: Int) {
            selectIndex = position
            notifyDataSetChanged()
        }
    }

    private class AddPicAdapter(private val context: Activity, mDatas: MutableList<MutilItem>)
        : BaseArrayRecyclerAdapter<MutilItem>(context, mDatas) {
        private val size = (DisplayUtils.getWindowWidth(context) - DisplayUtils.dip2px(context, 44f)) / 3
        private var onItemOptionListener: OnItemOptionListener? = null
        override fun onBindHoder(holder: RecyclerHolder, t: MutilItem, position: Int) {
            holder.itemView.layoutParams = LinearLayout.LayoutParams(size, size)
            if (holder.itemViewType == 1) {
                holder.itemView.setOnClickListener { onItemOptionListener?.onAddPic() }
            } else {
                val mdl = t as PicMDL
                val ivPic = holder.obtainView<ImageView>(R.id.ivPic)
                val ivCancel = holder.obtainView<ImageView>(R.id.ivCancel)
                ImageLoaderV4.getInstance().displayImage(context, mdl.path, ivPic)
                ivCancel.setOnClickListener {
                    mDatas.removeAt(position)
                    notifyDataSetChanged()
                    onItemOptionListener?.onItemRemove(mDatas)
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            return mDatas[position].getItemType()
        }

        override fun bindView(viewType: Int): Int {
            if (viewType == 1) return R.layout.item_addpic_button
            return R.layout.item_addpic2
        }

        interface OnItemOptionListener {
            fun onAddPic()
            fun onItemRemove(mDatas: MutableList<MutilItem>)
        }

        fun setOnItemOptionListener(onItemOptionListener: OnItemOptionListener) {
            this.onItemOptionListener = onItemOptionListener
        }
    }

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_feedback)
        withTitle(resources.getString(R.string.feedback_title))
        intent.extras?.let { isFromRoad = it.getBoolean("isFromRoad", false) }
        initRv()
        btSubmit.setOnClickListener { onSubmit() }
    }

    override fun initData() {
        doRequest(WebApiService.FEEDBACK_TYPE, WebApiService.getBaseParams(), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                setPageLoading()
            }

            override fun onSuccess(data: String?) {
                if (GsonUtils.isResultOk(data)) {
                    setPageEndLoading()
                    val mdLs = GsonUtils.fromDataToList(data, FeedbackTypeMDL::class.java)
                    updateUI(mdLs)
                } else {
                    setPageError()
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                setPageError()
            }
        })
    }

    private fun updateUI(mdLs: MutableList<FeedbackTypeMDL>) {
        this.data.clear()
        this.data.addAll(mdLs)
        var selectIndex = 0
        if (isFromRoad) {
            val index = data.indexOf(FeedbackTypeMDL().apply { dictcode = FeedbackTypeMDL.Type.ROAD.CODE })
            if (index >= 0) selectIndex = index
        } else {
            for (i in 0 until data.size) {
                if (TextUtils.equals(data[i].dictcode, FeedbackTypeMDL.Type.OTHER.CODE) ||
                        TextUtils.isEmpty(data[i].dictcode)) {
                    selectIndex = i
                    break
                }
            }
        }
        if (selectIndex in 0 until data.size) fdtype = data[selectIndex].dictcode
        typeAdapter.setSelectIndex(selectIndex)
    }

    private fun initRv() {
        rvType.addItemDecoration(GridSpacingItemDecoration(3, DisplayUtils.dip2px(this, 10f), false))
        rvType.layoutManager = GridLayoutManager(this, 4).apply { orientation = GridLayoutManager.VERTICAL }
        typeAdapter = FBTypeAdapter(this, data)
        rvType.adapter = typeAdapter
        rvPics.addItemDecoration(GridSpacingItemDecoration(3, DisplayUtils.dip2px(this, 10f), false))
        rvPics.layoutManager = GridLayoutManager(this, 3).apply { orientation = GridLayoutManager.VERTICAL }
        picData.add(addItem)
        picAdapter = AddPicAdapter(this, picData)
        rvPics.adapter = picAdapter
        picAdapter.setOnItemOptionListener(object : AddPicAdapter.OnItemOptionListener {
            override fun onAddPic() {
                ImagePicker.from(this@FeedbackActivity)
                        .isMutilyChoice(4 - picData.size)
                        .isCompress(true)
                        .requestCode(1)
                        .start()
            }

            override fun onItemRemove(mDatas: MutableList<MutilItem>) {
                picData.remove(addItem)
                picData.add(addItem)
                picAdapter.notifyDataSetChanged()
            }
        })
    }

    //取图片回调
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val items = data?.getStringArrayListExtra(ImagePicker.EXTRA_PATHS)
            items?.let {
                picData.remove(addItem)
                for (i in 0 until it.size) {
                    picData.add(PicMDL().apply { path = it[i] })
                }
                if (picData.size < 3) {
                    picData.add(addItem)
                }
                picAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun needUploadPic(): Boolean {
        for (item in picData) {
            return item.getItemType() == 2
        }
        return false
    }

    private fun onSubmit() {
        val content = etContent.text.toString()
        if (TextUtils.isEmpty(content.trim())) {
            showShortToast("请输入您宝贵的意见")
        } else {
            if (needUploadPic()) {
                addDisposable(Observable.fromArray(picData)
                        .map { it ->
                            val sb = StringBuffer()
                            for (item in it) {
                                if (item.getItemType() == 2) {
                                    val picItem = item as PicMDL
                                    RxHttpManager.createApi(ApiService::class.java)
                                            .uploadFile(createMultipart(File(picItem.path), "file"))
                                            .subscribe({ body ->
                                                val json = body?.string()
                                                if (GsonUtils.isResultOk(json)) {
                                                    val imageMDL = GsonUtils.fromDataBean(json, UploadMDL::class.java)
                                                    imageMDL?.imgurl?.file?.let {
                                                        sb.append("$it,")
                                                    }
                                                }
                                            }, {})
                                }
                            }
                            sb
                        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            imageUrls = it.toString()
                            endLoading()
                            commit(content)
                        }, {
                            showShortToast("图片上传失败")
                        }, {
                            endLoading()
                        }, {
                            showLoading("正在上传图片…")
                        }))
            } else {
                commit(content)
            }
        }
    }

    private fun commit(content: String) {
        doRequest(WebApiService.FEEDBACK, WebApiService.feedbackParams(getUserId(), content, imageUrls, fdtype), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                showLoading("正在提交…")
            }

            override fun onSuccess(data: String?) {
                endLoading()
                if (GsonUtils.isResultOk(data)) {
                    showShortToast("您已提交反馈")
                    Handler().postDelayed({ if (!isFinishing) finish() }, 1500)
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
}