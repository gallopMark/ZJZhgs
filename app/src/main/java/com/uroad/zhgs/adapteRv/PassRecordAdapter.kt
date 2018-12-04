package com.uroad.zhgs.adapteRv

import android.app.Activity
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import com.uroad.zhgs.R
import com.uroad.zhgs.model.PassRecordMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import java.text.DecimalFormat

/**
 * @author MFB
 * @create 2018/11/1
 * @describe 通行记录列表适配器
 */
class PassRecordAdapter(context: Activity, mDatas: MutableList<PassRecordMDL>)
    : BaseArrayRecyclerAdapter<PassRecordMDL>(context, mDatas) {
    private val colorGray = ContextCompat.getColor(context, R.color.color_99)
    private val ts18 = context.resources.getDimensionPixelOffset(R.dimen.font_18)

    override fun bindView(viewType: Int): Int = R.layout.item_passrecord

    override fun onBindHoder(holder: RecyclerHolder, t: PassRecordMDL, position: Int) {
        holder.setText(R.id.tvEnter, getEnterInfo(t.n_en_station_name, t.n_en_date))
        holder.setText(R.id.tvExit, getExitInfo(t.n_ex_station_name, t.n_ex_date))
        holder.setText(R.id.tvMileage, getMileageInfo(t.d_fee_length))
        holder.setText(R.id.tvMoney, getMoneyInfo(t.money))
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
        var mile = "里程\u3000"
        val start = mile.length
        length?.let { mile += "${it}Km" }
        return SpannableString(mile).apply { setSpan(ForegroundColorSpan(colorGray), start, mile.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE) }
    }

    private fun getMoneyInfo(money: Double?): CharSequence {
        var moneyInfo = "0.00元"
        money?.let {
            val df = DecimalFormat(".00")
            moneyInfo = "${df.format(it)}元"
        }
        return SpannableString(moneyInfo).apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, moneyInfo.length - 1, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(AbsoluteSizeSpan(ts18, false), moneyInfo.length - 1, moneyInfo.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}