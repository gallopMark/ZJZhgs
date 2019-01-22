package com.uroad.zhgs.activity

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.model.AppealMDL
import com.uroad.zhgs.utils.GsonUtils
import kotlinx.android.synthetic.main.activity_appealdetail.*

/**
 * @author MFB
 * @create 2019/1/19
 * @describe 申诉详情
 */
class AppealDetailActivity : BaseActivity() {
    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_appealdetail)
        withTitle(getString(R.string.appeal_detail_title))
        hideBaseLine(true)
        initBundle()
    }

    private fun initBundle() {
        val json = intent.extras?.getString("json")
        val mdl = GsonUtils.fromJsonToObject(json, AppealMDL::class.java)
        mdl?.let { updateUI(it) }
    }

    private fun updateUI(appealMDL: AppealMDL) {
        setSeekBar(appealMDL.auditStatus)
        setProgressText(appealMDL.auditStatus)
        tvDescription.text = appealMDL.auditDescription
        tvLicense.text = getUIText(getString(R.string.appeal_license), appealMDL.license)
        tvCarColor.text = getUIText(getString(R.string.appeal_carColor), appealMDL.licenseColor)
        tvIllegalType.text = getUIText(getString(R.string.appeal_illegalType), appealMDL.illeaglTypeName)
        tvIllegalTime.text = getUIText(getString(R.string.appeal_illegalTime), appealMDL.illegalTime)
        tvIllegalAddress.text = getUIText(getString(R.string.appeal_illegalAddress), appealMDL.illegalLocale)
        tvAppealUser.text = getUIText(getString(R.string.appeal_user), appealMDL.appear)
        tvAppealTime.text = getUIText(getString(R.string.appeal_time), appealMDL.appealTime)
    }

    private fun setSeekBar(auditStatus: Int?) {
        val progress = when (auditStatus) {
            1 -> 0
            2 -> 50
            3, 4 -> 100
            else -> 0
        }
        indicatorSeekBar.setProgress(progress.toFloat())
    }

    private fun setProgressText(auditStatus: Int?) {
        val arrayTexts = if (auditStatus == 4) {
            arrayOf("待审核", "审核中", "失败")
        } else {
            arrayOf("待审核", "审核中", "成功")
        }
        tvStatusStart.text = arrayTexts[0]
        tvStatusCenter.text = arrayTexts[1]
        tvStatusEnd.text = arrayTexts[2]
        val unit = TypedValue.COMPLEX_UNIT_PX
        val size16 = resources.getDimension(R.dimen.font_16)
        val size14 = resources.getDimension(R.dimen.font_14)
        val colorAccent = ContextCompat.getColor(this, R.color.colorAccent)
        val colorGary = ContextCompat.getColor(this, R.color.gary)
        when (auditStatus) {
            2 -> {
                tvStatusStart.setTextSize(unit, size14)
                tvStatusStart.setTextColor(colorAccent)
                tvStatusCenter.setTextSize(unit, size16)
                tvStatusCenter.setTextColor(colorAccent)
                tvStatusEnd.setTextSize(unit, size14)
                tvStatusEnd.setTextColor(colorGary)
            }
            3, 4 -> {
                tvStatusStart.setTextSize(unit, size14)
                tvStatusStart.setTextColor(colorAccent)
                tvStatusCenter.setTextSize(unit, size14)
                tvStatusCenter.setTextColor(colorAccent)
                tvStatusEnd.setTextSize(unit, size16)
                tvStatusEnd.setTextColor(colorAccent)
            }
            else -> {
                tvStatusStart.setTextSize(unit, size16)
                tvStatusStart.setTextColor(colorAccent)
                tvStatusCenter.setTextSize(unit, size14)
                tvStatusCenter.setTextColor(colorGary)
                tvStatusEnd.setTextSize(unit, size14)
                tvStatusEnd.setTextColor(colorGary)
            }
        }
    }

    private fun getUIText(resString: String, sourceString: String?): SpannableString {
        var source = resString
        val start = source.length
        sourceString?.let { source += it }
        return SpannableString(source).apply { setSpan(ForegroundColorSpan(ContextCompat.getColor(this@AppealDetailActivity, R.color.color_33)), start, source.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE) }
    }
}