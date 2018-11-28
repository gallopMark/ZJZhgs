package com.uroad.zhgs.adapteRv

import android.app.Activity
import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.airbnb.lottie.LottieAnimationView
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.helper.UserPreferenceHelper
import com.uroad.zhgs.model.RidersReportMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

/**
 *Created by MFB on 2018/8/7.
 * 报料列表适配器
 */
class RidersReportAdapter(private val context: Activity, mData: MutableList<RidersReportMDL>)
    : BaseArrayRecyclerAdapter<RidersReportMDL>(context, mData) {

    private val default = ContextCompat.getDrawable(context, R.mipmap.ic_support_default).apply { this?.setBounds(0, 0, minimumWidth, minimumHeight) }
    private val pressed = ContextCompat.getDrawable(context, R.mipmap.ic_support_pressed).apply { this?.setBounds(0, 0, minimumWidth, minimumHeight) }
    private val width = (DisplayUtils.getWindowWidth(context) * 0.4).toInt()
    private val height = (width * 1.3).toInt()
    private var onAdapterChildClickListener: OnAdapterChildClickListener? = null
    private val currUserId = UserPreferenceHelper.getUserId(context)
    private val colorWhite = ContextCompat.getColor(context, R.color.white)
    private val sp13 = context.resources.getDimensionPixelOffset(R.dimen.font_13)

    override fun bindView(viewType: Int): Int = R.layout.item_ridersreport

    override fun onBindHoder(holder: RecyclerHolder, t: RidersReportMDL, position: Int) {
        holder.displayImage(R.id.ivIco, t.iconfile, R.mipmap.ic_user_default)
        holder.setText(R.id.tvUserName, t.username)
        holder.setText(R.id.tvShortname, t.shortname)
        val rvPics = holder.obtainView<RecyclerView>(R.id.rvPics)
        val flVideo = holder.obtainView<FrameLayout>(R.id.flVideo)
        val lottieView = holder.obtainView<LottieAnimationView>(R.id.lottieView)
        holder.setText(R.id.tvRemark, getTextStyle(t.getEventType(), t.remark, t.getColor(context)))
        if (t.isfollow == 0) {
            holder.setImageResource(R.id.ivFollow, R.mipmap.ic_follow_ok)
        } else {
            holder.setImageResource(R.id.ivFollow, R.mipmap.ic_follow)
        }
        if (TextUtils.equals(t.userid, currUserId)) {  //不能关注自己，隐藏关注按钮
            holder.setVisibility(R.id.ivFollow, View.GONE)
        } else {
            if (t.isfollowstatus == 1) {
                holder.setVisibility(R.id.ivFollow, View.VISIBLE)
            } else {
                if (t.isfollow == 0) { //用户关闭了被关注（在已关注的情况下可以取消关注）
                    holder.setVisibility(R.id.ivFollow, View.VISIBLE)
                } else {
                    holder.setVisibility(R.id.ivFollow, View.GONE)
                }
            }
        }
        if (t.category == 2) {
            rvPics.visibility = View.GONE
            holder.setVisibility(R.id.flVoice, View.GONE)
            flVideo.visibility = View.VISIBLE
            flVideo.layoutParams = flVideo.layoutParams.apply {
                this.width = this@RidersReportAdapter.width
                this.height = this@RidersReportAdapter.height
            }
            holder.displayImage(R.id.ivThumb, t.remark1s, R.color.white)
        } else if (t.category == 3) {
            rvPics.visibility = View.GONE
            flVideo.visibility = View.GONE
            holder.setVisibility(R.id.flVoice, View.VISIBLE)
            var seconds = ""
            t.remark1s?.let { seconds += it }
            holder.setText(R.id.tvVoiceTime, "$seconds″")
            if (t.isVoicePlaying) {
                lottieView.playAnimation()
            } else {
                lottieView.cancelAnimation()
                lottieView.progress = 1f
            }
        } else {
            flVideo.visibility = View.GONE
            holder.setVisibility(R.id.flVoice, View.GONE)
            rvPics.visibility = View.VISIBLE
            rvPics.isNestedScrollingEnabled = false
            rvPics.layoutManager = GridLayoutManager(context, 3).apply { orientation = GridLayoutManager.VERTICAL }
            if (t.getImageUrls().size > 0) {
                rvPics.adapter = PicAdapter(context, t.getImageUrls())
                rvPics.visibility = View.VISIBLE
            } else {
                rvPics.visibility = View.GONE
            }
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
        holder.setOnClickListener(R.id.flVoice, View.OnClickListener { onAdapterChildClickListener?.onVoiceClick(position, lottieView) })
        flVideo.setOnClickListener { onAdapterChildClickListener?.onVideoClick(position) }
        holder.setOnClickListener(R.id.ivFollow, View.OnClickListener { onAdapterChildClickListener?.onFollowClick(position) })
        holder.setOnClickListener(R.id.tvCommentCount, View.OnClickListener { onAdapterChildClickListener?.onCommentClick(position, holder.obtainView(R.id.bottomView)) })
        holder.setOnClickListener(R.id.tvSupportCount, View.OnClickListener { onAdapterChildClickListener?.onSupportClick(position, holder.itemView) })
    }

    private fun getTextStyle(eventType: String?, remark: String?, color: Int): SpannableString {
        var content = ""
        val end: Int
        content += if (!TextUtils.isEmpty(eventType)) "\u2000$eventType\u2000" else ""
        end = content.length
        content += if (!TextUtils.isEmpty(content)) "\u2000$remark" else remark
        val ss = SpannableString(content)
        ss.setSpan(BackgroundColorSpan(color), 0, end, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)
        ss.setSpan(ForegroundColorSpan(colorWhite), 0, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(AbsoluteSizeSpan(sp13, false), 0, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        return ss
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
                                       mDatas: MutableList<RidersReportMDL.Comment>,
                                       private val view: View)
        : BaseArrayRecyclerAdapter<RidersReportMDL.Comment>(context, mDatas) {
        private val color = ContextCompat.getColor(context, R.color.color_33)
        override fun onBindHoder(holder: RecyclerHolder, t: RidersReportMDL.Comment, position: Int) {
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
        fun onFollowClick(position: Int)
        fun onVoiceClick(position: Int, lottieView: LottieAnimationView)
        fun onVideoClick(position: Int)
        fun onCommentClick(position: Int, view: View)
        fun onSupportClick(position: Int, view: View)
        fun onChildCommentClick(view: View, position: Int, parentid: String?, userid: String?, username: String?)
    }

    fun setOnAdapterChildClickListener(onAdapterChildClickListener: OnAdapterChildClickListener) {
        this.onAdapterChildClickListener = onAdapterChildClickListener
    }
}