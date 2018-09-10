package com.uroad.zhgs.adapteRv

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.widget.FrameLayout
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.model.UserMsgMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/15.
 */
class UserMsgAdapter(private val context: Context, mDatas: MutableList<UserMsgMDL>)
    : BaseArrayRecyclerAdapter<UserMsgMDL>(context, mDatas) {
    override fun onBindHoder(holder: RecyclerHolder, t: UserMsgMDL, position: Int) {
        val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
        when (position) {
            0 -> {
                params.topMargin = DisplayUtils.dip2px(context, 10f)
                params.bottomMargin = 0
            }
            itemCount - 1 -> {
                params.topMargin = 0
                params.bottomMargin = DisplayUtils.dip2px(context, 10f)
            }
            else -> {
                params.topMargin = 0
                params.bottomMargin = 0
            }
        }
        holder.itemView.layoutParams = params
        if (TextUtils.equals(t.msgtype, UserMsgMDL.Type.RESCUE.code)) {
            holder.setImageResource(R.id.ivIcon, R.mipmap.ic_msg_rescue)
        } else {
            holder.setImageResource(R.id.ivIcon, R.mipmap.ic_msg_system)
        }
        holder.setText(R.id.tvMsgType, t.msgtypename)
        holder.setText(R.id.tvContent, t.msg)
        holder.setText(R.id.tvInTime, t.getInTime())
    }

    override fun bindView(viewType: Int): Int {
        return R.layout.item_user_msg
    }
}