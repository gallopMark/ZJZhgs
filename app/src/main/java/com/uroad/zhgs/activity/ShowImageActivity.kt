package com.uroad.zhgs.activity

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.uroad.imageloader_v4.ImageLoaderV4
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.photopicker.utils.ImageUtil
import kotlinx.android.synthetic.main.activity_showbig_image.*

/**
 *Created by MFB on 2018/8/7.
 */
class ShowImageActivity : BaseActivity() {
    private val photos = ArrayList<String>()

    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayoutWithoutTitle(R.layout.activity_showbig_image)
        requestWindowFullScreen()
        intent.getStringArrayListExtra("photos")?.let { photos.addAll(it) }
        val position = intent.getIntExtra("position", 0)
        setCurrent(position)
        val adapter = ImagePageAdapter(this, photos)
        viewPager.adapter = adapter
        viewPager.currentItem = position
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(position: Int) {
                setCurrent(position)
            }
        })
    }

    private fun setCurrent(position: Int) {
        val pos = if (position > photos.size) photos.size else position
        val current = "${(pos + 1)}/${photos.size}"
        tvIndex.text = current
    }

    inner class ImagePageAdapter(private val context: Context,
                                 private val images: ArrayList<String>) : PagerAdapter() {

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return images.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val photoView = PhotoView(context)
            photoView.adjustViewBounds = true
            container.addView(photoView)
            ImageLoaderV4.getInstance().getBitmapFromCache(context, images[position]) { resource ->
                resource?.let {
                    val bw = resource.width
                    val bh = resource.height
                    if (bw > 8192 || bh > 8192) {
                        val bitmap = ImageUtil.zoomBitmap(resource, 8192, 8192)
                        setBitmap(photoView, bitmap)
                    } else {
                        setBitmap(photoView, resource)
                    }
                }
            }
            photoView.setOnClickListener { onBackPressed() }
            return photoView
        }

        private fun setBitmap(imageView: PhotoView, bitmap: Bitmap?) {
            imageView.setImageBitmap(bitmap)
            bitmap?.let {
                val bw = it.width
                val bh = it.height
                val vw = imageView.width
                val vh = imageView.height
                if (bw != 0 && bh != 0 && vw != 0 && vh != 0) {
                    if (1.0f * bh / bw > 1.0f * vh / vw) {
                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    } else {
                        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                    }
                }
            }
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            if (`object` is PhotoView) {
                Glide.with(context).clear(`object`)
                `object`.setImageDrawable(null)
                container.removeView(`object`)
            }
        }
    }

    override fun finish() {
        overridePendingTransition(0, R.anim.zoom_out)
        super.finish()
    }
}