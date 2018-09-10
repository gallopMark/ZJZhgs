package com.uroad.zhgs.photopicker.ui

import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.KeyEvent
import android.view.View
import com.uroad.library.utils.DisplayUtils
import com.uroad.library.utils.PermissionHelper
import com.uroad.zhgs.BuildConfig
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.dialog.MaterialDialog
import com.uroad.zhgs.photopicker.adapter.ImageFolderAdapter
import com.uroad.zhgs.photopicker.adapter.ImageGridAdapter
import com.uroad.zhgs.photopicker.data.ImagePicker
import com.uroad.zhgs.photopicker.data.ImageSource
import com.uroad.zhgs.photopicker.model.ImageFolder
import com.uroad.zhgs.photopicker.model.ImageItem
import com.uroad.zhgs.photopicker.utils.AnimationUtil
import com.uroad.zhgs.rv.BaseRecyclerAdapter
import com.uroad.zhgs.widget.CurrencyLoadView
import com.uroad.zhgs.widget.GridSpacingItemDecoration
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_photopicker_imagegrid.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 *Created by MFB on 2018/7/30.
 * 图片选择页面
 */
class ImageGridActivity : BaseActivity() {
    private val folders = ArrayList<ImageFolder>()
    private lateinit var folderAdapter: ImageFolderAdapter
    private val mDatas = ArrayList<ImageItem>()
    private lateinit var adapter: ImageGridAdapter
    private var isLoadData: Boolean = false
    private var cameraPath: String? = null
    private var isMutily: Boolean = false
    private var limit: Int = 1
    private var isCrop = false    //是否需要裁剪
    private var isSaveRectangle = true  //裁剪后的图片是否是矩形，否者跟随裁剪框的形状
    private var outPutX = 800           //裁剪保存宽度
    private var outPutY = 800           //裁剪保存高度
    private var focusWidth = 280         //焦点框的宽度
    private var focusHeight = 280        //焦点框的高度
    private var permissionHelper: PermissionHelper? = null

    companion object {
        private const val REQUEST_CAMERA = 0x0001
        private const val REQUEST_PREVIEW = 0x0002
        private const val REQUEST_CROP = 0x0003
    }

    @Suppress("UNCHECKED_CAST")
    override fun setUp(savedInstanceState: Bundle?) {
        setBaseContentLayout(R.layout.activity_photopicker_imagegrid)
        withTitle(resources.getString(R.string.photopicker_selection_of_photos))
        withOption(resources.getString(R.string.photopicker_confirm))
        val bundle = intent.extras
        bundle?.let {
            isMutily = it.getBoolean("mMutilyMode", false)
            limit = it.getInt("limit", 1)
            isCrop = it.getBoolean("isCrop", false)
            outPutX = it.getInt("outPutX", outPutX)
            outPutY = it.getInt("outPutY", outPutY)
            focusWidth = it.getInt("focusWidth", focusWidth)
            focusHeight = it.getInt("focusHeight", focusHeight)
            isSaveRectangle = it.getBoolean("isSaveRectangle", false)
        }
        if (isMutily) {
            rlFolder.visibility = View.VISIBLE
        } else {
            rlFolder.visibility = View.GONE
        }
        rvFolder.layoutManager = LinearLayoutManager(this).apply { LinearLayoutManager.VERTICAL }
        folderAdapter = ImageFolderAdapter(this, folders)
        rvFolder.adapter = folderAdapter
        recyclerView.addItemDecoration(GridSpacingItemDecoration(3, DisplayUtils.dip2px(this, 2f), false))
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        adapter = ImageGridAdapter(this, mDatas, isMutily, limit)
        recyclerView.adapter = adapter
        if (!hasPermission()) {
            applyPermissions()
        } else {
            initFolders()
        }
    }

