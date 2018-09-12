package com.uroad.zhgs.adapteRv

import android.app.Activity
import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.R.id.ivPic
import com.uroad.zhgs.model.UserEventMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.widget.GridSpacingItemDecoration

/**
 *Created by MFB on 2018/8/7.
 * 报料列表适配器
 */
class UserEventAdapter(private val context: Context, mDatas: MutableList<UserEventMDL>)
    : BaseArrayRecyclerAdapter<UserEventMDL>(context, mDatas) {

    private val default = ContextCompat.getDrawable(context, R.mipmap.ic_support_default).apply { this?.setBounds(0, 0, minimumWidth, minimumHeight) }
    private val pressed = ContextCompat.getDrawable(context, R.mipmap.ic_support_pressed).apply { this?.setBounds(0, 0, minimumWidth, minimumHeight) }

    private var onAdapterChildClickListener: OnAdapterChildClickListener? = null

    override fun onBindHoder(holder: RecyclerHolder, t: UserEventMDL, position: Int) {
        holder.displayImage(R.id.ivIco, t.iconfile, R.mipmap.ic_user_default)
        holder.setText(R.id.tvUserName, t.username)
        holder.setText(R.id.tvShortname, t.shortname)
        holder.setText(R.id.tvRemark, t.remark)
        if (!TextUtils.isEmpty(t.getEventType())) {
            holder.setVisibility(R.id.tvEventType, true)
            holder.setText(R.id.tvEventType, t.getEventType())
            holder.setBackgroundColor(R.id.tvEventType, t.getColor(context))
        } else {
            holder.setVisibility(R.id.tvEventType, false)
        }
        val rvPics = holder.obtainView<RecyclerView>(R.id.rvPics)
        rvPics.isNestedScrollingEnabled = false
        rvPics.layoutManager = GridLayoutManager(context, 3).apply { orientation = GridLayoutManager.VERTICAL }
        if (t.getImageUrls().size > 0) {
            rvPics.adapter = PicAdapter(context, t.getImageUrls())
            rvPics.visibility = View.VISIBLE
        } else {
            rvPics.visibility = View.GONE
        }
        holder.setText(R.id.tvTime, t.getTime())
        holder.setText(R.id.tvCommentCount, t.commentcount.toString())
        holder.setText(R.id.tvSupportCount, t.supportcount.toString())
        if (t.issupport != null) {
            holder.setDrawableLeft(R.id.tvSupportCount, pressed)
        } else {
            holder.setDrawableLeft(R.id.tvSupportCount, default)
        }
        val rvComment = holder.obtainView<RecyclerView>(R.id.rvComment)
        rvComment.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
        rvComment.isNestedScrollingEnabled = false
        if (t.hasComment()) {
            rvComment.visibility = View.VISIBLE
            rvComment.adapter = CommentAdapter(position, context, t.getCommentList(), holder.obtainView(R.id.bottomView))
        } else {
            rvComment.visibility = View.GONE
        }
        holder.setOnClickListener(R.id.tvCommentCount, View.OnClickListener { onAdapterChildClickListener?.onCommentClick(position, holder.obtainView(R.id.bottomView)) })
        holder.setOnClickListener(R.id.tvSupportCount, View.OnClickListener { onAdapterChildClickListener?.onSupportClick(position, holder.itemView) })
    }

    override fun bindView(viewType: Int): Int {
        return R.layout.item_userevent_main
    }

    inner class PicAdapter(context: Context, private val photos: MutableList<String>)
        : BaseArrayRecyclerAdapter<String>(context, photos) {
        val size = (DisplayUtils.getWindowWidth(context as Activity)
                - DisplayUtils.dip2px(context, 95f)) / 3

        override fun onBindHoder(holder: RecyclerHolder, t: String, position: Int) {
            holder.setLayoutParams(R.id.ivPic, LinearLayout.LayoutParams(size, size))
            holder.displayImage(R.id.ivPic, t, R.color.color_f2)
            holder.itemView.setOnClickListener { onAdapterChildClickListener?.onImageClick(position, photos) }
        }

        override fun bindView(viewType: Int): Int {
            return R.layout.item_userevent_pic
        }
    }

    private inner class CommentAdapter(private val mainIndex: Int,
                                       context: Context,
                                       mDatas: MutableList<UserEventMDL.Comment>,
                                       private val view: View)
        : BaseArrayRecyclerAdapter<UserEventMDL.Comment>(context, mDatas) {
        private val color = ContextCompat.getColor(context, R.color.color_33)
        override fun onBindHoder(holder: RecyclerHolder, t: UserEventMDL.Comment, position: Int) {
            var text = ""
            text += t.username ?: ""
            t.tousername?.let { if (it.trim().isNotEmpty()) text += " 回复 $it" }
            text += "："
            text += t.usercomment
            holder.setText(R.id.tvComment, SpannableString(text).apply { setSpan(ForegroundColorSpan(color), 0, text.indexOf("："), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE) })
            holder.itemView.setOnClickListener { onAdapterChildClickListener?.onChildCommentClick(view, mainIndex, t.eventid, t.userid, t.username) }
        }

        override fun bindView(viewType: Int): Int {
            return R.layout.item_userevent_comment
        }
    }

    interface OnAdapterChildClickListener {
        fun onImageClick(position: Int, photos: MutableList<String>)
        fun onCommentClick(position: Int, view: View)
        fun onSupportClick(position: Int, view: View)
        fun onChildCommentClick(view: View, position: Int, parentid: String?, userid: String?, username: String?)
    }

    fun setOnAdapterChildClickListener(onAdapterChildClickListener: OnAdapterChildClickListener) {
        this.onAdapterChildClickListener = onAdapterChildClickListener
    }
}