package com.uroad.zhgs.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.uroad.library.utils.BitmapUtils
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.dialog.WheelViewDialog
import com.uroad.zhgs.fragment.MainFragment
import com.uroad.zhgs.model.CarMDL
import kotlinx.android.synthetic.main.activity_carinquiry.*

/**
 * @author MFB
 * @create 2018/9/21
 * @describe 车辆诚信查询
 */
class CarInquiryActivity : BaseActivity() {
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
        val mdLs = ArrayList<CarMDL>()
        MainFragment.cars?.let { mdLs.addAll(it) }
        updateData(mdLs)
    }

    private fun updateData(mdLs: MutableList<CarMDL>) {
        var hasDefault = false
        for (i in 0 until mdLs.size) {
            if (mdLs[i].isdefault == 1) {  //如果有默认车辆则显示默认车辆
                index = i
                hasDefault = true
                tvSelectCar.text = mdLs[i].carno
                post()
                break
            }
        }
        if (!hasDefault && mdLs.size > 0) {  //如果没有默认车辆 则显示第一辆车
            tvSelectCar.text = mdLs[0].carno
            post()
        }
        if (mdLs.size > 1) {
            tvSelectCar.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_arrow_down_default, 0)
            val data = ArrayList<String>().apply {
                for (item in mdLs) {
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
                                dialog.dismiss()
                                if (position in 0 until data.size) {
                                    index = position
                                    tvSelectCar.text = text
                                    post()
                                }
                            }
                        }).show()
            }
        }
    }

    private fun post() {
        ivContent.visibility = View.INVISIBLE
        cpView.visibility = View.VISIBLE
        cpView.postDelayed({
            cpView.visibility = View.GONE
            ivContent.visibility = View.VISIBLE
        }, 2000)
    }
}