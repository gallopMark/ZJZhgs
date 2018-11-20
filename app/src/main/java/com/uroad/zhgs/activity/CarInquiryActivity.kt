package com.uroad.zhgs.activity

import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.uroad.library.utils.BitmapUtils
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.common.CurrApplication
import com.uroad.zhgs.dialog.WheelViewDialog
import com.uroad.zhgs.model.CarInquiryMDL
import com.uroad.zhgs.model.CarMDL
import com.uroad.zhgs.utils.GsonUtils
import com.uroad.zhgs.webservice.HttpRequestCallback
import com.uroad.zhgs.webservice.WebApiService
import kotlinx.android.synthetic.main.activity_carinquiry.*

/**
 * @author MFB
 * @create 2018/9/21
 * @describe 车辆诚信查询
 */
class CarInquiryActivity : BaseActivity() {
    private val cars = ArrayList<CarMDL>()
    private var index: Int = 0
    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_carinquiry)
        requestWindowFullScreen()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            ivBack.layoutParams = (ivBack.layoutParams as FrameLayout.LayoutParams).apply { topMargin = DisplayUtils.getStatusHeight(this@CarInquiryActivity) }
        ivBack.setOnClickListener { onBackPressed() }
        setImage()
    }

    //重新计算图片高度 避免图片压缩
    private fun setImage() {
        val width = DisplayUtils.getWindowWidth(this)
        val height = (width * 0.7).toInt()
        ivTopPic.layoutParams = ivTopPic.layoutParams.apply {
            this.width = width
            this.height = height
        }
        ivTopPic.scaleType = ImageView.ScaleType.FIT_XY
        ivTopPic.setImageBitmap(BitmapUtils.decodeSampledBitmapFromResource(resources, R.mipmap.ic_carinquiry_topbg, width, height))
        val width2 = DisplayUtils.getWindowWidth(this) - DisplayUtils.dip2px(this, 110f)
        ivContent.layoutParams = ivContent.layoutParams.apply {
            this.width = width2
            this.height = width2
        }
        ivContent.scaleType = ImageView.ScaleType.FIT_XY
        ivContent.setImageBitmap(BitmapUtils.decodeSampledBitmapFromResource(resources, R.mipmap.ic_carinquiry_image, width2, width2))
    }

    override fun initData() {
        CurrApplication.cars?.let { cars.addAll(it) }
        updateData()
    }

    private fun updateData() {
        var hasDefault = false
        for (i in 0 until cars.size) {
            if (cars[i].isdefault == 1) {  //如果有默认车辆则显示默认车辆
                index = i
                hasDefault = true
                tvSelectCar.text = cars[i].carno
                post()
                break
            }
        }
        if (!hasDefault && cars.size > 0) {  //如果没有默认车辆 则显示第一辆车
            tvSelectCar.text = cars[0].carno
            post()
        }
        if (cars.size > 1) {
            tvSelectCar.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_arrow_down_default, 0)
            val data = ArrayList<String>().apply {
                for (item in cars) {
                    item.carno?.let { add(it) }
                }
            }
            tvSelectCar.setOnClickListener {
                if (data.size < 2) return@setOnClickListener
                val itemNum = if (data.size < 3) 3 else if (data.size > 7) 7 else data.size
                WheelViewDialog(this).withData(data).default(index)
                        .withItemNum(itemNum)
                        .withListener(object : WheelViewDialog.OnItemSelectListener {
                            override fun onItemSelect(position: Int, text: String, dialog: WheelViewDialog) {
                                index = position
                                tvSelectCar.text = text
                                post()
                                dialog.dismiss()
                            }
                        }).show()
            }
        }
    }

    private fun post() {
        if (index !in 0 until cars.size) return
        val car = cars[index]
        doRequest(WebApiService.CAR_INQUIRY, WebApiService.sincerityParams(car.carno, ""), object : HttpRequestCallback<String>() {
            override fun onPreExecute() {
                cpView.visibility = View.VISIBLE
                flContent.visibility = View.INVISIBLE
            }

            override fun onSuccess(data: String?) {
                cpView.visibility = View.GONE
                if (GsonUtils.isResultOk(data)) {
                    val mdLs = GsonUtils.fromDataToList(data, CarInquiryMDL::class.java)
                    if (mdLs.size == 0) {
                        showShortToast("暂无数据哦~")
                    } else {
                        flContent.visibility = View.VISIBLE
                        val mdl = mdLs[0]
                        updateUI(mdl)
                    }
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                cpView.visibility = View.GONE
                onHttpError(e)
            }
        })
    }

    private fun updateUI(mdl: CarInquiryMDL) {
        when {
            TextUtils.equals(mdl.creditRank, "黑一") -> {
                ivContent.setImageResource(R.mipmap.ic_car_blacklist)
                tvStatus.text = "一级黑名单"
                tvScore.setTextColor(ContextCompat.getColor(this,R.color.color_34))
            }
            TextUtils.equals(mdl.creditRank, "黑二") -> {
                ivContent.setImageResource(R.mipmap.ic_car_blacklist)
                tvStatus.text = "二级黑名单"
                tvScore.setTextColor(ContextCompat.getColor(this,R.color.color_34))
            }
            TextUtils.equals(mdl.creditRank, "灰名单") -> {
                ivContent.setImageResource(R.mipmap.ic_car_greylist)
                tvStatus.text = "灰名单"
                tvScore.setTextColor(ContextCompat.getColor(this,R.color.color_8d))
            }
            TextUtils.equals(mdl.creditRank, "正常") -> {
                ivContent.setImageResource(R.mipmap.ic_car_normal)
                tvStatus.text = "正常"
                tvScore.setTextColor(ContextCompat.getColor(this,R.color.color_normal))
            }
            else -> {
                ivContent.setImageResource(R.mipmap.ic_car_normal)
                tvStatus.text = "正常"
                tvScore.setTextColor(ContextCompat.getColor(this,R.color.color_normal))
            }
        }
        tvScore.text = mdl.sinceScore
    }
}