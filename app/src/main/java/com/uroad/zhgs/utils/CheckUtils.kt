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
            phone?.let {
                if (it.isEmpty()) return false
                val regex = "[1]\\d{10}"
                return it.matches(regex.toRegex())
            }
            return false
        }

        /*车牌号验证*/
        fun isCarNum(carNo: String?): Boolean {
            carNo?.let {
                if (it.trim().isEmpty()) return false
//                val regex = "[\\u4e00-\\u9fa5][A-Z][A-Z0-9]{5}"
                val regex = "^(([\\u4e00-\\u9fa5][a-zA-Z]|[\\u4e00-\\u9fa5]{2}\\d{2}|[\\u4e00-\\u9fa5]{2}[a-zA-Z])[-]?|([wW][Jj][\\u4e00-\\u9fa5]{1}[-]?)|([a-zA-Z]{2}))([A-Za-z0-9]{5}|[DdFf][A-HJ-NP-Za-hj-np-z0-9][0-9]{4}|[0-9]{5}[DdFf])\$"
                return it.matches(regex.toRegex())
            }
            return false
        }

        /*身份证校验*/
        fun isIDCard(idCard: String?): Boolean {
            idCard?.let {
                if (it.isEmpty()) return false
                val regex = "(^[0-9]{15})|([0-9]{17}([0-9]|X))"
                return it.matches(regex.toRegex())
            }
            return false
        }
    }
}