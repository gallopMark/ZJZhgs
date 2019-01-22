package com.uroad.zhgs.adapteRv

import android.content.Context
import com.uroad.zhgs.R
import com.uroad.zhgs.model.AppealMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 * @author MFB
 * @create 2019/1/19
 * @describe 申诉列表适配器
 */
class AppealListAdapter(private val context: Context, data: MutableList<AppealMDL>)
    : BaseArrayRecyclerAdapter<AppealMDL>(context, data) {
    override fun bindView(viewType: Int): Int = R.layout.item_appeal

    override fun onBindHoder(holder: RecyclerHolder, t: AppealMDL, position: Int) {
        holder.setBackgroundColor(R.id.tvStatus, t.color(context))
        holder.setText(R.id.tvStatus, t.auditStatusName)
        var license = "车牌号码："
        t.license?.let { license += it }
        holder.setText(R.id.tvLicense, license)
        var illegalType = "违规类型："
        t.illeaglTypeName?.let { illegalType += it }
        holder.setText(R.id.tvIllegalType, illegalType)
        var illegalTime = "违规时间："
        t.illegalTime?.let { illegalTime += it }
        holder.setText(R.id.tvIllegalTime, illegalTime)
        var illegalAddress = "违规地点："
        t.illegalLocale?.let { illegalAddress += it }
        holder.setText(R.id.tvIllegalAddress, illegalAddress)
    }
}