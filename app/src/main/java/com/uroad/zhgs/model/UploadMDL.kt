package com.uroad.zhgs.model

/**
 *Created by MFB on 2018/8/4.
 * 图片上传成功后数据
 */
class UploadMDL {
    var imgurl: ImageUrl? = null

    inner class ImageUrl {
        var file: String? = null
    }
}