package com.uroad.zhgs.utils


/**
 * @author MFB
 * @create 2018/9/11
 * @describe
 */
class CheckUtils {
    companion object {
        /*手机号码1开头，11位数  其他不做处理*/
        fun isMobile(phone: String?): Boolean {
            phone?.let { if (it.startsWith("1") && it.length == 11) return true }
            return false
        }
    }
}