package com.uroad.zhgs.adapteRv

import android.app.Activity
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import com.amap.api.col.sln3.bd
import com.uroad.zhgs.R
import com.uroad.zhgs.model.PassRecordMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import java.math.BigDecimal
import java.text.DecimalFormat

/**
 * @author MFB
 * @create 2018/11/1
 * @describe 通行记录列表适配器
 */
class PassRecordAdapter(context: Activity, mDatas: MutableList<PassRecordMDL>)
    : BaseArrayRecyclerAdapter<PassRecordMDL>(context, mDatas) {
    private val colorGray = ContextCompat.getColor(context, R.color.color_99)
    private val ts16 = context.resources.getDimensionPixelOffset(R.dimen.font_16)

    override fun bindView(viewType: Int): Int = R.layout.item_passrecord

    override fun onBindHoder(holder: RecyclerHolder, t: PassRecordMDL, position: Int) {
        holder.setText(R.id.tvEnter, getEnterInfo(t.n_en_station_name, t.getEnDateTime()))
        holder.setText(R.id.tvExit, getExitInfo(t.n_ex_station_name, t.getExDateTime()))
        holder.setText(R.id.tvMileage, getMileageInfo(t.d_fee_length))
    }

    private fun getEnterInfo(enStation: String?, enDate: String?): SpannableString {
        var enterInfo = ""
        enStation?.let { enterInfo += it }
        val start = enterInfo.length
        enDate?.let { enterInfo += "\u3000$it" }
        return SpannableString(enterInfo).apply { setSpan(ForegroundColorSpan(colorGray), start, enterInfo.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE) }
    }

    private fun getExitInfo(exStation: String?, exDate: String?): SpannableString {
        var exitInfo = ""
        exStation?.let { exitInfo += it }
        val start = exitInfo.length
        exDate?.let { exitInfo += "\u3000$it" }
        return SpannableString(exitInfo).apply { setSpan(ForegroundColorSpan(colorGray), start, exitInfo.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE) }
    }

    private fun getMileageInfo(length: String?): SpannableString {
        var mile = ""
        length?.let { mile += it }
        val start = mile.length
        mile += "Km"
        return SpannableString(mile).apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, start, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(AbsoluteSizeSpan(ts16, false), start, mile.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

//    private fun getMoneyInfo(money: Double?): CharSequence {
//        var moneyInfo = "0.00元"
//        money?.let {
//            val bigDecimal = BigDecimal(it)
//            val value = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toDouble()
//            moneyInfo = "${value}元"
//        }
//        return SpannableString(moneyInfo).apply {
//            setSpan(StyleSpan(Typeface.BOLD), 0, moneyInfo.length - 1, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
//            setSpan(AbsoluteSizeSpan(ts18, false), moneyInfo.length - 1, moneyInfo.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
//        }
//    }
}