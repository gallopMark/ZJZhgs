package com.uroad.zhgs.photopicker.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.util.ArrayMap
import android.support.v4.view.ViewPager
import com.uroad.zhgs.common.BaseActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.uroad.zhgs.R
import com.uroad.zhgs.photopicker.adapter.ImagePageAdapter
import com.uroad.zhgs.photopicker.adapter.ImageRvAdapter
import com.uroad.zhgs.photopicker.model.ImageItem
import com.uroad.zhgs.photopicker.utils.AnimationUtil
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.utils.GsonUtils
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_photopicker_preview.*


/**
 *Created by MFB on 2018/7/31.
 */
class ImagePreViewActivity : BaseActivity() {
    private val photos = ArrayList<ImageItem>()
    private val checkItems = ArrayMap<Int, ImageItem>()
    private lateinit var pageAdapter: ImagePageAdapter
    private lateinit var imageAdapter: ImageRvAdapter
    private var isOpen = true
    private var onRightClick = false

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_photopicker_preview)
        val mDatas = intent.getParcelableArrayListExtra<ImageItem>("photos")
        if (mDatas.size > 0) {
            photos.addAll(mDatas)
            for (i in 0 until photos.size) {
                checkItems[i] = photos[i]
            }
        }
        if (photos.size > 0) {
            withTitle("1/${photos.size}")
            setFinishButton()
        } else {
            withTitle("0/0")
        }
        pageAdapter = ImagePageAdapter(this, photos)
        viewPager.adapter = pageAdapter
        recyclerView.layoutManager = LinearLayoutManager(this).apply { orientation = LinearLayoutManager.HORIZONTAL }
        imageAdapter = ImageRvAdapter(this, photos)
        recyclerView.adapter = imageAdapter
    }

    override fun setListener() {
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(position: Int) {
                withTitle("${position + 1}/${photos.size}")
                imageAdapter.setSelectIndex(position)
                recyclerView.smoothScrollToPosition(position)
                imageAdapter.getUnCheckItems()[position]?.let { checkBox.isChecked = it }
            }
        })
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) checkItems.remove(viewPager.currentItem)
            else checkItems[viewPager.currentItem] = photos[viewPager.currentItem]
            imageAdapter.setUnCheckItem(viewPager.currentItem, isChecked)
            setFinishButton()
        }
        pageAdapter.setOnItemClickListener(object : ImagePageAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, path: ImageItem) {
                if (isOpen) {
                    closeView()
                    isOpen = false
                } else {
                    openView()
                    isOpen = true
                }
            }
        })
        imageAdapter.setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                viewPager.setCurrentItem(position, false)
                imageAdapter.setSelectIndex(position)
            }
        })
    }

    private fun openView() {
        AnimationUtil.topMoveToViewLocation(toolbar, 200)
        AnimationUtil.bottomMoveToViewLocation(llBottom, 200)
        flContainer.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }

    private fun closeView() {
        AnimationUtil.moveToViewBottom(llBottom, 200)
        AnimationUtil.moveToViewTop(toolbar, 200)
        flContainer.systemUiVisibility = View.INVISIBLE
    }

    private fun setFinishButton() {
        val text = "${resources.getString(R.string.photopicker_finish)}(${checkItems.size}/${photos.size})"
        withOption(text)
        getOptionView().isEnabled = checkItems.size != 0
    }

    override fun onOptionClickListener(tvBaseOption: TextView) {
        onRightClick = true
        finish()
    }

    override fun finish() {
        val intent = Intent().apply {
            val images = ArrayList<ImageItem>().apply { addAll(checkItems.values) }
            putParcelableArrayListExtra("images", images)
        }
        if (onRightClick) {
            setResult(RESULT_OK, intent)
        } else {
            setResult(RESULT_CANCELED, intent)
        }
        super.finish()
    }
}