package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.model.RidersMsgMDL
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter

@Suppress("DEPRECATION")
/**
 * @author MFB
 * @create 2018/10/26
 * @describe 车有组队 多人邀请弹窗
 */
class RidersMultiInvitDialog(private val context: Activity,
                             private val msgList: MutableList<RidersMsgMDL.Content>)
    : Dialog(context, R.style.translucentDialog) {
    private var onViewClickListener: OnViewClickListener? = null

    fun onViewClickListener(onViewClickListener: OnViewClickListener): RidersMultiInvitDialog {
        this.onViewClickListener = onViewClickListener
        return this
    }

    override fun show() {
        super.show()
        initView()
    }

    private fun initView() {
        window?.let { window ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_riders_multiinvit, LinearLayout(context), false)
            val tvTitle = contentView.findViewById<TextView>(R.id.tvTitle)
            val recyclerView = contentView.findViewById<RecyclerView>(R.id.recyclerView)
            val tvClose = contentView.findViewById<TextView>(R.id.tvClose)
            val source = context.resources.getString(R.string.dialog_riders_multiInviting_title)
            val start = source.indexOf("《")
            val end = source.length - 1
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(view: View) {
                    onViewClickListener?.onViewClick(1, this@RidersMultiInvitDialog)
                }

                override fun updateDrawState(ds: TextPaint?) {
                    ds?.isUnderlineText = false
                }
            }
            tvTitle.text = SpannableString(source).apply {
                setSpan(clickableSpan, start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorAccent)), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            tvTitle.movementMethod = LinkMovementMethod.getInstance()
            recyclerView.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
            recyclerView.adapter = RidersInvitingAdapter(context, msgList)
            setMaxHeight(recyclerView)
            tvClose.setOnClickListener { onViewClickListener?.onViewClick(2, this@RidersMultiInvitDialog) }
            window.setContentView(contentView)
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.CENTER)
        }
    }

    /**设置recyclerView最大高度*/
    private fun setMaxHeight(recyclerView: RecyclerView) {
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                //设置recyclerView高度
                val layoutParams = recyclerView.layoutParams
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                } else {
                    recyclerView.viewTreeObserver.removeGlobalOnLayoutListener(this)
                }
                val height = DisplayUtils.getWindowHeight(context) / 2
                if (recyclerView.height < height) {
                    layoutParams.height = recyclerView.height
                } else {
                    layoutParams.height = height
                }
                recyclerView.layoutParams = layoutParams
            }
        })
    }

    private inner class RidersInvitingAdapter(private val context: Activity, list: MutableList<RidersMsgMDL.Content>)
        : BaseArrayRecyclerAdapter<RidersMsgMDL.Content>(context, list) {
        private var mSelected = -1
        override fun bindView(viewType: Int): Int = R.layout.item_riders_multiinviting

        override fun onBindHoder(holder: RecyclerHolder, t: RidersMsgMDL.Content, position: Int) {
            var nameSource = ""
            var start = 0
            t.username?.let {
                nameSource += it
                start = nameSource.length
            }
            nameSource += "邀请你组队"
            var placeSource = "目的地："
            val end = placeSource.length
            t.toplace?.let { placeSource += it }
            if (mSelected == position) {
                holder.setText(R.id.tvName, nameSource)
                holder.setTextColorRes(R.id.tvName, R.color.colorAccent)
                holder.setText(R.id.tvToPlace, placeSource)
                holder.setTextColorRes(R.id.tvToPlace, R.color.colorAccent)
                holder.setImageResource(R.id.ivArrow, R.mipmap.ic_arrow_right_accent)
            } else {
                holder.setText(R.id.tvName, SpannableString(nameSource).apply { setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.blow_gray)), start, nameSource.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE) })
                holder.setTextColorRes(R.id.tvName, R.color.appTextColor)
                holder.setText(R.id.tvToPlace, SpannableString(placeSource).apply { setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.blow_gray)), 0, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE) })
                holder.setTextColorRes(R.id.tvToPlace, R.color.appTextColor)
                holder.setImageResource(R.id.ivArrow, R.mipmap.ic_arrow_right_grey)
            }
            holder.itemView.setOnClickListener {
                onViewClickListener?.onItemSelected(t, this@RidersMultiInvitDialog)
                mSelected = position
                notifyDataSetChanged()
            }
        }
    }

    interface OnViewClickListener {
        fun onViewClick(type: Int, dialog: RidersMultiInvitDialog)
        fun onItemSelected(content: RidersMsgMDL.Content, dialog: RidersMultiInvitDialog)
    }
}