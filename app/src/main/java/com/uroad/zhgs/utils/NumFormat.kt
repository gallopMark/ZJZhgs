package com.uroad.zhgs.utils

import java.math.BigDecimal

/**
 *Created by MFB on 2018/8/14.
 */
class NumFormat {
    companion object {
        fun formatNum(num: Int): String {
            return if (num < 10 * 1000) {
                num.toString()
            } else {
                val count: Double
                val unit: String
                if (num > 10 * 1000 && num < 10 * 1000 * 10000) {
                    count = num / (10 * 1000).toDouble()
                    unit = "万"
                } else {
                    count = num / (10 * 1000 * 10000).toDouble()
                    unit = "亿"
                }
                val bd = BigDecimal(count)
                "${bd.setScale(1, BigDecimal.ROUND_HALF_UP).toDouble()}$unit"
            }
        }
    }
}