    private fun hasPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun applyPermissions() {
        permissionHelper = PermissionHelper(this).apply {
            requestPermissions(object : PermissionHelper.PermissionListener {
                override fun doAfterGrand(vararg permission: String?) {
                    initFolders()
                }

                override fun doAfterDenied(vararg permission: String?) {
                    afterDenied()
                }
            }, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun initFolders() {
        loadView.setState(CurrencyLoadView.STATE_LOADING)
        val disposable = Flowable.fromCallable { ImageSource.getFolders(this) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    rlContainer.visibility = View.VISIBLE
                    loadView.setState(CurrencyLoadView.STATE_GONE)
                    folders.addAll(it)
                    folderAdapter.notifyDataSetChanged()
                    mDatas.add(ImageItem().apply { showCamera = true })
                    if (folders.size > 0) {
                        tvText.text = folders[0].name
                        mDatas.addAll(folders[0].mediaItems)
                        adapter.notifyDataSetChanged()
                    }
                }, {
                    loadView.setState(CurrencyLoadView.STATE_GONE)
                })
        addDisposable(disposable)
        isLoadData = true
    }

    private fun afterDenied() {
        var isOpen = false
        val dialog = MaterialDialog(this@ImageGridActivity)
        dialog.setTitle("温馨提示")
        dialog.setMessage("存储权限已被禁止，请重新打开存储权限！")
        dialog.setCanceledOnTouchOutside(false)
        dialog.setNegativeButton("取消", object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                dialog.dismiss()
                finish()
            }
        })
        dialog.setPositiveButton("打开", object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                isOpen = true
                openSettings()
                dialog.dismiss()
            }
        })
        dialog.setOnDismissListener {
            if (!isOpen) finish()
        }
    }

    override fun setListener() {
        folderAdapter.setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(adapter: BaseRecyclerAdapter, holder: BaseRecyclerAdapter.RecyclerHolder, view: View, position: Int) {
                folderAdapter.setSelectedItem(position)
                mDatas.clear()
                if (position == 0) {
                    mDatas.add(ImageItem().apply { showCamera = true })
                }
                mDatas.addAll(folders[position].mediaItems)
                adapter.notifyDataSetChanged()
                tvText.text = folders[position].name
                closeFolder()
            }
        })
        adapter.setOnImageItemClickListener(object : ImageGridAdapter.OnImageItemClickListener {
            override fun onCamera() {
                if (hasCameara()) takePhoto()
                else {
                    requestCamera()
                }
            }

            override fun onOverSelected(limit: Int) {
                showLongToast("您最多只能选择${limit}张图片")
            }

            override fun onSelected(mDatas: ArrayList<ImageItem>) {
                if (isMutily) {
                    dealWith(mDatas)
                } else {
                    if (isCrop) {
                        val intent = Intent(this@ImageGridActivity, ImageCropActivity::class.java).apply { putExtra("imagePath", mDatas[0].path) }
                        openActivityForResult(intent, getBundle(), REQUEST_CROP)
                    } else {
                        onResult(mDatas)
                    }
                }
            }
        })
        flFolder.setOnClickListener { closeFolder() }
        rlFolder.setOnClickListener {
            if (flFolder.visibility == View.VISIBLE) {
                closeFolder()
            } else {
                openFolder()
            }
        }
    }

    private fun hasCameara(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCamera() {
        permissionHelper = PermissionHelper(this).apply {
            requestPermissions(object : PermissionHelper.PermissionListener {
                override fun doAfterGrand(vararg permission: String?) {
                    takePhoto()
                }

                override fun doAfterDenied(vararg permission: String?) {
                }
            }, android.Manifest.permission.CAMERA)
        }
    }

    private fun takePhoto() {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            val uri = getMediaFileUri()
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            openActivityForResult(intent, REQUEST_CAMERA)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getMediaFileUri(): Uri? {
        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath)
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return null
            }
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date())
        val file = File(dir.path + File.separator + "IMG_" + timeStamp + ".jpg")
        cameraPath = file.absolutePath
        val authority = BuildConfig.APPLICATION_ID + ".provider"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FileProvider.getUriForFile(this, authority, file)//通过FileProvider创建一个content类型的Uri
        } else {
            Uri.fromFile(file)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionHelper?.handleRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            if (File(cameraPath).exists()) {
                if (isCrop) {
                    val intent = Intent(this@ImageGridActivity, ImageCropActivity::class.java).apply { putExtra("imagePath", cameraPath) }
                    openActivityForResult(intent, getBundle(), REQUEST_CROP)
                } else {
                    onResult(ArrayList<ImageItem>().apply {
                        add(ImageItem().apply { path = cameraPath })
                    })
                }
            }
        } else if (requestCode == REQUEST_PREVIEW) {
            val images = data?.getParcelableArrayListExtra<ImageItem>("images")
            images?.let { dealWith(it) }
            when (resultCode) {
                RESULT_OK -> images?.let { onResult(it) }
                RESULT_CANCELED -> images?.let {
                    adapter.setSelects(it)
                }
            }
        } else if (requestCode == REQUEST_CROP && resultCode == RESULT_OK) {
            onResult(ArrayList<ImageItem>().apply { add(ImageItem().apply { path = data?.getStringExtra("crop_image") }) })
        }
    }

    private fun getBundle(): Bundle = Bundle().apply {
        putBoolean("mMutilyMode", isSaveRectangle)
        putInt("limit", limit)
        putBoolean("isCrop", isCrop)
        putBoolean("isSaveRectangle", isSaveRectangle)
        putInt("outPutX", outPutX)
        putInt("outPutY", outPutY)
        putInt("focusWidth", focusWidth)
        putInt("focusHeight", focusHeight)
    }

    private fun dealWith(mDatas: ArrayList<ImageItem>) {
        var preview = resources.getString(R.string.photopicker_preview)
        var confirm = resources.getString(R.string.photopicker_confirm)
        if (mDatas.size > 0) {
            tvBaseOption.isEnabled = true
            tvPreview.isEnabled = true
            confirm += "(${mDatas.size}/$limit)"
            preview += "(${mDatas.size})"
            tvPreview.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
        } else {
            tvBaseOption.isEnabled = false
            tvPreview.isEnabled = false
            tvPreview.setTextColor(ContextCompat.getColor(this, R.color.grey))
        }
        tvBaseOption.text = confirm
        tvPreview.text = preview
        tvPreview.setOnClickListener {
            val intent = Intent(this@ImageGridActivity, ImagePreViewActivity::class.java)
                    .apply { putParcelableArrayListExtra("photos", mDatas) }
            openActivityForResult(intent, REQUEST_PREVIEW)
        }
        tvBaseOption.setOnClickListener { onResult(mDatas) }
    }

    private fun onResult(mDatas: MutableList<ImageItem>) {
        val intent = Intent()
        val paths = ArrayList<String>()
        for (item in mDatas) {
            item.path?.let { paths.add(it) }
        }
        intent.putStringArrayListExtra(ImagePicker.EXTRA_PATHS, paths)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN && flFolder.visibility == View.VISIBLE) {
            closeFolder()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun openFolder() {
        AnimationUtil.bottomMoveToViewLocation(flFolder, 300)
    }

    /*** 收起文件夹列表*/
    private fun closeFolder() {
        AnimationUtil.moveToViewBottom(flFolder, 300)
    }

    override fun onRestart() {
        super.onRestart()
        if (hasPermission() && !isLoadData) {
            initFolders()
        }
    }
}