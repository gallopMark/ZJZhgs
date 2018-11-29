package com.uroad.zhgs.dialog

import android.app.Activity
import android.app.Dialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.R
import com.uroad.zhgs.common.CarNoType
import com.uroad.zhgs.model.MutilItem
import com.uroad.zhgs.photopicker.widget.RecycleViewDivider
import com.uroad.zhgs.rv.BaseArrayRecyclerAdapter
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.widget.GridSpacingItemDecoration

/**
 * @author MFB
 * @create 2018/11/20
 * @describe 车牌号输入模拟软键盘输入框
 */
class CarNoInputDialog(private val context: Activity)
    : Dialog(context, R.style.transparentDialog) {
    private var onCarNoClickListener: OnCarNoClickListener? = null

    fun setOnCarNoClickListener(onCarNoClickListener: OnCarNoClickListener?): CarNoInputDialog {
        this.onCarNoClickListener = onCarNoClickListener
        return this
    }

    override fun show() {
        super.show()
        initView()
    }

    private fun initView() {
        window?.let { window ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_carnoinput, LinearLayout(context), false)
            val rvSpan = contentView.findViewById<RecyclerView>(R.id.rvSpan)
            val rvOption = contentView.findViewById<RecyclerView>(R.id.rvOption)
            val mdLs = CarNoType.getCarMulti()
            rvSpan.layoutManager = GridLayoutManager(context, 10)
            rvSpan.addItemDecoration(GridSpacingItemDecoration(10, DisplayUtils.dip2px(context, 5f), false))
            val mdLs1 = ArrayList<CarNoType.TextType>().apply { for (i in 0 until 30) add(mdLs[i] as CarNoType.TextType) }
            rvSpan.adapter = CarNoInputAdapter(context, mdLs1).apply {
                setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
                    override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                        if (position in 0 until mdLs1.size) onCarNoClickListener?.onCarNoClick(mdLs1[position].text, 0, this@CarNoInputDialog)
                    }
                })
            }
            val mdLs2 = ArrayList<MutilItem>().apply { for (i in 30 until mdLs.size) add(mdLs[i]) }
            rvOption.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.HORIZONTAL }
            rvOption.adapter = CarNoInputOptionAdapter(context, mdLs2).apply {
                setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
                    override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                        if (position in 0 until mdLs2.size) {
                            val mdl = mdLs2[position]
                            val option: Int
                            val province: String
                            if (mdl is CarNoType.TextType) {
                                province = mdl.text
                                option = 0
                            } else {
                                val t = mdl as CarNoType.Option
                                province = ""
                                option = if (t.text == "取消") 1 else 2
                            }
                            onCarNoClickListener?.onCarNoClick(province, option, this@CarNoInputDialog)
                        }
                    }
                })
            }
            window.setContentView(contentView)
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setWindowAnimations(R.style.dialog_anim)
            window.setGravity(Gravity.BOTTOM)
        }
    }

    private class CarNoInputAdapter(context: Activity, mDatas: MutableList<CarNoType.TextType>)
        : BaseArrayRecyclerAdapter<CarNoType.TextType>(context, mDatas) {
        private val size = (DisplayUtils.getWindowWidth(context)
                - DisplayUtils.dip2px(context, 10f) * 2
                - DisplayUtils.dip2px(context, 5f) * 9) / 10

        override fun bindView(viewType: Int): Int {
            return R.layout.item_carno_input
        }

        override fun onBindHoder(holder: RecyclerHolder, t: CarNoType.TextType, position: Int) {
            holder.setText(R.id.tvText, t.text)
            holder.setLayoutParams(R.id.tvText, FrameLayout.LayoutParams(size, size))
        }
    }

    private class CarNoInputOptionAdapter(context: Activity, mDatas: MutableList<MutilItem>)
        : BaseArrayRecyclerAdapter<MutilItem>(context, mDatas) {
        private val size1 = (DisplayUtils.getWindowWidth(context)
                - DisplayUtils.dip2px(context, 10f) * 2
                - DisplayUtils.dip2px(context, 5f) * 9) / 10
        private val size2 = ((DisplayUtils.getWindowWidth(context)
                - DisplayUtils.dip2px(context, 10f) * 2
                - DisplayUtils.dip2px(context, 5f) * 8) - size1 * 7) / 2
        private val dp5 = DisplayUtils.dip2px(context, 5f)

        override fun getItemViewType(position: Int): Int {
            return mDatas[position].getItemType()
        }

        override fun bindView(viewType: Int): Int {
            if (viewType == 1) return R.layout.item_carno_input
            return R.layout.item_carno_option
        }

        override fun onBindHoder(holder: RecyclerHolder, t: MutilItem, position: Int) {
            val itemType = holder.itemViewType
            val text = if (itemType == 1) {
                (t as CarNoType.TextType).text
            } else {
                (t as CarNoType.Option).text
            }
            holder.setText(R.id.tvText, text)
            val params = if (position == 0) {
                FrameLayout.LayoutParams(size2, FrameLayout.LayoutParams.WRAP_CONTENT).apply { rightMargin = dp5 }
            } else if (position == itemCount - 1) {
                FrameLayout.LayoutParams(size2, FrameLayout.LayoutParams.WRAP_CONTENT).apply { rightMargin = 0 }
            } else {
                FrameLayout.LayoutParams(size1, size1).apply { rightMargin = dp5 }
            }
            params.gravity = Gravity.CENTER
            holder.setLayoutParams(R.id.tvText, params)
        }
    }

    interface OnCarNoClickListener {
        fun onCarNoClick(province: String, option: Int, dialog: CarNoInputDialog)
    }
}