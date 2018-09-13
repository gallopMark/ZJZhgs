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
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.KeyEvent
import android.view.View
import com.uroad.library.utils.DisplayUtils
import com.uroad.zhgs.BuildConfig
import com.uroad.zhgs.R
import com.uroad.zhgs.common.BaseActivity
import com.uroad.zhgs.common.CurrApplication
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
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_photopicker_imagegrid.*
import top.zibin.luban.Luban
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
    private var isCompress = false //是否需要对图片压缩处理（非剪裁图片的情况下）
    private var isCrop = false    //是否需要裁剪
    private var isSaveRectangle = true  //裁剪后的图片是否是矩形，否者跟随裁剪框的形状
    private var outPutX = 800           //裁剪保存宽度
    private var outPutY = 800           //裁剪保存高度
    private var focusWidth = 280         //焦点框的宽度
    private var focusHeight = 280        //焦点框的高度

    companion object {
        private const val REQUEST_CAMERA = 0x0001
        private const val REQUEST_PREVIEW = 0x0002
        private const val REQUEST_CROP = 0x0003
        private const val PERMISSION_SDCARD = 0x0004
        private const val PERMISSION_CAMERA = 0x0005
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
            isCompress = it.getBoolean("isCompress", false)
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
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun applyPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_SDCARD)
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

    private fun afterDenied(type: Int) {
        var isOpen = false
        val dialog = MaterialDialog(this@ImageGridActivity)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setTitle(getString(R.string.dialog_default_title))
        val message = if (type == 1) getString(R.string.photopicker_noSdcard) else getString(R.string.photopicker_dismiss_sdcard)
        dialog.setMessage(message)
        val positive = if (type == 1) getString(R.string.dialog_button_confirm) else getString(R.string.dialog_button_open)
        dialog.setNegativeButton(getString(R.string.dialog_button_cancel), object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                dialog.dismiss()
            }
        })
        dialog.setPositiveButton(positive, object : MaterialDialog.ButtonClickListener {
            override fun onClick(v: View, dialog: AlertDialog) {
                isOpen = true
                openSettings()
                dialog.dismiss()
            }
        })
        dialog.setOnDismissListener { if (!isOpen) finish() }
        dialog.show()
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
                if (hasCamera()) takePhoto()
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

    private fun hasCamera(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCamera() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), PERMISSION_CAMERA)
    }

    private fun takePhoto() {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.resolveActivity(packageManager)?.let {
                var takeImageFile = File(getExternalStorageDir())
                takeImageFile = createFile(takeImageFile, "IMG_", ".jpg")
                cameraPath = takeImageFile.absolutePath
                val imageUri: Uri
                val authority = BuildConfig.APPLICATION_ID + ".provider"
                imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    FileProvider.getUriForFile(this, authority, takeImageFile)//通过FileProvider创建一个content类型的Uri
                } else {
                    Uri.fromFile(takeImageFile)
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(intent, REQUEST_CAMERA)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getExternalStorageDir(): String {
        val path = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            Environment.getExternalStorageDirectory().absolutePath
        } else Environment.getRootDirectory().absolutePath
        return "$path${File.separator}${getString(R.string.app_name)}"
    }

    private fun createFile(folder: File, prefix: String, suffix: String): File {
        if (!folder.exists() || !folder.isDirectory) folder.mkdirs()
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
        val filename = prefix + dateFormat.format(Date(System.currentTimeMillis())) + suffix
        return File(folder, filename)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_SDCARD -> {
                var allGranted = true
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false
                        break
                    }
                }
                if (allGranted) {
                    initFolders()
                } else {
                    for (permission in permissions) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                            afterDenied(1)
                            break
                        } else {
                            afterDenied(2)
                            break
                        }
                    }
                }
            }
            PERMISSION_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto()
                } else {
                    if (permissions.isNotEmpty() && !ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                        dismissCamera(1)
                    } else {
                        dismissCamera(2)
                    }
                }
            }
        }
    }

    //用户点击禁止相机或点击不再提示
    private fun dismissCamera(type: Int) {
        when (type) {
            1 -> {
                showDialog(getString(R.string.dialog_default_title), getString(R.string.photopicker_noCamera), object : MaterialDialog.ButtonClickListener {
                    override fun onClick(v: View, dialog: AlertDialog) {
                        dialog.dismiss()
                    }
                }, object : MaterialDialog.ButtonClickListener {
                    override fun onClick(v: View, dialog: AlertDialog) {
                        openSettings()
                        dialog.dismiss()
                    }
                })
            }
            else -> {
                showDialog(getString(R.string.dialog_default_title), getString(R.string.photopicker_dismiss_camera), object : MaterialDialog.ButtonClickListener {
                    override fun onClick(v: View, dialog: AlertDialog) {
                        dialog.dismiss()
                    }
                }, object : MaterialDialog.ButtonClickListener {
                    override fun onClick(v: View, dialog: AlertDialog) {
                        requestCamera()
                        dialog.dismiss()
                    }
                })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            cameraPath?.let {
                if (File(cameraPath).exists()) {
                    if (isCrop) {
                        val intent = Intent(this@ImageGridActivity, ImageCropActivity::class.java).apply { putExtra("imagePath", cameraPath) }
                        openActivityForResult(intent, getBundle(), REQUEST_CROP)
                    } else {
                        onResult(ArrayList<ImageItem>().apply { add(ImageItem().apply { path = cameraPath }) })
                    }
                }
            }
        } else if (requestCode == REQUEST_PREVIEW) {
            val images = data?.getParcelableArrayListExtra<ImageItem>("images")
            images?.let { dealWith(it) }
            when (resultCode) {
                RESULT_OK -> images?.let { onResult(it) }
                RESULT_CANCELED -> images?.let { adapter.setSelects(it) }
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
        if (!isCrop && isCompress) {   //非剪裁情况下，对图片压缩
            addDisposable(Observable.fromArray(mDatas).map { items ->
                val paths = ArrayList<String>().apply { for (item in items) item.path?.let { add(it) } }
                if (File(CurrApplication.COMPRESSOR_PATH).exists())
                    Luban.with(this@ImageGridActivity).setTargetDir(CurrApplication.COMPRESSOR_PATH).load(paths).get()
                else
                    Luban.with(this@ImageGridActivity).load(paths).get()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        endLoading()
                        val intent = Intent()
                        val paths = ArrayList<String>()
                        for (item in it) paths.add(item.absolutePath)
                        intent.putStringArrayListExtra(ImagePicker.EXTRA_PATHS, paths)
                        setResult(RESULT_OK, intent)
                        finish()
                    }, {
                        endLoading()
                        showShortToast("图片处理异常，请稍后再试")
                    }, { endLoading() }, { showLoading() }))
        } else {
            val intent = Intent()
            val paths = ArrayList<String>()
            for (item in mDatas) item.path?.let { paths.add(it) }
            intent.putStringArrayListExtra(ImagePicker.EXTRA_PATHS, paths)
            setResult(RESULT_OK, intent)
            finish()
        }
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