package com.uroad.zhgs.photopicker.data

import android.content.Context
import android.provider.MediaStore
import com.uroad.zhgs.R
import com.uroad.zhgs.photopicker.model.ImageFolder
import com.uroad.zhgs.photopicker.model.ImageItem
import java.io.File


/**
 *Created by MFB on 2018/7/30.
 */
object ImageSource {

    //获取相册图片
    fun queryImages(context: Context, isLimit: Boolean): MutableList<ImageItem> {
        val projection = arrayOf(//查询图片需要的数据列
                MediaStore.Images.Media.DISPLAY_NAME, //图片的显示名称
                MediaStore.Images.Media.DATA, //图片的真实路径
                MediaStore.Images.Media.SIZE, //图片的大小，long型
                MediaStore.Images.Media.MIME_TYPE, //图片的类型     image/jpeg
                MediaStore.Images.Media.DATE_ADDED)    //图片被添加的时间，long型
        val mResolver = context.contentResolver
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor = if (isLimit)
            mResolver.query(uri, projection, null, null, "${projection[4]} DESC LIMIT 0,100")
        else
            mResolver.query(uri, projection, null, null, "${projection[4]} DESC")
        val mDatas = ArrayList<ImageItem>()
        cursor.let {
            while (it.moveToNext()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(projection[0]))
                val path = cursor.getString(cursor.getColumnIndexOrThrow(projection[1]))
                val size = cursor.getLong(cursor.getColumnIndexOrThrow(projection[2]))
                val type = cursor.getString(cursor.getColumnIndexOrThrow(projection[3]))
                val added = cursor.getLong(cursor.getColumnIndexOrThrow(projection[4]))
                val item = ImageItem().apply {
                    this.name = name
                    this.path = path
                    this.size = size
                    this.mimeType = type
                    this.addTime = added
                }
                mDatas.add(item)
            }
        }
        cursor?.close()
        return mDatas
    }

    //图片按文件夹分类
    fun getFolders(context: Context): MutableList<ImageFolder> {
        val recentDatas = queryImages(context, true)
        val folders = ArrayList<ImageFolder>()
        folders.add(ImageFolder().apply {
            mediaItems.addAll(recentDatas)
            name = context.resources.getString(R.string.photopicker_recent_pictures)
        })
        val mDatas = queryImages(context, false)
        for (i in 0 until mDatas.size) {
            val item = mDatas[i]
            val parent = File(item.path).parentFile
            val folder = ImageFolder(parent.absolutePath, parent.name)
            if (!folders.contains(folder)) {
                folder.firstImagePath = item.path
                folder.mediaItems.add(item)
                folders.add(folder)
            } else {
                folders[folders.indexOf(folder)].mediaItems.add(item)
            }
        }
        return folders
    }

    /*根据父目录查找图片*/
    fun queryImageFromPath(context: Context, path: String): MutableList<ImageItem> {
        val projection = arrayOf(//查询图片需要的数据列
                MediaStore.Images.Media.DISPLAY_NAME, //图片的显示名称
                MediaStore.Images.Media.DATA, //图片的真实路径
                MediaStore.Images.Media.SIZE, //图片的大小，long型
                MediaStore.Images.Media.MIME_TYPE, //图片的类型     image/jpeg
                MediaStore.Images.Media.DATE_ADDED)    //图片被添加的时间，long型
        val mResolver = context.contentResolver
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val selection = "${projection[1]} LIKE '%$path%'"
        val cursor = mResolver.query(uri, projection, selection, null, "${projection[4]} DESC")
        val mDatas = ArrayList<ImageItem>()
        cursor.let {
            while (it.moveToNext()) {
                val item = ImageItem().apply {
                    this.name = cursor.getString(cursor.getColumnIndexOrThrow(projection[0]))
                    this.path = cursor.getString(cursor.getColumnIndexOrThrow(projection[1]))
                    this.size = cursor.getLong(cursor.getColumnIndexOrThrow(projection[2]))
                    this.mimeType = cursor.getString(cursor.getColumnIndexOrThrow(projection[3]))
                    this.addTime = cursor.getLong(cursor.getColumnIndexOrThrow(projection[4]))
                }
                mDatas.add(item)
            }
        }
        cursor?.close()
        return mDatas
    }
}