package com.uroad.zhgs.activity

import android.os.Build
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
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
            ivBack.layoutParams = (ivBack.layoutParams as ConstraintLayout.LayoutParams).apply { topMargin = DisplayUtils.getStatusHeight(this@CarInquiryActivity) }
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
            this.height = (width2 * 0.66).toInt()
        }
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
                WheelViewDialog(this).withData(data).default(index)
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
                onLoading()
            }

            override fun onSuccess(data: String?) {
                finishLoad()
                if (GsonUtils.isResultOk(data)) {
                    val mdLs = GsonUtils.fromDataToList(data, CarInquiryMDL::class.java)
                    if (mdLs.size == 0) {
                        onEmpty()
                    } else {
                        val mdl = mdLs[0]
                        updateUI(mdl)
                    }
                } else {
                    showShortToast(GsonUtils.getMsg(data))
                }
            }

            override fun onFailure(e: Throwable, errorMsg: String?) {
                finishLoad()
                onHttpError(e)
            }
        })
    }

    private fun onLoading() {
        cpView.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        llContent.visibility = View.GONE
    }

    private fun finishLoad() {
        cpView.visibility = View.GONE
    }

    private fun onEmpty() {
        tvEmpty.visibility = View.VISIBLE
        llContent.visibility = View.GONE
    }

    private fun updateUI(mdl: CarInquiryMDL) {
        if (TextUtils.isEmpty(mdl.creditRank) && TextUtils.isEmpty(mdl.sinceScore)) {
            onEmpty()
        } else {
            tvEmpty.visibility = View.GONE
            llContent.visibility = View.VISIBLE
            var creditRank = ""
            mdl.creditRank?.let { creditRank = it }
            when {
                creditRank.contains("黑") -> {
                    ivContent.setImageResource(R.mipmap.ic_car_blacklist)
                    tvStatus.setTextColor(ContextCompat.getColor(this, R.color.color_34))
                    tvScore.setTextColor(ContextCompat.getColor(this, R.color.color_34))
                }
                creditRank.contains("灰") -> {
                    ivContent.setImageResource(R.mipmap.ic_car_greylist)
                    tvStatus.setTextColor(ContextCompat.getColor(this, R.color.color_8d))
                    tvScore.setTextColor(ContextCompat.getColor(this, R.color.color_8d))
                }
                else -> {
                    ivContent.setImageResource(R.mipmap.ic_car_normal)
                    tvStatus.setTextColor(ContextCompat.getColor(this, R.color.color_normal))
                    tvScore.setTextColor(ContextCompat.getColor(this, R.color.color_normal))
                }
            }
            tvStatus.text = creditRank
            val score = "${mdl.sinceScore}分"
            tvScore.text = SpannableString(score).apply { setSpan(AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.font_14), false), score.length - 1, score.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE) }
        }
    }
}