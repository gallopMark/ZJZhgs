package com.uroad.zhgs.adapteRv

import android.app.Activity
import android.text.TextUtils
import android.widget.ImageView
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.zhgs.R
import com.uroad.zhgs.model.HarvestMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 * @author MFB
 * @create 2018/11/24
 * @describe 我的成果（成功邀请人员列表适配器）
 */
class MyHarvestPersonAdapter(private val context: Activity, mDatas: MutableList<HarvestMDL.Person>)
    : BaseArrayRecyclerAdapter<HarvestMDL.Person>(context, mDatas) {
    override fun bindView(viewType: Int): Int = R.layout.item_harvestperson

    override fun onBindHoder(holder: RecyclerHolder, t: HarvestMDL.Person, position: Int) {
        val ivIconFile = holder.obtainView<ImageView>(R.id.ivIconFile)
        ImageLoaderV4.getInstance().displayCircleImage(context, t.iconfile, ivIconFile, R.mipmap.ic_user_default)
        holder.setText(R.id.tvName, t.name)
        holder.setText(R.id.tvTime, t.invitetime)
    }
}