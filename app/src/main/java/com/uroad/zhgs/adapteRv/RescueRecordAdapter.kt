package com.uroad.zhgs.adapteRv

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import com.amap.api.col.sln3.ho
import com.uroad.zhgs.R
import com.uroad.zhgs.model.RescueItemMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/4.
 * 救援记录适配器
 */
class RescueRecordAdapter(context: Context, mDatas: MutableList<RescueItemMDL>) :
        BaseArrayRecyclerAdapter<RescueItemMDL>(context, mDatas) {
    private val textRescueNum = context.resources.getString(R.string.rescue_detail_request_num)
    private val textRescueType = context.resources.getString(R.string.rescue_detail_rescue_type)
    private val textAddress = context.resources.getString(R.string.rescue_detail_rescue_address)
    private val colorGrey = ContextCompat.getColor(context, R.color.grey)
    private val ts20 = context.resources.getDimensionPixelSize(R.dimen.font_20)

    override fun onBindHoder(holder: RecyclerHolder, t: RescueItemMDL, position: Int) {
        var rescueNo = ""
        t.rescueno?.let { rescueNo = it }
        holder.setText(R.id.tvRequestNum, SpannableString("$textRescueNum\u3000\u2000$rescueNo").apply { setSpan(ForegroundColorSpan(colorGrey), 0, textRescueNum.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) })
        var rescuetype = ""
        t.rescuetype?.let { rescuetype = it }
        holder.setText(R.id.tvRequestType, SpannableString("$textRescueType\u3000\u2000$rescuetype").apply { setSpan(ForegroundColorSpan(colorGrey), 0, textRescueType.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) })
        var rescueAddress = ""
        t.rescue_address?.let { rescueAddress = it }
        holder.setText(R.id.tvRequestAddress, SpannableString("$textAddress\u3000\u2000$rescueAddress").apply { setSpan(ForegroundColorSpan(colorGrey), 0, textAddress.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) })
        holder.setText(R.id.tvStatus, t.statusname)
        if (t.ispay == 1 && !TextUtils.isEmpty(t.paymoney)) { //待支付
            holder.setVisibility(R.id.tvPay, true)
        } else {
            holder.setVisibility(R.id.tvPay, false)
        }
        if (TextUtils.isEmpty(t.paymoney)) {
            holder.setVisibility(R.id.tvMoney, false)
        } else {
            holder.setVisibility(R.id.tvMoney, true)
            var payMoney = "¥"
            t.paymoney?.let { payMoney += it }
            holder.setText(R.id.tvMoney, SpannableString(payMoney).apply { setSpan(AbsoluteSizeSpan(ts20, false), 1, payMoney.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE) })
        }
        if (t.iscomment == 1) {
            holder.setVisibility(R.id.tvEvaluate, true)
        } else {
            holder.setVisibility(R.id.tvEvaluate, false)
        }
        if (t.isinvoice == 1) {
            holder.setVisibility(R.id.tvInvoice, true)
        } else {
            holder.setVisibility(R.id.tvInvoice, false)
        }
        if (!holder.isVisibility(R.id.tvEvaluate)
                && !holder.isVisibility(R.id.tvInvoice)
                && !holder.isVisibility(R.id.tvPay)
                && !holder.isVisibility(R.id.tvMoney)) {
            holder.setVisibility(R.id.llBottom, false)
        } else {
            holder.setVisibility(R.id.llBottom, true)
        }
        holder.bindChildClick(R.id.tvEvaluate)
        holder.bindChildClick(R.id.tvInvoice)
        holder.bindChildClick(R.id.tvPay)
    }

    override fun bindView(viewType: Int): Int {
        return R.layout.item_rescue_record
    }
}