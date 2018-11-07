package com.uroad.zhgs.adapteRv

import android.content.Context
import android.support.v4.util.ArrayMap
import android.view.View
import com.uroad.zhgs.R
import com.uroad.zhgs.model.MutilItem
import com.uroad.zhgs.model.RidersInvitingMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 * @author MFB
 * @create 2018/10/22
 * @describe 车友列表适配器（上次组队好友，关注的好友）
 */
class RidersInvitingAdapter(context: Context, mDatas: MutableList<MutilItem>)
    : BaseArrayRecyclerAdapter<MutilItem>(context, mDatas) {

    private val arrayMap = ArrayMap<String, Boolean>()
    private val mSelected = ArrayList<RidersInvitingMDL.Riders>()
    private var onCheckChangeListener: OnCheckChangeListener? = null
    fun setOnCheckChangeListener(onCheckChangeListener: OnCheckChangeListener?) {
        this.onCheckChangeListener = onCheckChangeListener
    }

    override fun getItemViewType(position: Int): Int = mDatas[position].getItemType()

    override fun bindView(viewType: Int): Int {
        if (viewType == 0) return R.layout.item_riders_inviting1
        return R.layout.item_riders_inviting2
    }

    override fun onBindHoder(holder: RecyclerHolder, t: MutilItem, position: Int) {
        val itemType = holder.itemViewType
        if (itemType == 0) {
            val mdl = t as RidersInvitingMDL.RiderType
            holder.setText(R.id.tvText, mdl.text)
        } else {
            val mdl = t as RidersInvitingMDL.Riders
            holder.displayImage(R.id.ivIcon, mdl.iconfile, R.mipmap.ic_user_default)
            holder.setText(R.id.tvName, mdl.username)
            if ((position + 1) in 0 until mDatas.size && mDatas[position + 1].getItemType() == 0) {
                holder.setVisibility(R.id.divider, View.GONE)
            } else {
                holder.setVisibility(R.id.divider, View.VISIBLE)
            }
            holder.itemView.setOnClickListener {
                if (mdl.isInvitation == 1) {
                    val isChecked = arrayMap[mdl.userid]
                    if (isChecked == null || !isChecked) {
                        holder.setImageResource(R.id.ivCheck, R.mipmap.ic_circle_checkbox_checked)
                        arrayMap[mdl.userid] = true
                        mSelected.add(mdl)
                    } else {
                        holder.setImageResource(R.id.ivCheck, R.mipmap.ic_circle_checkbox_default)
                        arrayMap[mdl.userid] = false
                        mSelected.remove(mdl)
                    }
                    onCheckChangeListener?.onSelected(mSelected)
                }
            }
            if (mdl.isInvitation == 1) {
                val isChecked = arrayMap[mdl.userid]
                if (isChecked == null || !isChecked) {
                    holder.setImageResource(R.id.ivCheck, R.mipmap.ic_circle_checkbox_default)
                } else {
                    holder.setImageResource(R.id.ivCheck, R.mipmap.ic_circle_checkbox_checked)
                }
                holder.setVisibility(R.id.tvInTeam, View.GONE)
            } else {
                holder.setImageResource(R.id.ivCheck, R.mipmap.ic_circle_checkbox_enable)
                holder.setVisibility(R.id.tvInTeam, View.VISIBLE)
            }
        }
    }

    interface OnCheckChangeListener {
        fun onSelected(mSelected: MutableList<RidersInvitingMDL.Riders>)
    }
}