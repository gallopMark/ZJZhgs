package com.uroad.zhgs.utils

import android.content.Context
import android.util.Log
import com.amap.api.col.sln3.va
import java.io.*
import java.lang.Exception

/**
 * @author MFB
 * @create 2018/10/16
 * @describe assets目录通过流的形式存储到sd卡的制位置
 */
class AssetsUtils {
    companion object {
        /***
         * 调用方式
         *
         * String path = Environment.getExternalStorageDirectory().toString() + "/" + "Tianchaoxiong/useso";
        String modelFilePath = "Model/seeta_fa_v1.1.bin";
        Assets2Sd(this, modelFilePath, path + "/" + modelFilePath);
         * @param context
         * @param fileAssetPath assets中的目录
         * @param fileSdPath 要复制到sd卡中的目录
         */
        fun assets2SD(context: Context, fileAssetPath: String, fileSdPath: String): String {
            //测试把文件直接复制到sd卡中 fileSdPath完整路径
            val file = File(fileSdPath)
            if (!file.exists()) {
                try {
                    copyToSD(context, fileAssetPath, fileSdPath)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return file.absolutePath
        }

        @Throws(Exception::class)
        private fun copyToSD(context: Context, fileAssetPath: String, strOutFileName: String) {
            val myInput: InputStream = context.assets.open(fileAssetPath)
            val myOutput = FileOutputStream(strOutFileName)
            val buffer = byteArrayOf(1024.toByte())
            var length = myInput.read(buffer)
            while (length > 0) {
                myOutput.write(buffer, 0, length)
                length = myInput.read(buffer)
            }
            myOutput.flush()
            myInput.close()
            myOutput.close()
        }
    }
}