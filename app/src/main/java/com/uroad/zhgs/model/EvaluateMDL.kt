package com.uroad.zhgs.model

/**
 *Created by MFB on 2018/8/5.
 * 评价跟对应的文本
 */
class EvaluateMDL {
    var type: MutableList<Type>? = null

    inner class Type {
        var dictcode: String? = null
        var dictname: String? = null
        var sontype: MutableList<SonType>? = null
        override fun equals(other: Any?): Boolean {
            return when (other) {
                !is Type -> false
                else -> this === other || dictcode == other.dictcode
            }
        }

        override fun hashCode(): Int {
            return 31 + (dictcode?.hashCode() ?: 0)
        }

        inner class SonType {
            var dictcode: String? = null
            var dictname: String? = null
        }
    }
}